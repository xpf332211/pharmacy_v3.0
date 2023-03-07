package jmu.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jmu.common.R;
import jmu.pojo.User;
import jmu.service.UserService;
import jmu.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        String key = "login:" + phone;
        if (StringUtils.isNotEmpty(phone)){
            String code = null;
            code = (String) redisTemplate.opsForValue().get(key);
            if (code != null){
                log.info("验证码未过期，为：{}",code);
                return R.success("重复发送验证码");
            }
            code = ValidateCodeUtils.generateValidateCode(4).toString();
            long expire = 60;
            log.info("用户登录验证码为:{}，该验证码{}秒内有效",code,expire);
//            session.setAttribute(phone,code);
            redisTemplate.opsForValue().set(key,code,expire,TimeUnit.SECONDS);
            return R.success("手机验证码发送成功");
        }
        return R.error("手机验证码发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        String phone  = map.get("phone").toString();
        String code = map.get("code").toString();
        String key = "login:" + phone;
//        Object codeInSession = session.getAttribute(phone);
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        if (codeInRedis!=null&&codeInRedis.equals(code)){
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user == null){
                user = new User();
                user.setPhone(phone);
                user.setName("user");
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            //登录成功，验证码可以删除
            redisTemplate.delete(key);
            return R.success(user);
        }
        return R.error("登陆失败");
    }


    /**
     * 用户退出
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }




}
