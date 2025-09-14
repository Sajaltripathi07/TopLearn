package com.coursecomparison.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_criteria_preferences")
public class UserCriteriaPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id")
    private Criteria criteria;
    
    @NotNull(message = "Weight is required")
    @Min(value = 0, message = "Weight must be at least 0")
    @Max(value = 1, message = "Weight must be at most 1")
    private Double weight;
    
    @NotNull(message = "Is enabled flag is required")
    private Boolean isEnabled;
    
    private String customNotes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserCriteriaPreference() {}
    
    public UserCriteriaPreference(User user, Criteria criteria, Double weight, Boolean isEnabled) {
        this.user = user;
        this.criteria = criteria;
        this.weight = weight;
        this.isEnabled = isEnabled;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Criteria getCriteria() { return criteria; }
    public void setCriteria(Criteria criteria) { this.criteria = criteria; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { 
        this.weight = weight; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { 
        this.isEnabled = isEnabled; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCustomNotes() { return customNotes; }
    public void setCustomNotes(String customNotes) { 
        this.customNotes = customNotes; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Pre-persist method to set timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
