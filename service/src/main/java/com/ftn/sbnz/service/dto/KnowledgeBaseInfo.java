package com.ftn.sbnz.service.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record KnowledgeBaseInfo(
        long version,
        Instant rebuiltAt,
        int templateRows,
        int openCepSessions,
        List<Base> bases) {

    public record Base(String name, int rules, int queries, Map<String, Long> rulesPerAgendaGroup,
                       List<RuleInfo> ruleList, List<String> queryList) {
    }

    public record RuleInfo(String name, String agendaGroup, String source, String opis) {
    }
}
