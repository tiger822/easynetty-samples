package com.freestyle.netty.protobuf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by rocklee on 2022/1/21 17:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatBean implements Serializable {
  private String uid;
  private String body;
  private Date timestamp;
}
