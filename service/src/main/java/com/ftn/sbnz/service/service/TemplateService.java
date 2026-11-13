package com.ftn.sbnz.service.service;

import com.ftn.sbnz.kjar.KnowledgeBaseFactory;
import com.ftn.sbnz.kjar.TemplateCompiler;
import com.ftn.sbnz.model.template.MaxConcentrationByPhototype;
import com.ftn.sbnz.service.config.KieBaseHolder;
import com.ftn.sbnz.service.dto.TemplateRuleDto;
import com.ftn.sbnz.service.dto.TemplateRuleRequest;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.MaxConcentrationRuleEntity;
import com.ftn.sbnz.service.exception.BadRequestException;
import com.ftn.sbnz.service.exception.NotFoundException;
import com.ftn.sbnz.service.repository.MaxConcentrationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private static final Pattern INGREDIENT = Pattern.compile("[\\p{L}\\p{N} .,%()+\\-]+");
    private static final Pattern FORBIDDEN_IN_NOTE = Pattern.compile("[\"\\\\@$]");

    private final MaxConcentrationRuleRepository rules;
    private final KieBaseHolder kieBaseHolder;
    private final TemplateCompiler templateCompiler = new TemplateCompiler();

    @Transactional(readOnly = true)
    public List<TemplateRuleDto> list() {
        return rules.findAllByOrderByIdAsc().stream().map(TemplateRuleDto::from).toList();
    }

    @Transactional
    public TemplateRuleDto create(TemplateRuleRequest request, AppUser dermatologist) {
        MaxConcentrationRuleEntity entity = new MaxConcentrationRuleEntity();
        apply(entity, request, dermatologist);
        MaxConcentrationRuleEntity saved = rules.save(entity);
        rebuildFromDatabase();
        return TemplateRuleDto.from(saved);
    }

    @Transactional
    public TemplateRuleDto update(Long id, TemplateRuleRequest request, AppUser dermatologist) {
        MaxConcentrationRuleEntity entity = require(id);
        apply(entity, request, dermatologist);
        rules.save(entity);
        rebuildFromDatabase();
        return TemplateRuleDto.from(entity);
    }

    @Transactional
    public void delete(Long id) {
        rules.delete(require(id));
        rebuildFromDatabase();
    }

    @Transactional(readOnly = true)
    public String generatedDrl() {
        List<MaxConcentrationByPhototype> rows = toRows(rules.findAllByOrderByIdAsc());
        if (rows.isEmpty()) {
            return "";
        }
        return templateCompiler.compile(KnowledgeBaseFactory.MAX_CONCENTRATION_TEMPLATE, rows);
    }

    public void rebuildFromDatabase() {
        try {
            kieBaseHolder.rebuild(toRows(rules.findAllByOrderByIdAsc()));
        } catch (IllegalStateException e) {
            throw new BadRequestException("Red template-a ne može da se pretvori u pravilo, izmena nije sačuvana", e);
        }
    }

    private MaxConcentrationRuleEntity require(Long id) {
        return rules.findById(id).orElseThrow(() -> new NotFoundException("Red template-a " + id + " ne postoji"));
    }

    private void apply(MaxConcentrationRuleEntity entity, TemplateRuleRequest request, AppUser dermatologist) {
        String sastojak = ProfileService.name(request.sastojak());
        if (!INGREDIENT.matcher(sastojak).matches()) {
            throw new BadRequestException("Naziv sastojka sme da sadrži samo slova, brojeve, razmak i . , % ( ) + -");
        }
        if (FORBIDDEN_IN_NOTE.matcher(request.napomena()).find()) {
            throw new BadRequestException("Napomena ne sme da sadrži znake \" \\ @ $");
        }
        if (request.minFototip() > request.maxFototip()) {
            throw new BadRequestException("minFototip ne sme biti veći od maxFototip");
        }
        entity.setSastojak(sastojak);
        entity.setMinFototip(request.minFototip());
        entity.setMaxFototip(request.maxFototip());
        entity.setMaxKoncentracija(request.maxKoncentracija());
        entity.setNapomena(request.napomena().trim());
        entity.setUpdatedBy(dermatologist.getEmail());
        entity.setUpdatedAt(Instant.now());
    }

    static MaxConcentrationByPhototype toRow(MaxConcentrationRuleEntity e) {
        return new MaxConcentrationByPhototype(e.getSastojak(), e.getMinFototip(), e.getMaxFototip(),
                e.getMaxKoncentracija(), e.getNapomena());
    }

    static List<MaxConcentrationByPhototype> toRows(List<MaxConcentrationRuleEntity> entities) {
        return entities.stream().map(TemplateService::toRow).toList();
    }
}
