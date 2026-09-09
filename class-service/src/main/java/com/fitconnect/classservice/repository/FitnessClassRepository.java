package com.fitconnect.classservice.repository;
import com.fitconnect.classservice.domain.FitnessClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface FitnessClassRepository extends JpaRepository<FitnessClass, Long>, JpaSpecificationExecutor<FitnessClass> {}
