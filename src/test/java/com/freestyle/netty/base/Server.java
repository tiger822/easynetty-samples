package com.freestyle.netty.base;


import com.freestyle.netty.customcode.OrderInfo;
import com.freestyle.netty.customcode.UserInfo;
import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.codes.JsonMultipleDecode;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.dto.JSONData;
import com.freestyle.netty.easynetty.server.GeneralNettyServerFactory;
import com.freestyle.netty.easynetty.server.interfaces.IGeneralServer;
import com.freestyle.netty.multiple.Consts;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

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
        // 添加用于处理粘包和拆包问题的处理器
        pipeline//.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                //.addLast(new LengthFieldPrepender(4))
                .addLast(new LoggingHandler(LogLevel.INFO))
                .addLast(new IdleStateHandler(30,30,30))
                .addLast("multiDecoder",new JsonMultipleDecode().registerClass(Consts.OrderInfoHeader, OrderInfo.class)
                .registerClass(Consts.UserInfoHeader,UserInfo.class).setReDeliverRawData(false)
                .registerClass(1234,JSONData.class))
                .addLast(new SimpleChannelInboundHandler() {
                  @Override
                  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                    if (evt instanceof IdleStateEvent) {
                      IdleStateEvent e = (IdleStateEvent) evt;
                      if (e.state() == IdleState.READER_IDLE) {
                        ctx.close();
                      } else if (e.state() == IdleState.WRITER_IDLE) {
                        //ctx.writeAndFlush(new PingMessage());
                      }
                    }
                  }
                  @Override
                  @SuppressWarnings("deprecation")
                  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                          throws Exception {
                    super.exceptionCaught(ctx,cause);
                    cause.printStackTrace();
                  }
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof UserInfo) {
                      UserInfo userInfo=(UserInfo)msg;
                      ctx.channel().eventLoop().schedule(()->{
                       System.out.println("recved:"+userInfo.getUserId());
                       /*if (userInfo.getUserId().equalsIgnoreCase("B001")) {
                         try {
                           ctx.close().sync();
                         } catch (InterruptedException e) {
                           e.printStackTrace();
                         }
                       }*/
                     },0, TimeUnit.SECONDS);
                    }
                    else if (msg instanceof OrderInfo){
                      OrderInfo orderInfo=(OrderInfo) msg;
                      ctx.channel().eventLoop().schedule(()->{
                        System.out.println("recved:"+orderInfo.getUserId());
                        if (orderInfo.getUserId().equalsIgnoreCase("O999")){
                          ctx.channel().close();
                        }
                     },0, TimeUnit.SECONDS);
                    }
                    else if (msg instanceof JSONData){
                      ctx.channel().eventLoop().schedule(()->{
                        System.out.println(msg);
                      },0,TimeUnit.SECONDS);
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
