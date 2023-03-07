package jmu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jmu.dto.DrugDto;
import jmu.pojo.Drug;


public interface DrugService extends IService<Drug> {
    /**
     * 新增药品，同时新增药品对应的标签
     * @param drugDto
     */
    void saveDrugWithLabel(DrugDto drugDto);

    /**
     * 根据id查询药品信息，同时查询对应的标签
     * @param id
     * @return
     */
    DrugDto getByIdWithLabel(Long id);

    /**
     * 修改药品，同时修改药品对应的标签
     * @param drugDto
     */
    void updateDrugWithLabel(DrugDto drugDto);

    /**
     * 单个或批量删除药品以及相应的标签
     * @param ids
     */
    void removeDrugWithLabel(String ids);
}
