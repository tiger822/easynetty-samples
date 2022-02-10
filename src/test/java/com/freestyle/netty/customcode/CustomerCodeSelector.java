package com.freestyle.netty.customcode;


import com.freestyle.netty.easynetty.codes.AbstractCodeSelector;
import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.common.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.util.Arrays;

/**
 * Created by rocklee on 2022/1/26 13:57
 */
public class CustomerCodeSelector extends AbstractCodeSelector {
  @Override
  public ChannelInboundHandlerAdapter determineDecode(ByteBuf in) {
    int byteSize = in.readableBytes(); //这次有多少
    if (byteSize < 7) {//不足以形成一帧，则退出
      return null;
    } else {
      //尝试看这篮子里面的数据够不够一帧
      in.markReaderIndex();
      try {
        byte headerSize = in.readByte();
        if (headerSize != CodeConsts.UserHeader.length) {
          return null;
        }
        byte[] inHeader = new byte[headerSize];
        in.readBytes(inHeader);
        if (Arrays.equals(inHeader, CodeConsts.UserHeader)) {
          return new CustomFrameDecoder<UserInfo>(CodeConsts.UserHeader, b -> Utils.fromJsonBytes(b, UserInfo.class));
        } else if (Arrays.equals(inHeader, CodeConsts.OrderHeader)) {
          return new CustomFrameDecoder<OrderInfo>(CodeConsts.OrderHeader, b -> Utils.fromJsonBytes(b, OrderInfo.class));
        }
      }
      finally {
        in.resetReaderIndex();
      }
      return null;
    }
  }

  @Override
  public void addHandler(ChannelPipeline pipeline, ChannelInboundHandlerAdapter channelInboundHandlerAdapter) {
      if (channelInboundHandlerAdapter!=null) {
        pipeline.addLast(channelInboundHandlerAdapter);
      }
  }
}
