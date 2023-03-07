package jmu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jmu.common.R;
import jmu.dto.DrugDto;
import jmu.pojo.Category;
import jmu.pojo.Drug;
import jmu.pojo.DrugLabel;
import jmu.redisson.BloomFilter;
import jmu.service.CategoryService;
import jmu.service.DrugLabelService;
import jmu.service.DrugService;
import jmu.utils.IllegalRequestLog;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
@RestController
@RequestMapping("/drug")
@Slf4j
public class DrugController {
    @Autowired
    private DrugService drugService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DrugLabelService drugLabelService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    /**
     * 清空对应分类的缓存
     * 添加药品信息以及相应的口味
     * @param drugDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DrugDto drugDto){
        drugService.saveDrugWithLabel(drugDto);
        String key = "drug:" + drugDto.getCategoryId() + ":" + drugDto.getStatus();
        redisTemplate.delete(key);
        return R.success("添加成功");
    }

    /**
     * 药品分页展示
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Drug> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Drug> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Drug::getName,name)
                .orderByDesc(Drug::getUpdateTime);
        drugService.page(pageInfo,queryWrapper);
        /**
         * 由于pageInfo的record中缺少categoryName，导致前端无法获得该参数
         * new Page<DishDto>的一个实例，先将除了record的属性拷贝进来
         * 对于record属性 为若干个Dish对象组成的数组，在此基础设置categoryName，（根据分类id获得分类名称）
         * 采用 .stream.map( ()->{} )
         */
        Page<DrugDto> DtoPageInfo = new Page<>();
        BeanUtils.copyProperties(pageInfo,DtoPageInfo,"records");
        List<DrugDto> dtoRecords = pageInfo.getRecords().stream().map( (drug)-> {
            DrugDto drugDto = new DrugDto();
            BeanUtils.copyProperties(drug,drugDto);
            Category category = categoryService.getById(drug.getCategoryId());
            drugDto.setCategoryName(category.getName());
            return drugDto;
        }).collect(Collectors.toList());
        DtoPageInfo.setRecords(dtoRecords);
        return R.success(DtoPageInfo);
    }


    /**
     * 根据id回显药品信息和相应的标签
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DrugDto> get(@PathVariable Long id){
        DrugDto byIdWithLabel = drugService.getByIdWithLabel(id);
        System.out.println("修改成功了吗？？？？");
        System.out.println(R.success(byIdWithLabel));
        return R.success(byIdWithLabel);
    }

    /**
     * 清空对应分类的缓存
     * 修改药品
     * @param drugDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DrugDto drugDto){
        drugService.updateDrugWithLabel(drugDto);
        String key = "drug:" + drugDto.getCategoryId() + ":" + drugDto.getStatus();
        redisTemplate.delete(key);
        return R.success("修改成功");
    }




    /**
     * 根据ids批量或单独删除药品记录
     * 清空药品全部缓存
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String ids){
        drugService.removeDrugWithLabel(ids);
        Set keys = redisTemplate.keys("drug:*");
        redisTemplate.delete(keys);
        return R.success("删除成功");
    }

    /**
     * 根据ids批量或单个修改状态
     * 清空药品全部缓存
     * @param status
     * @param ids
     * @return
     */

    @PostMapping("/status/{status}")
    public R<String> update(@PathVariable int status,String ids) throws Exception {
        List<String> idList = Arrays.asList(ids.split(","));
        List<Drug> dishList = idList.stream().map(d -> {
            Drug drug = new Drug();
            drug.setStatus(status);
            drug.setId(Long.parseLong(d));
            return drug;
        }).collect(Collectors.toList());
        drugService.updateBatchById(dishList);

        Set keys = redisTemplate.keys("drug:*");
        redisTemplate.delete(keys);
        return R.success("修改状态成功");
    }

//      查询页面 加入缓存
//    根据药品种类id查询相应的药品

    @GetMapping("/list")
    public R<List<DrugDto>> getList(Drug drug){
        //判断该categoryId是否是假数据
        RBloomFilter<Integer> bloomFilter = redissonClient.getBloomFilter(BloomFilter.BF_KEY);
        if (!bloomFilter.contains(drug.getCategoryId().intValue())){
            IllegalRequestLog.addCategoryStr(drug.getCategoryId());
            return R.error("该类型不存在！");
        }
        //bl判断存在，实际不一定存在，查数据库
        if (categoryService.getById(drug.getCategoryId()) == null){
            IllegalRequestLog.addCategoryStr(drug.getCategoryId());
            return R.error("该类型不存在！");
        }

        long expireTime = (long)Math.random() * 5 + 20;
        String key = "drug:" + drug.getCategoryId() + ":" + drug.getStatus();
        List<DrugDto> dtoList = null;
        dtoList = (List<DrugDto>) redisTemplate.opsForValue().get(key);
        if (dtoList != null){
            return R.success(dtoList);
        }

        LambdaQueryWrapper<Drug> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(drug.getCategoryId()!=null,Drug::getCategoryId,drug.getCategoryId())
                .eq(Drug::getStatus,1)
                .orderByDesc(Drug::getUpdateTime);
        List<Drug> list = drugService.list(queryWrapper);
        dtoList = list.stream().map( d -> {
            DrugDto drugDto = new DrugDto();
            BeanUtils.copyProperties(d,drugDto);
            Long categoryId = d.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null){
                drugDto.setCategoryName(category.getName());
            }
            Long drugId = d.getId();
            LambdaQueryWrapper<DrugLabel> queryWrapper1 = new LambdaQueryWrapper();
            queryWrapper1.eq(DrugLabel::getDrugId,drugId);
            List<DrugLabel> drugLabelList = drugLabelService.list(queryWrapper1);
            drugDto.setLabels(drugLabelList);
            return drugDto;
        }).collect(Collectors.toList());

        redisTemplate.opsForValue().set(key,dtoList,expireTime, TimeUnit.MINUTES);
        return R.success(dtoList);
    }



}
