package com.freestyle.netty.promise.code;


import com.freestyle.netty.easynetty.codes.CustomFrameEncoder;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.dto.Message;

/**
 * Created by rocklee on 2022/1/29 11:17
 */
public class MessageEncode extends CustomFrameEncoder<Message> {
  public final static int HEADER=12;

  public MessageEncode() {
    super(Message.class,HEADER,msg-> Utils.toJsonBytes(msg));
  }
}
