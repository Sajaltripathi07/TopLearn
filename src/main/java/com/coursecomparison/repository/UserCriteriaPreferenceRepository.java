package com.coursecomparison.repository;

import com.coursecomparison.model.UserCriteriaPreference;
import com.coursecomparison.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCriteriaPreferenceRepository extends JpaRepository<UserCriteriaPreference, Long> {
    
    List<UserCriteriaPreference> findByUser(User user);
    
    List<UserCriteriaPreference> findByUserAndCriteriaIsActiveTrue(User user);
    
    Optional<UserCriteriaPreference> findByUserAndCriteriaId(User user, Long criteriaId);
    
    @Query("SELECT ucp FROM UserCriteriaPreference ucp WHERE ucp.user = :user AND ucp.isEnabled = true")
    List<UserCriteriaPreference> findEnabledPreferencesByUser(@Param("user") User user);
    
    @Query("SELECT ucp FROM UserCriteriaPreference ucp WHERE ucp.user = :user AND ucp.isEnabled = true ORDER BY ucp.weight DESC")
    List<UserCriteriaPreference> findEnabledPreferencesByUserOrderedByWeight(@Param("user") User user);
    
    void deleteByUser(User user);
    
    boolean existsByUserAndCriteriaId(User user, Long criteriaId);
}
