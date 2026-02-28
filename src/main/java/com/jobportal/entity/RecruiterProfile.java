package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "recruiter_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterProfile {

    @Id
    @Column(name = "user_account_id")
    private Integer userAccountId;

    @Column(name = "city")
    private String city;

    @Column(name = "company")
    private String company;

    @Column(name = "country")
    private String country;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "state")
    private String state;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", insertable = false, updatable = false)
    private Users users;
}
