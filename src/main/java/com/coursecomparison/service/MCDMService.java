package com.coursecomparison.service;

import com.coursecomparison.model.Course;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class implementing TOPSIS (Technique for Order Preference by Similarity to Ideal Solution)
 * for multi-criteria decision making in course ranking.
 */
@Service
public class MCDMService {
    private static final Logger logger = LoggerFactory.getLogger(MCDMService.class);

    // Criteria weights (can be adjusted based on importance)
    private static final double CONTENT_QUALITY_WEIGHT = 0.25;
    private static final double INSTRUCTOR_RATING_WEIGHT = 0.20;
    private static final double VALUE_FOR_MONEY_WEIGHT = 0.15;
    private static final double COURSE_STRUCTURE_WEIGHT = 0.15;
    private static final double PRACTICAL_EXERCISES_WEIGHT = 0.15;
    private static final double SUPPORT_QUALITY_WEIGHT = 0.10;

    // Weights for different criteria
    private static final double RATING_WEIGHT = 0.3;
    private static final double STUDENT_COUNT_WEIGHT = 0.2;
    private static final double PRICE_WEIGHT = 0.2;

    /**
     * Ranks courses using TOPSIS method based on multiple criteria.
     * 
     * @param courses List of courses to rank
     * @return Ranked list of courses (best to worst)
     * @throws IllegalArgumentException if courses list is null or empty
     */
    public List<Course> rankCoursesUsingTOPSIS(List<Course> courses) {
        Assert.notNull(courses, "Courses list cannot be null");
        if (courses.isEmpty()) {
            return courses;
        }

        validateCourses(courses);

        // Calculate TOPSIS scores for each course
        Map<Course, Double> topsisScores = calculateTOPSISScores(courses);
        
        // Sort courses by TOPSIS score in descending order
        return courses.stream()
                .sorted(Comparator.comparingDouble(course -> -topsisScores.getOrDefault(course, 0.0)))
                .collect(Collectors.toList());
    }

    private void validateCourses(List<Course> courses) {
        for (Course course : courses) {
            Assert.notNull(course.getContentQuality(), "Content quality cannot be null");
            Assert.notNull(course.getInstructorRating(), "Instructor rating cannot be null");
            Assert.notNull(course.getValueForMoney(), "Value for money cannot be null");
            Assert.notNull(course.getCourseStructure(), "Course structure cannot be null");
            Assert.notNull(course.getPracticalExercises(), "Practical exercises cannot be null");
            Assert.notNull(course.getSupportQuality(), "Support quality cannot be null");

            validateValueRange(course.getContentQuality(), "Content quality");
            validateValueRange(course.getInstructorRating(), "Instructor rating");
            validateValueRange(course.getValueForMoney(), "Value for money");
            validateValueRange(course.getCourseStructure(), "Course structure");
            validateValueRange(course.getPracticalExercises(), "Practical exercises");
            validateValueRange(course.getSupportQuality(), "Support quality");
        }
    }

    private void validateValueRange(Double value, String fieldName) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException(fieldName + " must be between 0 and 1");
        }
    }

    private Map<Course, Double> calculateTOPSISScores(List<Course> courses) {
        // Normalize criteria values
        Map<Course, Double> normalizedContentQuality = normalizeCriteria(courses, Course::getContentQuality);
        Map<Course, Double> normalizedInstructorRating = normalizeCriteria(courses, Course::getInstructorRating);
        Map<Course, Double> normalizedValueForMoney = normalizeCriteria(courses, Course::getValueForMoney);
        Map<Course, Double> normalizedCourseStructure = normalizeCriteria(courses, Course::getCourseStructure);
        Map<Course, Double> normalizedPracticalExercises = normalizeCriteria(courses, Course::getPracticalExercises);
        Map<Course, Double> normalizedSupportQuality = normalizeCriteria(courses, Course::getSupportQuality);
        
        // Calculate weighted normalized values
        Map<Course, Double> weightedScores = new HashMap<>();
        for (Course course : courses) {
            double score = 
                normalizedContentQuality.get(course) * CONTENT_QUALITY_WEIGHT +
                normalizedInstructorRating.get(course) * INSTRUCTOR_RATING_WEIGHT +
                normalizedValueForMoney.get(course) * VALUE_FOR_MONEY_WEIGHT +
                normalizedCourseStructure.get(course) * COURSE_STRUCTURE_WEIGHT +
                normalizedPracticalExercises.get(course) * PRACTICAL_EXERCISES_WEIGHT +
                normalizedSupportQuality.get(course) * SUPPORT_QUALITY_WEIGHT;
            
            weightedScores.put(course, score);
        }
        
        return weightedScores;
    }
    
    private Map<Course, Double> normalizeCriteria(List<Course> courses, java.util.function.Function<Course, Double> criteriaGetter) {
        // Find min and max values
        double min = courses.stream()
                .mapToDouble(criteriaGetter::apply)
                .min()
                .orElse(0.0);
        
        double max = courses.stream()
                .mapToDouble(criteriaGetter::apply)
                .max()
                .orElse(1.0);
        
        // Normalize values
        Map<Course, Double> normalizedValues = new HashMap<>();
        for (Course course : courses) {
            double value = criteriaGetter.apply(course);
            double normalized = (value - min) / (max - min);
            normalizedValues.put(course, normalized);
        }
        
        return normalizedValues;
    }

    public Map<String, Double> getCriteriaWeights() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Content Quality", CONTENT_QUALITY_WEIGHT);
        weights.put("Instructor Rating", INSTRUCTOR_RATING_WEIGHT);
        weights.put("Value for Money", VALUE_FOR_MONEY_WEIGHT);
        weights.put("Course Structure", COURSE_STRUCTURE_WEIGHT);
        weights.put("Practical Exercises", PRACTICAL_EXERCISES_WEIGHT);
        weights.put("Support Quality", SUPPORT_QUALITY_WEIGHT);
        return weights;
    }
    
    public Map<String, Double> analyzeCourse(Course course) {
        Map<String, Double> analysis = new HashMap<>();
        analysis.put("Content Quality Score", course.getContentQuality() * CONTENT_QUALITY_WEIGHT);
        analysis.put("Instructor Rating Score", course.getInstructorRating() * INSTRUCTOR_RATING_WEIGHT);
        analysis.put("Value for Money Score", course.getValueForMoney() * VALUE_FOR_MONEY_WEIGHT);
        analysis.put("Course Structure Score", course.getCourseStructure() * COURSE_STRUCTURE_WEIGHT);
        analysis.put("Practical Exercises Score", course.getPracticalExercises() * PRACTICAL_EXERCISES_WEIGHT);
        analysis.put("Support Quality Score", course.getSupportQuality() * SUPPORT_QUALITY_WEIGHT);
        return analysis;
    }

    public List<Course> rankCourses(List<Course> courses) {
        logger.info("Ranking {} courses using MCDM", courses.size());
        
        if (courses == null || courses.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Calculate scores for each course
        List<Course> scoredCourses = courses.stream()
            .map(this::calculateMCDMScore)
            .collect(Collectors.toList());
        
        // Sort by MCDM score in descending order
        scoredCourses.sort(Comparator.comparingDouble(Course::getMcdmScore).reversed());
        
        logger.info("Ranked courses using MCDM");
        return scoredCourses;
    }
    
    private Course calculateMCDMScore(Course course) {
        // Normalize values
        double normalizedRating = normalizeRating(course.getRating());
        double normalizedStudentCount = normalizeStudentCount(course.getStudentCount());
        double normalizedPrice = normalizePrice(course.getPrice());
        double normalizedContentQuality = normalizeContentQuality(course.getContentQuality());
        double normalizedInstructorRating = normalizeInstructorRating(course.getInstructorRating());
        
        // Calculate weighted score
        double mcdmScore = (normalizedRating * RATING_WEIGHT) +
                          (normalizedStudentCount * STUDENT_COUNT_WEIGHT) +
                          (normalizedPrice * PRICE_WEIGHT) +
                          (normalizedContentQuality * CONTENT_QUALITY_WEIGHT) +
                          (normalizedInstructorRating * INSTRUCTOR_RATING_WEIGHT);
        
        course.setMcdmScore(mcdmScore);
        return course;
    }
    
    private double normalizeRating(Double rating) {
        if (rating == null) return 0.0;
        return rating / 5.0; // Normalize to 0-1 range
    }
    
    private double normalizeStudentCount(Integer studentCount) {
        if (studentCount == null) return 0.0;
        // Normalize to 0-1 range, assuming max student count is 100,000
        return Math.min(studentCount / 100000.0, 1.0);
    }
    
    private double normalizePrice(Double price) {
        if (price == null || price == 0.0) return 1.0; // Free courses get max score
        // Normalize to 0-1 range, assuming max price is $200
        return Math.max(0.0, 1.0 - (price / 200.0));
    }
    
    private double normalizeContentQuality(Double contentQuality) {
        if (contentQuality == null) return 0.7; // Default value
        return contentQuality;
    }
    
    private double normalizeInstructorRating(Double instructorRating) {
        if (instructorRating == null) return 0.7; // Default value
        return instructorRating;
    }
} 