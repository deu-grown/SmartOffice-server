package com.grown.smartoffice.domain.guest.entity;

import com.grown.smartoffice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "guests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Long guestId;

    @Column(name = "guest_name", nullable = false, length = 50)
    private String guestName;

    @Column(name = "company", length = 100)
    private String company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id")
    private User hostUser;

    @Column(name = "purpose", length = 200)
    private String purpose;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "guest_status", nullable = false, length = 15)
    private GuestStatus guestStatus;

    @Column(name = "scheduled_entry_at", nullable = false)
    private LocalDateTime scheduledEntryAt;

    @Column(name = "actual_entry_at")
    private LocalDateTime actualEntryAt;

    @Column(name = "actual_exit_at")
    private LocalDateTime actualExitAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Guest(String guestName, String company, User hostUser, String purpose,
                 String contactPhone, GuestStatus guestStatus, LocalDateTime scheduledEntryAt) {
        this.guestName = guestName;
        this.company = company;
        this.hostUser = hostUser;
        this.purpose = purpose;
        this.contactPhone = contactPhone;
        this.guestStatus = (guestStatus != null) ? guestStatus : GuestStatus.SCHEDULED;
        this.scheduledEntryAt = scheduledEntryAt;
    }

    public void update(String guestName, String company, User hostUser, String purpose,
                       String contactPhone, GuestStatus guestStatus, LocalDateTime scheduledEntryAt) {
        if (guestName != null) this.guestName = guestName;
        if (company != null) this.company = company;
        this.hostUser = hostUser;
        if (purpose != null) this.purpose = purpose;
        if (contactPhone != null) this.contactPhone = contactPhone;
        if (guestStatus != null) this.guestStatus = guestStatus;
        if (scheduledEntryAt != null) this.scheduledEntryAt = scheduledEntryAt;
    }

    /** 방문 시작 — SCHEDULED 상태에서만 허용. */
    public void checkIn(LocalDateTime now) {
        this.guestStatus = GuestStatus.VISITING;
        this.actualEntryAt = now;
    }

    /** 방문 종료 — VISITING 상태에서만 허용. */
    public void checkOut(LocalDateTime now) {
        this.guestStatus = GuestStatus.COMPLETED;
        this.actualExitAt = now;
    }
}
