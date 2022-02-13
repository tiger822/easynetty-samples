package com.freestyle.netty.protobuf.server;

import com.freestyle.netty.easynetty.common.MessageUtil;
import com.freestyle.netty.easynetty.server.ProtobufMessageNettyServerFactory;
import com.freestyle.netty.easynetty.server.interfaces.IProtobufMessageServer;
import com.freestyle.netty.protobuf.pojo.ChatBean;
import com.freestyle.protobuf.proto.MessageOuterClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by rocklee on 2022/1/21 17:34
 */
public class TestProtobufMessageServer {
  public static void main(String[] args){
    IProtobufMessageServer server= new ProtobufMessageNettyServerFactory<MessageOuterClass.Message>().getGeneralServer(9900);
    try{
      server.getServerBootstrap().option(ChannelOption.SO_BACKLOG, 128);
      server.run(()-> new SimpleChannelInboundHandler<MessageOuterClass.Message>() {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws
                Exception {
          System.out.println(cause.getMessage());
          ctx.close();
        }
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MessageOuterClass.Message msg) throws Exception {
          System.out.println(String.format("Server:收到来自UUID:%s的信息",msg.getProperties().getUuid()));
          if (msg.getProperties().getSClass().contains("String")){
            System.out.println("Server<<:"+ MessageUtil.dePackFromMessage(msg));
          }
          else {
            ChatBean chatBean = (ChatBean) MessageUtil.dePackFromMessage(msg);
            System.out.println(chatBean);
          }
          ctx.channel().writeAndFlush(MessageUtil.packMessage("这是服务器回应","",2));
        }
      });
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      server.close();
    }
  }
}
