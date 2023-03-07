package jmu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jmu.common.CustomException;
import jmu.dto.SuitDto;
import jmu.mapper.SuitMapper;
import jmu.pojo.Suit;
import jmu.pojo.SuitDrug;
import jmu.service.CategoryService;
import jmu.service.SuitDrugService;
import jmu.service.SuitService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class SuitServiceImpl extends ServiceImpl<SuitMapper, Suit> implements SuitService {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SuitDrugService suitDrugService;

    @Override
    public void saveSuitWithDrug(SuitDto suitDto) {

        Long categoryId = suitDto.getCategoryId();
        String categoryName = categoryService.getById(categoryId).getName();
        suitDto.setCategoryName(categoryName);
        this.save(suitDto);
        List<SuitDrug> suitDrugs = suitDto.getSuitDrugs();
        suitDrugs.stream().map(s -> {
            s.setSuitId(suitDto.getId());
            return s;
        }).collect(Collectors.toList());
        suitDrugService.saveBatch(suitDrugs);
    }

    @Override
    public SuitDto getSuitWithDrug(Long id) {
        Suit suit = this.getById(id);
        LambdaQueryWrapper<SuitDrug> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SuitDrug::getSuitId,id);
        List<SuitDrug> list = suitDrugService.list(queryWrapper);
        SuitDto suitDto = new SuitDto();
        suitDto.setSuitDrugs(list);
        BeanUtils.copyProperties(suit,suitDto);
        return suitDto;
    }

    @Override
    public void updateSuitWithDrug(SuitDto suitDto) {
        this.updateById(suitDto);
        Long id = suitDto.getId();
        LambdaQueryWrapper<SuitDrug> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SuitDrug::getSuitId,id);
        suitDrugService.remove(queryWrapper);
        List<SuitDrug> suitDrugs = suitDto.getSuitDrugs();
        suitDrugs.stream().map(suitDrug -> {
            suitDrug.setSuitId(id);
            return suitDrug;
        }).collect(Collectors.toList());
        suitDrugService.saveBatch(suitDrugs);
    }

    @Override
    public void removeSuitWithDrug(String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        List<Long> idListLong = idList.stream().map(s -> {
            Long idLong = Long.parseLong(s);
            return idLong;
        }).collect(Collectors.toList());
        LambdaQueryWrapper<Suit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Suit::getId,idListLong)
                .eq(Suit::getStatus,1);
        int count = this.count(queryWrapper);
        if(count > 0){
            throw new CustomException("套餐售卖中，无法删除");
        }
        LambdaQueryWrapper<SuitDrug> suitDrugLambdaQueryWrapper = new LambdaQueryWrapper<>();
        suitDrugLambdaQueryWrapper.in(SuitDrug::getSuitId,idListLong);
        suitDrugService.remove(suitDrugLambdaQueryWrapper);
        this.removeByIds(idListLong);
    }


}
