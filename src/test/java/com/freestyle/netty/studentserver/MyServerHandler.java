package com.freestyle.netty.studentserver;

import com.freestyle.proto.StudentOuterClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MyServerHandler extends SimpleChannelInboundHandler<StudentOuterClass.Student> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, StudentOuterClass.Student student) throws Exception {
    System.out.println("channelRead0:");
    System.out.println(student.getAge());
    System.out.println(student.getCity());
    System.out.println(student.getName());
    student=student.toBuilder().setName(student.getName()+" new name").build();
    ctx.channel().writeAndFlush(student);

  }

}
