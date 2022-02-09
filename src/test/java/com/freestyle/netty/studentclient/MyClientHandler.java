package com.freestyle.netty.studentclient;

import com.freestyle.proto.StudentOuterClass;
import com.google.protobuf.util.JsonFormat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by rocklee on 2022/1/20 11:28
 */
public class MyClientHandler extends SimpleChannelInboundHandler<StudentOuterClass.Student> {
  @Override
  protected void channelRead0(ChannelHandlerContext cxt, StudentOuterClass.Student student) throws Exception {
    System.out.println("Client received");
    System.out.println(JsonFormat.printer().print(student));
    System.out.println(student.toString());
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    StudentOuterClass.Student student= StudentOuterClass.Student.newBuilder().setAge(20).setCity("DG")
            .setName("张三").build();
    ctx.channel().writeAndFlush(student);
  }
}
