package com.ftn.sbnz.service.service;

import com.ftn.sbnz.service.config.KieBaseHolder;
import com.ftn.sbnz.service.dto.KnowledgeBaseInfo;
import lombok.RequiredArgsConstructor;
import org.drools.base.definitions.rule.impl.RuleImpl;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseInfoService {

    private final KieBaseHolder kieBaseHolder;
    private final CepSessionManager cepSessionManager;

    public KnowledgeBaseInfo info() {
        return new KnowledgeBaseInfo(
                kieBaseHolder.getVersion(),
                kieBaseHolder.getRebuiltAt(),
                kieBaseHolder.getTemplateRows(),
                cepSessionManager.openSessions(),
                List.of(
                        describe("analysis", kieBaseHolder.getAnalysisKieBase()),
                        describe("backward", kieBaseHolder.getBackwardKieBase()),
                        describe("cep", kieBaseHolder.getCepKieBase())));
    }

    private KnowledgeBaseInfo.Base describe(String name, KieBase kieBase) {
        List<KnowledgeBaseInfo.RuleInfo> rules = new ArrayList<>();
        List<String> queries = new ArrayList<>();
        for (KiePackage kiePackage : kieBase.getKiePackages()) {
            for (Rule rule : kiePackage.getRules()) {
                RuleImpl impl = (RuleImpl) rule;
                if (impl.isQuery()) {
                    queries.add(rule.getName());
                } else {
                    rules.add(new KnowledgeBaseInfo.RuleInfo(rule.getName(), impl.getAgendaGroup(),
                            text(rule, "source"), text(rule, "opis")));
                }
            }
        }
        rules.sort(Comparator.comparing(KnowledgeBaseInfo.RuleInfo::agendaGroup)
                .thenComparing(KnowledgeBaseInfo.RuleInfo::name));
        queries.sort(Comparator.naturalOrder());
        Map<String, Long> perGroup = rules.stream().collect(Collectors.groupingBy(
                KnowledgeBaseInfo.RuleInfo::agendaGroup, TreeMap::new, Collectors.counting()));
        return new KnowledgeBaseInfo.Base(name, rules.size(), queries.size(), perGroup, rules, queries);
    }

    private static String text(Rule rule, String key) {
        Object value = rule.getMetaData().get(key);
        return value == null ? null : value.toString();
    }
}
