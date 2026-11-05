package com.ftn.sbnz.model.questionnaire;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RosaceaAnswers {

    private String userId;

    private boolean persistentErythema;
    private boolean centrofacialDistribution;
    private int flushingPerWeek;
    private boolean telangiectasia;
    private boolean papulesPustulesWithoutComedones;
    private boolean ocularSymptoms;
    private boolean phymatousChanges;

    private int flushingGrade;
    private int persistentErythemaGrade;
    private int telangiectasiaGrade;
    private int papulesPustulesGrade;

    private int papuleCount;
    private int pustuleCount;
    private boolean perioralDistribution;
    private boolean recentTopicalSteroid;
}
