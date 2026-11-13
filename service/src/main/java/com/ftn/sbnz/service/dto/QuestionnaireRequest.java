package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.LesionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record QuestionnaireRequest(
        @NotNull @Valid PhototypeSection phototype,
        @NotNull Map<FaceRegion, LesionType> lesions,
        @NotNull @Valid RosaceaSection rosacea,
        @NotNull @Valid ScoradSection scorad,
        @NotNull @Valid LifestyleSection lifestyle) {
}
