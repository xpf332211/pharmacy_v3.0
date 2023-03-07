package jmu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jmu.mapper.ShoppingCartMapper;
import jmu.pojo.ShoppingCart;
import jmu.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

}
