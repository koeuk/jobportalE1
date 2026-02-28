package com.jobportal.repository;

import com.jobportal.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserTypeRepository extends JpaRepository<UserType, Integer> {
    Optional<UserType> findByUserTypeName(String userTypeName);
}
