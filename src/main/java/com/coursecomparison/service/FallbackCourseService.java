package com.coursecomparison.service;

import com.coursecomparison.model.Course;
import com.coursecomparison.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to provide fallback course data when live APIs are not accessible
 */
@Service
public class FallbackCourseService {
    
    private static final Logger logger = LoggerFactory.getLogger(FallbackCourseService.class);
    
    @Autowired
    private CourseRepository courseRepository;
    
    /**
     * Get fallback courses when live data is not available
     */
    public List<Course> getFallbackCourses(String keyword) {
        logger.info("Providing fallback courses for keyword: {}", keyword);
        
        // Try to get existing courses from database first
        List<Course> existingCourses = courseRepository.findAll();
        if (!existingCourses.isEmpty()) {
            logger.info("Found {} existing courses in database", existingCourses.size());
            return existingCourses;
        }
        
        // If no courses in database, create sample courses
        return createSampleCourses(keyword);
    }
    
    /**
     * Create sample courses as fallback
     */
    private List<Course> createSampleCourses(String keyword) {
        logger.info("Creating sample courses for keyword: {}", keyword);
        
        List<Course> sampleCourses = new ArrayList<>();
        
        // Python courses
        if (keyword.toLowerCase().contains("python")) {
            sampleCourses.add(createSampleCourse(
                "Python for Everybody",
                "Comprehensive Python course for beginners to advanced users",
                "University of Michigan",
                "Coursera",
                "https://www.coursera.org/specializations/python",
                4.5,
                1000,
                0.0,
                "Programming",
                70,
                "English",
                "Beginner"
            ));
            
            sampleCourses.add(createSampleCourse(
                "Complete Python Bootcamp",
                "Learn Python like a Professional! Start from the basics and go all the way to creating your own applications and games!",
                "Jose Portilla",
                "Udemy",
                "https://www.udemy.com/course/complete-python-bootcamp/",
                4.6,
                500,
                99.99,
                "Programming",
                22,
                "English",
                "Beginner"
            ));
        }
        
        // Java courses
        if (keyword.toLowerCase().contains("java")) {
            sampleCourses.add(createSampleCourse(
                "Java Programming and Software Engineering Fundamentals",
                "Learn to code in Java and improve your programming and problem-solving skills",
                "Duke University",
                "Coursera",
                "https://www.coursera.org/specializations/java-programming",
                4.4,
                800,
                0.0,
                "Programming",
                60,
                "English",
                "Beginner"
            ));
        }
        
        // JavaScript courses
        if (keyword.toLowerCase().contains("javascript") || keyword.toLowerCase().contains("js")) {
            sampleCourses.add(createSampleCourse(
                "JavaScript: The Complete Guide",
                "Master JavaScript from beginner to advanced with real-world projects",
                "Maximilian Schwarzmüller",
                "Udemy",
                "https://www.udemy.com/course/javascript-the-complete-guide-2020-beginner-advanced/",
                4.7,
                300,
                89.99,
                "Programming",
                40,
                "English",
                "Intermediate"
            ));
        }
        
        // Web Development courses
        if (keyword.toLowerCase().contains("web") || keyword.toLowerCase().contains("html") || keyword.toLowerCase().contains("css")) {
            sampleCourses.add(createSampleCourse(
                "HTML, CSS, and Javascript for Web Developers",
                "Learn the fundamental tools that every web page coder needs to know",
                "Johns Hopkins University",
                "Coursera",
                "https://www.coursera.org/learn/html-css-javascript-for-web-developers",
                4.5,
                1200,
                0.0,
                "Web Development",
                40,
                "English",
                "Beginner"
            ));
        }
        
        // Data Science courses
        if (keyword.toLowerCase().contains("data") || keyword.toLowerCase().contains("machine learning") || keyword.toLowerCase().contains("ai")) {
            sampleCourses.add(createSampleCourse(
                "Machine Learning",
                "Learn the fundamentals of machine learning and how to apply it to real-world problems",
                "Stanford University",
                "Coursera",
                "https://www.coursera.org/learn/machine-learning",
                4.9,
                2000,
                0.0,
                "Data Science",
                55,
                "English",
                "Intermediate"
            ));
        }
        
        // If no specific keyword match, add general programming courses
        if (sampleCourses.isEmpty()) {
            sampleCourses.add(createSampleCourse(
                "Introduction to Computer Science",
                "An introduction to the intellectual enterprises of computer science and the art of programming",
                "Harvard University",
                "edX",
                "https://www.edx.org/course/introduction-computer-science-harvardx-cs50x",
                4.8,
                1500,
                0.0,
                "Computer Science",
                100,
                "English",
                "Beginner"
            ));
            
            sampleCourses.add(createSampleCourse(
                "The Complete Web Developer Course",
                "Learn web development with HTML, CSS, JavaScript, PHP, Python, MySQL & more!",
                "Rob Percival",
                "Udemy",
                "https://www.udemy.com/course/the-complete-web-developer-course-2/",
                4.3,
                400,
                199.99,
                "Web Development",
                30,
                "English",
                "Beginner"
            ));
        }
        
        logger.info("Created {} sample courses", sampleCourses.size());
        return sampleCourses;
    }
    
    /**
     * Create a sample course with the given parameters
     */
    private Course createSampleCourse(String title, String description, String instructor, 
                                    String platform, String url, Double rating, Integer studentCount, 
                                    Double price, String topic, Integer durationHours, 
                                    String language, String difficultyLevel) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setInstructor(instructor);
        course.setPlatform(platform);
        course.setUrl(url);
        course.setRating(rating);
        course.setStudentCount(studentCount);
        course.setPrice(price);
        course.setTopic(topic);
        course.setDurationHours(durationHours);
        course.setLanguage(language);
        course.setDifficultyLevel(difficultyLevel);
        course.setIsActive(true);
        course.setLastUpdated(LocalDateTime.now());
        
        // Set quality metrics
        course.setContentQuality(0.85);
        course.setInstructorRating(0.90);
        course.setValueForMoney(0.80);
        course.setCourseStructure(0.85);
        course.setPracticalExercises(0.75);
        course.setSupportQuality(0.80);
        
        // Set certificate availability
        course.setHasCertificate(platform.equals("Coursera") || platform.equals("edX"));
        
        return course;
    }
}
