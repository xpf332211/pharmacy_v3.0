package jmu.dto;

import jmu.pojo.Suit;
import jmu.pojo.SuitDrug;
import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
public class SuitDto extends Suit {
    private List<SuitDrug> suitDrugs;

    private String categoryName;
}
