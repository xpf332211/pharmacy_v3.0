package jmu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jmu.common.BaseContext;
import jmu.common.R;
import jmu.pojo.ShoppingCart;
import jmu.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加商品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        shoppingCart.setUserId(BaseContext.getCurrentId());
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(shoppingCart.getDrugId()!=null,ShoppingCart::getDrugId,shoppingCart.getDrugId())
                .eq(shoppingCart.getSuitId()!=null,ShoppingCart::getSuitId,shoppingCart.getSuitId())
                .eq(shoppingCart.getUserId()!=null,ShoppingCart::getUserId,shoppingCart.getUserId());
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (shoppingCartServiceOne == null){
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            shoppingCartServiceOne = shoppingCart;
        }else {
            shoppingCartServiceOne.setNumber(shoppingCartServiceOne.getNumber() + 1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        }
        return R.success(shoppingCartServiceOne);
    }

    /**
     * 减少商品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        shoppingCart.setUserId(BaseContext.getCurrentId());
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(shoppingCart.getDrugId()!=null,ShoppingCart::getDrugId,shoppingCart.getDrugId())
                .eq(shoppingCart.getSuitId()!=null,ShoppingCart::getSuitId,shoppingCart.getSuitId())
                .eq(shoppingCart.getUserId()!=null,ShoppingCart::getUserId,shoppingCart.getUserId());
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (shoppingCartServiceOne.getNumber() > 1){
            shoppingCartServiceOne.setNumber(shoppingCartServiceOne.getNumber() - 1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else if (shoppingCartServiceOne.getNumber() == 1){
            shoppingCartServiceOne.setNumber(0);
            shoppingCartService.removeById(shoppingCartServiceOne.getId());
        }
        return R.success(shoppingCartServiceOne);
    }

    /**
     * 获取购物车内商品集合
     * @param
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId())
                .orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @param shoppingCart
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(ShoppingCart shoppingCart){
        shoppingCart.setUserId(BaseContext.getCurrentId());
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("成功清空购物车");
    }
}