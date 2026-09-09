package com.fitconnect.classservice.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class FitnessClass {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private String name;
    @Column(length = 2000) private String description;
    private String instructor;
    private String gymLocation;
    @Enumerated(EnumType.STRING) private ClassCategory category;
    @Enumerated(EnumType.STRING) private ClassLevel level;
    private Integer durationMinutes;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private BigDecimal price;
    private LocalDateTime dateTime;
    @Enumerated(EnumType.STRING) private ClassStatus status;

    protected FitnessClass() {}
    public FitnessClass(String name, String description, String instructor, String gymLocation, ClassCategory category, ClassLevel level, Integer durationMinutes, Integer maxParticipants, Integer currentParticipants, BigDecimal price, LocalDateTime dateTime) {
        this.name=name; this.description=description; this.instructor=instructor; this.gymLocation=gymLocation; this.category=category; this.level=level; this.durationMinutes=durationMinutes; this.maxParticipants=maxParticipants; this.currentParticipants=currentParticipants; this.price=price; this.dateTime=dateTime; this.status=ClassStatus.SCHEDULED;
    }
    public void incrementParticipants(int spots) { if (currentParticipants + spots > maxParticipants) throw new NoSpotsAvailableException("Plus de places disponibles pour ce cours"); currentParticipants += spots; }
    public void decrementParticipants(int spots) { currentParticipants = Math.max(0, currentParticipants - spots); }
    public void cancel() { status = ClassStatus.CANCELLED; }
    public Long getId(){return id;} public Long getVersion(){return version;} public String getName(){return name;} public String getDescription(){return description;} public String getInstructor(){return instructor;} public String getGymLocation(){return gymLocation;} public ClassCategory getCategory(){return category;} public ClassLevel getLevel(){return level;} public Integer getDurationMinutes(){return durationMinutes;} public Integer getMaxParticipants(){return maxParticipants;} public Integer getCurrentParticipants(){return currentParticipants;} public BigDecimal getPrice(){return price;} public LocalDateTime getDateTime(){return dateTime;} public ClassStatus getStatus(){return status;}
    public void updateFrom(FitnessClass other) { name=other.name; description=other.description; instructor=other.instructor; gymLocation=other.gymLocation; category=other.category; level=other.level; durationMinutes=other.durationMinutes; maxParticipants=other.maxParticipants; price=other.price; dateTime=other.dateTime; }
}
