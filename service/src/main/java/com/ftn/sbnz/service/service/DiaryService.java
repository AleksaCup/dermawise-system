package com.ftn.sbnz.service.service;

import com.ftn.sbnz.service.dto.DiaryEntryDto;
import com.ftn.sbnz.service.dto.DiaryEntryResponse;
import com.ftn.sbnz.service.dto.FlushingEntryRequest;
import com.ftn.sbnz.service.dto.ProductDto;
import com.ftn.sbnz.service.dto.ProductEntryRequest;
import com.ftn.sbnz.service.dto.SunEntryRequest;
import com.ftn.sbnz.service.dto.SymptomEntryRequest;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.DiaryEntry;
import com.ftn.sbnz.service.entity.DiaryEntryType;
import com.ftn.sbnz.service.exception.BadRequestException;
import com.ftn.sbnz.service.repository.DiaryEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryEntryRepository diary;
    private final CepService cepService;
    private final FactAssembler assembler;

    @Transactional
    public DiaryEntryResponse addFlushing(AppUser user, FlushingEntryRequest request) {
        DiaryEntry entry = entry(user, DiaryEntryType.FLUSHING, request.occurredAt());
        entry.setDurationMinutes(request.durationMinutes());
        entry.setFlushTrigger(request.trigger());
        return save(user, entry);
    }

    @Transactional
    public DiaryEntryResponse addSun(AppUser user, SunEntryRequest request) {
        DiaryEntry entry = entry(user, DiaryEntryType.SUN, request.occurredAt());
        entry.setUvIndex(request.uvIndex());
        entry.setMinutesInSun(request.minutesInSun());
        entry.setSunscreenSpf(request.sunscreenSpf());
        return save(user, entry);
    }

    @Transactional
    public DiaryEntryResponse addProduct(AppUser user, ProductEntryRequest request) {
        if (request.product().applicationAreas() == null || request.product().applicationAreas().isEmpty()) {
            throw new BadRequestException("Navedite gde se proizvod nanosi");
        }
        ProductDto product = ProfileService.normalize(request.product());
        DiaryEntry entry = entry(user, DiaryEntryType.NEW_PRODUCT, request.occurredAt());
        entry.setProductName(product.name());
        entry.setProductJson(assembler.writeJson(product));
        return save(user, entry);
    }

    @Transactional
    public DiaryEntryResponse addSymptom(AppUser user, SymptomEntryRequest request) {
        DiaryEntry entry = entry(user, DiaryEntryType.SYMPTOM, request.occurredAt());
        entry.setSymptomType(request.type());
        entry.setSymptomLocation(request.location());
        return save(user, entry);
    }

    @Transactional(readOnly = true)
    public List<DiaryEntryDto> list(AppUser user) {
        return diary.findByUserOrderByOccurredAtDesc(user).stream().map(DiaryEntryDto::from).toList();
    }

    private DiaryEntry entry(AppUser user, DiaryEntryType type, Instant occurredAt) {
        Instant now = Instant.now();
        Instant time = occurredAt == null ? now : occurredAt;
        if (time.isAfter(now.plus(Duration.ofMinutes(5)))) {
            throw new BadRequestException("Unos u dnevnik ne može biti u budućnosti");
        }
        DiaryEntry entry = new DiaryEntry();
        entry.setUser(user);
        entry.setType(type);
        entry.setOccurredAt(time);
        return entry;
    }

    private DiaryEntryResponse save(AppUser user, DiaryEntry entry) {
        return cepService.process(user, diary.save(entry));
    }
}
