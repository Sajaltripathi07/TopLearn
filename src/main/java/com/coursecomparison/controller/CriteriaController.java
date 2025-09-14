package com.coursecomparison.controller;

import com.coursecomparison.model.Criteria;
import com.coursecomparison.model.User;
import com.coursecomparison.service.CriteriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/criteria")
@CrossOrigin(origins = "*")
public class CriteriaController {
    
    private static final Logger logger = LoggerFactory.getLogger(CriteriaController.class);
    
    @Autowired
    private CriteriaService criteriaService;
    
    /**
     * Get all active criteria
     */
    @GetMapping("/")
    public ResponseEntity<List<Criteria>> getAllActiveCriteria() {
        List<Criteria> criteria = criteriaService.getAllActiveCriteria();
        return ResponseEntity.ok(criteria);
    }
    
    /**
     * Get user-customizable criteria
     */
    @GetMapping("/customizable")
    public ResponseEntity<List<Criteria>> getUserCustomizableCriteria() {
        List<Criteria> criteria = criteriaService.getUserCustomizableCriteria();
        return ResponseEntity.ok(criteria);
    }
    
    /**
     * Get criteria by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Criteria>> getCriteriaByType(@PathVariable String type) {
        try {
            Criteria.CriteriaType criteriaType = Criteria.CriteriaType.valueOf(type.toUpperCase());
            List<Criteria> criteria = criteriaService.getCriteriaByType(criteriaType);
            return ResponseEntity.ok(criteria);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid criteria type: {}", type);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get user's criteria preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Object>> getUserCriteriaPreferences(@RequestParam(required = false) Long userId) {
        // For now, we'll use null user (default preferences)
        // In a real app, you'd get the user from authentication context
        User user = null; // TODO: Get from authentication context
        
        List<Criteria> allCriteria = criteriaService.getAllActiveCriteria();
        Map<String, Double> weights = criteriaService.getCriteriaWeightsMap(user);
        
        Map<String, Object> response = Map.of(
            "criteria", allCriteria,
            "weights", weights,
            "totalWeight", weights.values().stream().mapToDouble(Double::doubleValue).sum()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Save user criteria preferences
     */
    @PostMapping("/preferences")
    public ResponseEntity<Map<String, Object>> saveUserCriteriaPreferences(
            @RequestBody Map<Long, Double> criteriaWeights,
            @RequestParam(required = false) Long userId) {
        
        // Validate weights sum to 1
        if (!criteriaService.validateCriteriaWeights(criteriaWeights)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Criteria weights must sum to 1.0",
                "totalWeight", criteriaWeights.values().stream().mapToDouble(Double::doubleValue).sum()
            ));
        }
        
        // For now, we'll use null user (default preferences)
        // In a real app, you'd get the user from authentication context
        User user = null; // TODO: Get from authentication context
        
        try {
            criteriaService.saveUserCriteriaPreferences(user, criteriaWeights);
            logger.info("Saved criteria preferences for user: {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Criteria preferences saved successfully",
                "weights", criteriaWeights
            ));
        } catch (Exception e) {
            logger.error("Error saving criteria preferences: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to save criteria preferences"
            ));
        }
    }
    
    /**
     * Get criteria weights for MCDM calculations
     */
    @GetMapping("/weights")
    public ResponseEntity<Map<String, Double>> getCriteriaWeights(@RequestParam(required = false) Long userId) {
        // For now, we'll use null user (default preferences)
        User user = null; // TODO: Get from authentication context
        
        Map<String, Double> weights = criteriaService.getCriteriaWeightsMap(user);
        return ResponseEntity.ok(weights);
    }
    
    /**
     * Initialize default criteria (admin only)
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeDefaultCriteria() {
        try {
            criteriaService.initializeDefaultCriteria();
            return ResponseEntity.ok(Map.of(
                "message", "Default criteria initialized successfully"
            ));
        } catch (Exception e) {
            logger.error("Error initializing default criteria: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to initialize default criteria"
            ));
        }
    }
}
