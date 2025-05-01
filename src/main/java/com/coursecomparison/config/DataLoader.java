package com.coursecomparison.config;

import com.coursecomparison.model.Course;
import com.coursecomparison.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public void run(String... args) {
        try {
            logger.info("Checking if database is empty...");
            
            // Only load data if database is empty
            if (courseRepository.count() == 0) {
                logger.info("Database is empty, loading sample data...");
                
                // Create and save courses
                List<Course> javaCourses = createJavaCourses();
                List<Course> pythonCourses = createPythonCourses();
                List<Course> webCourses = createWebCourses();

                // Save all courses
                courseRepository.saveAll(javaCourses);
                courseRepository.saveAll(pythonCourses);
                courseRepository.saveAll(webCourses);

                logger.info("Successfully loaded {} sample courses", 
                    javaCourses.size() + pythonCourses.size() + webCourses.size());
            } else {
                logger.info("Database already contains data, skipping sample data loading");
            }
        } catch (Exception e) {
            logger.error("Error in data loader: {}", e.getMessage(), e);
            // Don't throw exception, just log it
        }
    }

    private List<Course> createJavaCourses() {
        return Arrays.asList(
            createCourse(
                "Complete Java Programming Masterclass",
                "Java Programming",
                "Udemy",
                "Tim Buchalka",
                4.8,
                29.99,
                80,
                150000,
                "Comprehensive Java course covering from basics to advanced concepts",
                "https://udemy.com/java-masterclass",
                0.95, 0.90, 0.95, 0.85, 0.90, 0.85
            ),
            createCourse(
                "Java Programming and Software Engineering Fundamentals",
                "Java Programming",
                "Coursera",
                "Duke University",
                4.7,
                49.00,
                60,
                120000,
                "University-level Java programming course with software engineering principles",
                "https://coursera.org/java-programming",
                0.90, 0.95, 0.85, 0.90, 0.85, 0.90
            ),
            createCourse(
                "Java Developer Course",
                "Java Programming",
                "edX",
                "Microsoft",
                4.6,
                199.00,
                120,
                80000,
                "Professional Java development course with industry projects",
                "https://edx.org/java-developer",
                0.85, 0.90, 0.80, 0.95, 0.90, 0.85
            )
        );
    }

    private List<Course> createPythonCourses() {
        return Arrays.asList(
            createCourse(
                "Python for Everybody",
                "Python Programming",
                "Coursera",
                "University of Michigan",
                4.9,
                49.00,
                70,
                200000,
                "Comprehensive Python course for beginners to advanced users",
                "https://coursera.org/python-for-everybody",
                0.95, 0.95, 0.90, 0.90, 0.85, 0.90
            ),
            createCourse(
                "Complete Python Bootcamp",
                "Python Programming",
                "Udemy",
                "Jose Portilla",
                4.7,
                19.99,
                85,
                180000,
                "Hands-on Python course with real-world projects",
                "https://udemy.com/python-bootcamp",
                0.90, 0.85, 0.95, 0.85, 0.90, 0.85
            ),
            createCourse(
                "Python Data Science",
                "Python Programming",
                "edX",
                "MIT",
                4.8,
                150.00,
                90,
                100000,
                "Advanced Python course focusing on data science applications",
                "https://edx.org/python-data-science",
                0.95, 0.90, 0.85, 0.90, 0.95, 0.90
            )
        );
    }

    private List<Course> createWebCourses() {
        return Arrays.asList(
            createCourse(
                "The Complete Web Developer Bootcamp",
                "Web Development",
                "Udemy",
                "Angela Yu",
                4.8,
                29.99,
                100,
                250000,
                "Full-stack web development course covering HTML, CSS, JavaScript, and more",
                "https://udemy.com/web-developer-bootcamp",
                0.95, 0.90, 0.95, 0.90, 0.95, 0.90
            ),
            createCourse(
                "Web Development with Python and JavaScript",
                "Web Development",
                "Coursera",
                "Johns Hopkins University",
                4.7,
                79.00,
                80,
                150000,
                "University-level web development course with modern frameworks",
                "https://coursera.org/web-development",
                0.90, 0.95, 0.85, 0.95, 0.90, 0.90
            ),
            createCourse(
                "Full Stack Web Development",
                "Web Development",
                "edX",
                "Harvard University",
                4.6,
                199.00,
                120,
                90000,
                "Comprehensive full-stack development course with industry projects",
                "https://edx.org/full-stack-development",
                0.85, 0.90, 0.80, 0.90, 0.85, 0.90
            )
        );
    }

    private Course createCourse(
            String title, String topic, String platform, String instructor,
            Double rating, Double price, Integer durationHours, Integer studentCount,
            String description, String url,
            Double contentQuality, Double instructorRating, Double valueForMoney,
            Double courseStructure, Double practicalExercises, Double supportQuality) {
        
        validateCourseParameters(title, topic, platform, instructor, rating, price, 
            durationHours, studentCount, contentQuality, instructorRating, 
            valueForMoney, courseStructure, practicalExercises, supportQuality);
        
        Course course = new Course();
        course.setTitle(title);
        course.setTopic(topic);
        course.setPlatform(platform);
        course.setInstructor(instructor);
        course.setRating(rating);
        course.setPrice(price);
        course.setDurationHours(durationHours);
        course.setStudentCount(studentCount);
        course.setDescription(description);
        course.setUrl(url);
        course.setContentQuality(contentQuality);
        course.setInstructorRating(instructorRating);
        course.setValueForMoney(valueForMoney);
        course.setCourseStructure(courseStructure);
        course.setPracticalExercises(practicalExercises);
        course.setSupportQuality(supportQuality);
        
        return course;
    }

    private void validateCourseParameters(
            String title, String topic, String platform, String instructor,
            Double rating, Double price, Integer durationHours, Integer studentCount,
            Double contentQuality, Double instructorRating, Double valueForMoney,
            Double courseStructure, Double practicalExercises, Double supportQuality) {
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic cannot be empty");
        }
        if (platform == null || platform.trim().isEmpty()) {
            throw new IllegalArgumentException("Platform cannot be empty");
        }
        if (instructor == null || instructor.trim().isEmpty()) {
            throw new IllegalArgumentException("Instructor cannot be empty");
        }
        if (rating == null || rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (durationHours == null || durationHours < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        if (studentCount == null || studentCount < 0) {
            throw new IllegalArgumentException("Student count cannot be negative");
        }
        validateMCDMParameter(contentQuality, "Content quality");
        validateMCDMParameter(instructorRating, "Instructor rating");
        validateMCDMParameter(valueForMoney, "Value for money");
        validateMCDMParameter(courseStructure, "Course structure");
        validateMCDMParameter(practicalExercises, "Practical exercises");
        validateMCDMParameter(supportQuality, "Support quality");
    }

    private void validateMCDMParameter(Double value, String parameterName) {
        if (value == null || value < 0 || value > 1) {
            throw new IllegalArgumentException(parameterName + " must be between 0 and 1");
        }
    }
} 