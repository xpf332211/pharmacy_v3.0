package jmu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jmu.dto.SuitDto;
import jmu.pojo.Suit;

public interface SuitService extends IService<Suit> {
    //新增套装信息以及相应的药品
    void saveSuitWithDrug(SuitDto suitDto);

    //根据id 获取套装信息以及相应的药品
    SuitDto getSuitWithDrug(Long id);

    //修改套装信息以及对应的药品
    void updateSuitWithDrug(SuitDto suitDto);

    //删除套装信息以及对应的药品
    void removeSuitWithDrug(String ids);
}
