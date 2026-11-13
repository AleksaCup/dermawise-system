package com.ftn.sbnz.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.sbnz.model.diary.FlushingEpizoda;
import com.ftn.sbnz.model.diary.FlushingSummary;
import com.ftn.sbnz.model.diary.NoviProizvod;
import com.ftn.sbnz.model.diary.SkinSymptom;
import com.ftn.sbnz.model.diary.UVDanEvent;
import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.FlushTrigger;
import com.ftn.sbnz.model.facts.QuestionnaireSubmitted;
import com.ftn.sbnz.model.facts.User;
import com.ftn.sbnz.model.questionnaire.LifestyleAnswers;
import com.ftn.sbnz.model.questionnaire.PhototypeAnswers;
import com.ftn.sbnz.model.questionnaire.Product;
import com.ftn.sbnz.model.questionnaire.ProductIngredient;
import com.ftn.sbnz.model.questionnaire.RegionLesion;
import com.ftn.sbnz.model.questionnaire.RosaceaAnswers;
import com.ftn.sbnz.model.questionnaire.ScoradAnswers;
import com.ftn.sbnz.service.dto.LifestyleSection;
import com.ftn.sbnz.service.dto.PhototypeSection;
import com.ftn.sbnz.service.dto.ProductDto;
import com.ftn.sbnz.service.dto.QuestionnaireRequest;
import com.ftn.sbnz.service.dto.RosaceaSection;
import com.ftn.sbnz.service.dto.ScoradSection;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.DiaryEntry;
import com.ftn.sbnz.service.entity.DiaryEntryType;
import com.ftn.sbnz.service.entity.SkinProfile;
import com.ftn.sbnz.service.repository.DiaryEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FactAssembler {

    public static final Duration DIARY_HISTORY = Duration.ofDays(31);
    public static final ZoneId ZONE = ZoneId.of("Europe/Belgrade");

    private final ObjectMapper objectMapper;
    private final DiaryEntryRepository diary;

    public QuestionnaireRequest readQuestionnaire(SkinProfile profile) {
        try {
            return objectMapper.readValue(profile.getQuestionnaireJson(), QuestionnaireRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored questionnaire cannot be read", e);
        }
    }

    public String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Value cannot be written as JSON", e);
        }
    }

    public List<Object> analysisFacts(AppUser user, QuestionnaireRequest questionnaire, Instant now) {
        List<Object> facts = questionnaireFacts(user.factId(), questionnaire);
        facts.addAll(diaryFacts(user, now));
        return facts;
    }

    public User cepUser(AppUser user, SkinProfile profile) {
        User fact = new User(user.factId());
        if (profile != null) {
            fact.setFototip(profile.getFototip());
            fact.setMed(profile.getMed());
            fact.setPregnant(profile.isPregnant());
            fact.setBreastfeeding(profile.isBreastfeeding());
        }
        return fact;
    }

    public Object toEvent(DiaryEntry entry) {
        String userId = entry.getUser().factId();
        Date time = Date.from(entry.getOccurredAt());
        return switch (entry.getType()) {
            case FLUSHING -> new FlushingEpizoda(userId, time, entry.getDurationMinutes(), entry.getFlushTrigger());
            case SUN -> new UVDanEvent(userId, time, entry.getUvIndex(), entry.getMinutesInSun(),
                    entry.getSunscreenSpf());
            case NEW_PRODUCT -> new NoviProizvod(userId, time, product(readProduct(entry)), 0);
            case SYMPTOM -> new SkinSymptom(userId, time, entry.getSymptomType(), entry.getSymptomLocation());
        };
    }

    public ProductDto readProduct(DiaryEntry entry) {
        try {
            return objectMapper.readValue(entry.getProductJson(), ProductDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored product cannot be read", e);
        }
    }

    public static Product product(ProductDto dto) {
        List<ProductIngredient> ingredients = dto.ingredients() == null ? List.of() : dto.ingredients().stream()
                .map(i -> new ProductIngredient(i.name(), i.concentration()))
                .toList();
        Set<BodyArea> areas = dto.applicationAreas() == null
                ? new HashSet<>() : new HashSet<>(dto.applicationAreas());
        return new Product(dto.name(), new ArrayList<>(ingredients), areas);
    }

    private List<Object> questionnaireFacts(String userId, QuestionnaireRequest q) {
        List<Object> facts = new ArrayList<>();

        PhototypeSection p = q.phototype();
        facts.add(new PhototypeAnswers(userId, p.skinColor(), p.eyeColor(), p.hairColor(), p.ethnicOrigin(),
                p.sunReaction(), p.tanTendency()));

        for (FaceRegion region : FaceRegion.values()) {
            facts.add(new RegionLesion(userId, region, q.lesions().get(region)));
        }

        RosaceaSection r = q.rosacea();
        facts.add(new RosaceaAnswers(userId, r.persistentErythema(), r.centrofacialDistribution(),
                r.flushingPerWeek(), r.telangiectasia(), r.papulesPustulesWithoutComedones(), r.ocularSymptoms(),
                r.phymatousChanges(), r.flushingGrade(), r.persistentErythemaGrade(), r.telangiectasiaGrade(),
                r.papulesPustulesGrade(), r.papuleCount(), r.pustuleCount(), r.perioralDistribution(),
                r.recentTopicalSteroid()));

        ScoradSection s = q.scorad();
        facts.add(new ScoradAnswers(userId, s.affectedAreaPercent(), s.erythema(), s.edemaPapulation(),
                s.excoriation(), s.lichenification(), s.oozingCrusting(), s.dryness(), s.pruritus(),
                s.sleeplessness()));

        LifestyleSection l = q.lifestyle();
        List<String> allergies = l.allergies() == null ? List.of() : l.allergies();
        List<Product> products = l.currentProducts() == null ? List.of()
                : l.currentProducts().stream().map(FactAssembler::product).toList();
        facts.add(new LifestyleAnswers(userId, l.pregnant(), l.breastfeeding(), l.hormonalTherapy(),
                new ArrayList<>(allergies), new ArrayList<>(products), l.dailySunMinutes()));

        facts.add(new QuestionnaireSubmitted(userId));
        return facts;
    }

    private List<Object> diaryFacts(AppUser user, Instant now) {
        String userId = user.factId();
        List<Object> facts = new ArrayList<>();
        int flushingLastWeek = 0;
        Set<FlushTrigger> triggers = EnumSet.noneOf(FlushTrigger.class);

        for (DiaryEntry entry : diary.findByUserAndOccurredAtAfterOrderByOccurredAtAsc(user, now.minus(DIARY_HISTORY))) {
            if (entry.getType() == DiaryEntryType.SYMPTOM) {
                facts.add(toEvent(entry));
            } else if (entry.getType() == DiaryEntryType.NEW_PRODUCT) {
                int days = (int) ChronoUnit.DAYS.between(
                        entry.getOccurredAt().atZone(ZONE).toLocalDate(), now.atZone(ZONE).toLocalDate());
                facts.add(new NoviProizvod(userId, Date.from(entry.getOccurredAt()),
                        product(readProduct(entry)), days));
            } else if (entry.getType() == DiaryEntryType.FLUSHING
                    && entry.getOccurredAt().isAfter(now.minus(Duration.ofDays(7)))) {
                flushingLastWeek++;
                if (entry.getFlushTrigger() != null) {
                    triggers.add(entry.getFlushTrigger());
                }
            }
        }
        if (flushingLastWeek > 0) {
            facts.add(new FlushingSummary(userId, flushingLastWeek, new HashSet<>(triggers)));
        }
        return facts;
    }
}
