package com.freestyle.netty.multiple;

import java.nio.ByteBuffer;

/**
 * Created by rocklee on 2022/2/8 20:38
 */
public class Consts {
  public static final byte[] UserInfoHeader= ByteBuffer.allocate(4).putInt(1).array();
  public static final byte[] OrderInfoHeader= ByteBuffer.allocate(4).putInt(2).array();

}
