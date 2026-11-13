package com.ftn.sbnz.service.service;

import com.ftn.sbnz.kjar.CepResult;
import com.ftn.sbnz.kjar.CepSession;
import com.ftn.sbnz.service.config.KieBaseHolder;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.DiaryEntry;
import com.ftn.sbnz.service.repository.DiaryEntryRepository;
import com.ftn.sbnz.service.repository.SkinProfileRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class CepSessionManager {

    private static final Logger log = LoggerFactory.getLogger(CepSessionManager.class);

    private final KieBaseHolder kieBaseHolder;
    private final DiaryEntryRepository diary;
    private final SkinProfileRepository profiles;
    private final FactAssembler assembler;

    private final Map<Long, ManagedSession> sessions = new ConcurrentHashMap<>();

    private record ManagedSession(CepSession session, long kieBaseVersion) {
    }

    public synchronized CepResult submit(AppUser user, DiaryEntry entry) {
        CepSession session = sessionFor(user, entry.getId());
        return session.insert(assembler.toEvent(entry), Date.from(entry.getOccurredAt()));
    }

    public synchronized void invalidate(Long userId) {
        ManagedSession removed = sessions.remove(userId);
        if (removed != null) {
            removed.session().close();
        }
    }

    public int openSessions() {
        return sessions.size();
    }

    private CepSession sessionFor(AppUser user, Long newEntryId) {
        ManagedSession managed = sessions.get(user.getId());
        if (managed != null && managed.kieBaseVersion() == kieBaseHolder.getVersion()) {
            return managed.session();
        }
        if (managed != null) {
            managed.session().close();
        }

        CepSession session = new CepSession(kieBaseHolder.getCepKieBase(),
                assembler.cepUser(user, profiles.findByUser(user).orElse(null)));
        int replayed = 0;
        Instant from = Instant.now().minus(FactAssembler.DIARY_HISTORY);
        for (DiaryEntry past : diary.findByUserAndOccurredAtAfterOrderByOccurredAtAsc(user, from)) {
            if (!Objects.equals(past.getId(), newEntryId)) {
                session.insert(assembler.toEvent(past), Date.from(past.getOccurredAt()));
                replayed++;
            }
        }
        sessions.put(user.getId(), new ManagedSession(session, kieBaseHolder.getVersion()));
        log.info("CEP session for user {} created, {} diary entries replayed without alerts", user.getId(), replayed);
        return session;
    }

    @PreDestroy
    public synchronized void closeAll() {
        sessions.values().forEach(managed -> managed.session().close());
        sessions.clear();
    }
}
