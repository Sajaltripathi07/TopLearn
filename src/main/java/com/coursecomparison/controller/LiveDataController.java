package com.coursecomparison.controller;

import com.coursecomparison.model.Course;
import com.coursecomparison.service.LiveCourseService;
import com.coursecomparison.service.CourseService;
import com.coursecomparison.repository.CourseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for live data operations
 */
@RestController
@RequestMapping("/api/live")
@CrossOrigin(origins = "*")
@Tag(name = "Live Data Management", description = "APIs for fetching and managing live course data")
public class LiveDataController {

    @Autowired
    private LiveCourseService liveCourseService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private CourseRepository courseRepository;

    @PostMapping("/search/{keyword}")
    @Operation(summary = "Search Live Courses", description = "Search for live courses and optionally save them to database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Live search completed successfully"),
        @ApiResponse(responseCode = "500", description = "Error in live search")
    })
    public ResponseEntity<Map<String, Object>> searchLiveCourses(
            @PathVariable String keyword,
            @RequestParam(defaultValue = "false") boolean saveToDatabase) {
        
        try {
            List<Course> courses = liveCourseService.searchLiveCourses(keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("keyword", keyword);
            response.put("coursesFound", courses.size());
            response.put("courses", courses);
            response.put("timestamp", System.currentTimeMillis());
            
            if (saveToDatabase && !courses.isEmpty()) {
                int savedCount = 0;
                for (Course course : courses) {
                    try {
                        courseRepository.save(course);
                        savedCount++;
                    } catch (Exception e) {
                        // Log error but continue with other courses
                        System.err.println("Error saving course: " + e.getMessage());
                    }
                }
                response.put("savedToDatabase", savedCount);
                response.put("message", "Live search completed and " + savedCount + " courses saved to database");
            } else {
                response.put("message", "Live search completed successfully");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to perform live search: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/refresh-all")
    @Operation(summary = "Refresh All Course Data", description = "Fetch fresh live data for all existing courses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course refresh completed successfully"),
        @ApiResponse(responseCode = "500", description = "Error refreshing courses")
    })
    public ResponseEntity<Map<String, Object>> refreshAllCourses() {
        try {
            // Get all existing courses
            List<Course> existingCourses = courseService.getAllCourses();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalCourses", existingCourses.size());
            response.put("refreshedCourses", 0);
            response.put("timestamp", System.currentTimeMillis());
            
            int refreshedCount = 0;
            for (Course course : existingCourses) {
                try {
                    List<Course> liveCourses = liveCourseService.searchLiveCourses(course.getTitle());
                    if (!liveCourses.isEmpty()) {
                        Course updatedCourse = liveCourses.get(0);
                        updatedCourse.setId(course.getId()); // Preserve ID
                        courseRepository.save(updatedCourse);
                        refreshedCount++;
                    }
                } catch (Exception e) {
                    // Log error but continue with other courses
                    System.err.println("Error refreshing course " + course.getTitle() + ": " + e.getMessage());
                }
            }
            
            response.put("refreshedCourses", refreshedCount);
            response.put("message", "Refreshed " + refreshedCount + " out of " + existingCourses.size() + " courses");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to refresh courses: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/status")
    @Operation(summary = "Get Live Data Status", description = "Get the current status of live data fetching")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getLiveDataStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("liveFetchEnabled", true); // This should be read from configuration
        status.put("timestamp", System.currentTimeMillis());
        status.put("message", "Live data fetching is active");
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/test-filter")
    @Operation(summary = "Test Filter Endpoint", description = "Test the filter functionality with sample parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filter test completed successfully")
    })
    public ResponseEntity<Map<String, Object>> testFilter() {
        try {
            // Test with sample filter parameters
            List<Course> courses = courseService.advancedSearch(null, Map.of(
                "platform", "Coursera",
                "minRating", 4.0
            ), null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Filter test successful");
            response.put("coursesFound", courses.size());
            response.put("courses", courses);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Filter test failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
