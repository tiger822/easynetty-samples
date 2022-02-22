package com.freestyle.netty.multiple;


import com.freestyle.netty.customcode.OrderInfo;
import com.freestyle.netty.customcode.UserInfo;
import com.freestyle.netty.easynetty.client.GeneralNettyClientFactory;
import com.freestyle.netty.easynetty.client.interfaces.IGeneralClient;
import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.codes.JsonMultipleDecode;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.dto.JSONData;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created by rocklee on 2022/1/25 15:50
 */
public class CustomCodeClientMultiProtocolTest {
  public static void main(String[] args) throws InterruptedException {
    IGeneralClient client = new GeneralNettyClientFactory().getClient("localhost", 9900);
    try {
      client.run(false, ch -> {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline//.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
        //pipeline.addLast(new LengthFieldPrepender(4))
                .addLast(new LoggingHandler(LogLevel.INFO))
                .addLast("orderEncoder", new CustomFrameEncoder<>(OrderInfo.class, Consts.OrderInfoHeader, Utils::toJsonBytes))
                .addLast("userEncoder", new CustomFrameEncoder<>(UserInfo.class, Consts.UserInfoHeader, Utils::toJsonBytes))
                .addLast("anotherEncoder",new CustomFrameEncoder<>(JSONData.class,1234,Utils::toJsonBytes))
                .addLast("multiDecoder",new JsonMultipleDecode().registerClass(Consts.OrderInfoHeader, OrderInfo.class)
                        .registerClass(Consts.UserInfoHeader,UserInfo.class))
                .addLast(new SimpleChannelInboundHandler() {
                    @Override
                    @SuppressWarnings("deprecation")
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                            throws Exception {
                      super.exceptionCaught(ctx,cause);
                    }
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                      super.channelActive(ctx);
                      System.out.println("Connected:"+ctx.channel().remoteAddress());
                    }
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                      super.channelInactive(ctx);
                      System.out.println("DisConnected:"+ctx.channel().remoteAddress());
                    }
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                      ctx.channel().eventLoop().schedule(()->{
                        System.out.println("Server<<" +msg.getClass().getSimpleName()+","+ msg.toString());
                      },0, TimeUnit.SECONDS);
                  }
                });
      });
      for (int i=0;i<2000;i++) {
          client.getChannel().write(new UserInfo("U"+i, "陳大文", 20));
          client.getChannel().write(new OrderInfo("O"+i, 11));
      }
      client.getChannel().write(JSONData.fromErr(1,1,"err message"));
      client.getChannel().writeAndFlush(new UserInfo("B001", "陳大文", 20));
      client.getChannel().closeFuture().sync();
      while (client.isConnected()){
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      client.close();
    }
  }
}