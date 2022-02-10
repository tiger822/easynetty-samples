package com.freestyle.netty.customcode;

import com.freestyle.netty.easynetty.codes.CustomFrameDecoder;
import com.freestyle.netty.easynetty.common.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Created by rocklee on 2022/1/26 12:48
 */
public class UserDecode extends CustomFrameDecoder<UserInfo> {
  public UserDecode() {
    super(CodeConsts.UserHeader, b-> Utils.fromJsonBytes(b,UserInfo.class));
  }
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    super.decode(ctx,in,out);
  }
}
