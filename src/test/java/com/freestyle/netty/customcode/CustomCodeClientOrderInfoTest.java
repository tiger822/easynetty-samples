package com.freestyle.netty.customcode;


import com.freestyle.netty.easynetty.client.GeneralNettyClientFactory;
import com.freestyle.netty.easynetty.client.interfaces.IGeneralClient;
import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.common.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Created by rocklee on 2022/1/25 15:50
 */
public class CustomCodeClientOrderInfoTest {

  public static void main(String[] args) throws InterruptedException {
    IGeneralClient client = new GeneralNettyClientFactory().getClient("localhost", 9900);
    try {
      client.run(false, ch -> {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4))
        .addLast("orderDecoder",new CustomFrameDecoder<OrderInfo>(CodeConsts.OrderHeader, b-> Utils.fromJsonBytes(b,OrderInfo.class)))
                .addLast("orderEncoder",new CustomFrameEncoder<OrderInfo>(OrderInfo.class,CodeConsts.OrderHeader, o->Utils.toJsonBytes(o)))
                .addLast(new SimpleChannelInboundHandler() {
                  @Override
                  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                    super.userEventTriggered(ctx,evt);
                    System.out.println(evt);
                  }
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    System.out.println("ord:Server<<" + msg.toString());
                  }
                });
      });
      Channel channel=client.getChannel();
      for (int i=1;i<=200;i++) {
        channel.writeAndFlush(new OrderInfo("O00"+i, i));
        Thread.sleep(200);
      }
      channel.writeAndFlush(new OrderInfo("O999", 20)).sync();
      channel.closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      client.close();
    }
  }
}