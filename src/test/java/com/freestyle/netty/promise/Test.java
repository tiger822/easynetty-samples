package com.freestyle.netty.promise;



import com.freestyle.netty.easynetty.lock.StampedLockPromiseUtil;
import com.freestyle.netty.easynetty.lock.interfaces.PromiseUtil;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Created by rocklee on 2022/1/29 11:46
 */
public class Test {
  @org.junit.Test
  public void testStampedLockP() throws InterruptedException {

  }
  public static void main(String[] args) throws InterruptedException {
  PromiseUtil<String> promiseUtil=new StampedLockPromiseUtil<>();
  Long a=promiseUtil.newLock();
    System.out.println(promiseUtil.await());
    CompletableFuture.runAsync(() -> {
      System.out.println("wait ...");
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      promiseUtil.signal(a, "OK");
    });
    System.out.println(promiseUtil.await());
    promiseUtil.release();
    long ab=promiseUtil.newLock();
    CompletableFuture.runAsync(() -> {
      System.out.println("wait ...");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      promiseUtil.signal(ab, "CC");
    });
    System.out.println(promiseUtil.await());
    promiseUtil.release();
    if (ab!=0)return;

    for (int i=0;i<=1000;i++) {
      int finalI = i;
      CompletableFuture.runAsync(()->{
        Long id = promiseUtil.newLock();
        new Thread(() -> {
          System.out.println("wait ...");
          try {
            Thread.sleep(new Random().nextInt(1000));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          promiseUtil.signal(id, "OK"+ finalI);
        }).start();
        //System.out.println("pool size:"+((StampedLockPromiseUtil)promiseUtil).getLockPoolSize());
        String ret = promiseUtil.await();
        System.out.println(id+":"+ret);
        promiseUtil.release();
      });
    }
    Thread.sleep(30000);
  }
}
