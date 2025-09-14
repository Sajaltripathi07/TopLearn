package com.coursecomparison.service;

import com.coursecomparison.model.Course;
import com.coursecomparison.model.User;
import com.coursecomparison.repository.CourseRepository;
import com.coursecomparison.service.MCDMService.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private static final int TOP_COURSES_LIMIT = 12;
    private static final int SEARCH_RESULTS_LIMIT = 50;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private LiveCourseService liveCourseService;
    
    @Autowired
    private MCDMService mcdmService;
    
    @Autowired
    private CourseValidationService courseValidationService;
    
    /**
     * Get all courses with caching and ranking
     */
    @Cacheable("courses")
    public List<Course> getAllCourses() {
        try {
            logger.info("Fetching all courses");
            
            List<Course> databaseCourses = courseRepository.findAll();
            logger.info("Found {} courses in database", databaseCourses.size());
            
            // Apply MCDM ranking
            List<Course> rankedCourses = mcdmService.rankCourses(databaseCourses, Algorithm.TOPSIS, null);
            logger.info("Applied MCDM ranking to courses");
            
            return rankedCourses;
            
        } catch (Exception e) {
            logger.error("Error occurred while fetching all courses", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get courses with pagination and sorting
     */
    public Page<Course> getCoursesWithPagination(int page, int size, String sortBy, String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Course> coursePage = courseRepository.findAll(pageable);
            
            // Apply ranking to the current page
            List<Course> rankedCourses = mcdmService.rankCourses(coursePage.getContent(), Algorithm.TOPSIS, null);
            coursePage = coursePage.map(course -> rankedCourses.stream()
                .filter(c -> c.getId().equals(course.getId()))
                .findFirst()
                .orElse(course));
            
            return coursePage;
            
        } catch (Exception e) {
            logger.error("Error occurred while fetching courses with pagination", e);
            return Page.empty();
        }
    }
    
    /**
     * Advanced search with multiple filters
     */
    @Cacheable(value = "searchResults", key = "#keyword + #filters.hashCode()")
    public List<Course> advancedSearch(String keyword, Map<String, Object> filters, User user) {
        try {
            logger.info("Advanced search with keyword: {} and filters: {}", keyword, filters);
            
            List<Course> results = new ArrayList<>();
            
            // Apply filters
            if (filters != null && !filters.isEmpty()) {
                results = applyFilters(filters);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                results = searchCourses(keyword, user);
            } else {
                results = getAllCourses();
            }
            
            // Apply personalized ranking if user is logged in
            Algorithm algorithm = Algorithm.TOPSIS;
            if (user != null) {
                algorithm = Algorithm.PERSONALIZED;
            }
            
            List<Course> rankedResults = mcdmService.rankCourses(results, algorithm, user);
            
            // Limit results
            return rankedResults.stream()
                .limit(SEARCH_RESULTS_LIMIT)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error occurred during advanced search", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Search courses with keyword
     */
    @Cacheable(value = "searchResults", key = "#keyword")
    public List<Course> searchCourses(String keyword, User user) {
        try {
            logger.info("Searching courses with keyword: {}", keyword);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllCourses();
            }
            
            // Search in database
            List<Course> databaseCourses = courseRepository.searchCourses(keyword, PageRequest.of(0, 100)).getContent();
            logger.info("Found {} courses in database matching keyword", databaseCourses.size());
            
            // Search live courses
            List<Course> liveCourses = liveCourseService.searchLiveCourses(keyword);
            logger.info("Found {} live courses matching keyword", liveCourses.size());
            
            // Combine and remove duplicates
            List<Course> allCourses = new ArrayList<>(databaseCourses);
            allCourses.addAll(liveCourses);
            
            Set<String> seen = new HashSet<>();
            allCourses.removeIf(course -> !seen.add(course.getTitle() + "|" + course.getPlatform()));
            
            // Validate course URLs to ensure they are accessible
            allCourses = allCourses.stream()
                .filter(courseValidationService::validateCourseData)
                .filter(courseValidationService::isFromTrustedPlatform)
                .collect(Collectors.toList());
            
            // Apply ranking
            Algorithm algorithm = user != null ? Algorithm.PERSONALIZED : Algorithm.TOPSIS;
            List<Course> rankedCourses = mcdmService.rankCourses(allCourses, algorithm, user);
            
            return rankedCourses.stream()
                .limit(TOP_COURSES_LIMIT)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error occurred while searching courses with keyword: {}", keyword, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Apply advanced filters to courses
     */
    private List<Course> applyFilters(Map<String, Object> filters) {
        try {
            Double minPrice = (Double) filters.get("minPrice");
            Double maxPrice = (Double) filters.get("maxPrice");
            Double minRating = (Double) filters.get("minRating");
            String platform = (String) filters.get("platform");
            String topic = (String) filters.get("topic");
            String difficulty = (String) filters.get("difficulty");
            String language = (String) filters.get("language");
            Boolean hasCertificate = (Boolean) filters.get("hasCertificate");
            
            // Build dynamic query
            StringBuilder queryBuilder = new StringBuilder();
            Map<String, Object> params = new HashMap<>();
            
            if (minPrice != null) {
                queryBuilder.append("c.price >= :minPrice AND ");
                params.put("minPrice", minPrice);
            }
            
            if (maxPrice != null) {
                queryBuilder.append("c.price <= :maxPrice AND ");
                params.put("maxPrice", maxPrice);
            }
            
            if (minRating != null) {
                queryBuilder.append("c.rating >= :minRating AND ");
                params.put("minRating", minRating);
            }
            
            if (platform != null && !platform.trim().isEmpty()) {
                queryBuilder.append("LOWER(c.platform) = LOWER(:platform) AND ");
                params.put("platform", platform);
            }
            
            if (topic != null && !topic.trim().isEmpty()) {
                queryBuilder.append("LOWER(c.topic) LIKE LOWER(CONCAT('%', :topic, '%')) AND ");
                params.put("topic", topic);
            }
            
            if (difficulty != null && !difficulty.trim().isEmpty()) {
                queryBuilder.append("c.difficultyLevel = :difficulty AND ");
                params.put("difficulty", difficulty);
            }
            
            if (language != null && !language.trim().isEmpty()) {
                queryBuilder.append("c.language = :language AND ");
                params.put("language", language);
            }
            
            if (hasCertificate != null) {
                queryBuilder.append("c.hasCertificate = :hasCertificate AND ");
                params.put("hasCertificate", hasCertificate);
            }
            
            // Remove trailing AND
            if (queryBuilder.length() > 0) {
                queryBuilder.setLength(queryBuilder.length() - 5);
            }
            
            // Execute query
            if (queryBuilder.length() > 0) {
                return courseRepository.findCoursesByAdvancedFilters(
                    minPrice, maxPrice, minRating, platform, topic, difficulty, language, hasCertificate);
            } else {
                return courseRepository.findAll();
            }
            
        } catch (Exception e) {
            logger.error("Error applying filters", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get courses by provider/platform
     */
    public List<Course> getCoursesByProvider(String provider, User user) {
        try {
            if (provider == null || provider.trim().isEmpty()) {
                logger.warn("Provider parameter is null or empty");
                return Collections.emptyList();
            }
            
            List<Course> courses = courseRepository.findByPlatformIgnoreCase(provider);
            Algorithm algorithm = user != null ? Algorithm.PERSONALIZED : Algorithm.TOPSIS;
            return mcdmService.rankCourses(courses, algorithm, user);
            
        } catch (Exception e) {
            logger.error("Error occurred while fetching courses for provider: {}", provider, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get courses by IDs
     */
    public List<Course> getCoursesByIds(List<Long> ids, User user) {
        try {
            if (ids == null || ids.isEmpty()) {
                logger.warn("Course IDs list is null or empty");
                return Collections.emptyList();
            }
            
            List<Course> courses = courseRepository.findAllById(ids);
            Algorithm algorithm = user != null ? Algorithm.PERSONALIZED : Algorithm.TOPSIS;
            return mcdmService.rankCourses(courses, algorithm, user);
            
        } catch (Exception e) {
            logger.error("Error occurred while fetching courses by IDs: {}", ids, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Rank courses using specified algorithm
     */
    public List<Course> rankCourses(List<Course> courses, Algorithm algorithm, User user) {
        try {
            if (courses == null || courses.isEmpty()) {
                logger.warn("Courses list is null or empty");
                return Collections.emptyList();
            }
            return mcdmService.rankCourses(courses, algorithm, user);
            
        } catch (Exception e) {
            logger.error("Error occurred while ranking courses", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Search courses by topic
     */
    public List<Course> searchCoursesByTopic(String topic, User user) {
        try {
            if (topic == null || topic.trim().isEmpty()) {
                logger.warn("Topic parameter is null or empty");
                return Collections.emptyList();
            }
            
            List<Course> courses = courseRepository.findByTopicOrderByRatingAndStudents(topic, PageRequest.of(0, 100)).getContent();
            Algorithm algorithm = user != null ? Algorithm.PERSONALIZED : Algorithm.TOPSIS;
            List<Course> rankedCourses = mcdmService.rankCourses(courses, algorithm, user);
            
            return rankedCourses.stream()
                .limit(TOP_COURSES_LIMIT)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error occurred while searching courses by topic: {}", topic, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get courses by platform
     */
    public List<Course> getCoursesByPlatform(String platform, User user) {
        try {
            if (platform == null || platform.trim().isEmpty()) {
                logger.warn("Platform parameter is null or empty");
                return Collections.emptyList();
            }
            
            List<Course> courses = courseRepository.findByPlatformIgnoreCase(platform);
            Algorithm algorithm = user != null ? Algorithm.PERSONALIZED : Algorithm.TOPSIS;
            List<Course> rankedCourses = mcdmService.rankCourses(courses, algorithm, user);
            
            return rankedCourses.stream()
                .limit(TOP_COURSES_LIMIT)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error occurred while fetching courses for platform: {}", platform, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get all topics
     */
    public List<String> getAllTopics() {
        try {
            return courseRepository.findAllTopics();
        } catch (Exception e) {
            logger.error("Error occurred while fetching all topics", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get course by ID
     */
    public Optional<Course> getCourseById(Long id) {
        logger.info("Fetching course by ID: {}", id);
        return courseRepository.findById(id);
    }
    
    /**
     * Get course recommendations for user
     */
    public List<Course> getCourseRecommendations(User user, int limit) {
        try {
            if (user == null) {
                return getTopRatedCourses(limit);
            }
            
            // Get courses matching user interests
            List<Course> interestBasedCourses = new ArrayList<>();
            if (user.getInterests() != null) {
                for (String interest : user.getInterests()) {
                    interestBasedCourses.addAll(searchCoursesByTopic(interest, user));
                }
            }
            
            // Remove duplicates and apply personalized ranking
            Set<Long> seenIds = new HashSet<>();
            List<Course> uniqueCourses = interestBasedCourses.stream()
                .filter(course -> seenIds.add(course.getId()))
                .collect(Collectors.toList());
            
            List<Course> rankedCourses = mcdmService.rankCourses(uniqueCourses, Algorithm.PERSONALIZED, user);
            
            return rankedCourses.stream()
                .limit(limit)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error getting course recommendations", e);
            return getTopRatedCourses(limit);
        }
    }
    
    /**
     * Get top rated courses
     */
    private List<Course> getTopRatedCourses(int limit) {
        try {
            List<Course> allCourses = getAllCourses();
            return allCourses.stream()
                .sorted(Comparator.comparingDouble(Course::getRating).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting top rated courses", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Clear cache
     */
    @CacheEvict(value = {"courses", "searchResults"}, allEntries = true)
    public void clearCache() {
        logger.info("Cache cleared");
    }
} 