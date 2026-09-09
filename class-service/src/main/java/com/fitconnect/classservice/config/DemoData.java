package com.fitconnect.classservice.config;
import com.fitconnect.classservice.domain.*;
import com.fitconnect.classservice.repository.FitnessClassRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Configuration public class DemoData { @Bean CommandLineRunner seed(FitnessClassRepository r){return args->{if(r.count()==0){r.save(new FitnessClass("Yoga Matin","Yoga mobilité","Marie","Paris",ClassCategory.YOGA,ClassLevel.BEGINNER,60,20,0,new BigDecimal("25.00"),LocalDateTime.now().plusDays(3)));r.save(new FitnessClass("Crossfit Force","Entraînement fonctionnel","Paul","Lyon",ClassCategory.CROSSFIT,ClassLevel.INTERMEDIATE,60,10,5,new BigDecimal("80.00"),LocalDateTime.now().plusDays(5)));r.save(new FitnessClass("Boxing Premium","Boxe avancée","Sam","Paris",ClassCategory.BOXING,ClassLevel.ADVANCED,90,8,0,new BigDecimal("120.00"),LocalDateTime.now().plusDays(7)));}};}}
