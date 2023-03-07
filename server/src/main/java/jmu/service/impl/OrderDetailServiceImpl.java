package jmu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jmu.mapper.OrderDetailMapper;
import jmu.pojo.OrderDetail;
import jmu.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}