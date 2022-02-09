package com.freestyle.netty.promise.code;

import com.freestyle.netty.codes.CustomFrameEncoder;
import com.freestyle.netty.common.Utils;
import com.freestyle.netty.pojo.Message;

/**
 * Created by rocklee on 2022/1/29 11:17
 */
public class MessageEncode extends CustomFrameEncoder<Message> {
  public final static byte[] HEADER={1,2};

  public MessageEncode() {
    super(Message.class,HEADER,msg-> Utils.toJsonBytes(msg));
  }
}
