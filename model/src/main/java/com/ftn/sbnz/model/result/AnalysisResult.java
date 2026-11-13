package com.ftn.sbnz.model.result;

import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.ExcludedDiagnosis;
import com.ftn.sbnz.model.facts.ExplanationStep;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.Referral;
import com.ftn.sbnz.model.facts.SafetyNote;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class AnalysisResult {

    private String userId;
    private int fototip;
    private double med;

    private List<Diagnosis> diagnoses = new ArrayList<>();
    private List<ExcludedDiagnosis> excludedDiagnoses = new ArrayList<>();

    private List<Recommendation> morningRoutine = new ArrayList<>();
    private List<Recommendation> eveningRoutine = new ArrayList<>();
    private List<Recommendation> withdrawn = new ArrayList<>();
    private List<Referral> referrals = new ArrayList<>();

    private List<SafetyNote> safetyNotes = new ArrayList<>();
    private List<String> contactDermatitisCauses = new ArrayList<>();

    private List<ExplanationStep> explanation = new ArrayList<>();
}
