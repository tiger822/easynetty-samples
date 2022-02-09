package com.freestyle.netty.customcode;

import com.freestyle.netty.client.GeneralNettyClientFactory;
import com.freestyle.netty.client.interfaces.IGeneralClient;
import com.freestyle.netty.codes.CustomFrameDecoder;
import com.freestyle.netty.codes.CustomFrameEncoder;
import com.freestyle.netty.common.Utils;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.io.IOException;

/**
 * Created by rocklee on 2022/1/25 15:50
 */
public class CustomCodeClientUserInfoTest {

  public static void main(String[] args) throws InterruptedException {
    IGeneralClient client = new GeneralNettyClientFactory().getClient("localhost", 9900);
    try {
      client.run(false, ch -> {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4))
        .addLast("userDecoder",new CustomFrameDecoder<UserInfo>(CodeConsts.UserHeader, b -> Utils.fromJsonBytes(b, UserInfo.class)))
                .addLast("userEncoder",new CustomFrameEncoder<UserInfo>(UserInfo.class,CodeConsts.UserHeader,o->Utils.toJsonBytes(o)))
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
                    System.out.println("Server<<" + msg.toString());
                  }
                });
      });
      client.getChannel().writeAndFlush(new UserInfo("B002", "陳大文", 20)).sync();
      client.disconnect();
      client.connect();
      client.getChannel().writeAndFlush(new UserInfo("B001", "陳大文", 20)).sync();
      for (int i=1;i<=20;i++) {
        client.getChannel().writeAndFlush(new UserInfo("A00" + i, "陳大文", 20));
        Thread.sleep(200);
        if (!client.isConnected()){
          client.connect();
        }
      }
      client.getChannel().writeAndFlush(new UserInfo("B001", "陳大文", 20)).sync();
      client.getChannel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      client.close();
    }
  }
}