package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job_post_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_post_id")
    private Integer jobPostId;

    @Column(name = "description_of_job", length = 10000)
    private String descriptionOfJob;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "job_type")
    private String jobType;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    @Column(name = "remote")
    private String remote;

    @Column(name = "salary")
    private String salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_company_id")
    private JobCompany jobCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_location_id")
    private JobLocation jobLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_id")
    private Users postedBy;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<JobSeekerApply> applicants;
}
