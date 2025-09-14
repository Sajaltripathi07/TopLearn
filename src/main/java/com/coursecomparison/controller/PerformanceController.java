package com.coursecomparison.controller;

import com.coursecomparison.service.ScheduledCourseUpdateService;
import com.coursecomparison.service.LiveCourseService;
import com.coursecomparison.model.Course;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Controller for performance monitoring and system management
 */
@RestController
@RequestMapping("/api/performance")
@CrossOrigin(origins = "*")
@Tag(name = "Performance & System Management", description = "APIs for monitoring system performance and managing background tasks")
public class PerformanceController {

    @Autowired
    private ScheduledCourseUpdateService scheduledCourseUpdateService;
    
    @Autowired
    private LiveCourseService liveCourseService;

    @GetMapping("/health")
    @Operation(summary = "System Health Check", description = "Returns system health status and performance metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Health check completed successfully")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "2.0.0");
        health.put("uptime", System.currentTimeMillis() - Long.parseLong(System.getProperty("java.startTime", "0")));
        
        // Add memory information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        health.put("memory", memory);
        
        return ResponseEntity.ok(health);
    }

    @PostMapping("/update/trigger")
    @Operation(summary = "Trigger Manual Course Update", description = "Manually trigger course data update process")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course update triggered successfully"),
        @ApiResponse(responseCode = "500", description = "Error triggering course update")
    })
    public ResponseEntity<Map<String, String>> triggerCourseUpdate() {
        try {
            scheduledCourseUpdateService.triggerManualUpdate();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course update triggered successfully");
            response.put("timestamp", String.valueOf(System.currentTimeMillis()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to trigger course update: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/update/course/{courseId}")
    @Operation(summary = "Update Specific Course", description = "Trigger update for a specific course by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course update triggered successfully"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "500", description = "Error triggering course update")
    })
    public ResponseEntity<Map<String, String>> updateSpecificCourse(
            @PathVariable Long courseId) {
        try {
            scheduledCourseUpdateService.updateSpecificCourse(courseId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course update triggered for ID: " + courseId);
            response.put("timestamp", String.valueOf(System.currentTimeMillis()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to trigger course update: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get Performance Metrics", description = "Returns system performance metrics and statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // JVM Metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvmMetrics = new HashMap<>();
        jvmMetrics.put("totalMemory", runtime.totalMemory());
        jvmMetrics.put("freeMemory", runtime.freeMemory());
        jvmMetrics.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        jvmMetrics.put("maxMemory", runtime.maxMemory());
        jvmMetrics.put("availableProcessors", runtime.availableProcessors());
        metrics.put("jvm", jvmMetrics);
        
        // System Metrics
        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("currentTimeMillis", System.currentTimeMillis());
        systemMetrics.put("nanoTime", System.nanoTime());
        metrics.put("system", systemMetrics);
        
        return ResponseEntity.ok(metrics);
    }
    
    @PostMapping("/live-search/{keyword}")
    @Operation(summary = "Test Live Course Search", description = "Test live course search with enhanced methods")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Live search completed successfully"),
        @ApiResponse(responseCode = "500", description = "Error in live search")
    })
    public ResponseEntity<Map<String, Object>> testLiveSearch(@PathVariable String keyword) {
        try {
            List<Course> courses = liveCourseService.searchLiveCourses(keyword);
            Map<String, Object> response = new HashMap<>();
            response.put("keyword", keyword);
            response.put("coursesFound", courses.size());
            response.put("courses", courses);
            response.put("message", "Live search completed successfully");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to perform live search: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
