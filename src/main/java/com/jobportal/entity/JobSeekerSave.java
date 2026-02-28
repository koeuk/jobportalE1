package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_seeker_save")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerSave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

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
