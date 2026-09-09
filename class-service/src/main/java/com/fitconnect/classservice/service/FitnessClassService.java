package com.fitconnect.classservice.service;

import com.fitconnect.classservice.api.*;
import com.fitconnect.classservice.domain.*;
import com.fitconnect.classservice.repository.FitnessClassRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class FitnessClassService {
    private final FitnessClassRepository repository;
    public FitnessClassService(FitnessClassRepository repository){this.repository=repository;}
    public Page<FitnessClassResponse> search(ClassCategory category, ClassLevel level, LocalDateTime from, LocalDateTime to, String location, String instructor, int page, int size, String sort) {
        String[] sortParts=sort.split(","); Sort.Direction direction=sortParts.length>1 && "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Specification<FitnessClass> spec=(root,q,cb)->cb.conjunction();
        if(category!=null) spec=spec.and((r,q,c)->c.equal(r.get("category"),category));
        if(level!=null) spec=spec.and((r,q,c)->c.equal(r.get("level"),level));
        if(from!=null) spec=spec.and((r,q,c)->c.greaterThanOrEqualTo(r.get("dateTime"),from));
        if(to!=null) spec=spec.and((r,q,c)->c.lessThanOrEqualTo(r.get("dateTime"),to));
        if(location!=null && !location.isBlank()) spec=spec.and((r,q,c)->c.equal(r.get("gymLocation"),location));
        if(instructor!=null && !instructor.isBlank()) spec=spec.and((r,q,c)->c.equal(r.get("instructor"),instructor));
        return repository.findAll(spec, PageRequest.of(page,size,Sort.by(direction,sortParts[0]))).map(FitnessClassResponse::from);
    }
    public FitnessClassResponse get(Long id){return FitnessClassResponse.from(repository.findById(id).orElseThrow(()->new IllegalArgumentException("Cours introuvable")));}
    @Transactional public FitnessClassResponse create(FitnessClassRequest r){int current=r.currentParticipants()==null?0:r.currentParticipants(); if(current>r.maxParticipants()) throw new IllegalArgumentException("Participants invalides"); return FitnessClassResponse.from(repository.save(new FitnessClass(r.name(),r.description(),r.instructor(),r.gymLocation(),r.category(),r.level(),r.durationMinutes(),r.maxParticipants(),current,r.price(),r.dateTime())));}
    @Transactional public FitnessClassResponse update(Long id,FitnessClassRequest r){FitnessClass c=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Cours introuvable")); c.updateFrom(new FitnessClass(r.name(),r.description(),r.instructor(),r.gymLocation(),r.category(),r.level(),r.durationMinutes(),r.maxParticipants(),c.getCurrentParticipants(),r.price(),r.dateTime())); return FitnessClassResponse.from(repository.save(c));}
    @Transactional public void cancel(Long id){FitnessClass c=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Cours introuvable")); c.cancel(); repository.save(c);}
    @Transactional public FitnessClassResponse increment(Long id,int spots){FitnessClass c=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Cours introuvable")); c.incrementParticipants(spots); return FitnessClassResponse.from(repository.saveAndFlush(c));}
    @Transactional public FitnessClassResponse decrement(Long id,int spots){FitnessClass c=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Cours introuvable")); c.decrementParticipants(spots); return FitnessClassResponse.from(repository.save(c));}
}
