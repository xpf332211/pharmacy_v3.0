package jmu.test;

import jmu.rabbitmq.publisher.BasicPublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 功能描述
 *
 * @author: 林振鑫
 * @date: 2022年12月12日 10:58
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitMqTest {

    @Autowired
    private BasicPublisher basicPublisher;

    @Test
    public void test01(){
        String msg = "6666666666666666666666666666666";
        basicPublisher.sendMsg(msg);
    }

}
