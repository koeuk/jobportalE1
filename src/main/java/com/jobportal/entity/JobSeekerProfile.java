package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "job_seeker_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerProfile {

    @Id
    @Column(name = "user_account_id")
    private Integer userAccountId;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "resume")
    private String resume;

    @Column(name = "state")
    private String state;

    @Column(name = "work_authorization")
    private String workAuthorization;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", insertable = false, updatable = false)
    private Users users;
}
