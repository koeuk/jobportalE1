package com.jobportal.repository;

import com.jobportal.entity.JobCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCompanyRepository extends JpaRepository<JobCompany, Integer> {
}
