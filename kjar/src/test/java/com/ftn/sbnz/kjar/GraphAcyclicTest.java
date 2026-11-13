package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.seed.EvidenceGraph;
import com.ftn.sbnz.kjar.seed.IngredientGraph;
import com.ftn.sbnz.model.facts.Dokazuje;
import com.ftn.sbnz.model.facts.IngredientGroup;
import com.ftn.sbnz.model.facts.Veza;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GraphAcyclicTest {

    @Test
    void evidenceGraphHasNoCycles() {
        Map<String, List<String>> graph = new HashMap<>();
        for (Dokazuje edge : EvidenceGraph.edges()) {
            graph.computeIfAbsent(edge.getChild(), k -> new ArrayList<>()).add(edge.getParent());
        }
        assertThat(hasCycle(graph)).isFalse();
    }

    @Test
    void ingredientGraphHasNoCycles() {
        Map<String, List<String>> graph = new HashMap<>();
        for (Veza edge : IngredientGraph.edges()) {
            graph.computeIfAbsent(edge.getFromNode(), k -> new ArrayList<>()).add(edge.getToNode());
        }
        assertThat(hasCycle(graph)).isFalse();
    }

    @Test
    void cycleIsDetected() {
        Map<String, List<String>> graph = Map.of("a", List.of("b"), "b", List.of("c"), "c", List.of("a"));

        assertThat(hasCycle(graph)).isTrue();
    }

    @Test
    void everyGroupIsUsedInTheGraph() {
        Set<String> targets = new HashSet<>();
        IngredientGraph.edges().forEach(edge -> targets.add(edge.getToNode()));

        assertThat(IngredientGraph.groups()).extracting(IngredientGroup::getName).allMatch(targets::contains);
    }

    private boolean hasCycle(Map<String, List<String>> graph) {
        Set<String> done = new HashSet<>();
        for (String node : graph.keySet()) {
            if (visit(node, graph, new HashSet<>(), done)) {
                return true;
            }
        }
        return false;
    }

    private boolean visit(String node, Map<String, List<String>> graph, Set<String> path, Set<String> done) {
        if (path.contains(node)) {
            return true;
        }
        if (done.contains(node)) {
            return false;
        }
        path.add(node);
        for (String next : graph.getOrDefault(node, List.of())) {
            if (visit(next, graph, path, done)) {
                return true;
            }
        }
        path.remove(node);
        done.add(node);
        return false;
    }
}
