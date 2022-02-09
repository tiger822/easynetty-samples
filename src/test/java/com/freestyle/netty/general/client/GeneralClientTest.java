package com.freestyle.netty.general.client;

import com.freestyle.netty.client.GeneralClient;
import com.freestyle.netty.client.GeneralNettyClientFactory;
import com.freestyle.netty.client.interfaces.IGeneralClient;
import com.freestyle.protobuf.proto.MessageOuterClass;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Created by rocklee on 2022/1/20 17:19
 */
public class GeneralClientTest {
  public static void main(String[] args) throws InterruptedException {
    IGeneralClient client=new GeneralNettyClientFactory().getClient("localhost",9900);
    try{
      client.run(true,ch->{
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufDecoder(MessageOuterClass.Message.getDefaultInstance()));
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new ProtobufEncoder());
        pipeline.addLast(new GeneralClientHandler());
      });
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      client.close();
    }
  }
}
