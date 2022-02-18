package com.freestyle.netty.bigpackage;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.freestyle.netty.customcode.CodeConsts;
import com.freestyle.netty.easynetty.common.Utils;
import com.freestyle.netty.easynetty.dto.MessageProperties;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by rocklee on 2022/2/17 21:20
 */
public class ResponseDeserializer extends StdDeserializer<Response> {
  public ResponseDeserializer(Class<?> vc) {
    super(vc);
  }
  public ResponseDeserializer(){
    this(null);
  }


  @Override
  public Response deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
    JsonNode node = p.getCodec().readTree(p);
    MessageProperties properties= Utils.fromJson(node.get("properties").toString(), MessageProperties.class);
    Response obj=new Response();
    obj.setProperties(properties);
    obj.setData(node.get("data").binaryValue());
    return obj;
  }
}
