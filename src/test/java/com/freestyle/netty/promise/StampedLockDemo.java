package com.freestyle.netty.promise;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

/**
 * Created by rocklee on 2022/1/29 14:22
 */
public class StampedLockDemo {
  public static int x;
  public static int y;

  public static void main(String[] args) {
    StampedLock stampedLock = new StampedLock();


    new Thread(() -> {
      try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
      System.out.println(LocalDateTime.now() + " Read Thread id#" + Thread.currentThread().getId() + ", begin to get read lock");
      long stamp = stampedLock.readLock();
      System.out.println(LocalDateTime.now() + " Read Thread id#" + Thread.currentThread().getId() + ", get read lock success, stamp=" + stamp);
      try {
        System.out.println(LocalDateTime.now() + " Read Thread id#" + Thread.currentThread().getId() + ", x=" + x + ", y=" + y);
      } finally {
        stampedLock.unlockRead(stamp);
      }
    }).start();

    new Thread(() -> {
      try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
      System.out.println(LocalDateTime.now() + " Write Thread id#" + Thread.currentThread().getId() + ", begin to get write lock");
      long stamp = stampedLock.writeLock();
      System.out.println(LocalDateTime.now() + " Write Thread id#" + Thread.currentThread().getId() + ", get write lock success, stamp=" + stamp);
      try { TimeUnit.SECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
      try {
        x = 10;
        y = 10;
        System.out.println(LocalDateTime.now() + " Write Thread id#" + Thread.currentThread().getId() + ", x=" + x + ", y=" + y);
      } finally {
        stampedLock.unlockWrite(stamp);
      }
    }).start();

    new Thread(() -> {
      System.out.println(LocalDateTime.now() + " OptimisticRead Thread id#" + Thread.currentThread().getId() + ", begin to get OptimisticRead");
      // 获得一个乐观读的标记（不是锁）
      long stamp = stampedLock.tryOptimisticRead();
      int curX = x;
      int curY = y;
      // 检查乐观读标记是否被其他写锁更改
      if (!stampedLock.validate(stamp)) {
        System.out.println(LocalDateTime.now() + " OptimisticRead Thread id#" + Thread.currentThread().getId() + ", get OptimisticRead failed, stamp=" + stamp);

        // 获取一个悲观读锁
        stamp = stampedLock.readLock();
        try {
          curX = x;
          curY = y;
          System.out.println(LocalDateTime.now() + " OptimisticRead transfer to read lock Thread id#" + Thread.currentThread().getId() + ", curX=" + curX + ", curY=" + curY);
        } finally {
          stampedLock.unlockRead(stamp);
        }
      } else {
        System.out.println(LocalDateTime.now() + " OptimisticRead Thread id#" + Thread.currentThread().getId() + ", get OptimisticRead success, stamp=" + stamp + ", curX=" + curX + ", curY=" + curY);
      }
    }).start();

    new Thread(() -> {
      try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }
      System.out.println(LocalDateTime.now() + " Try Read Thread id#" + Thread.currentThread().getId() + ", begin to get try read lock");
      long stamp =stampedLock.tryReadLock();
      if (stampedLock.validate(stamp)) {
        System.out.println(LocalDateTime.now() + " Try Read Thread id#" + Thread.currentThread().getId() + ", get try read lock success, stamp=" + stamp);
        try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
        try {
          System.out.println(LocalDateTime.now() + " Try Read Thread id#" + Thread.currentThread().getId() + ", x=" + x + ", y=" + y);
        } finally {
          stampedLock.unlockRead(stamp);
        }
      } else {
        System.out.println(LocalDateTime.now() + " Try Read Thread id#" + Thread.currentThread().getId() + ", get try read lock failed, stamp=" + stamp);
      }
    }).start();
  }
}
