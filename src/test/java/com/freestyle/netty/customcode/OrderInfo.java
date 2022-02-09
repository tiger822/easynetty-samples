package com.freestyle.netty.customcode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by rocklee on 2022/1/25 20:54
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class OrderInfo {
  private String userId;
  private int Orderid;
}
