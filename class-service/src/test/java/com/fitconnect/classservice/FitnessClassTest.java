package com.fitconnect.classservice;

import com.fitconnect.classservice.domain.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FitnessClassTest {
    private FitnessClass course(int current, int max) { return new FitnessClass("Yoga", "Desc", "Marie", "Paris", ClassCategory.YOGA, ClassLevel.BEGINNER, 60, max, current, new BigDecimal("25.00"), LocalDateTime.now().plusDays(1)); }
    @Test void incrementAndDecrementParticipants() { FitnessClass c=course(2,10); c.incrementParticipants(3); assertEquals(5,c.getCurrentParticipants()); c.decrementParticipants(2); assertEquals(3,c.getCurrentParticipants()); c.decrementParticipants(20); assertEquals(0,c.getCurrentParticipants()); }
    @Test void incrementRejectsOverflow() { FitnessClass c=course(9,10); assertThrows(NoSpotsAvailableException.class,()->c.incrementParticipants(2)); }
    @Test void cancelAndUpdateExposeState() { FitnessClass c=course(0,10); c.cancel(); assertEquals(ClassStatus.CANCELLED,c.getStatus()); FitnessClass other=course(1,12); c.updateFrom(other); assertEquals("Yoga",c.getName()); assertEquals(12,c.getMaxParticipants()); assertNotNull(c.getDateTime()); assertNull(c.getId()); }
    @Test void persistenceConstructorIsAvailable() throws Exception { var constructor=FitnessClass.class.getDeclaredConstructor(); constructor.setAccessible(true); assertNotNull(constructor.newInstance()); }
}
