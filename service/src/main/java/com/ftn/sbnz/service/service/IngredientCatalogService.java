package com.ftn.sbnz.service.service;

import com.ftn.sbnz.kjar.seed.IngredientGraph;
import com.ftn.sbnz.model.enums.VezaType;
import com.ftn.sbnz.model.facts.IngredientGroup;
import com.ftn.sbnz.model.facts.Veza;
import com.ftn.sbnz.service.dto.IngredientOption;
import com.ftn.sbnz.service.dto.IngredientOption.Kind;
import com.ftn.sbnz.service.entity.MaxConcentrationRuleEntity;
import com.ftn.sbnz.service.repository.MaxConcentrationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class IngredientCatalogService {

    private static final List<String> THERAPY_INGREDIENTS = List.of(
            "adapalen", "benzoil peroksid", "azelainska kiselina", "metronidazol", "ivermektin", "brimonidin",
            "niacinamid", "hidrokortizon", "cink pirition", "tretinoin", "vitamin c");

    private final MaxConcentrationRuleRepository templateRows;

    @Transactional(readOnly = true)
    public List<IngredientOption> all() {
        Map<String, TreeSet<String>> partOf = new TreeMap<>();
        Map<String, Kind> kinds = new LinkedHashMap<>();
        for (Veza edge : IngredientGraph.edges()) {
            if (edge.getType() == VezaType.CONTAINS) {
                kinds.put(edge.getFromNode(), Kind.MIXTURE);
                partOf.computeIfAbsent(edge.getToNode(), k -> new TreeSet<>()).add(edge.getFromNode());
            } else {
                partOf.computeIfAbsent(edge.getFromNode(), k -> new TreeSet<>()).add(edge.getToNode());
            }
            kinds.putIfAbsent(edge.getFromNode(), Kind.INGREDIENT);
            kinds.putIfAbsent(edge.getToNode(), Kind.INGREDIENT);
        }
        for (IngredientGroup group : IngredientGraph.groups()) {
            kinds.put(group.getName(), Kind.GROUP);
        }
        for (String name : THERAPY_INGREDIENTS) {
            kinds.put(name, Kind.THERAPY);
        }
        for (MaxConcentrationRuleEntity row : templateRows.findAllByOrderByIdAsc()) {
            kinds.putIfAbsent(row.getSastojak(), Kind.THERAPY);
        }

        List<IngredientOption> options = new ArrayList<>();
        kinds.forEach((name, kind) -> options.add(
                new IngredientOption(name, kind, List.copyOf(partOf.getOrDefault(name, new TreeSet<>())))));
        options.sort(Comparator.comparing(IngredientOption::kind).thenComparing(IngredientOption::name));
        return options;
    }
}
