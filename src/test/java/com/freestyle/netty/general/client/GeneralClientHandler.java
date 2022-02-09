package com.freestyle.netty.general.client;

import com.freestyle.netty.general.client.pojo.Order;
import com.freestyle.netty.common.MessageUtil;
import com.freestyle.protobuf.proto.MessageOuterClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rocklee on 2022/1/20 17:55
 */
public class GeneralClientHandler extends SimpleChannelInboundHandler<MessageOuterClass.Message> {
  @Override
  protected void channelRead0(ChannelHandlerContext cxt, MessageOuterClass.Message msg) throws Exception {
    //System.out.println("Client received");
    Order order= (Order) MessageUtil.dePackFromMessage(msg);
    System.out.println(order);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    Order order=new Order();
    order.setOrderId(1);
    order.setCustomId(100);
    order.setAmount(BigDecimal.valueOf(5000));
    Map<Long,BigDecimal> prods=new HashMap<>();
    prods.put(1L,BigDecimal.valueOf(2000));
    prods.put(2L,BigDecimal.valueOf(3000));
    order.setProductList(prods);
    MessageOuterClass.Message msg=MessageUtil.packMessage(order,"1",1);
    ctx.channel().writeAndFlush(msg);
    super.channelActive(ctx);
  }
}
