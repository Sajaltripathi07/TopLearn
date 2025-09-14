package com.coursecomparison.config;

import com.coursecomparison.model.Course;
import com.coursecomparison.repository.CourseRepository;
import com.coursecomparison.service.CriteriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CriteriaService criteriaService;

    @PostConstruct
    public void loadData() {
        // Initialize criteria first
        criteriaService.initializeDefaultCriteria();
        
        // Then load courses
        if (courseRepository.count() == 0) {
            loadSampleCourses();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        // This method is called after the application context is loaded
        logger.info("DataLoader completed");
    }

    private void loadSampleCourses() {
        logger.info("Loading sample courses...");
        
        List<Course> courses = Arrays.asList(
            createCourse("Complete Python Bootcamp", "Python Programming", "Jose Portilla", "Learn Python from scratch", 
                      "Udemy", 4.5, 150000, 29.99, "https://udemy.com/python-bootcamp"),
            createCourse("JavaScript: The Complete Guide", "Web Development", "Max Schwarzmüller", "Master JavaScript", 
                      "Udemy", 4.6, 120000, 34.99, "https://udemy.com/javascript-complete"),
            createCourse("Machine Learning A-Z", "Data Science", "Kirill Eremenko", "Learn Machine Learning", 
                      "Udemy", 4.4, 80000, 39.99, "https://udemy.com/machine-learning-az"),
            createCourse("Web Development Bootcamp", "Web Development", "Colt Steele", "Full-stack web development", 
                      "Udemy", 4.7, 200000, 24.99, "https://udemy.com/web-dev-bootcamp"),
            createCourse("Data Science Specialization", "Data Science", "Johns Hopkins University", "Comprehensive data science", 
                      "Coursera", 4.8, 50000, 49.99, "https://coursera.org/data-science"),
            createCourse("Introduction to Computer Science", "Computer Science", "Harvard University", "CS50: Introduction to Computer Science", 
                      "edX", 4.9, 30000, 0.0, "https://edx.org/cs50"),
            createCourse("React - The Complete Guide", "Web Development", "Max Schwarzmüller", "Learn React from scratch", 
                      "Udemy", 4.6, 100000, 29.99, "https://udemy.com/react-complete-guide"),
            createCourse("AWS Certified Solutions Architect", "Cloud Computing", "Stephane Maarek", "AWS certification prep", 
                      "Udemy", 4.5, 60000, 44.99, "https://udemy.com/aws-solutions-architect"),
            createCourse("Docker and Kubernetes", "DevOps", "Stephen Grider", "Container orchestration", 
                      "Udemy", 4.4, 40000, 34.99, "https://udemy.com/docker-kubernetes"),
            createCourse("Complete SQL Bootcamp", "Database", "Jose Portilla", "Master SQL and databases", 
                      "Udemy", 4.6, 90000, 19.99, "https://udemy.com/sql-bootcamp")
        );
        
        courseRepository.saveAll(courses);
        logger.info("Loaded {} sample courses", courses.size());
    }
    
    private Course createCourse(String title, String topic, String instructor, String description, 
                               String platform, Double rating, Integer studentCount, Double price, String url) {
        Course course = new Course();
        course.setTitle(title);
        course.setTopic(topic);
        course.setInstructor(instructor);
        course.setDescription(description);
        course.setPlatform(platform);
        course.setRating(rating);
        course.setStudentCount(studentCount);
        course.setPrice(price);
        course.setUrl(url);
        course.setDurationHours(80); // Default duration
        course.setDifficultyLevel("Intermediate");
        course.setLanguage("English");
        course.setHasCertificate(true);
        course.setIsActive(true);
        
        // Set MCDM scores (normalized to 0-1 range)
        course.setContentQuality(0.8 + (Math.random() * 0.2)); // 0.8-1.0
        course.setInstructorRating(0.7 + (Math.random() * 0.3)); // 0.7-1.0
        course.setValueForMoney(0.6 + (Math.random() * 0.4)); // 0.6-1.0
        course.setCourseStructure(0.7 + (Math.random() * 0.3)); // 0.7-1.0
        course.setPracticalExercises(0.6 + (Math.random() * 0.4)); // 0.6-1.0
        course.setSupportQuality(0.5 + (Math.random() * 0.5)); // 0.5-1.0
        
        return course;
    }
} 