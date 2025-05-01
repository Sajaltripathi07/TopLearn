package com.coursecomparison.service;

import com.coursecomparison.model.Course;
import com.coursecomparison.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class CourseService {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private static final int TOP_COURSES_LIMIT = 6;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private LiveCourseService liveCourseService;
    
    @Autowired
    private MCDMService mcdmService;
    
    public List<Course> getAllCourses() {
        try {
            logger.info("Fetching all courses");
            
            // Get courses from database
            List<Course> databaseCourses = courseRepository.findAll();
            logger.info("Found {} courses in database", databaseCourses.size());
            
            // Apply MCDM ranking
            List<Course> rankedCourses = mcdmService.rankCourses(databaseCourses);
            logger.info("Applied MCDM ranking to courses");
            
            return rankedCourses;
            
        } catch (Exception e) {
            logger.error("Error occurred while fetching all courses", e);
            return Collections.emptyList();
        }
    }
    
    public List<Course> searchCourses(String keyword) {
        try {
            logger.info("Searching courses with keyword: {}", keyword);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllCourses();
            }
            
            // First search in database
            List<Course> databaseCourses = courseRepository.searchCourses(keyword, PageRequest.of(0, 100)).getContent();
            logger.info("Found {} courses in database matching keyword", databaseCourses.size());
            
            // Then search live courses and save them to database
            List<Course> liveCourses = liveCourseService.searchLiveCourses(keyword);
            logger.info("Found {} live courses matching keyword", liveCourses.size());
            
            // Combine results
            List<Course> allCourses = new ArrayList<>(databaseCourses);
            allCourses.addAll(liveCourses);
            
            // Remove duplicates based on title and platform
            Set<String> seen = new HashSet<>();
            allCourses.removeIf(course -> !seen.add(course.getTitle() + "|" + course.getPlatform()));
            
            // Apply MCDM ranking
            List<Course> rankedCourses = mcdmService.rankCourses(allCourses);
            logger.info("Applied MCDM ranking to {} courses", rankedCourses.size());
            
            // Return top 6 courses
            List<Course> topCourses = rankedCourses.stream()
                .limit(TOP_COURSES_LIMIT)
                .collect(Collectors.toList());
            
            logger.info("Returning top {} courses for keyword '{}'", topCourses.size(), keyword);
            return topCourses;
            
        } catch (Exception e) {
            logger.error("Error occurred while searching courses with keyword: {}", keyword, e);
            return Collections.emptyList();
        }
    }
    
    public List<Course> getCoursesByProvider(String provider) {
        try {
            if (provider == null || provider.trim().isEmpty()) {
                logger.warn("Provider parameter is null or empty");
                return Collections.emptyList();
            }
            List<Course> courses = courseRepository.findByPlatformIgnoreCase(provider);
            return mcdmService.rankCourses(courses);
        } catch (Exception e) {
            logger.error("Error occurred while fetching courses for provider: {}", provider, e);
            return Collections.emptyList();
        }
    }

    public List<Course> getCoursesByIds(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                logger.warn("Course IDs list is null or empty");
                return Collections.emptyList();
            }
            List<Course> courses = courseRepository.findAllById(ids);
            return mcdmService.rankCourses(courses);
        } catch (Exception e) {
            logger.error("Error occurred while fetching courses by IDs: {}", ids, e);
            return Collections.emptyList();
        }
    }
    
    public List<Course> rankCourses(List<Course> courses) {
        try {
            if (courses == null || courses.isEmpty()) {
                logger.warn("Courses list is null or empty");
                return Collections.emptyList();
            }
            return mcdmService.rankCourses(courses);
        } catch (Exception e) {
            logger.error("Error occurred while ranking courses", e);
            return Collections.emptyList();
        }
    }
    
    public List<Course> searchCoursesByTopic(String topic) {
        try {
            if (topic == null || topic.trim().isEmpty()) {
                logger.warn("Topic parameter is null or empty");
                return Collections.emptyList();
            }
            List<Course> courses = courseRepository.findByTopicOrderByRatingAndStudents(topic, PageRequest.of(0, 100)).getContent();
            List<Course> rankedCourses = mcdmService.rankCourses(courses);
            return rankedCourses.stream()
                .limit(TOP_COURSES_LIMIT)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error occurred while searching courses by topic: {}", topic, e);
            return Collections.emptyList();
        }
    }

    public List<Course> getCoursesByPlatform(String platform) {
        try {
            if (platform == null || platform.trim().isEmpty()) {
                logger.warn("Platform parameter is null or empty");
                return Collections.emptyList();
            }
            List<Course> courses = courseRepository.findByPlatformIgnoreCase(platform);
            List<Course> rankedCourses = mcdmService.rankCourses(courses);
            return rankedCourses.stream()
                .limit(TOP_COURSES_LIMIT)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error occurred while fetching courses for platform: {}", platform, e);
            return Collections.emptyList();
        }
    }

    public List<String> getAllTopics() {
        try {
            return courseRepository.findAllTopics();
        } catch (Exception e) {
            logger.error("Error occurred while fetching all topics", e);
            return Collections.emptyList();
        }
    }

    public Optional<Course> getCourseById(Long id) {
        logger.info("Fetching course by ID: {}", id);
        return courseRepository.findById(id);
    }
} 