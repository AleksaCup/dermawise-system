package com.ftn.sbnz.service.config;

import com.ftn.sbnz.kjar.KnowledgeBaseFactory;
import com.ftn.sbnz.kjar.KnowledgeBases;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class KieBaseHolder {

    private static final Logger log = LoggerFactory.getLogger(KieBaseHolder.class);

    private final KnowledgeBaseFactory factory = new KnowledgeBaseFactory();
    private final AtomicReference<KnowledgeBases> current = new AtomicReference<>();

    public KieBaseHolder() {
        rebuild(List.of());
    }

    public synchronized void rebuild(List<String> generatedDrl) {
        KnowledgeBases rebuilt = factory.buildAll(generatedDrl);
        current.set(rebuilt);
        log.info("Knowledge bases rebuilt with {} generated rule files", generatedDrl.size());
    }

    public KieBase getAnalysisKieBase() {
        return current.get().analysis();
    }

    public KieBase getBackwardKieBase() {
        return current.get().backward();
    }

    public KieBase getCepKieBase() {
        return current.get().cep();
    }
}
