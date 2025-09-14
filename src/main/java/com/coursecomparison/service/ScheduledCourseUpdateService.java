package com.coursecomparison.service;

import com.coursecomparison.model.Course;
import com.coursecomparison.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for scheduled course updates and maintenance tasks
 */
@Service
public class ScheduledCourseUpdateService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledCourseUpdateService.class);
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private LiveCourseService liveCourseService;
    
    @Autowired
    private CourseService courseService;

    @Value("${live.fetch.enabled:true}")
    private boolean liveFetchEnabled;
    
    /**
     * Update course data every 6 hours
     */
    @Scheduled(fixedRate = 21600000) // 6 hours in milliseconds
    public void updateCourseData() {
        logger.info("Starting scheduled course data update...");
        if (!liveFetchEnabled) {
            logger.warn("Live fetch disabled; skipping scheduled course update.");
            return;
        }
        
        try {
            // Get courses that haven't been updated in the last 6 hours
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(6);
            List<Course> staleCourses = courseRepository.findByLastUpdatedBefore(cutoffTime);
            
            logger.info("Found {} courses that need updating", staleCourses.size());
            
            // Update courses in batches
            updateCoursesInBatches(staleCourses);
            
            logger.info("Completed scheduled course data update");
            
        } catch (Exception e) {
            logger.error("Error during scheduled course update", e);
        }
    }
    
    /**
     * Refresh popular courses every 2 hours
     */
    @Scheduled(fixedRate = 7200000) // 2 hours in milliseconds
    public void refreshPopularCourses() {
        logger.info("Starting refresh of popular courses...");
        if (!liveFetchEnabled) {
            logger.warn("Live fetch disabled; skipping popular courses refresh.");
            return;
        }
        
        try {
            // Get top 50 courses by student count
            List<Course> popularCourses = courseRepository.findTop50ByOrderByStudentCountDesc();
            
            // Update each course asynchronously
            for (Course course : popularCourses) {
                updateCourseAsync(course);
            }
            
            logger.info("Completed refresh of popular courses");
            
        } catch (Exception e) {
            logger.error("Error during popular courses refresh", e);
        }
    }
    
    /**
     * Clean up inactive courses daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupInactiveCourses() {
        logger.info("Starting cleanup of inactive courses...");
        
        try {
            // Find courses that haven't been updated in 30 days
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
            List<Course> inactiveCourses = courseRepository.findByLastUpdatedBeforeAndIsActiveTrue(cutoffTime);
            
            logger.info("Found {} inactive courses to deactivate", inactiveCourses.size());
            
            // Deactivate old courses
            for (Course course : inactiveCourses) {
                course.setIsActive(false);
                course.setLastUpdated(LocalDateTime.now());
            }
            
            courseRepository.saveAll(inactiveCourses);
            logger.info("Deactivated {} inactive courses", inactiveCourses.size());
            
        } catch (Exception e) {
            logger.error("Error during inactive courses cleanup", e);
        }
    }
    
    /**
     * Update course rankings every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void updateCourseRankings() {
        logger.info("Starting course rankings update...");
        
        try {
            // Get all active courses
            List<Course> activeCourses = courseRepository.findByIsActiveTrue();
            
            // Recalculate MCDM scores
            courseService.rankCourses(activeCourses, com.coursecomparison.service.MCDMService.Algorithm.TOPSIS, null);
            
            // Save updated courses
            courseRepository.saveAll(activeCourses);
            
            logger.info("Updated rankings for {} courses", activeCourses.size());
            
        } catch (Exception e) {
            logger.error("Error during course rankings update", e);
        }
    }
    
    /**
     * Update courses in batches to avoid overwhelming the system
     */
    private void updateCoursesInBatches(List<Course> courses) {
        int batchSize = 10;
        for (int i = 0; i < courses.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, courses.size());
            List<Course> batch = courses.subList(i, endIndex);
            
            // Process batch asynchronously
            updateBatchAsync(batch);
            
            // Small delay between batches
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Update a single course asynchronously
     */
    @Async("courseUpdateExecutor")
    public CompletableFuture<Void> updateCourseAsync(Course course) {
        try {
            logger.debug("Updating course: {}", course.getTitle());
            
            // Search for updated information
            List<Course> updatedCourses = liveCourseService.searchLiveCourses(course.getTitle());
            
            if (!updatedCourses.isEmpty()) {
                Course updatedCourse = updatedCourses.get(0);
                
                // Update course data
                course.setRating(updatedCourse.getRating());
                course.setStudentCount(updatedCourse.getStudentCount());
                course.setPrice(updatedCourse.getPrice());
                course.setLastUpdated(LocalDateTime.now());
                
                courseRepository.save(course);
                logger.debug("Updated course: {}", course.getTitle());
            }
            
        } catch (Exception e) {
            logger.error("Error updating course: {}", course.getTitle(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Update a batch of courses asynchronously
     */
    @Async("courseUpdateExecutor")
    public CompletableFuture<Void> updateBatchAsync(List<Course> courses) {
        for (Course course : courses) {
            try {
                updateCourseAsync(course).get();
            } catch (Exception e) {
                logger.error("Error in batch update for course: {}", course.getTitle(), e);
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Manual trigger for course updates
     */
    public void triggerManualUpdate() {
        logger.info("Manual course update triggered");
        updateCourseData();
    }
    
    /**
     * Update specific course by ID
     */
    public void updateSpecificCourse(Long courseId) {
        try {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course != null) {
                updateCourseAsync(course);
                logger.info("Triggered update for course ID: {}", courseId);
            } else {
                logger.warn("Course not found with ID: {}", courseId);
            }
        } catch (Exception e) {
            logger.error("Error triggering update for course ID: {}", courseId, e);
        }
    }
}
