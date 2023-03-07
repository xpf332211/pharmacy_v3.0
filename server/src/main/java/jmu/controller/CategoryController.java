package jmu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jmu.common.R;
import jmu.pojo.Category;
import jmu.redisson.BloomFilter;
import jmu.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 */
@RestController
@RequestMapping("category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 添加分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        //更新bl
        RBloomFilter<Integer> bloomFilter = redissonClient.getBloomFilter(BloomFilter.BF_KEY);
        bloomFilter.add(category.getId().intValue());
        return R.success("添加分类成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        Page<Category> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        categoryService.remove(ids);
        //重新初始化bl
        RBloomFilter<Integer> bloomFilter = redissonClient.getBloomFilter(BloomFilter.BF_KEY);
        if (bloomFilter.delete()){
            RBloomFilter<Integer> bloomFilter1 = redissonClient.getBloomFilter(BloomFilter.BF_KEY);
            bloomFilter1.tryInit(1 * 1000,0.01);
            List<Category> allCategory = categoryService.list();
            for (Category category : allCategory){
                bloomFilter1.add(category.getId().intValue());
            }
        }
        return R.success("删除成功");
    }


    /**
     * 修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        //重新初始化bl
        RBloomFilter<Integer> bloomFilter = redissonClient.getBloomFilter(BloomFilter.BF_KEY);
        if (bloomFilter.delete()){
            RBloomFilter<Integer> bloomFilter1 = redissonClient.getBloomFilter(BloomFilter.BF_KEY);
            bloomFilter1.tryInit(1 * 1000,0.01);
            List<Category> allCategory = categoryService.list();
            for (Category category1 : allCategory){
                bloomFilter1.add(category1.getId().intValue());
            }
        }
        return R.success("修改成功");
    }

    /**
     * 展示菜品分类
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
