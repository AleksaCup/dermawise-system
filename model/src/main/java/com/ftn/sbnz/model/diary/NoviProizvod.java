package com.ftn.sbnz.model.diary;

import com.ftn.sbnz.model.questionnaire.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Position;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoviProizvod {

    @Position(0)
    private String userId;
    private Date timestamp;
    private Product proizvod;
    private int daysSinceIntroduced;
}
