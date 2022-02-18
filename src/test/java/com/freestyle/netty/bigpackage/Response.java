package com.freestyle.netty.bigpackage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.freestyle.netty.easynetty.dto.Message;

/**
 * Created by rocklee on 2022/2/17 21:19
 */
@JsonDeserialize(using = ResponseDeserializer.class)
public class Response extends Message<byte[]> {
}
