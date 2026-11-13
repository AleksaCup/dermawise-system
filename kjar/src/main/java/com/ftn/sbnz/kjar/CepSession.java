package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.seed.SeedFacts;
import com.ftn.sbnz.model.cep.CepAlert;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.SafetyNote;
import com.ftn.sbnz.model.facts.User;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.time.SessionPseudoClock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CepSession implements AutoCloseable {

    private final KieSession session;
    private final SessionPseudoClock clock;
    private final FactHandle userHandle;

    public CepSession(KieBase cepKieBase, User user) {
        KieSessionConfiguration config = KieServices.Factory.get().newKieSessionConfiguration();
        config.setOption(ClockTypeOption.PSEUDO);
        session = cepKieBase.newKieSession(config, null);
        clock = session.getSessionClock();
        SeedFacts.all().forEach(session::insert);
        userHandle = session.insert(user);
    }

    public CepResult insert(Object event, Date time) {
        moveClockTo(time.getTime());
        session.insert(event);
        return fire();
    }

    public CepResult advanceTo(Date time) {
        moveClockTo(time.getTime());
        return fire();
    }

    public void updateUser(User user) {
        session.update(userHandle, user);
    }

    public Date now() {
        return new Date(clock.getCurrentTime());
    }

    public <T> List<T> all(Class<T> type) {
        return session.getObjects(type::isInstance).stream().map(type::cast).toList();
    }

    private void moveClockTo(long time) {
        long now = clock.getCurrentTime();
        if (time > now) {
            clock.advanceTime(time - now, TimeUnit.MILLISECONDS);
        }
    }

    private CepResult fire() {
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("safety").setFocus();
        session.fireAllRules();
        return new CepResult(take(CepAlert.class), take(Recommendation.class), take(SafetyNote.class));
    }

    private <T> List<T> take(Class<T> type) {
        List<T> taken = new ArrayList<>();
        List<FactHandle> handles = new ArrayList<>(session.<FactHandle>getFactHandles(type::isInstance));
        for (FactHandle handle : handles) {
            taken.add(type.cast(session.getObject(handle)));
            session.delete(handle);
        }
        return taken;
    }

    @Override
    public void close() {
        session.dispose();
    }
}
