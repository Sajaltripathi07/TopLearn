package com.coursecomparison.controller;

import com.coursecomparison.model.Course;
import com.coursecomparison.model.User;
import com.coursecomparison.service.CourseService;
import com.coursecomparison.service.MCDMService.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
@Tag(name = "Course Management", description = "APIs for course search, comparison, and ranking using MCDM algorithms")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/")
    @Operation(summary = "Get API Information", description = "Returns information about available endpoints and algorithms")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API information retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("name", "Course Comparison API");
        apiInfo.put("version", "2.0.0");
        apiInfo.put("description", "Advanced course comparison platform with MCDM algorithms");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("search", "/api/courses/search?keyword=your_search_term");
        endpoints.put("advanced_search", "/api/courses/advanced-search (POST with filters)");
        endpoints.put("platform", "/api/courses/platform/{platform_name}");
        endpoints.put("topic", "/api/courses/topic/{topic_name}");
        endpoints.put("topics", "/api/courses/topics");
        endpoints.put("platforms", "/api/courses/platforms");
        endpoints.put("difficulties", "/api/courses/difficulties");
        endpoints.put("languages", "/api/courses/languages");
        endpoints.put("rank", "/api/courses/rank (POST)");
        endpoints.put("recommendations", "/api/courses/recommendations?limit=10");
        endpoints.put("pagination", "/api/courses/page?page=0&size=20&sortBy=rating&sortDir=desc");
        
        apiInfo.put("endpoints", endpoints);
        apiInfo.put("algorithms", List.of("TOPSIS", "AHP", "PERSONALIZED"));
        
        return ResponseEntity.ok(apiInfo);
    }

    @GetMapping("/search")
    @Operation(summary = "Search Courses", description = "Search courses by keyword with optional MCDM algorithm selection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Courses found successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<List<Course>> searchCourses(
            @Parameter(description = "Search keyword", required = true) @RequestParam String keyword,
            @Parameter(description = "MCDM Algorithm (TOPSIS, AHP, PERSONALIZED)") @RequestParam(required = false) String algorithm) {
        
        Algorithm algo = Algorithm.TOPSIS;
        if (algorithm != null) {
            try {
                algo = Algorithm.valueOf(algorithm.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default TOPSIS
            }
        }
        
        List<Course> courses = courseService.searchCourses(keyword, null);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/advanced-search")
    public ResponseEntity<List<Course>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestBody(required = false) Map<String, Object> filters,
            @RequestParam(required = false) String algorithm) {
        
        Algorithm algo = Algorithm.TOPSIS;
        if (algorithm != null) {
            try {
                algo = Algorithm.valueOf(algorithm.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default TOPSIS
            }
        }
        
        List<Course> courses = courseService.advancedSearch(keyword, filters, null);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<Course>> getCoursesByPlatform(
            @PathVariable String platform,
            @RequestParam(required = false) String algorithm) {
        
        Algorithm algo = Algorithm.TOPSIS;
        if (algorithm != null) {
            try {
                algo = Algorithm.valueOf(algorithm.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default TOPSIS
            }
        }
        
        List<Course> courses = courseService.getCoursesByPlatform(platform, null);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/topic/{topic}")
    public ResponseEntity<List<Course>> getCoursesByTopic(
            @PathVariable String topic,
            @RequestParam(required = false) String algorithm) {
        
        Algorithm algo = Algorithm.TOPSIS;
        if (algorithm != null) {
            try {
                algo = Algorithm.valueOf(algorithm.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default TOPSIS
            }
        }
        
        List<Course> courses = courseService.searchCoursesByTopic(topic, null);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/topics")
    public ResponseEntity<List<String>> getAllTopics() {
        List<String> topics = courseService.getAllTopics();
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/platforms")
    public ResponseEntity<List<String>> getAllPlatforms() {
        // This would need to be implemented in CourseService
        List<String> platforms = List.of("Udemy", "Coursera", "edX", "LinkedIn Learning", "Pluralsight");
        return ResponseEntity.ok(platforms);
    }

    @GetMapping("/difficulties")
    public ResponseEntity<List<String>> getAllDifficultyLevels() {
        List<String> difficulties = List.of("Beginner", "Intermediate", "Advanced");
        return ResponseEntity.ok(difficulties);
    }

    @GetMapping("/languages")
    public ResponseEntity<List<String>> getAllLanguages() {
        List<String> languages = List.of("English", "Spanish", "French", "German", "Chinese", "Japanese");
        return ResponseEntity.ok(languages);
    }

    @PostMapping("/rank")
    public ResponseEntity<List<Course>> rankCourses(
            @RequestBody List<Course> courses,
            @RequestParam(required = false, defaultValue = "TOPSIS") String algorithm) {
        
        Algorithm algo = Algorithm.TOPSIS;
        try {
            algo = Algorithm.valueOf(algorithm.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Use default TOPSIS
        }
        
        List<Course> rankedCourses = courseService.rankCourses(courses, algo, null);
        return ResponseEntity.ok(rankedCourses);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Course>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        List<Course> recommendations = courseService.getCourseRecommendations(null, limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Course>> getCoursesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<Course> courses = courseService.getCoursesWithPagination(page, size, sortBy, sortDir);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Course>> filterCourses(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean hasCertificate) {
        
        Map<String, Object> filters = new HashMap<>();
        if (minPrice != null) filters.put("minPrice", minPrice);
        if (maxPrice != null) filters.put("maxPrice", maxPrice);
        if (minRating != null) filters.put("minRating", minRating);
        if (platform != null) filters.put("platform", platform);
        if (topic != null) filters.put("topic", topic);
        if (difficulty != null) filters.put("difficulty", difficulty);
        if (language != null) filters.put("language", language);
        if (hasCertificate != null) filters.put("hasCertificate", hasCertificate);
        
        List<Course> courses = courseService.advancedSearch(null, filters, null);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/compare")
    public ResponseEntity<Map<String, List<Course>>> compareAlgorithms(
            @RequestParam List<Long> courseIds) {
        
        List<Course> courses = courseService.getCoursesByIds(courseIds, null);
        if (courses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // This would need to be implemented in MCDMService
        Map<String, List<Course>> comparison = new HashMap<>();
        comparison.put("TOPSIS", courseService.rankCourses(courses, Algorithm.TOPSIS, null));
        comparison.put("AHP", courseService.rankCourses(courses, Algorithm.AHP, null));
        
        return ResponseEntity.ok(comparison);
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        courseService.clearCache();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cache cleared successfully");
        return ResponseEntity.ok(response);
    }
} 