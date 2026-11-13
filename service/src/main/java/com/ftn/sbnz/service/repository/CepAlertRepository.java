package com.ftn.sbnz.service.repository;

import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.CepAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CepAlertRepository extends JpaRepository<CepAlertEntity, Long> {

    List<CepAlertEntity> findByUserOrderByOccurredAtDesc(AppUser user);
}
