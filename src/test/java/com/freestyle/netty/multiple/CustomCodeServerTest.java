package com.freestyle.netty.multiple;

import com.freestyle.netty.codes.CustomFrameEncoder;
import com.freestyle.netty.codes.JsonMultipleDecode;
import com.freestyle.netty.common.Utils;
import com.freestyle.netty.customcode.OrderInfo;
import com.freestyle.netty.customcode.UserInfo;
import com.freestyle.netty.server.GeneralNettyServerFactory;
import com.freestyle.netty.server.intefaces.IGeneralServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Created by rocklee on 2022/1/25 15:33
 */
public class CustomCodeServerTest {
  public static void main(String[] args){
    IGeneralServer server= new GeneralNettyServerFactory().getGeneralServer(9900);
    try{
      server.getServerBootstrap().option(ChannelOption.SO_BACKLOG, 128) // tcp最大缓存链接个数
      .childOption(ChannelOption.SO_KEEPALIVE, true);
      server.run(ch -> {
        ChannelPipeline pipeline = ch.pipeline();
        // 添加用于处理粘包和拆包问题的处理器
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                .addLast(new LengthFieldPrepender(4))
                .addLast("orderEncoder", new CustomFrameEncoder<>(OrderInfo.class, Consts.OrderInfoHeader, o -> Utils.toJsonBytes(o)))
                .addLast("userEncoder", new CustomFrameEncoder<>(UserInfo.class, Consts.UserInfoHeader, o -> Utils.toJsonBytes(o)))
                .addLast("multiDecoder",new JsonMultipleDecode().registerClass(Consts.OrderInfoHeader, OrderInfo.class)
                .registerClass(Consts.UserInfoHeader,UserInfo.class))
                .addLast(new SimpleChannelInboundHandler() {
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof UserInfo) {
                      //System.out.println("Client<<<:"+msg.toString());
                      UserInfo userInfo=(UserInfo)msg;
                      userInfo.setUserName(userInfo.getUserName() + ",srv");
                      ctx.channel().writeAndFlush(msg);
                      if (userInfo.getUserId().equalsIgnoreCase("B001")) {
                        ctx.close().sync();
                      }
                    }
                    else if (msg instanceof OrderInfo){
                      OrderInfo orderInfo=(OrderInfo) msg;
                      ctx.channel().writeAndFlush(orderInfo);
                      if (orderInfo.getUserId().equalsIgnoreCase("O999")){
                        ctx.close().sync();
                      }
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