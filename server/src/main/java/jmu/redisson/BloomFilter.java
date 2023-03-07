package jmu.redisson;

import jmu.pojo.Category;
import jmu.pojo.Drug;
import jmu.pojo.Suit;
import jmu.service.CategoryService;
import jmu.service.DrugService;
import jmu.service.SuitService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 *
 */
@Configuration
@Slf4j
public class BloomFilter {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private CategoryService categoryService;

    public static final String BF_KEY = "bf:pharmacy:category";

    @Bean
    public void initBloomFilter(){
        RBloomFilter<Integer> bloomFilter = redissonClient.getBloomFilter(BF_KEY);
        bloomFilter.tryInit(1 * 1000,0.01);
        List<Category> allCategory = categoryService.list();
        for (Category category : allCategory){
            bloomFilter.add(category.getId().intValue());
        }
    }
}



