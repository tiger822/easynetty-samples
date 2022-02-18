package com.freestyle.netty.bigpackage;


import com.freestyle.netty.customcode.CodeConsts;
import com.freestyle.netty.customcode.OrderInfo;
import com.freestyle.netty.customcode.UserInfo;
import com.freestyle.netty.easynetty.bigpackage.BigPackageChannelInboundHandler;
import com.freestyle.netty.easynetty.bigpackage.BigPackageUtil;
import com.freestyle.netty.easynetty.client.GeneralNettyClientFactory;
import com.freestyle.netty.easynetty.client.interfaces.IGeneralClient;
import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.codes.JsonMultipleDecode;
import com.freestyle.netty.easynetty.common.ArrayUtil;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.dto.BigPackageProperties;
import com.freestyle.netty.easynetty.dto.JSONData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Created by rocklee on 2022/1/25 15:50
 */
public class Client {

  public static void main(String[] args) throws InterruptedException {
    IGeneralClient client = new GeneralNettyClientFactory().getClient("localhost", 9900);
    try {
      client.run(false, ch -> {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline//.addLast(new LoggingHandler(LogLevel.INFO))
                .addLast(new LengthFieldBasedFrameDecoder(1024*1024, 0, 4, 0, 4))
                .addLast(new LengthFieldPrepender(4))
                .addLast("multiDecoder",new JsonMultipleDecode().registerClass(CodeConsts.UserHeader,UserInfo.class)
                .registerClass(CodeConsts.OrderHeader,OrderInfo.class)
                .registerClass(CodeConsts.ResponseHeader, JSONData.class)
                )
                .addLast("userEncoder", new CustomFrameEncoder<>(UserInfo.class, CodeConsts.UserHeader, Utils::toJsonBytes))
                .addLast("orderEncoder",new CustomFrameEncoder<>(OrderInfo.class,CodeConsts.OrderHeader,Utils::toJsonBytes))
                //增加大数据包处理程序
                .addLast(new BigPackageChannelInboundHandler(pipeline) {
                  @Override
                  public void onPackageOutput(ChannelHandlerContext ctx,BigPackageProperties properties, byte[] data) {
                    if (data==null){
                      System.out.println("Start to receive :"+properties.getId());
                    }
                    else{
                      System.out.println("Receipted "+data.length);
                      if (properties.getRt()==properties.getTotal()){
                        System.out.println("Receipte finished");
                      }
                    }
                  }
                })
                  .addLast(new SimpleChannelInboundHandler() {
                    @Override
                    @SuppressWarnings("deprecation")
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                            throws Exception {
                      super.exceptionCaught(ctx,cause);
                    }
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                      super.channelActive(ctx);
                      System.out.println("Connected:"+ctx.channel().remoteAddress());
                    }
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                      super.channelInactive(ctx);
                      System.out.println("DisConnected:"+ctx.channel().remoteAddress());
                    }
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    System.out.println("Server<<" +msg.getClass().getSimpleName()+","+ msg.toString());
                  }
                });
      });
      BigPackageUtil util=new BigPackageUtil(client.getChannel());
      client.getChannel().write(new UserInfo("A001", "陳大文", 20));
      client.getChannel().write(new OrderInfo("A002", 11));

      try(FileInputStream fis=new FileInputStream("d:/temp/a.txt")){
        util.sendData("ID12345",new Date(),fis,fis.available(),1024,false);
      } catch (IOException e) {
        e.printStackTrace();
      }
      byte[] data={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
      AtomicInteger sentC=new AtomicInteger();
      int dataLen=data.length;
      int finalDataLen1 = dataLen;
      util.sendData("small",null,()->{
         int st=sentC.get();
         int frameLen= finalDataLen1 >=st+5?5: finalDataLen1 -st;
         byte[] bytes= Arrays.copyOfRange(data,st,st+frameLen);
         sentC.set(st+frameLen);
         return bytes;
      },dataLen,false);
      for (int i=0;i<5;i++) {
        try (FileInputStream fis = new FileInputStream("d:/source/goland-2020.3.2.exe")) {
          util.sendData("goland-2020.3.2.exe"+i, new Date(), fis, fis.available(), 1024*100);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      client.getChannel().writeAndFlush(new OrderInfo("A003", 11)).sync();

      sentC.set(0);
      dataLen=data.length;
      int finalDataLen = dataLen;
      util.sendData("varify",null,()->{
        int st=sentC.get();
        int frameLen= finalDataLen >=st+5?5: finalDataLen -st;
        byte[] bytes= Arrays.copyOfRange(data,st,st+frameLen);
        sentC.set(st+frameLen);
        return bytes;
      },dataLen);

      client.getChannel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      client.close();
    }
  }
}