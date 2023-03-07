package jmu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jmu.common.BaseContext;
import jmu.common.R;
import jmu.dto.OrderDto;
import jmu.pojo.OrderDetail;
import jmu.pojo.Orders;
//import jmu.rabbitmq.consumer.BasicConsumer;
import jmu.rabbitmq.publisher.BasicPublisher;
import jmu.service.OrderDetailService;
import jmu.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private BasicPublisher basicPublisher;
//    @Autowired
//    private BasicConsumer basicConsumer;
    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        //通过redisson来实现锁的获取
        String key = "lock:order:" + BaseContext.getCurrentId();
        RLock lock = redissonClient.getLock(key);
        boolean isLock = lock.tryLock();
        if (!isLock){
            log.info("用户操作频繁");
            return R.error("操作频繁！");
        }
        try {
            orderService.submit(orders);
            return R.success("下单成功");
        }finally {
            lock.unlock();
        }

    }

    /**
     * 用户查看全部订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page,int pageSize){
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        orderService.page(pageInfo);
        List<Orders> records = pageInfo.getRecords();
        Page<OrderDto> dtoPageInfo = new Page<>();
        BeanUtils.copyProperties(pageInfo,dtoPageInfo,"records");
        List<OrderDto> dtoRecords = records.stream().map(orders ->{
            OrderDto orderDto = new OrderDto();
            BeanUtils.copyProperties(orders,orderDto);
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderDetail::getOrderId,orders.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);
            orderDto.setOrderDetails(orderDetails);
            return orderDto;
        }).collect(Collectors.toList());
        dtoPageInfo.setRecords(dtoRecords);
        return R.success(dtoPageInfo);
    }

    /**
     * 商家查看订单
     * @param page
     * @param pageSize
     * @param number
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number,
                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime beginTime,
                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime endTime){
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(number!=null,Orders::getNumber,number)
                .between(beginTime!=null&&endTime!=null,Orders::getOrderTime,beginTime,endTime)
                .orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Orders orders){
        Long ordersId = orders.getId();
        Integer status = orders.getStatus();
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId,ordersId)
                .set(Orders::getStatus,status);
        orderService.update(updateWrapper);
        return R.success("修改成功");
    }

    /**
     * 再来一单
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        Long ordersId = orders.getId();
        if (ordersId==null){
            return R.error("再来一单失败");
        }
        return R.success("再来一单");
    }

}
