package jmu.dto;


import jmu.pojo.OrderDetail;
import jmu.pojo.Orders;
import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
public class OrderDto extends Orders {
    List<OrderDetail> orderDetails;
}
