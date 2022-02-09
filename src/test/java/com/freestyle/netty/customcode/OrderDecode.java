package com.freestyle.netty.customcode;

import com.freestyle.netty.codes.CustomFrameDecoder;
import com.freestyle.netty.common.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Created by rocklee on 2022/1/26 12:49
 */
public class OrderDecode extends CustomFrameDecoder<OrderInfo> {
  public OrderDecode() {
    super(CodeConsts.OrderHeader, b-> Utils.fromJsonBytes(b,OrderInfo.class));
  }
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    super.decode(ctx,in,out);
  }
}
