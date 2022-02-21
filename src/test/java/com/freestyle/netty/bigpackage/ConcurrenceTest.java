package com.freestyle.netty.bigpackage;

import com.freestyle.netty.customcode.CodeConsts;
import com.freestyle.netty.customcode.OrderInfo;
import com.freestyle.netty.customcode.UserInfo;
import com.freestyle.netty.easynetty.bigpackage.BigPackageChannelInboundHandler;
import com.freestyle.netty.easynetty.bigpackage.BigPackageUtil;
import com.freestyle.netty.easynetty.client.GeneralNettyClientFactory;
import com.freestyle.netty.easynetty.client.interfaces.IGeneralClient;
import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.codes.JsonMultipleDecode;
import com.freestyle.netty.easynetty.common.MD5Utils;
import com.freestyle.netty.easynetty.common.NettyUtil;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.dto.BigPackageProperties;
import com.freestyle.netty.easynetty.dto.JSONData;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by rocklee on 2022/2/17 21:15
 */
public class ConcurrenceTest {
  private static void runTask()  {
    System.out.println("Thread :"+Thread.currentThread().getId()+" start");
    IGeneralClient client = new GeneralNettyClientFactory().getClient("localhost", 9900);
    try {
      int testTimes=10000;
      ConcurrentHashMap<String,String> sentMap=new ConcurrentHashMap<>();
      client.run(false, ch -> {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline//.addLast(new LoggingHandler(LogLevel.INFO))
                /*.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                .addLast(new LengthFieldPrepender(4))*/
                .addLast("multiDecoder",new JsonMultipleDecode().registerClass(CodeConsts.UserHeader, UserInfo.class)
                        .registerClass(CodeConsts.OrderHeader, OrderInfo.class)
                        .registerClass(CodeConsts.ResponseHeader, JSONData.class)
                )
                .addLast("userEncoder", new CustomFrameEncoder<>(UserInfo.class, CodeConsts.UserHeader, Utils::toJsonBytes))
                .addLast("orderEncoder",new CustomFrameEncoder<>(OrderInfo.class,CodeConsts.OrderHeader,Utils::toJsonBytes))
                //增加大数据包处理程序
                .addLast(new BigPackageChannelInboundHandler(pipeline) {
                  @Override
                  public void onPackageOutput(ChannelHandlerContext ctx, BigPackageProperties properties, byte[] data) {
                    if (data==null){
                      System.out.println("Start to receive :"+properties.getId());
                    }
                    else{
                      /*System.out.println("Receipted "+data.length);
                      if (properties.getRt()==properties.getTotal()){
                        System.out.println("Receipte finished");
                      }*/
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
                    ctx.channel().eventLoop().schedule(()->{
                      BigPackageUtil util=new BigPackageUtil(client.getChannel());
                      new Random(new Random().nextInt(123456)).nextInt();
                      for (int i=0;i<testTimes;i++) {
                        byte[] data=new byte[4321] ;
                        for (int j=0;j<data.length;j++){
                          data[j]= (byte)(new Random(new Random().nextInt(123456)).nextInt());
                          //data[j]= (byte)(j%16);
                        }
                        AtomicInteger sentC = new AtomicInteger(0);
                        int dataLen = data.length;
                        int finalDataLen = dataLen;
                        sentMap.put("verify-"+i, MD5Utils.encryptMD5(data));
                        util.sendData("verify-"+i, null, () -> {
                          int st = sentC.get();
                          int frameLen = finalDataLen >= st + 100 ? 100 : finalDataLen - st;
                          byte[] bytes = Arrays.copyOfRange(data, st, st + frameLen);
                          sentC.set(st + frameLen);
                          return bytes;
                        }, dataLen);
                        try {
                          ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).sync();
                        } catch (InterruptedException e) {
                          e.printStackTrace();
                        }
                      }
                    },100, TimeUnit.MILLISECONDS);
                  }
                  @Override
                  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                    super.channelInactive(ctx);
                    System.out.println("DisConnected:"+ctx.channel().remoteAddress());
                  }
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof JSONData){
                      //检验一下数据
                      JSONData response=(JSONData) msg;
                      if (response.getMessage().endsWith("00")){
                         ctx.channel().eventLoop().schedule(()->{
                            System.out.println(Thread.currentThread().getId()+":"+response.getMessage());
                         },0,TimeUnit.SECONDS);
                      }
                      String md5=sentMap.get(response.getMessage());
                      String md5_=(String)response.getResult();
                      if (md5.equalsIgnoreCase(md5_)){
                        //System.out.println("test "+response.getMessage()+" OK");
                      }
                      else{
                        System.out.println("test "+response.getMessage()+" failed");
                      }
                      if (response.getMessage().endsWith(""+(testTimes-1))){
                        ctx.channel().disconnect();
                      }
                      return;
                    }
                    else {
                      NettyUtil.reDeliver(ctx.pipeline(),msg);
                    }
                  }
                });
      });
      client.getChannel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      try {
        client.close();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Thread :"+Thread.currentThread().getId()+" end");
    }
  }
  public static void main(String[] args) throws InterruptedException {
    CompletableFuture<Void>[] tasks=new CompletableFuture[60];
    for (int i=0;i<tasks.length;i++){
      tasks[i]= CompletableFuture.runAsync(()-> {
        runTask();
      });
    }
    CompletableFuture.allOf(tasks).join();
    System.out.println("All done.");
  }
}
