package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.diary.SkinSymptom;
import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.EthnicOrigin;
import com.ftn.sbnz.model.enums.EyeColor;
import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.HairColor;
import com.ftn.sbnz.model.enums.LesionType;
import com.ftn.sbnz.model.enums.SkinColor;
import com.ftn.sbnz.model.enums.SunReaction;
import com.ftn.sbnz.model.enums.SymptomType;
import com.ftn.sbnz.model.enums.TanTendency;
import com.ftn.sbnz.model.facts.QuestionnaireSubmitted;
import com.ftn.sbnz.model.facts.User;
import com.ftn.sbnz.model.questionnaire.LifestyleAnswers;
import com.ftn.sbnz.model.questionnaire.PhototypeAnswers;
import com.ftn.sbnz.model.questionnaire.RegionLesion;
import com.ftn.sbnz.model.questionnaire.RosaceaAnswers;
import com.ftn.sbnz.model.questionnaire.ScoradAnswers;
import com.ftn.sbnz.kjar.seed.MedValues;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Questionnaires {

    public static final String USER_ID = "u1";

    private final User user = new User(USER_ID);
    private PhototypeAnswers phototype = new PhototypeAnswers(USER_ID, SkinColor.FAIR, EyeColor.HAZEL,
            HairColor.LIGHT_BROWN, EthnicOrigin.SOUTHERN_EUROPEAN, SunReaction.BURNS_THEN_TANS, TanTendency.GRADUAL);
    private final Map<FaceRegion, LesionType> lesions = new EnumMap<>(FaceRegion.class);
    private RosaceaAnswers rosacea = new RosaceaAnswers();
    private ScoradAnswers scorad = new ScoradAnswers();
    private LifestyleAnswers lifestyle = new LifestyleAnswers();
    private final List<Object> extraFacts = new ArrayList<>();

    public Questionnaires() {
        for (FaceRegion region : FaceRegion.values()) {
            lesions.put(region, LesionType.NONE);
        }
        rosacea.setUserId(USER_ID);
        scorad.setUserId(USER_ID);
        lifestyle.setUserId(USER_ID);
    }

    public static Questionnaires standard() {
        return new Questionnaires();
    }

    public Questionnaires phototype(PhototypeAnswers answers) {
        answers.setUserId(USER_ID);
        this.phototype = answers;
        return this;
    }

    public Questionnaires sun(SunReaction reaction, TanTendency tan, SkinColor skin) {
        phototype.setSunReaction(reaction);
        phototype.setTanTendency(tan);
        phototype.setSkinColor(skin);
        return this;
    }

    public Questionnaires lesion(FaceRegion region, LesionType lesion) {
        lesions.put(region, lesion);
        return this;
    }

    public Questionnaires withoutScorad() {
        this.scorad = null;
        return this;
    }

    public Questionnaires withoutRegion(FaceRegion region) {
        lesions.remove(region);
        return this;
    }

    public Questionnaires symptom(SymptomType type, BodyArea location) {
        extraFacts.add(new SkinSymptom(USER_ID, new Date(), type, location));
        return this;
    }

    public PhototypeAnswers phototype() {
        return phototype;
    }

    public RosaceaAnswers rosacea() {
        return rosacea;
    }

    public ScoradAnswers scorad() {
        return scorad;
    }

    public LifestyleAnswers lifestyle() {
        return lifestyle;
    }

    public User user() {
        return user;
    }

    public List<Object> facts() {
        List<Object> facts = new ArrayList<>(MedValues.all());
        facts.add(user);
        facts.add(phototype);
        lesions.forEach((region, lesion) -> facts.add(new RegionLesion(USER_ID, region, lesion)));
        facts.add(rosacea);
        if (scorad != null) {
            facts.add(scorad);
        }
        facts.add(lifestyle);
        facts.addAll(extraFacts);
        facts.add(new QuestionnaireSubmitted(USER_ID));
        return facts;
    }
}
