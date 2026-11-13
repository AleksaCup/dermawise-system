package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.enums.EthnicOrigin;
import com.ftn.sbnz.model.enums.EyeColor;
import com.ftn.sbnz.model.enums.HairColor;
import com.ftn.sbnz.model.enums.SkinColor;
import com.ftn.sbnz.model.enums.SunReaction;
import com.ftn.sbnz.model.enums.TanTendency;
import jakarta.validation.constraints.NotNull;

public record PhototypeSection(
        @NotNull SkinColor skinColor,
        @NotNull EyeColor eyeColor,
        @NotNull HairColor hairColor,
        @NotNull EthnicOrigin ethnicOrigin,
        @NotNull SunReaction sunReaction,
        @NotNull TanTendency tanTendency) {
}
