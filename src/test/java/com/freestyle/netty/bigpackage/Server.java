package com.freestyle.netty.bigpackage;


import com.freestyle.netty.customcode.CodeConsts;
import com.freestyle.netty.customcode.OrderInfo;
import com.freestyle.netty.customcode.UserInfo;
import com.freestyle.netty.easynetty.bigpackage.BigPackageDecoder;
import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.codes.JsonMultipleDecode;
import com.freestyle.netty.easynetty.common.NettyUtil;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.dto.BigPackage;
import com.freestyle.netty.easynetty.dto.BigPackageProperties;
import com.freestyle.netty.easynetty.dto.JSONData;
import com.freestyle.netty.easynetty.server.GeneralNettyServerFactory;
import com.freestyle.netty.easynetty.server.interfaces.IGeneralServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by rocklee on 2022/1/25 15:33
 * updated by rocklee on 2022/2/22 09:50
 */
public class Server {
  public static void main(String[] args){
    IGeneralServer server= new GeneralNettyServerFactory().getGeneralServer(9900);
    try{
      server.getServerBootstrap().option(ChannelOption.SO_BACKLOG, 128) // tcp最大缓存链接个数
      .childOption(ChannelOption.SO_KEEPALIVE, true);
      server.run(ch -> {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline
               //.addLast(new LoggingHandler(LogLevel.INFO))
                //.addLast(new LengthFieldBasedFrameDecoder(1024*1024, 0, 4, 0, 4))
                //.addLast(new LengthFieldPrepender(4))
                .addLast("orderEncoder", new CustomFrameEncoder<>(OrderInfo.class, CodeConsts.OrderHeader, Utils::toJsonBytes))
                .addLast("userEncoder", new CustomFrameEncoder<>(UserInfo.class, CodeConsts.UserHeader, Utils::toJsonBytes))
                .addLast("responseEncoder",new CustomFrameEncoder<>(JSONData.class,CodeConsts.ResponseHeader,Utils::toJsonBytes))
                //增加大数据包解码
                .addLast(new BigPackageDecoder() {
                  @Override
                  public void onPackageOutput(ChannelHandlerContext ctx, BigPackageProperties properties, byte[] data, List<Object> out) {
                    out.add(new BigPackage(properties,data));
                  }
                })
                .addLast("multiDecoder",new JsonMultipleDecode().registerClass(CodeConsts.UserHeader,UserInfo.class)
                        .registerClass(CodeConsts.OrderHeader,OrderInfo.class).setReDeliverRawData(true))
                .addLast(new SimpleChannelInboundHandler() {
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    ctx.channel().eventLoop().schedule(()->{
                      if (msg instanceof BigPackage) {
                        BigPackage bigPackage = (BigPackage) msg;
                        BigPackageProperties properties = bigPackage.getProperties();
                        if (bigPackage.getData() == null) {//新大包通知
                          System.out.println("Start to receive :" + properties.getId());
                        } else {//每个小包收到后请自行处理
                          System.out.println("Receipted "+properties.getId()+", "+bigPackage.getData().length);
                          if (properties.getRt()==properties.getTotal()) { //最后一个包收完
                            System.out.println("Receipte finished:" + properties.getId() + ",total:" + properties.getTotal());
                            if (properties.getId().equalsIgnoreCase("verify")){
                               ctx.channel().close();
                            }
                          }
                        }
                        return;
                      }
                      if (msg instanceof UserInfo) {
                        UserInfo userInfo=(UserInfo)msg;
                        userInfo.setUserName(userInfo.getUserName() + ",srv");
                        ctx.channel().writeAndFlush(msg);
                      }
                      else if (msg instanceof OrderInfo){
                        OrderInfo orderInfo=(OrderInfo) msg;
                        ctx.channel().writeAndFlush(orderInfo);
                      }
                      else{
                        //NettyUtil.reDeliver(pipeline,msg); //只要上面解码正确，不会落到这里
                      }
                    },0,TimeUnit.SECONDS);
                  }
                });
      });
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      server.close();
    }
  }
}
