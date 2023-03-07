package jmu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jmu.common.CustomException;
import jmu.mapper.CategoryMapper;
import jmu.pojo.Category;

import jmu.pojo.Drug;
import jmu.pojo.Suit;
import jmu.service.CategoryService;

import jmu.service.DrugService;
import jmu.service.SuitDrugService;
import jmu.service.SuitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DrugService drugService;
    @Autowired
    private SuitService suitService;
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Drug> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Drug::getCategoryId,id);
        int count = drugService.count(dishLambdaQueryWrapper);
        if (count > 0){
            throw new CustomException("该分类下关联药品，不可删除");
        }

        LambdaQueryWrapper<Suit> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Suit::getCategoryId,id);
        int count2 = suitService.count(setmealLambdaQueryWrapper);
        if (count2 > 0){
            throw new CustomException("该分类下关联了套装，不可删除");
        }

        super.removeById(id);
    }
}
