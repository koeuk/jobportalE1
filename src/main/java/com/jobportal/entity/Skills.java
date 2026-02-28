package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skills {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "experience_level")
    private String experienceLevel;

    @Column(name = "name")
    private String name;

    @Column(name = "years_of_experience")
    private String yearsOfExperience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_profile")
    private JobSeekerProfile jobSeekerProfile;
}
