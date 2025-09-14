package com.coursecomparison.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    private Set<String> interests = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "user_wishlist", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "course_id")
    private Set<Long> wishlistCourseIds = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "user_criteria_weights", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "criteria")
    @Column(name = "weight")
    private Map<String, Double> personalCriteriaWeights = new HashMap<>();
    
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private Boolean isActive = true;
    private String role = "USER"; // USER, ADMIN
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastLoginAt = LocalDateTime.now();
        initializeDefaultWeights();
    }
    
    private void initializeDefaultWeights() {
        personalCriteriaWeights.put("Content Quality", 0.25);
        personalCriteriaWeights.put("Instructor Rating", 0.20);
        personalCriteriaWeights.put("Value for Money", 0.15);
        personalCriteriaWeights.put("Course Structure", 0.15);
        personalCriteriaWeights.put("Practical Exercises", 0.15);
        personalCriteriaWeights.put("Support Quality", 0.10);
    }
    
    // Constructors
    public User() {}
    
    public User(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Set<String> getInterests() {
        return interests;
    }
    
    public void setInterests(Set<String> interests) {
        this.interests = interests;
    }
    
    public Set<Long> getWishlistCourseIds() {
        return wishlistCourseIds;
    }
    
    public void setWishlistCourseIds(Set<Long> wishlistCourseIds) {
        this.wishlistCourseIds = wishlistCourseIds;
    }
    
    public Map<String, Double> getPersonalCriteriaWeights() {
        return personalCriteriaWeights;
    }
    
    public void setPersonalCriteriaWeights(Map<String, Double> personalCriteriaWeights) {
        this.personalCriteriaWeights = personalCriteriaWeights;
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // Helper methods
    public void addToWishlist(Long courseId) {
        this.wishlistCourseIds.add(courseId);
    }
    
    public void removeFromWishlist(Long courseId) {
        this.wishlistCourseIds.remove(courseId);
    }
    
    public void addInterest(String interest) {
        this.interests.add(interest);
    }
    
    public void updateCriteriaWeight(String criteria, Double weight) {
        this.personalCriteriaWeights.put(criteria, weight);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
