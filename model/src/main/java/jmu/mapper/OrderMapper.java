package jmu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import jmu.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

}