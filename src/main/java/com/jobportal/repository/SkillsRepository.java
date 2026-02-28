package com.jobportal.repository;

import com.jobportal.entity.Skills;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SkillsRepository extends JpaRepository<Skills, Integer> {
    List<Skills> findByJobSeekerProfileUserAccountId(Integer userId);
}
