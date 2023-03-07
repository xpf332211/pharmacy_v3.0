package jmu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import jmu.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User>{
}
