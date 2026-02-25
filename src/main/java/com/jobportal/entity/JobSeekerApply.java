package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_seeker_apply")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "apply_date")
    private LocalDateTime applyDate;

    @Column(name = "cover_letter")
    private String coverLetter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private JobPostActivity job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private JobSeekerProfile user;
}
