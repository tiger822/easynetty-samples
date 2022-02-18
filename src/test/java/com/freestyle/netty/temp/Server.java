package com.freestyle.netty.temp;


import com.freestyle.netty.customcode.CodeConsts;
import com.freestyle.netty.customcode.OrderInfo;
import com.freestyle.netty.customcode.UserInfo;
import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.codes.JsonMultipleDecode;
import com.freestyle.netty.easynetty.common.NettyUtil;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.server.GeneralNettyServerFactory;
import com.freestyle.netty.easynetty.server.interfaces.IGeneralServer;
import com.freestyle.netty.multiple.Consts;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

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
        CustomFrameDecoder<UserInfo> decoder1=new CustomFrameDecoder<>(CodeConsts.UserHeader, b->Utils.fromJsonBytes(b,UserInfo.class))
                .setDecodeName("userDecoder");
        //decoder1.setSingleDecode(false);
        CustomFrameDecoder<OrderInfo> decoder2=new CustomFrameDecoder<>(CodeConsts.OrderHeader,b->Utils.fromJsonBytes(b,OrderInfo.class))
                .setDecodeName("orderDecoder");
        //decoder2.setSingleDecode(false);
        pipeline
                .addLast(new LoggingHandler(LogLevel.INFO))
                /*.addLast("userDecoder",decoder1)
                .addLast("orderDecoder",decoder2)*/
                .addLast("multiDecoder",new JsonMultipleDecode().registerClass(CodeConsts.OrderHeader, OrderInfo.class)
                        .registerClass(CodeConsts.UserHeader,UserInfo.class))
                .addLast(new SimpleChannelInboundHandler() {
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof UserInfo) {
                      System.out.println("Client<<<:"+msg.toString());
                     /* UserInfo userInfo=(UserInfo)msg;
                      userInfo.setUserName(userInfo.getUserName() + ",srv");
                      ctx.channel().writeAndFlush(msg);
                      if (userInfo.getUserId().equalsIgnoreCase("B001")) {
                        ctx.close().sync();
                      }*/
                    }
                    else if (msg instanceof OrderInfo){
                      OrderInfo orderInfo=(OrderInfo) msg;
                      System.out.println("Client<<<:"+msg.toString());
                      /*ctx.channel().writeAndFlush(orderInfo);
                      if (orderInfo.getUserId().equalsIgnoreCase("O999")){
                        ctx.close().sync();
                      }*/
                    }
                    else if (msg instanceof ByteBuf){
                      /*((ByteBuf)msg).retain();
                       pipeline.fireChannelRead(msg);*/
                      NettyUtil.reDeliver(pipeline, (ByteBuf) msg);
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
