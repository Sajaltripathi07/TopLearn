package com.coursecomparison.service;

import com.coursecomparison.model.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for validating course URLs and ensuring they are accessible
 */
@Service
public class CourseValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseValidationService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Validate a single course URL
     */
    public boolean validateCourseUrl(Course course) {
        if (course == null || course.getUrl() == null || course.getUrl().trim().isEmpty()) {
            return false;
        }
        
        try {
            logger.debug("Validating URL for course: {}", course.getTitle());
            
            // Make a HEAD request to check if the URL is accessible
            ResponseEntity<String> response = restTemplate.getForEntity(course.getUrl(), String.class);
            
            boolean isValid = response.getStatusCode().is2xxSuccessful();
            logger.debug("URL validation for {}: {}", course.getTitle(), isValid ? "VALID" : "INVALID");
            
            return isValid;
            
        } catch (Exception e) {
            logger.warn("URL validation failed for course {}: {}", course.getTitle(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate multiple course URLs asynchronously
     */
    public CompletableFuture<List<Course>> validateCourseUrlsAsync(List<Course> courses) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Starting validation for {} courses", courses.size());
            
            List<Course> validCourses = courses.parallelStream()
                .filter(this::validateCourseUrl)
                .collect(Collectors.toList());
            
            logger.info("Validation complete: {} valid courses out of {}", validCourses.size(), courses.size());
            return validCourses;
        });
    }
    
    /**
     * Validate and filter courses by platform
     */
    public List<Course> validateCoursesByPlatform(List<Course> courses, String platform) {
        return courses.stream()
            .filter(course -> platform.equalsIgnoreCase(course.getPlatform()))
            .filter(this::validateCourseUrl)
            .collect(Collectors.toList());
    }
    
    /**
     * Get course availability status
     */
    public CourseAvailabilityStatus getCourseAvailability(Course course) {
        if (course == null || course.getUrl() == null) {
            return CourseAvailabilityStatus.NOT_AVAILABLE;
        }
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(course.getUrl(), String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return CourseAvailabilityStatus.AVAILABLE;
            } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                return CourseAvailabilityStatus.NOT_FOUND;
            } else if (response.getStatusCode().is4xxClientError()) {
                return CourseAvailabilityStatus.ACCESS_DENIED;
            } else if (response.getStatusCode().is5xxServerError()) {
                return CourseAvailabilityStatus.SERVER_ERROR;
            } else {
                return CourseAvailabilityStatus.UNKNOWN;
            }
            
        } catch (Exception e) {
            logger.warn("Error checking course availability for {}: {}", course.getTitle(), e.getMessage());
            return CourseAvailabilityStatus.ERROR;
        }
    }
    
    /**
     * Check if course is from a trusted platform
     */
    public boolean isFromTrustedPlatform(Course course) {
        if (course == null || course.getPlatform() == null) {
            return false;
        }
        
        String platform = course.getPlatform().toLowerCase();
        return platform.equals("udemy") || 
               platform.equals("coursera") || 
               platform.equals("edx") || 
               platform.equals("udacity") || 
               platform.equals("futurelearn");
    }
    
    /**
     * Validate course data integrity
     */
    public boolean validateCourseData(Course course) {
        if (course == null) {
            return false;
        }
        
        // Check required fields
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            return false;
        }
        
        if (course.getPlatform() == null || course.getPlatform().trim().isEmpty()) {
            return false;
        }
        
        if (course.getUrl() == null || course.getUrl().trim().isEmpty()) {
            return false;
        }
        
        // Check URL format
        if (!course.getUrl().startsWith("http://") && !course.getUrl().startsWith("https://")) {
            return false;
        }
        
        // Check rating range
        if (course.getRating() != null && (course.getRating() < 0 || course.getRating() > 5)) {
            return false;
        }
        
        // Check price range
        if (course.getPrice() != null && course.getPrice() < 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Course availability status enum
     */
    public enum CourseAvailabilityStatus {
        AVAILABLE,
        NOT_AVAILABLE,
        NOT_FOUND,
        ACCESS_DENIED,
        SERVER_ERROR,
        ERROR,
        UNKNOWN
    }
}
