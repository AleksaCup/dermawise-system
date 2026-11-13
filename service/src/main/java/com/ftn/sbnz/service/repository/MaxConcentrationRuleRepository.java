package com.ftn.sbnz.service.repository;

import com.ftn.sbnz.service.entity.MaxConcentrationRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaxConcentrationRuleRepository extends JpaRepository<MaxConcentrationRuleEntity, Long> {

    List<MaxConcentrationRuleEntity> findAllByOrderByIdAsc();
}
