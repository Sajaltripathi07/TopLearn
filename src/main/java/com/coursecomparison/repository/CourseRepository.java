package com.coursecomparison.repository;

import com.coursecomparison.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByPlatformIgnoreCase(String platform);
    
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.topic) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.instructor) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Course> searchCourses(@Param("keyword") String keyword, Pageable pageable);
    
    Page<Course> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
    Page<Course> findByRatingGreaterThanEqual(Double rating, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE LOWER(c.topic) LIKE LOWER(CONCAT('%', :topic, '%')) " +
           "ORDER BY c.rating DESC, c.studentCount DESC")
    Page<Course> findByTopicOrderByRatingAndStudents(@Param("topic") String topic, Pageable pageable);
    
    @Query("SELECT DISTINCT c.topic FROM Course c ORDER BY c.topic")
    List<String> findAllTopics();

    @Query("SELECT c FROM Course c WHERE " +
           "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR c.rating >= :minRating) AND " +
           "(:platform IS NULL OR LOWER(c.platform) = LOWER(:platform)) AND " +
           "(:topic IS NULL OR LOWER(c.topic) LIKE LOWER(CONCAT('%', :topic, '%')))")
    Page<Course> findCoursesByFilters(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minRating") Double minRating,
            @Param("platform") String platform,
            @Param("topic") String topic,
            Pageable pageable);

    @Query("SELECT c FROM Course c WHERE " +
           "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR c.rating >= :minRating) AND " +
           "(:platform IS NULL OR LOWER(c.platform) = LOWER(:platform)) AND " +
           "(:topic IS NULL OR LOWER(c.topic) LIKE LOWER(CONCAT('%', :topic, '%'))) AND " +
           "(:difficulty IS NULL OR c.difficultyLevel = :difficulty) AND " +
           "(:language IS NULL OR c.language = :language) AND " +
           "(:hasCertificate IS NULL OR c.hasCertificate = :hasCertificate)")
    List<Course> findCoursesByAdvancedFilters(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minRating") Double minRating,
            @Param("platform") String platform,
            @Param("topic") String topic,
            @Param("difficulty") String difficulty,
            @Param("language") String language,
            @Param("hasCertificate") Boolean hasCertificate);

    Optional<Course> findByTitleAndPlatform(String title, String platform);
    
    // Additional query methods for enhanced functionality
    List<Course> findByDifficultyLevel(String difficultyLevel);
    List<Course> findByLanguage(String language);
    List<Course> findByHasCertificate(Boolean hasCertificate);
    
    @Query("SELECT DISTINCT c.platform FROM Course c ORDER BY c.platform")
    List<String> findAllPlatforms();
    
    @Query("SELECT DISTINCT c.difficultyLevel FROM Course c WHERE c.difficultyLevel IS NOT NULL ORDER BY c.difficultyLevel")
    List<String> findAllDifficultyLevels();
    
    @Query("SELECT DISTINCT c.language FROM Course c WHERE c.language IS NOT NULL ORDER BY c.language")
    List<String> findAllLanguages();
    
    // Additional methods for scheduled updates
    List<Course> findByLastUpdatedBefore(java.time.LocalDateTime dateTime);
    List<Course> findByLastUpdatedBeforeAndIsActiveTrue(java.time.LocalDateTime dateTime);
    List<Course> findTop50ByOrderByStudentCountDesc();
    List<Course> findByIsActiveTrue();
} 