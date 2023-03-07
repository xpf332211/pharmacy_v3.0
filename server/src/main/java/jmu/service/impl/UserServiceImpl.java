package jmu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jmu.mapper.UserMapper;
import jmu.pojo.User;
import jmu.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
