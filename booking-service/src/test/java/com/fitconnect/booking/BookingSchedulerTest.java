package com.fitconnect.booking;

import com.fitconnect.booking.service.*;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class BookingSchedulerTest {
    @Test
    void scheduledExpirationDelegatesToService() {
        BookingService service = mock(BookingService.class);
        new BookingScheduler(service).expire();
        verify(service).expire();
        new BookingScheduler(service).remind();
        verify(service).remind();
    }
}
