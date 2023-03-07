package jmu.test;

import jmu.pojo.Category;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test01(){
        redisTemplate.opsForValue().set("name","nn");
        redisTemplate.opsForValue().set("obj",new Category());

        Object name = redisTemplate.opsForValue().get("name");
        Object obj = redisTemplate.opsForValue().get("obj");
        System.out.println(name);
        System.out.println(obj);
    }
}
