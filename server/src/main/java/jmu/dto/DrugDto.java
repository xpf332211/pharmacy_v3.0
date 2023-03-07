package jmu.dto;

import jmu.pojo.Drug;
import jmu.pojo.DrugLabel;
import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
public class DrugDto extends Drug {
    private List<DrugLabel> labels;

    private String categoryName;

    private Integer copies;
}
