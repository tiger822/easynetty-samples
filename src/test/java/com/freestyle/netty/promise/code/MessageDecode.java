package com.freestyle.netty.promise.code;


import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.dto.Message;

/**
 * Created by rocklee on 2022/1/29 11:22
 */
public class MessageDecode extends CustomFrameDecoder<Message> {
  public MessageDecode() {
    super(MessageEncode.HEADER,b-> Utils.fromJsonBytes(b,Message.class));
  }
}
