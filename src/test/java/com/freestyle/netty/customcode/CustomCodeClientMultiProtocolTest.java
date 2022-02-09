package com.freestyle.netty.customcode;


import com.freestyle.netty.easynetty.client.GeneralNettyClientFactory;
import com.freestyle.netty.easynetty.client.interfaces.IGeneralClient;
import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.common.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Created by rocklee on 2022/1/25 15:50
 */
public class CustomCodeClientMultiProtocolTest {

  public static void main(String[] args) throws InterruptedException {
    IGeneralClient client = new GeneralNettyClientFactory().getClient("localhost", 9900);
    try {
      client.run(false, ch -> {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4))
        .addLast("userDecoder",new CustomFrameDecoder<>(CodeConsts.UserHeader,b-> Utils.fromJsonBytes(b,UserInfo.class)))
                .addLast("orderDecoder",new CustomFrameDecoder<>(CodeConsts.OrderHeader,b->Utils.fromJsonBytes(b,OrderInfo.class)))
                .addLast("userEncoder", new CustomFrameEncoder<>(UserInfo.class, CodeConsts.UserHeader, Utils::toJsonBytes))
                .addLast("orderEncoder",new CustomFrameEncoder<>(OrderInfo.class,CodeConsts.OrderHeader,Utils::toJsonBytes))
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
                    System.out.println("Server<<" +msg.getClass().getSimpleName()+","+ msg.toString());
                  }
                });
      });
      client.getChannel().writeAndFlush(new UserInfo("B002", "陳大文", 20)).sync();
      client.getChannel().writeAndFlush(new OrderInfo("O00", 11)).sync();
      client.getChannel().writeAndFlush(new UserInfo("B003", "陳大文", 20)).sync();
      client.getChannel().writeAndFlush(new OrderInfo("O01", 11)).sync();
      client.getChannel().writeAndFlush(new UserInfo("B004", "陳大文", 20)).sync();
      client.getChannel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      client.close();
    }
  }
}