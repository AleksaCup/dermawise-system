package com.ftn.sbnz.model.questionnaire;

import com.ftn.sbnz.model.enums.EthnicOrigin;
import com.ftn.sbnz.model.enums.EyeColor;
import com.ftn.sbnz.model.enums.HairColor;
import com.ftn.sbnz.model.enums.SkinColor;
import com.ftn.sbnz.model.enums.SunReaction;
import com.ftn.sbnz.model.enums.TanTendency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhototypeAnswers {

    private String userId;
    private SkinColor skinColor;
    private EyeColor eyeColor;
    private HairColor hairColor;
    private EthnicOrigin ethnicOrigin;
    private SunReaction sunReaction;
    private TanTendency tanTendency;
}
