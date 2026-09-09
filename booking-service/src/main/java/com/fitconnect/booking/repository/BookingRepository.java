package com.fitconnect.booking.repository;

import com.fitconnect.booking.domain.*;
import org.springframework.data.jpa.repository.*;
import java.time.*;
import java.util.*;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByIdempotencyKey(String key);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByStatusAndPaymentDeadlineBefore(BookingStatus status, LocalDateTime deadline);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByClassDateBetweenAndReminderSentFalseAndStatusIn(LocalDateTime from, LocalDateTime to, Collection<BookingStatus> statuses);
}
