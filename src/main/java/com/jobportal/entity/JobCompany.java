package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "job_company")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "logo")
    private String logo;

    @Column(name = "name")
    private String name;
}
