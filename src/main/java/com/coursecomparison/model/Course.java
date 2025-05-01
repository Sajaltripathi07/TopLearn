package com.coursecomparison.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.hibernate.validator.constraints.URL;
import java.util.Map;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Topic is required")
    private String topic;
    
    @NotBlank(message = "Platform is required")
    private String platform;
    
    @NotBlank(message = "Instructor is required")
    private String instructor;
    
    @NotNull(message = "Rating is required")
    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 5, message = "Rating must be at most 5")
    private Double rating;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;
    
    @NotNull(message = "Duration in hours is required")
    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationHours;
    
    @NotNull(message = "Student count is required")
    @Min(value = 0, message = "Student count cannot be negative")
    private Integer studentCount;
    
    @Column(length = 1000)
    private String description;
    
    @URL(message = "Invalid URL format")
    private String url;
    
    // MCDM specific fields with validation
    @NotNull(message = "Content quality is required")
    @Min(value = 0, message = "Content quality must be at least 0")
    @Max(value = 1, message = "Content quality must be at most 1")
    private Double contentQuality;

    @NotNull(message = "Instructor rating is required")
    @Min(value = 0, message = "Instructor rating must be at least 0")
    @Max(value = 1, message = "Instructor rating must be at most 1")
    private Double instructorRating;

    @NotNull(message = "Value for money is required")
    @Min(value = 0, message = "Value for money must be at least 0")
    @Max(value = 1, message = "Value for money must be at most 1")
    private Double valueForMoney;

    @NotNull(message = "Course structure is required")
    @Min(value = 0, message = "Course structure must be at least 0")
    @Max(value = 1, message = "Course structure must be at most 1")
    private Double courseStructure;

    @NotNull(message = "Practical exercises is required")
    @Min(value = 0, message = "Practical exercises must be at least 0")
    @Max(value = 1, message = "Practical exercises must be at most 1")
    private Double practicalExercises;

    @NotNull(message = "Support quality is required")
    @Min(value = 0, message = "Support quality must be at least 0")
    @Max(value = 1, message = "Support quality must be at most 1")
    @Column(name = "support_quality")
    private Double supportQuality;

    @Column(name = "mcdm_score")
    private Double mcdmScore;
    
    @Transient
    private Map<String, Double> criteriaScores;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Double getContentQuality() {
        return contentQuality;
    }

    public void setContentQuality(Double contentQuality) {
        this.contentQuality = contentQuality;
    }

    public Double getInstructorRating() {
        return instructorRating;
    }

    public void setInstructorRating(Double instructorRating) {
        this.instructorRating = instructorRating;
    }

    public Double getValueForMoney() {
        return valueForMoney;
    }

    public void setValueForMoney(Double valueForMoney) {
        this.valueForMoney = valueForMoney;
    }

    public Double getCourseStructure() {
        return courseStructure;
    }

    public void setCourseStructure(Double courseStructure) {
        this.courseStructure = courseStructure;
    }

    public Double getPracticalExercises() {
        return practicalExercises;
    }

    public void setPracticalExercises(Double practicalExercises) {
        this.practicalExercises = practicalExercises;
    }

    public Double getSupportQuality() {
        return supportQuality;
    }

    public void setSupportQuality(Double supportQuality) {
        this.supportQuality = supportQuality;
    }

    public Double getMcdmScore() {
        return mcdmScore;
    }

    public void setMcdmScore(Double mcdmScore) {
        this.mcdmScore = mcdmScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return id != null && id.equals(course.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", topic='" + topic + '\'' +
                ", platform='" + platform + '\'' +
                ", instructor='" + instructor + '\'' +
                ", rating=" + rating +
                ", price=" + price +
                ", durationHours=" + durationHours +
                ", studentCount=" + studentCount +
                ", contentQuality=" + contentQuality +
                ", instructorRating=" + instructorRating +
                ", valueForMoney=" + valueForMoney +
                ", courseStructure=" + courseStructure +
                ", practicalExercises=" + practicalExercises +
                ", supportQuality=" + supportQuality +
                '}';
    }
} 