package com.jobportal.repository;

import com.jobportal.entity.JobSeekerSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobSeekerSaveRepository extends JpaRepository<JobSeekerSave, Integer> {
    List<JobSeekerSave> findByUserUserAccountId(Integer userId);

    Optional<JobSeekerSave> findByJobJobPostIdAndUserUserAccountId(Integer jobId, Integer userId);

    boolean existsByJobJobPostIdAndUserUserAccountId(Integer jobId, Integer userId);
}
