package com.jobportal.repository;

import com.jobportal.entity.JobSeekerApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobSeekerApplyRepository extends JpaRepository<JobSeekerApply, Integer> {
    List<JobSeekerApply> findByUserUserAccountId(Integer userId);

    List<JobSeekerApply> findByJobJobPostId(Integer jobId);

    Optional<JobSeekerApply> findByJobJobPostIdAndUserUserAccountId(Integer jobId, Integer userId);

    boolean existsByJobJobPostIdAndUserUserAccountId(Integer jobId, Integer userId);
}
