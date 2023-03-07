package jmu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jmu.common.R;
import jmu.dto.SuitDto;
import jmu.pojo.Suit;
import jmu.pojo.SuitDrug;
import jmu.redisson.BloomFilter;
import jmu.service.CategoryService;
import jmu.service.SuitDrugService;
import jmu.service.SuitService;
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
@RequestMapping("/suit")
@Slf4j
public class SuitController {

    @Autowired
    private SuitService suitService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SuitDrugService suitDrugService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 清空对应分类的缓存
     * 新增套装
     * @param suitDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SuitDto suitDto){
        suitService.saveSuitWithDrug(suitDto);
        String key = "suit:" + suitDto.getCategoryId() + ":" + suitDto.getStatus();
        redisTemplate.delete(key);
        return R.success("新增成功");
    }

    /**
     * 分页查询套装
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Suit> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Suit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Suit::getName,name)
                .orderByDesc(Suit::getUpdateTime);
        suitService.page(pageInfo,queryWrapper);
        List<Suit> records = pageInfo.getRecords();
        Page<SuitDto> dtoPageInfo = new Page<>();
        BeanUtils.copyProperties(pageInfo,dtoPageInfo,"records");
        List<SuitDto> dtoRecords =  records.stream().map(suit -> {
            SuitDto suitDto = new SuitDto();
            BeanUtils.copyProperties(suit,suitDto);
            Long categoryId = suit.getCategoryId();
            suitDto.setCategoryName(categoryService.getById(categoryId).getName());
            return suitDto;

        }).collect(Collectors.toList());
        dtoPageInfo.setRecords(dtoRecords);
        return R.success(dtoPageInfo);
    }


    /**
     * 根据id获取套餐信息以及相应的药品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SuitDto> get(@PathVariable Long id){
        SuitDto suitDto = suitService.getSuitWithDrug(id);
        return R.success(suitDto);
    }


    /**
     * 清空对应分类的缓存
     * 修改套装信息以及对应的药品
     * @param suitDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SuitDto suitDto){
        suitService.updateSuitWithDrug(suitDto);
        String key = "suit:" + suitDto.getCategoryId() + ":" + suitDto.getStatus();
        redisTemplate.delete(key);
        return R.success("修改成功");
    }


    /**
     * 清空套装全部缓存
     * 批量或单独删除套装以及套装相关的药品记录
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String ids) {
        suitService.removeSuitWithDrug(ids);
        Set keys = redisTemplate.keys("suit:*");
        redisTemplate.delete(keys);
        return R.success("修改成功");
    }


    /**
     * 修改套餐状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> update(@PathVariable int status,String ids){
        List<String> idList = Arrays.asList(ids.split(","));
        List<Suit> suitList = idList.stream().map(s -> {
            Suit suit = new Suit();
            suit.setId(Long.parseLong(s));
            suit.setStatus(status);
            return suit;
        }).collect(Collectors.toList());
        suitService.updateBatchById(suitList);

        Set keys = redisTemplate.keys("suit:*");
        redisTemplate.delete(keys);
        return R.success("修改状态成功");
    }


    @GetMapping("/list")
    public R<List<Suit>> list(Suit suit){
        //判断该categoryId是否是假数据
        RBloomFilter<Integer> bloomFilter = redissonClient.getBloomFilter(BloomFilter.BF_KEY);
        if (!bloomFilter.contains(suit.getCategoryId().intValue())){
            IllegalRequestLog.addCategoryStr(suit.getCategoryId());
            return R.error("该类型不存在！");
        }
        //bl判断存在，实际不一定存在，查数据库
        if (categoryService.getById(suit.getCategoryId()) == null){
            IllegalRequestLog.addCategoryStr(suit.getCategoryId());
            return R.error("该类型不存在！");
        }

        long expireTime = (long)Math.random() * 5 + 20;
        String key = "suit:" + suit.getCategoryId() + ":" + suit.getStatus();
        List<Suit> list = null;
        list = (List<Suit>) redisTemplate.opsForValue().get(key);
        if (list != null){
            return R.success(list);
        }
        LambdaQueryWrapper<Suit> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(suit.getCategoryId()!=null,Suit::getCategoryId,suit.getCategoryId())
                .eq(suit.getStatus()!=null,Suit::getStatus,suit.getStatus())
                .orderByDesc(Suit::getUpdateTime);
        list = suitService.list(queryWrapper);

        redisTemplate.opsForValue().set(key,list, expireTime, TimeUnit.MINUTES);
        return R.success(list);
    }

    /**
     * 获取套餐下的药品信息
     * @param id
     * @return
     */
    @GetMapping("/drug/{id}")
    public R<SuitDrug> get(@PathVariable String id){
        LambdaQueryWrapper<SuitDrug> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id!=null,SuitDrug::getDrugId,id);
        SuitDrug drug = suitDrugService.getOne(queryWrapper);
        return R.success(drug);
    }
}
