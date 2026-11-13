package com.ftn.sbnz.service.service;

import com.ftn.sbnz.model.template.MaxConcentrationByPhototype;
import com.ftn.sbnz.service.config.KieBaseHolder;
import com.ftn.sbnz.service.entity.MaxConcentrationRuleEntity;
import com.ftn.sbnz.service.repository.MaxConcentrationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final MaxConcentrationRuleRepository rules;
    private final KieBaseHolder kieBaseHolder;

    public void rebuildFromDatabase() {
        kieBaseHolder.rebuild(rules.findAllByOrderByIdAsc().stream().map(TemplateService::toRow).toList());
    }

    static MaxConcentrationByPhototype toRow(MaxConcentrationRuleEntity e) {
        return new MaxConcentrationByPhototype(e.getSastojak(), e.getMinFototip(), e.getMaxFototip(),
                e.getMaxKoncentracija(), e.getNapomena());
    }

    static List<MaxConcentrationByPhototype> toRows(List<MaxConcentrationRuleEntity> entities) {
        return entities.stream().map(TemplateService::toRow).toList();
    }
}
