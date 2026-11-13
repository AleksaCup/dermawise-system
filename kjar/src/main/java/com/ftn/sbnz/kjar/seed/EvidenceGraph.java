package com.ftn.sbnz.kjar.seed;

import com.ftn.sbnz.model.facts.Dokazuje;
import com.ftn.sbnz.model.facts.NalazCode;

import java.util.List;

public final class EvidenceGraph {

    private EvidenceGraph() {
    }

    public static List<Dokazuje> edges() {
        return List.of(
                new Dokazuje(NalazCode.FLUSHING_UPITNIK, NalazCode.TRANZIJENTNI_ERITEM),
                new Dokazuje(NalazCode.FLUSHING_DNEVNIK, NalazCode.TRANZIJENTNI_ERITEM),
                new Dokazuje(NalazCode.TRANZIJENTNI_ERITEM, NalazCode.PRIMARNI_ZNAK),
                new Dokazuje(NalazCode.PERZISTENTNI_ERITEM, NalazCode.PRIMARNI_ZNAK),
                new Dokazuje(NalazCode.TELANGIEKTAZIJE, NalazCode.PRIMARNI_ZNAK),
                new Dokazuje(NalazCode.PAPULE_PUSTULE_BEZ_KOMEDONA, NalazCode.PRIMARNI_ZNAK));
    }
}
