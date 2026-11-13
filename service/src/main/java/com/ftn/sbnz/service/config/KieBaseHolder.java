package com.ftn.sbnz.service.config;

import com.ftn.sbnz.kjar.KnowledgeBaseFactory;
import com.ftn.sbnz.kjar.KnowledgeBases;
import com.ftn.sbnz.model.template.MaxConcentrationByPhototype;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class KieBaseHolder {

    private static final Logger log = LoggerFactory.getLogger(KieBaseHolder.class);

    private final KnowledgeBaseFactory factory = new KnowledgeBaseFactory();
    private final AtomicReference<KnowledgeBases> current = new AtomicReference<>();
    private final AtomicLong version = new AtomicLong();
    private volatile Instant rebuiltAt;
    private volatile int templateRows;

    public KieBaseHolder() {
        rebuild(List.of());
    }

    public synchronized void rebuild(List<MaxConcentrationByPhototype> rows) {
        KnowledgeBases rebuilt = factory.buildWithMaxConcentration(rows);
        current.set(rebuilt);
        templateRows = rows.size();
        rebuiltAt = Instant.now();
        long newVersion = version.incrementAndGet();
        log.info("Knowledge bases rebuilt, version {}, {} MaxConcentrationByPhototype rows", newVersion, rows.size());
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

    public long getVersion() {
        return version.get();
    }

    public Instant getRebuiltAt() {
        return rebuiltAt;
    }

    public int getTemplateRows() {
        return templateRows;
    }
}
