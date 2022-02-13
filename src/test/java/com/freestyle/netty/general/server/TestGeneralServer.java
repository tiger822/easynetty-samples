package com.freestyle.netty.general.server;


import com.freestyle.netty.easynetty.server.GeneralNettyServerFactory;
import com.freestyle.netty.easynetty.server.GeneralServerProtobufInitailizer;
import com.freestyle.netty.easynetty.server.interfaces.IGeneralServer;
import io.netty.channel.ChannelOption;

/**
 * Created by rocklee on 2022/1/20 17:08
 */
public class TestGeneralServer {
  public static void main(String[] args){
    IGeneralServer server= new GeneralNettyServerFactory().getGeneralServer(9900);
    try{
      server.getServerBootstrap().option(ChannelOption.SO_BACKLOG, 128); // tcp最大缓存链接个数
      server.run(new GeneralServerProtobufInitailizer());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      server.close();
    }
  }
}
