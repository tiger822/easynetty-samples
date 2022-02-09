package com.freestyle.netty.studentclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
/**
 *
 * Created by rocklee on 2022/1/20 11:26
 */
public class MyClientTest {
  public static void main(String[] args) throws Exception{
    EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    try {
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
              .handler(new MyClientInitailizer());
      ChannelFuture channelFuture = bootstrap.connect("localhost",8899).sync();
      channelFuture.channel().closeFuture().sync();
    }finally {
      eventLoopGroup.shutdownGracefully();
    }
  }
}
