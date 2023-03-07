package jmu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jmu.pojo.Category;
import org.apache.ibatis.annotations.Param;

public interface CategoryService extends IService<Category> {
    void remove(@Param("id") Long id);
}
