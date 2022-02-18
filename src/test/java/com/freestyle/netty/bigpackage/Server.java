package com.freestyle.netty.bigpackage;


import com.freestyle.netty.customcode.CodeConsts;
import com.freestyle.netty.customcode.OrderInfo;
import com.freestyle.netty.customcode.UserInfo;
import com.freestyle.netty.easynetty.bigpackage.BigPackageChannelInboundHandler;
import com.freestyle.netty.easynetty.bigpackage.BigPackageUtil;
import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.codes.JsonMultipleDecode;
import com.freestyle.netty.easynetty.common.MD5Utils;
import com.freestyle.netty.easynetty.common.NettyUtil;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.dto.BigPackageConsts;
import com.freestyle.netty.easynetty.dto.BigPackageProperties;
import com.freestyle.netty.easynetty.dto.JSONData;
import com.freestyle.netty.easynetty.dto.MessageProperties;
import com.freestyle.netty.easynetty.server.GeneralNettyServerFactory;
import com.freestyle.netty.easynetty.server.interfaces.IGeneralServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by rocklee on 2022/1/25 15:33
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
                .addLast(new LengthFieldBasedFrameDecoder(1024*1024, 0, 4, 0, 4))
                .addLast(new LengthFieldPrepender(4))
                //增加大数据包处理程序
                .addLast(new BigPackageChannelInboundHandler(pipeline) {
                    @Override
                    public void onPackageOutput(ChannelHandlerContext ctx,BigPackageProperties properties, byte[] data) {
                       if (data==null){//新大包通知
                         System.out.println("Start to receive :"+properties.getId());
                       }
                       else{//每个小包收到后请自行处理
                         if (properties.getId().startsWith("varify")){
                           ByteBuf tmpBuf=properties.getTmpBuf();
                           if (tmpBuf==null){
                             tmpBuf= Unpooled.buffer(properties.getTotal().intValue()).writeBytes(data);
                             properties.setTmpBuf(tmpBuf);
                           }
                           else{
                             tmpBuf.writeBytes(data);
                           }
                         }
                         System.out.println("Receipted "+properties.getId()+", "+data.length);
                         if (properties.getRt()==properties.getTotal()){ //最后一个包收完
                           System.out.println("Receipte finished:"+properties.getId()+",total:"+properties.getTotal());
                           if (properties.getId().startsWith("varify")){
                             ctx.channel().eventLoop().schedule(()->{
                               byte[] toSend=ByteBufUtil.getBytes(properties.getTmpBuf());
                               JSONData<String> ret=JSONData.fromErr(0,0,properties.getId());
                               ret.setResult(MD5Utils.encryptMD5(toSend));
                               ctx.channel().writeAndFlush(ret);
                             },0, TimeUnit.MILLISECONDS);
                           }
                         }
                       }
                    }
                  })
                .addLast("orderEncoder", new CustomFrameEncoder<>(OrderInfo.class, CodeConsts.OrderHeader, Utils::toJsonBytes))
                .addLast("userEncoder", new CustomFrameEncoder<>(UserInfo.class, CodeConsts.UserHeader, Utils::toJsonBytes))
                .addLast("multiDecoder",new JsonMultipleDecode().registerClass(CodeConsts.UserHeader,UserInfo.class)
                        .registerClass(CodeConsts.OrderHeader,OrderInfo.class))

                .addLast("responseEncoder",new CustomFrameEncoder<>(JSONData.class,CodeConsts.ResponseHeader,Utils::toJsonBytes))
                .addLast(new SimpleChannelInboundHandler() {
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof UserInfo) {
                      //System.out.println("Client<<<:"+msg.toString());
                      UserInfo userInfo=(UserInfo)msg;
                      userInfo.setUserName(userInfo.getUserName() + ",srv");
                      ctx.channel().writeAndFlush(msg);
                    }
                    else if (msg instanceof OrderInfo){
                      OrderInfo orderInfo=(OrderInfo) msg;
                      ctx.channel().writeAndFlush(orderInfo);
                    }
                    else{
                      NettyUtil.reDeliver(pipeline,msg);
                    }
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
