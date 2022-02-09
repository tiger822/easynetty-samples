package com.freestyle.netty.customcode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by rocklee on 2022/1/25 15:46
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class UserInfo implements Serializable {
  private String userId;
  private String userName;
  private int age;

}
