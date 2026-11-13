package com.ftn.sbnz.model.questionnaire;

import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.LesionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionLesion {

    private String userId;
    private FaceRegion region;
    private LesionType lesion;
}
