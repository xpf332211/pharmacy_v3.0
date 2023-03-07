package jmu.rabbitmq.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.assertj.core.util.Strings;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 *
 * @author: 林振鑫
 * @date: 2022年10月23日 12:18
 */

@Component
public class BasicPublisher {
    private static final Logger log = LoggerFactory.getLogger(BasicPublisher.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    public void sendMsg(String message) {
        if(!Strings.isNullOrEmpty(message)){
            try{
//                rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                rabbitTemplate.setExchange("exchange_pushmsg");
                rabbitTemplate.setRoutingKey("rk_pushmsg");
                Message msg = MessageBuilder.withBody(message.getBytes("utf-8")).build();
                rabbitTemplate.convertAndSend(message);
                log.info("基本消息模型-生产者-发送消息：{}", msg);
            }catch (Exception e){
                log.error("基本消息模型-生产者-发送消息发生异常：{}", message, e.fillInStackTrace());
            }
        }
    }
}
