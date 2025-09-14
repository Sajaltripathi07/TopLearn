package com.coursecomparison.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Entity
@Table(name = "criteria")
public class Criteria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Criteria name is required")
    @Column(unique = true)
    private String name;
    
    @NotBlank(message = "Criteria description is required")
    private String description;
    
    @NotNull(message = "Criteria type is required")
    @Enumerated(EnumType.STRING)
    private CriteriaType type;
    
    @NotNull(message = "Default weight is required")
    @Min(value = 0, message = "Weight must be at least 0")
    @Max(value = 1, message = "Weight must be at most 1")
    private Double defaultWeight;
    
    @NotNull(message = "Is active flag is required")
    private Boolean isActive;
    
    @NotNull(message = "Is user customizable flag is required")
    private Boolean isUserCustomizable;
    
    private String unit; // e.g., "stars", "students", "hours", "dollars"
    
    @NotNull(message = "Min value is required")
    private Double minValue;
    
    @NotNull(message = "Max value is required")
    private Double maxValue;
    
    @NotNull(message = "Is benefit criteria flag is required")
    private Boolean isBenefitCriteria; // true if higher is better, false if lower is better
    
    // Constructors
    public Criteria() {}
    
    public Criteria(String name, String description, CriteriaType type, Double defaultWeight, 
                   Boolean isActive, Boolean isUserCustomizable, String unit, 
                   Double minValue, Double maxValue, Boolean isBenefitCriteria) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.defaultWeight = defaultWeight;
        this.isActive = isActive;
        this.isUserCustomizable = isUserCustomizable;
        this.unit = unit;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.isBenefitCriteria = isBenefitCriteria;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public CriteriaType getType() { return type; }
    public void setType(CriteriaType type) { this.type = type; }
    
    public Double getDefaultWeight() { return defaultWeight; }
    public void setDefaultWeight(Double defaultWeight) { this.defaultWeight = defaultWeight; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsUserCustomizable() { return isUserCustomizable; }
    public void setIsUserCustomizable(Boolean isUserCustomizable) { this.isUserCustomizable = isUserCustomizable; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public Double getMinValue() { return minValue; }
    public void setMinValue(Double minValue) { this.minValue = minValue; }
    
    public Double getMaxValue() { return maxValue; }
    public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }
    
    public Boolean getIsBenefitCriteria() { return isBenefitCriteria; }
    public void setIsBenefitCriteria(Boolean isBenefitCriteria) { this.isBenefitCriteria = isBenefitCriteria; }
    
    // Criteria Types Enum
    public enum CriteriaType {
        CONTENT_QUALITY("Content Quality"),
        INSTRUCTOR_RATING("Instructor Rating"),
        VALUE_FOR_MONEY("Value for Money"),
        COURSE_STRUCTURE("Course Structure"),
        PRACTICAL_EXERCISES("Practical Exercises"),
        SUPPORT_QUALITY("Support Quality"),
        CERTIFICATION("Certification"),
        FLEXIBILITY("Flexibility"),
        COMMUNITY("Community"),
        UPDATES("Content Updates");
        
        private final String displayName;
        
        CriteriaType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
