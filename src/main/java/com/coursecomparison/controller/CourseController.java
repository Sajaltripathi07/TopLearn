package com.coursecomparison.controller;

import com.coursecomparison.model.Course;
import com.coursecomparison.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> getApiInfo() {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("search", "/api/courses/search?keyword=your_search_term");
        endpoints.put("platform", "/api/courses/platform/{platform_name}");
        endpoints.put("topics", "/api/courses/topics");
        endpoints.put("rank", "/api/courses/rank (POST)");
        return ResponseEntity.ok(endpoints);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(@RequestParam String keyword) {
        List<Course> courses = courseService.searchCourses(keyword);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<Course>> getCoursesByPlatform(@PathVariable String platform) {
        List<Course> courses = courseService.getCoursesByPlatform(platform);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/topics")
    public ResponseEntity<List<String>> getAllTopics() {
        List<String> topics = courseService.getAllTopics();
        return ResponseEntity.ok(topics);
    }

    @PostMapping("/rank")
    public ResponseEntity<List<Course>> rankCourses(@RequestBody List<Course> courses) {
        List<Course> rankedCourses = courseService.rankCourses(courses);
        return ResponseEntity.ok(rankedCourses);
    }
} 