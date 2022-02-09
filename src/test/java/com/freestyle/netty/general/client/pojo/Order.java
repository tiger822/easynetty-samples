package com.freestyle.netty.general.client.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by rocklee on 2022/1/20 17:58
 */
@Data
@NoArgsConstructor
public class Order implements Serializable {
  private long orderId;
  private long customId;
  private BigDecimal amount;
  private Map<Long,BigDecimal> productList;
  private boolean confirmed;
}
