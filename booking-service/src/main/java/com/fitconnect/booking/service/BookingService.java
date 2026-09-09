package com.fitconnect.booking.service;

import com.fitconnect.booking.api.*;
import com.fitconnect.booking.client.*;
import com.fitconnect.booking.domain.*;
import com.fitconnect.booking.repository.BookingRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.function.Supplier;

@Service
public class BookingService {
    private final BookingRepository repo;
    private final ClassClient classes;
    private final PaymentClient payments;
    private final NotificationClient notifications;
    private final Clock clock;

    public BookingService(BookingRepository repo, ClassClient classes, PaymentClient payments,
                          NotificationClient notifications, Clock clock) {
        this.repo = repo;
        this.classes = classes;
        this.payments = payments;
        this.notifications = notifications;
        this.clock = clock;
    }

    public List<BookingResponse> all() { return repo.findAll().stream().map(BookingResponse::from).toList(); }
    public BookingResponse get(Long id) { return BookingResponse.from(find(id)); }
    public List<BookingResponse> user(Long id) { return repo.findByUserId(id).stream().map(BookingResponse::from).toList(); }

    @Transactional
    public BookingResponse create(String key, BookingRequest request) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Idempotency-Key requis");
        var existing = repo.findByIdempotencyKey(key);
        if (existing.isPresent()) return BookingResponse.from(existing.get());
        var course = dependency("class-service", () -> classes.get(request.classId()));
        if (course == null || course.currentParticipants() + request.numberOfSpots() > course.maxParticipants())
            throw new IllegalStateException("Plus de places disponibles pour ce cours");
        dependency("class-service", () -> { classes.increment(request.classId(), request.numberOfSpots()); return null; });
        Booking booking = repo.save(new Booking(key, request.userId(), request.userEmail(), request.userName(), request.classId(),
                course.name(), course.dateTime(), course.instructor(), course.price(), request.numberOfSpots(), clock));
        notify(booking, "BOOKING_CONFIRMATION", "Réservation en attente", "Payez avant " + booking.getPaymentDeadline());
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse confirm(Long id, ConfirmRequest request) {
        Booking booking = find(id);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) throw new IllegalStateException("Réservation non payable");
        if (booking.paymentExpired(clock)) throw new IllegalStateException("Paiement expiré");
        var payment = dependency("payment-service", () -> payments.pay(new PaymentClient.PaymentRequest(booking.getId(), booking.getBookingReference(),
                booking.getUserId(), booking.getTotalAmount(), request.paymentMethod(), request.cardLastFour(), request.transactionId())));
        if (payment == null || !"SUCCESS".equals(payment.status())) return BookingResponse.from(booking);
        booking.confirm();
        repo.save(booking);
        notify(booking, "PAYMENT_CONFIRMATION", "Paiement confirmé", "Votre réservation est confirmée");
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse cancel(Long id) {
        Booking booking = find(id);
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED)
            throw new IllegalStateException("Annulation impossible");
        if (booking.cancellationExpired(clock)) throw new IllegalStateException("Annulation hors délai");
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            var payment = dependency("payment-service", () -> payments.getByBooking(booking.getId()));
            if (payment != null) dependency("payment-service", () -> { payments.refund(payment.id()); return null; });
        }
        dependency("class-service", () -> { classes.decrement(booking.getClassId(), booking.getNumberOfSpots()); return null; });
        booking.cancel();
        repo.save(booking);
        notify(booking, "BOOKING_CANCELLED", "Réservation annulée", "Votre réservation est annulée");
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse complete(Long id) { Booking booking = find(id); booking.complete(); return BookingResponse.from(repo.save(booking)); }

    public List<BookingResponse> expired() {
        return repo.findByStatusAndPaymentDeadlineBefore(BookingStatus.PENDING_PAYMENT, LocalDateTime.now(clock)).stream().map(BookingResponse::from).toList();
    }

    public void expire() {
        repo.findByStatusAndPaymentDeadlineBefore(BookingStatus.PENDING_PAYMENT, LocalDateTime.now(clock)).forEach(booking -> {
            dependency("class-service", () -> { classes.decrement(booking.getClassId(), booking.getNumberOfSpots()); return null; });
            booking.cancel();
            repo.save(booking);
            notify(booking, "BOOKING_CANCELLED", "Réservation expirée", "Le délai de paiement est dépassé");
        });
    }

    public void remind() {
        repo.findByClassDateBetweenAndReminderSentFalseAndStatusIn(LocalDateTime.now(clock), LocalDateTime.now(clock).plusHours(24),
                List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED)).forEach(booking -> {
            notify(booking, "BOOKING_REMINDER", "Rappel de cours", "Votre cours " + booking.getClassName() + " commence bientôt");
            booking.sendReminder();
            repo.save(booking);
        });
    }

    private void notify(Booking booking, String type, String subject, String content) {
        try { notifications.send(new NotificationClient.NotificationRequest(booking.getUserId(), booking.getUserEmail(), type, subject, content)); }
        catch (FeignException e) { throw new ServiceUnavailableException("notification-service indisponible", e); }
    }

    private <T> T dependency(String name, Supplier<T> call) {
        try { return call.get(); }
        catch (FeignException e) { throw new ServiceUnavailableException(name + " indisponible", e); }
    }

    private Booking find(Long id) { return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Réservation introuvable")); }
}
