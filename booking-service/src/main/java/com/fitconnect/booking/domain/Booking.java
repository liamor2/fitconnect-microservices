package com.fitconnect.booking.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false) private String bookingReference;
    @Column(unique = true) private String idempotencyKey;
    private Long userId; private String userEmail; private String userName; private Long classId;
    private String className; private LocalDateTime classDate; private String instructor; private BigDecimal price;
    private Integer numberOfSpots; private BigDecimal totalAmount; private LocalDateTime bookingDate;
    @Enumerated(EnumType.STRING) private BookingStatus status;
    private LocalDateTime paymentDeadline; private LocalDateTime cancellationDeadline; private boolean reminderSent;

    protected Booking() {}

    public Booking(String key, Long userId, String email, String name, Long classId, String className,
                   LocalDateTime classDate, String instructor, BigDecimal price, int spots, Clock clock) {
        this.idempotencyKey = key; this.userId = userId; this.userEmail = email; this.userName = name; this.classId = classId;
        this.className = className; this.classDate = classDate; this.instructor = instructor; this.price = price;
        this.numberOfSpots = spots; this.totalAmount = price.multiply(BigDecimal.valueOf(spots));
        this.bookingDate = LocalDateTime.now(clock); this.paymentDeadline = bookingDate.plusHours(1);
        this.cancellationDeadline = classDate.minusHours(24); this.status = BookingStatus.PENDING_PAYMENT;
        this.bookingReference = "BK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
    }

    public Long getId() { return id; } public String getBookingReference() { return bookingReference; }
    public String getIdempotencyKey() { return idempotencyKey; } public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; } public String getUserName() { return userName; }
    public Long getClassId() { return classId; } public String getClassName() { return className; }
    public LocalDateTime getClassDate() { return classDate; } public String getInstructor() { return instructor; }
    public BigDecimal getPrice() { return price; } public Integer getNumberOfSpots() { return numberOfSpots; }
    public BigDecimal getTotalAmount() { return totalAmount; } public LocalDateTime getBookingDate() { return bookingDate; }
    public BookingStatus getStatus() { return status; } public LocalDateTime getPaymentDeadline() { return paymentDeadline; }
    public LocalDateTime getCancellationDeadline() { return cancellationDeadline; } public boolean isReminderSent() { return reminderSent; }
    public void confirm() { status = BookingStatus.CONFIRMED; } public void cancel() { status = BookingStatus.CANCELLED; }
    public void complete() { status = BookingStatus.COMPLETED; } public void sendReminder() { reminderSent = true; }
    public boolean paymentExpired(Clock c) { return paymentDeadline.isBefore(LocalDateTime.now(c)); }
    public boolean cancellationExpired(Clock c) { return cancellationDeadline.isBefore(LocalDateTime.now(c)); }
}
