package com.coursecomparison.service;

import com.coursecomparison.model.Criteria;
import com.coursecomparison.model.User;
import com.coursecomparison.model.UserCriteriaPreference;
import com.coursecomparison.repository.CriteriaRepository;
import com.coursecomparison.repository.UserCriteriaPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CriteriaService {
    
    private static final Logger logger = LoggerFactory.getLogger(CriteriaService.class);
    
    @Autowired
    private CriteriaRepository criteriaRepository;
    
    @Autowired
    private UserCriteriaPreferenceRepository userCriteriaPreferenceRepository;
    
    /**
     * Get all active criteria
     */
    public List<Criteria> getAllActiveCriteria() {
        return criteriaRepository.findByIsActiveTrue();
    }
    
    /**
     * Get all user-customizable criteria
     */
    public List<Criteria> getUserCustomizableCriteria() {
        return criteriaRepository.findByIsActiveTrueAndIsUserCustomizableTrue();
    }
    
    /**
     * Get criteria by type
     */
    public List<Criteria> getCriteriaByType(Criteria.CriteriaType type) {
        return criteriaRepository.findByTypeAndActive(type);
    }
    
    /**
     * Get user's criteria preferences
     */
    public List<UserCriteriaPreference> getUserCriteriaPreferences(User user) {
        if (user == null) {
            return getDefaultCriteriaPreferences();
        }
        return userCriteriaPreferenceRepository.findByUserAndCriteriaIsActiveTrue(user);
    }
    
    /**
     * Get user's enabled criteria preferences ordered by weight
     */
    public List<UserCriteriaPreference> getUserEnabledCriteriaPreferences(User user) {
        if (user == null) {
            return getDefaultCriteriaPreferences();
        }
        return userCriteriaPreferenceRepository.findEnabledPreferencesByUserOrderedByWeight(user);
    }
    
    /**
     * Save or update user criteria preferences
     */
    public void saveUserCriteriaPreferences(User user, Map<Long, Double> criteriaWeights) {
        if (user == null) {
            logger.warn("Cannot save criteria preferences for null user");
            return;
        }
        
        // Delete existing preferences
        userCriteriaPreferenceRepository.deleteByUser(user);
        
        // Create new preferences
        List<UserCriteriaPreference> preferences = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : criteriaWeights.entrySet()) {
            Optional<Criteria> criteriaOpt = criteriaRepository.findById(entry.getKey());
            if (criteriaOpt.isPresent()) {
                Criteria criteria = criteriaOpt.get();
                UserCriteriaPreference preference = new UserCriteriaPreference(
                    user, criteria, entry.getValue(), true
                );
                preferences.add(preference);
            }
        }
        
        userCriteriaPreferenceRepository.saveAll(preferences);
        logger.info("Saved {} criteria preferences for user {}", preferences.size(), user.getId());
    }
    
    /**
     * Get default criteria preferences (when no user is logged in)
     */
    private List<UserCriteriaPreference> getDefaultCriteriaPreferences() {
        List<Criteria> activeCriteria = criteriaRepository.findActiveCriteriaOrderedByWeight();
        return activeCriteria.stream()
            .map(criteria -> new UserCriteriaPreference(null, criteria, criteria.getDefaultWeight(), true))
            .collect(Collectors.toList());
    }
    
    /**
     * Get criteria weights as a map for MCDM calculations
     */
    public Map<String, Double> getCriteriaWeightsMap(User user) {
        List<UserCriteriaPreference> preferences = getUserEnabledCriteriaPreferences(user);
        Map<String, Double> weightsMap = new HashMap<>();
        
        for (UserCriteriaPreference preference : preferences) {
            if (preference.getIsEnabled() && preference.getWeight() > 0) {
                weightsMap.put(preference.getCriteria().getName(), preference.getWeight());
            }
        }
        
        // Normalize weights to sum to 1
        double totalWeight = weightsMap.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight > 0) {
            weightsMap.replaceAll((k, v) -> v / totalWeight);
        }
        
        return weightsMap;
    }
    
    /**
     * Initialize default criteria if none exist
     */
    public void initializeDefaultCriteria() {
        if (criteriaRepository.count() == 0) {
            logger.info("Initializing default criteria...");
            
            List<Criteria> defaultCriteria = Arrays.asList(
                new Criteria("Content Quality", "Overall quality and depth of course content", 
                           Criteria.CriteriaType.CONTENT_QUALITY, 0.25, true, true, "rating", 1.0, 5.0, true),
                new Criteria("Instructor Rating", "Instructor expertise and teaching quality", 
                           Criteria.CriteriaType.INSTRUCTOR_RATING, 0.20, true, true, "stars", 1.0, 5.0, true),
                new Criteria("Value for Money", "Cost-effectiveness of the course", 
                           Criteria.CriteriaType.VALUE_FOR_MONEY, 0.15, true, true, "dollars", 0.0, 200.0, false),
                new Criteria("Course Structure", "Organization and flow of course material", 
                           Criteria.CriteriaType.COURSE_STRUCTURE, 0.15, true, true, "rating", 1.0, 5.0, true),
                new Criteria("Practical Exercises", "Hands-on practice and projects", 
                           Criteria.CriteriaType.PRACTICAL_EXERCISES, 0.10, true, true, "rating", 1.0, 5.0, true),
                new Criteria("Support Quality", "Instructor and community support", 
                           Criteria.CriteriaType.SUPPORT_QUALITY, 0.10, true, true, "rating", 1.0, 5.0, true),
                new Criteria("Certification", "Availability of completion certificate", 
                           Criteria.CriteriaType.CERTIFICATION, 0.05, true, true, "boolean", 0.0, 1.0, true)
            );
            
            criteriaRepository.saveAll(defaultCriteria);
            logger.info("Initialized {} default criteria", defaultCriteria.size());
        }
    }
    
    /**
     * Validate criteria weights (must sum to 1)
     */
    public boolean validateCriteriaWeights(Map<Long, Double> criteriaWeights) {
        double totalWeight = criteriaWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        return Math.abs(totalWeight - 1.0) < 0.01; // Allow small floating point errors
    }
}
