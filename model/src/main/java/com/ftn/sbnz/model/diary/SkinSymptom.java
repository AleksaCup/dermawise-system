package com.ftn.sbnz.model.diary;

import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.SymptomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Position;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkinSymptom {

    @Position(0)
    private String userId;
    private Date timestamp;
    private SymptomType type;
    private BodyArea location;
}
