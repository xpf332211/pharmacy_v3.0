package jmu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jmu.common.CustomException;
import jmu.dto.DrugDto;
import jmu.mapper.DrugMapper;
import jmu.pojo.Drug;
import jmu.pojo.DrugLabel;
import jmu.service.DrugLabelService;
import jmu.service.DrugService;
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
public class DrugServiceImpl extends ServiceImpl<DrugMapper, Drug> implements DrugService {

    @Autowired
    private DrugLabelService drugLabelService;

    @Override
    public void saveDrugWithLabel(DrugDto drugDto) {

        /**
         * 新增药品，同时新增药品对应的标签
         */
            this.save(drugDto);
            Long drugId = drugDto.getId();
        List<DrugLabel> labels = drugDto.getLabels();
        labels = labels.stream().map(drugLabel -> {
                drugLabel.setDrugId(drugId);
                return drugLabel;
            }).collect(Collectors.toList());
            drugLabelService.saveBatch(labels);
        }


    /**
     * 根据id查询药品信息，同时查询对应的标签
     * @param id
     * @return
     */
    @Override
    public DrugDto getByIdWithLabel(Long id) {
        Drug drug = this.getById(id);
        LambdaQueryWrapper<DrugLabel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DrugLabel::getDrugId,id);
        List<DrugLabel> list = drugLabelService.list(queryWrapper);
        DrugDto drugDto = new DrugDto();
        BeanUtils.copyProperties(drug,drugDto);
        drugDto.setLabels(list);
        return drugDto;
    }

    /**
     * 修改药品，同时修改药品对应的标签
     * @param drugDto
     */
    @Override
    public void updateDrugWithLabel(DrugDto drugDto) {
        this.updateById(drugDto);
        Long id = drugDto.getId();
        LambdaQueryWrapper<DrugLabel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DrugLabel::getDrugId,id);
        drugLabelService.remove(queryWrapper);
        List<DrugLabel> labels = drugDto.getLabels();
        labels = labels.stream().map(drugLabel -> {
            drugLabel.setDrugId(id);
            return drugLabel;
        }).collect(Collectors.toList());
        drugLabelService.saveBatch(labels);
    }

    /**
     * 单个或批量删除药品以及相应的标签
     * @param ids
     */
    @Override
    public void removeDrugWithLabel(String ids) {

        List<String> idList = Arrays.asList((ids.split(",")));
        List<Long> idListLong = idList.stream().map(s -> {
            Long idLong = Long.parseLong(s);
            return idLong;
        }).collect(Collectors.toList());

        LambdaQueryWrapper<Drug> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Drug::getId,idListLong)
                .eq(Drug::getStatus,1);
        int count = this.count(queryWrapper);
        if(count > 0){
            throw new CustomException("药品售卖中，不可删除");
        }
        LambdaQueryWrapper<DrugLabel> drugLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        drugLabelLambdaQueryWrapper.in(DrugLabel::getDrugId,idListLong);
        drugLabelService.remove(drugLabelLambdaQueryWrapper);
        this.removeByIds(idListLong);
    }
}
