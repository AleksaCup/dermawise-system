package com.ftn.sbnz.kjar;

import org.kie.api.KieBase;

public record KnowledgeBases(KieBase analysis, KieBase backward, KieBase cep) {
}
