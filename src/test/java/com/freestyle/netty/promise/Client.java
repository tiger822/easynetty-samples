package com.freestyle.netty.promise;


import com.freestyle.netty.easynetty.client.GeneralNettyClientFactory;
import com.freestyle.netty.easynetty.client.interfaces.IGeneralClient;
import com.freestyle.netty.easynetty.dto.Message;
import com.freestyle.netty.easynetty.dto.MessageProperties;
import com.freestyle.netty.easynetty.lock.StampedLockPromiseUtil;
import com.freestyle.netty.easynetty.lock.interfaces.PromiseUtil;
import com.freestyle.netty.promise.code.MessageDecode;
import com.freestyle.netty.promise.code.MessageEncode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.util.concurrent.TimeUnit;

/**
 * Created by rocklee on 2022/1/29 11:10
 */
public class Client {
  private static final PromiseUtil<Message> promiseUtil=new StampedLockPromiseUtil<>();
  public static void main(String[] args) throws InterruptedException {
    IGeneralClient client=new GeneralNettyClientFactory().getClient("localhost",9900);
    try{
      client.run(false,ch->{
        ChannelPipeline pipeline = ch.pipeline();
        // 添加用于处理粘包和拆包问题的处理器
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                .addLast(new LengthFieldPrepender(4))
                .addLast(new MessageEncode())
                .addLast(new MessageDecode())
                .addLast(new SimpleChannelInboundHandler<Message>() {
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
                    ctx.channel().eventLoop().schedule(()->{
                      promiseUtil.signal(msg.getProperties().getId(),msg); //通知完成
                    },0, TimeUnit.SECONDS);
                  }
                });
      });
      Channel channel=client.getChannel();
      long tm=System.currentTimeMillis();
      for (int i=1;i<=100000;i++) {
        int finalI = i;
        //CompletableFuture.runAsync(()->{
          try {
            Long lock=promiseUtil.newLock(Message.class);
            Message<String> msgToSend=new Message<String>(new MessageProperties("", lock, ""), String.format("第%d个信息。。。", finalI));
            channel.writeAndFlush(msgToSend).sync();
            Message returnMessage = promiseUtil.await(Message.class);
            if (returnMessage==null){
              throw new IllegalStateException("出错了");
            }
            if (msgToSend.getProperties().getId()!=returnMessage.getProperties().getId()){
              throw new IllegalStateException("出错了");
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          } finally {
            promiseUtil.release(Message.class);
          }
      }
     // channel.closeFuture().sync();
      System.out.println("consumed:"+(System.currentTimeMillis()-tm)+"ms");

    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      client.close();
    }
  }
}
