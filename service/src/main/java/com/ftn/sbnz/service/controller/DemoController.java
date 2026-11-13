package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.model.enums.FlushTrigger;
import com.ftn.sbnz.service.dto.CepAlertDto;
import com.ftn.sbnz.service.dto.DemoWeekResponse;
import com.ftn.sbnz.service.dto.DiaryEntryResponse;
import com.ftn.sbnz.service.dto.FlushingEntryRequest;
import com.ftn.sbnz.service.dto.RecommendationDto;
import com.ftn.sbnz.service.dto.SunEntryRequest;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.SkinProfile;
import com.ftn.sbnz.service.repository.SkinProfileRepository;
import com.ftn.sbnz.service.security.SecurityUser;
import com.ftn.sbnz.service.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/api/demo")
@Profile("dev")
@RequiredArgsConstructor
public class DemoController {

    private final DiaryService diaryService;
    private final SkinProfileRepository profiles;

    private record Step(Instant at, Function<AppUser, DiaryEntryResponse> action) {
    }

    @PostMapping("/cep-week")
    public DemoWeekResponse cepWeek(@AuthenticationPrincipal SecurityUser principal) {
        AppUser user = principal.getUser();
        Instant now = Instant.now();
        List<Step> steps = new ArrayList<>();

        FlushTrigger[] triggers = {FlushTrigger.ALKOHOL, FlushTrigger.TOPLOTA, FlushTrigger.ALKOHOL,
                FlushTrigger.HRANA, FlushTrigger.ALKOHOL};
        int[] flushingDays = {6, 5, 4, 2, 1};
        for (int i = 0; i < flushingDays.length; i++) {
            Instant at = now.minus(Duration.ofDays(flushingDays[i])).minus(Duration.ofHours(3));
            FlushTrigger trigger = triggers[i];
            steps.add(new Step(at, u -> diaryService.addFlushing(u, new FlushingEntryRequest(at, 20, trigger))));
        }
        for (int day = 6; day >= 2; day--) {
            Instant at = now.minus(Duration.ofDays(day)).minus(Duration.ofHours(6));
            steps.add(new Step(at, u -> diaryService.addSun(u, new SunEntryRequest(at, 7.0, 60, 0))));
        }
        steps.sort(Comparator.comparing(Step::at));

        List<CepAlertDto> alerts = new ArrayList<>();
        List<RecommendationDto> recommendations = new ArrayList<>();
        for (Step step : steps) {
            DiaryEntryResponse response = step.action().apply(user);
            alerts.addAll(response.alerts());
            recommendations.addAll(response.recommendations());
        }

        boolean phototypeKnown = profiles.findByUser(user).map(SkinProfile::getFototip).orElse(0) > 0;
        String note = phototypeKnown ? "Dodato je 5 flushing epizoda i 5 dana na suncu (UV 7, 60 min, bez SPF)."
                : "Fototip još nije određen, pa pravila za MED ne mogu da se primene. Prvo pokrenite analizu.";
        return new DemoWeekResponse(steps.size(), note, alerts, recommendations);
    }
}
