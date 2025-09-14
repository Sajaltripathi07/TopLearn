package com.coursecomparison.repository;

import com.coursecomparison.model.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CriteriaRepository extends JpaRepository<Criteria, Long> {
    
    List<Criteria> findByIsActiveTrue();
    
    List<Criteria> findByIsActiveTrueAndIsUserCustomizableTrue();
    
    Optional<Criteria> findByName(String name);
    
    @Query("SELECT c FROM Criteria c WHERE c.isActive = true ORDER BY c.defaultWeight DESC")
    List<Criteria> findActiveCriteriaOrderedByWeight();
    
    @Query("SELECT c FROM Criteria c WHERE c.type = ?1 AND c.isActive = true")
    List<Criteria> findByTypeAndActive(Criteria.CriteriaType type);
    
    boolean existsByName(String name);
}
