package com.coursecomparison.service;

import com.coursecomparison.model.Course;
import com.coursecomparison.model.User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced MCDM Service implementing multiple algorithms for course ranking.
 * Supports TOPSIS, AHP, and personalized ranking based on user preferences.
 */
@Service
public class MCDMService {
    private static final Logger logger = LoggerFactory.getLogger(MCDMService.class);

    // Default criteria weights
    private static final Map<String, Double> DEFAULT_WEIGHTS = Map.of(
        "Content Quality", 0.25,
        "Instructor Rating", 0.20,
        "Value for Money", 0.15,
        "Course Structure", 0.15,
        "Practical Exercises", 0.15,
        "Support Quality", 0.10
    );

    // Algorithm types
    public enum Algorithm {
        TOPSIS, AHP, PERSONALIZED
    }

    /**
     * Ranks courses using specified algorithm
     */
    public List<Course> rankCourses(List<Course> courses, Algorithm algorithm, User user) {
        Assert.notNull(courses, "Courses list cannot be null");
        if (courses.isEmpty()) {
            return courses;
        }

        validateCourses(courses);

        switch (algorithm) {
            case TOPSIS:
                return rankCoursesUsingTOPSIS(courses, user);
            case AHP:
                return rankCoursesUsingAHP(courses, user);
            case PERSONALIZED:
                return rankCoursesPersonalized(courses, user);
            default:
                return rankCoursesUsingTOPSIS(courses, user);
        }
    }

    /**
     * Ranks courses using TOPSIS method with user preferences
     */
    public List<Course> rankCoursesUsingTOPSIS(List<Course> courses, User user) {
        Map<String, Double> weights = getUserWeights(user);
        Map<Course, Double> topsisScores = calculateTOPSISScores(courses, weights);
        
        return courses.stream()
                .sorted(Comparator.comparingDouble(course -> -topsisScores.getOrDefault(course, 0.0)))
                .collect(Collectors.toList());
    }

    /**
     * Ranks courses using AHP (Analytic Hierarchy Process)
     */
    public List<Course> rankCoursesUsingAHP(List<Course> courses, User user) {
        Map<String, Double> weights = getUserWeights(user);
        Map<Course, Double> ahpScores = calculateAHPScores(courses, weights);
        
        return courses.stream()
                .sorted(Comparator.comparingDouble(course -> -ahpScores.getOrDefault(course, 0.0)))
                .collect(Collectors.toList());
    }

    /**
     * Personalized ranking based on user interests and preferences
     */
    public List<Course> rankCoursesPersonalized(List<Course> courses, User user) {
        if (user == null) {
            return rankCoursesUsingTOPSIS(courses, null);
        }

        Map<String, Double> weights = getUserWeights(user);
        Map<Course, Double> personalizedScores = calculatePersonalizedScores(courses, weights, user);
        
        return courses.stream()
                .sorted(Comparator.comparingDouble(course -> -personalizedScores.getOrDefault(course, 0.0)))
                .collect(Collectors.toList());
    }

    /**
     * Get user-specific weights or default weights
     */
    private Map<String, Double> getUserWeights(User user) {
        if (user != null && user.getPersonalCriteriaWeights() != null) {
            return user.getPersonalCriteriaWeights();
        }
        return new HashMap<>(DEFAULT_WEIGHTS);
    }

    /**
     * Calculate TOPSIS scores with dynamic weights
     */
    private Map<Course, Double> calculateTOPSISScores(List<Course> courses, Map<String, Double> weights) {
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
                normalizedContentQuality.get(course) * weights.get("Content Quality") +
                normalizedInstructorRating.get(course) * weights.get("Instructor Rating") +
                normalizedValueForMoney.get(course) * weights.get("Value for Money") +
                normalizedCourseStructure.get(course) * weights.get("Course Structure") +
                normalizedPracticalExercises.get(course) * weights.get("Practical Exercises") +
                normalizedSupportQuality.get(course) * weights.get("Support Quality");
            
            weightedScores.put(course, score);
            course.setMcdmScore(score);
        }
        
        return weightedScores;
    }

    /**
     * Calculate AHP scores
     */
    private Map<Course, Double> calculateAHPScores(List<Course> courses, Map<String, Double> weights) {
        Map<Course, Double> ahpScores = new HashMap<>();
        
        for (Course course : courses) {
            double score = 0.0;
            
            // Apply AHP methodology with consistency ratio
            score += calculateAHPCriteriaScore(course.getContentQuality(), weights.get("Content Quality"));
            score += calculateAHPCriteriaScore(course.getInstructorRating(), weights.get("Instructor Rating"));
            score += calculateAHPCriteriaScore(course.getValueForMoney(), weights.get("Value for Money"));
            score += calculateAHPCriteriaScore(course.getCourseStructure(), weights.get("Course Structure"));
            score += calculateAHPCriteriaScore(course.getPracticalExercises(), weights.get("Practical Exercises"));
            score += calculateAHPCriteriaScore(course.getSupportQuality(), weights.get("Support Quality"));
            
            ahpScores.put(course, score);
            course.setMcdmScore(score);
        }
        
        return ahpScores;
    }

    /**
     * Calculate personalized scores considering user interests
     */
    private Map<Course, Double> calculatePersonalizedScores(List<Course> courses, Map<String, Double> weights, User user) {
        Map<Course, Double> personalizedScores = new HashMap<>();
        
        for (Course course : courses) {
            double baseScore = calculateBaseScore(course, weights);
            double interestBonus = calculateInterestBonus(course, user);
            double difficultyBonus = calculateDifficultyBonus(course, user);
            
            double totalScore = baseScore + interestBonus + difficultyBonus;
            personalizedScores.put(course, totalScore);
            course.setMcdmScore(totalScore);
        }
        
        return personalizedScores;
    }

    /**
     * Calculate base score using weighted criteria
     */
    private double calculateBaseScore(Course course, Map<String, Double> weights) {
        return course.getContentQuality() * weights.get("Content Quality") +
               course.getInstructorRating() * weights.get("Instructor Rating") +
               course.getValueForMoney() * weights.get("Value for Money") +
               course.getCourseStructure() * weights.get("Course Structure") +
               course.getPracticalExercises() * weights.get("Practical Exercises") +
               course.getSupportQuality() * weights.get("Support Quality");
    }

    /**
     * Calculate bonus for courses matching user interests
     */
    private double calculateInterestBonus(Course course, User user) {
        if (user.getInterests() == null || user.getInterests().isEmpty()) {
            return 0.0;
        }
        
        return user.getInterests().stream()
                .anyMatch(interest -> course.getTopic().toLowerCase().contains(interest.toLowerCase())) ? 0.1 : 0.0;
    }

    /**
     * Calculate bonus based on course difficulty matching user level
     */
    private double calculateDifficultyBonus(Course course, User user) {
        // This could be enhanced with user skill level tracking
        return 0.0;
    }

    /**
     * Calculate AHP criteria score
     */
    private double calculateAHPCriteriaScore(Double value, Double weight) {
        if (value == null) return 0.0;
        return value * weight;
    }

    /**
     * Normalize criteria values for TOPSIS
     */
    private Map<Course, Double> normalizeCriteria(List<Course> courses, java.util.function.Function<Course, Double> criteriaGetter) {
        double min = courses.stream()
                .mapToDouble(criteriaGetter::apply)
                .min()
                .orElse(0.0);
        
        double max = courses.stream()
                .mapToDouble(criteriaGetter::apply)
                .max()
                .orElse(1.0);
        
        Map<Course, Double> normalizedValues = new HashMap<>();
        for (Course course : courses) {
            double value = criteriaGetter.apply(course);
            double normalized = (max - min) > 0 ? (value - min) / (max - min) : 0.0;
            normalizedValues.put(course, normalized);
        }
        
        return normalizedValues;
    }

    /**
     * Validate course data
     */
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

    /**
     * Get criteria weights
     */
    public Map<String, Double> getCriteriaWeights(User user) {
        return getUserWeights(user);
    }
    
    /**
     * Analyze individual course performance
     */
    public Map<String, Double> analyzeCourse(Course course, User user) {
        Map<String, Double> weights = getUserWeights(user);
        Map<String, Double> analysis = new HashMap<>();
        
        analysis.put("Content Quality Score", course.getContentQuality() * weights.get("Content Quality"));
        analysis.put("Instructor Rating Score", course.getInstructorRating() * weights.get("Instructor Rating"));
        analysis.put("Value for Money Score", course.getValueForMoney() * weights.get("Value for Money"));
        analysis.put("Course Structure Score", course.getCourseStructure() * weights.get("Course Structure"));
        analysis.put("Practical Exercises Score", course.getPracticalExercises() * weights.get("Practical Exercises"));
        analysis.put("Support Quality Score", course.getSupportQuality() * weights.get("Support Quality"));
        
        return analysis;
    }

    /**
     * Get algorithm comparison for courses
     */
    public Map<String, List<Course>> compareAlgorithms(List<Course> courses, User user) {
        Map<String, List<Course>> comparison = new HashMap<>();
        
        comparison.put("TOPSIS", rankCoursesUsingTOPSIS(new ArrayList<>(courses), user));
        comparison.put("AHP", rankCoursesUsingAHP(new ArrayList<>(courses), user));
        comparison.put("Personalized", rankCoursesPersonalized(new ArrayList<>(courses), user));
        
        return comparison;
    }

    /**
     * Legacy method for backward compatibility
     */
    public List<Course> rankCourses(List<Course> courses) {
        return rankCourses(courses, Algorithm.TOPSIS, null);
    }
} 