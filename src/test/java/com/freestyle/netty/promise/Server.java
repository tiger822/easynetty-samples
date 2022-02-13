package com.freestyle.netty.promise;


import com.freestyle.netty.easynetty.pojo.Message;
import com.freestyle.netty.easynetty.server.GeneralNettyServerFactory;
import com.freestyle.netty.easynetty.server.interfaces.IGeneralServer;
import com.freestyle.netty.promise.code.MessageDecode;
import com.freestyle.netty.promise.code.MessageEncode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Created by rocklee on 2022/1/29 11:01
 */
public class Server {
  public static void main(String[] args){
    IGeneralServer server= new GeneralNettyServerFactory().getGeneralServer(9900);
    try{
      server.getServerBootstrap().option(ChannelOption.SO_BACKLOG, 128); // tcp最大缓存链接个数
      server.run(ch->{
        ChannelPipeline pipeline = ch.pipeline();
        // 添加用于处理粘包和拆包问题的处理器
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                .addLast(new LengthFieldPrepender(4))
                .addLast(new MessageEncode())
                .addLast(new MessageDecode())
                .addLast(new SimpleChannelInboundHandler<Message>() {
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
                    /*int n=new Random(new Random().nextInt(10000)).nextInt(100);
                    Thread.sleep(n);*/
                    int n=1;
                    Message<String> message=new Message<>(msg.getProperties(),"这是服务器发回的内容内容:"+msg.getData()+",n="+n);
                   // System.out.println(message.getProperties().getId());
                    ctx.pipeline().writeAndFlush(message);
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
