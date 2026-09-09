package com.fitconnect.booking;

import com.fitconnect.booking.api.*;
import com.fitconnect.booking.client.*;
import com.fitconnect.booking.domain.*;
import com.fitconnect.booking.repository.BookingRepository;
import com.fitconnect.booking.service.BookingService;
import com.fitconnect.booking.service.ServiceUnavailableException;
import feign.FeignException;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 12, 0);
    @Mock BookingRepository repository;
    @Mock ClassClient classes;
    @Mock PaymentClient payments;
    @Mock NotificationClient notifications;
    private BookingService service;
    private final Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private final BookingRequest request = new BookingRequest(7L, "user@example.com", "Alex", 3L, 2);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new BookingService(repository, classes, payments, notifications, clock);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ClassClient.ClassSnapshot snapshot(int current, int max) {
        return new ClassClient.ClassSnapshot(3L, "Yoga", "Marie", new BigDecimal("25"), NOW.plusDays(3), current, max, "SCHEDULED");
    }

    private Booking bookingAt(LocalDateTime creation, LocalDateTime classDate) {
        return new Booking("key-" + creation.getHour(), 7L, "user@example.com", "Alex", 3L, "Yoga", classDate,
                "Marie", new BigDecimal("25"), 2, Clock.fixed(creation.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
    }

    @Test
    void createIsSuccessfulAndIdempotent() {
        Booking existing = bookingAt(NOW, NOW.plusDays(3));
        when(repository.findByIdempotencyKey("idempotent")).thenReturn(Optional.empty(), Optional.of(existing));
        when(classes.get(3L)).thenReturn(snapshot(2, 10));

        BookingResponse first = service.create("idempotent", request);
        BookingResponse second = service.create("idempotent", request);

        assertEquals(BookingStatus.PENDING_PAYMENT, first.status());
        assertEquals(existing.getBookingReference(), second.bookingReference());
        verify(classes).increment(3L, 2);
        verify(notifications).send(any());
    }

    @Test
    void createRejectsMissingKeyMissingClassAndInsufficientCapacity() {
        assertThrows(IllegalArgumentException.class, () -> service.create(null, request));
        assertThrows(IllegalArgumentException.class, () -> service.create(" ", request));
        when(repository.findByIdempotencyKey("missing")).thenReturn(Optional.empty());
        when(classes.get(3L)).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> service.create("missing", request));
        when(classes.get(3L)).thenReturn(snapshot(9, 10));
        assertThrows(IllegalStateException.class, () -> service.create("full", request));
        verify(repository, never()).save(any());
    }

    @Test
    void confirmHandlesSuccessFailureAndExpiredPayment() {
        Booking success = bookingAt(NOW, NOW.plusDays(3));
        when(repository.findById(1L)).thenReturn(Optional.of(success));
        when(payments.pay(any())).thenReturn(new PaymentClient.PaymentSnapshot(1L, "PAY-1", 1L, new BigDecimal("50"), "PAYPAL", "SUCCESS", NOW));
        BookingResponse confirmed = service.confirm(1L, new ConfirmRequest("PAYPAL", null, "txn-1"));
        assertEquals(BookingStatus.CONFIRMED, confirmed.status());

        Booking failed = bookingAt(NOW, NOW.plusDays(3));
        when(repository.findById(2L)).thenReturn(Optional.of(failed));
        when(payments.pay(any())).thenReturn(new PaymentClient.PaymentSnapshot(2L, "PAY-2", 2L, new BigDecimal("100"), "PAYPAL", "FAILED", NOW));
        assertEquals(BookingStatus.PENDING_PAYMENT, service.confirm(2L, new ConfirmRequest("PAYPAL", null, "txn-2")).status());

        Booking expired = bookingAt(NOW.minusHours(2), NOW.plusDays(3));
        when(repository.findById(3L)).thenReturn(Optional.of(expired));
        assertThrows(IllegalStateException.class, () -> service.confirm(3L, new ConfirmRequest("PAYPAL", null, "txn-3")));

        Booking nonPayable = bookingAt(NOW, NOW.plusDays(3));
        nonPayable.confirm();
        when(repository.findById(4L)).thenReturn(Optional.of(nonPayable));
        assertThrows(IllegalStateException.class, () -> service.confirm(4L, new ConfirmRequest("PAYPAL", null, "txn-4")));

        Booking noPayment = bookingAt(NOW, NOW.plusDays(3));
        when(repository.findById(5L)).thenReturn(Optional.of(noPayment));
        when(payments.pay(any())).thenReturn(null);
        assertEquals(BookingStatus.PENDING_PAYMENT, service.confirm(5L, new ConfirmRequest("PAYPAL", null, "txn-5")).status());
    }

    @Test
    void cancelRefundsConfirmedAndCancelsPending() {
        Booking confirmed = bookingAt(NOW, NOW.plusDays(3));
        confirmed.confirm();
        when(repository.findById(1L)).thenReturn(Optional.of(confirmed));
        when(payments.getByBooking(isNull())).thenReturn(new PaymentClient.PaymentSnapshot(8L, "PAY-8", 1L, new BigDecimal("50"), "PAYPAL", "SUCCESS", NOW));
        assertEquals(BookingStatus.CANCELLED, service.cancel(1L).status());
        verify(payments).refund(8L);
        verify(classes).decrement(3L, 2);

        Booking pending = bookingAt(NOW, NOW.plusDays(3));
        when(repository.findById(2L)).thenReturn(Optional.of(pending));
        assertEquals(BookingStatus.CANCELLED, service.cancel(2L).status());
        verify(payments, times(1)).getByBooking(isNull());
    }

    @Test
    void cancelRejectsExpiredOrTerminalBookingsAndCompleteWorks() {
        Booking late = bookingAt(NOW, NOW.plusHours(12));
        when(repository.findById(1L)).thenReturn(Optional.of(late));
        assertThrows(IllegalStateException.class, () -> service.cancel(1L));

        Booking cancelled = bookingAt(NOW, NOW.plusDays(3));
        cancelled.cancel();
        when(repository.findById(2L)).thenReturn(Optional.of(cancelled));
        assertThrows(IllegalStateException.class, () -> service.cancel(2L));

        Booking alreadyCompleted = bookingAt(NOW, NOW.plusDays(3));
        alreadyCompleted.complete();
        when(repository.findById(4L)).thenReturn(Optional.of(alreadyCompleted));
        assertThrows(IllegalStateException.class, () -> service.cancel(4L));

        Booking completed = bookingAt(NOW, NOW.plusDays(3));
        when(repository.findById(3L)).thenReturn(Optional.of(completed));
        assertEquals(BookingStatus.COMPLETED, service.complete(3L).status());
    }

    @Test
    void listGetExpiredAndSchedulerWorkflowAreCovered() {
        Booking one = bookingAt(NOW, NOW.plusDays(3));
        when(repository.findAll()).thenReturn(List.of(one));
        when(repository.findByUserId(7L)).thenReturn(List.of(one));
        when(repository.findById(9L)).thenReturn(Optional.of(one));
        when(repository.findByStatusAndPaymentDeadlineBefore(eq(BookingStatus.PENDING_PAYMENT), any())).thenReturn(List.of(one));
        assertEquals(1, service.all().size());
        assertEquals(1, service.user(7L).size());
        assertNotNull(service.get(9L));
        assertEquals(1, service.expired().size());
        service.expire();
        assertEquals(BookingStatus.CANCELLED, one.getStatus());
        verify(classes).decrement(3L, 2);

        Booking reminder = bookingAt(NOW, NOW.plusHours(12));
        when(repository.findByClassDateBetweenAndReminderSentFalseAndStatusIn(any(), any(), any())).thenReturn(List.of(reminder));
        service.remind();
        assertTrue(reminder.isReminderSent());
    }

    @Test
    void missingBookingIsReported() {
        when(repository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.get(404L));
    }

    @Test
    void dependencyFailuresBecomeServiceUnavailable() {
        when(repository.findByIdempotencyKey("down")).thenReturn(Optional.empty());
        when(classes.get(3L)).thenThrow(mock(FeignException.class));
        assertThrows(ServiceUnavailableException.class, () -> service.create("down", request));

        when(repository.findById(10L)).thenReturn(Optional.of(bookingAt(NOW, NOW.plusDays(3))));
        when(payments.pay(any())).thenThrow(mock(FeignException.class));
        assertThrows(ServiceUnavailableException.class, () -> service.confirm(10L, new ConfirmRequest("PAYPAL", null, "txn")));

        when(repository.findByIdempotencyKey("notify-down")).thenReturn(Optional.empty());
        when(classes.get(3L)).thenReturn(snapshot(2, 10));
        doThrow(mock(FeignException.class)).when(notifications).send(any());
        assertThrows(ServiceUnavailableException.class, () -> service.create("notify-down", request));
    }
}
