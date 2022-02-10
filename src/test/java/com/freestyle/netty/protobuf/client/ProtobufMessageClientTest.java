package com.freestyle.netty.protobuf.client;


import com.freestyle.netty.easynetty.client.ProtobufMesssageNettyClientFactory;
import com.freestyle.netty.easynetty.client.interfaces.IProtobufMessageClient;
import com.freestyle.netty.easynetty.common.MessageUtil;
import com.freestyle.netty.protobuf.pojo.ChatBean;
import com.freestyle.protobuf.proto.MessageOuterClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;

/**
 * Created by rocklee on 2022/1/21 17:53
 */
public class ProtobufMessageClientTest {
  public static void main(String[] args) throws InterruptedException {
    IProtobufMessageClient client=new ProtobufMesssageNettyClientFactory<MessageOuterClass.Message>().getClient("localhost",9900);
    try{
      client.run(false, new SimpleChannelInboundHandler<MessageOuterClass.Message>() {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MessageOuterClass.Message msg) throws Exception {
          String str=msg.getData().toStringUtf8();
            System.out.println("Client<<"+str);
        }
      });
      client.getChannel().writeAndFlush(MessageUtil.packMessage("Hello","1",1));
      client.getChannel().writeAndFlush(MessageUtil.packMessage(new ChatBean("user1","这是聊天内容",new Date()),"1",2));
      Thread.sleep(30000);
      client.getChannel().close();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      client.close();
    }
  }
}
