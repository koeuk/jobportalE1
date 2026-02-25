package com.jobportal.repository;

import com.jobportal.entity.JobPostActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobPostActivityRepository extends JpaRepository<JobPostActivity, Integer> {
    List<JobPostActivity> findByPostedByUserId(Integer userId);

    List<JobPostActivity> findByJobTitleContaining(String keyword);
}
