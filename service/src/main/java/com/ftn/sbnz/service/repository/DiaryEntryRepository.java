package com.ftn.sbnz.service.repository;

import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.DiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    List<DiaryEntry> findByUserAndOccurredAtAfterOrderByOccurredAtAsc(AppUser user, Instant after);

    List<DiaryEntry> findByUserOrderByOccurredAtDesc(AppUser user);
}
