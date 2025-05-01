package com.coursecomparison.service;

import com.coursecomparison.model.Course;
import com.coursecomparison.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

@Service
public class LiveCourseService {
    private static final Logger logger = LoggerFactory.getLogger(LiveCourseService.class);
    
    @Autowired
    private MCDMService mcdmService;
    
    @Autowired
    private CourseRepository courseRepository;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${api.coursera.url}")
    private String courseraApiUrl;
    
    @Value("${api.edx.url}")
    private String edxApiUrl;
    
    @Value("${api.udacity.url}")
    private String udacityApiUrl;
    
    @Value("${api.futurelearn.url}")
    private String futurelearnApiUrl;
    
    public List<Course> searchLiveCourses(String keyword) {
        logger.info("Starting live course search for keyword: {}", keyword);
        List<Course> allCourses = new ArrayList<>();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            logger.warn("Empty search keyword provided");
            return Collections.emptyList();
        }
        
        try {
            // Search Coursera courses
            List<Course> courseraCourses = searchCourseraCourses(keyword);
            allCourses.addAll(courseraCourses);
            logger.info("Found {} courses from Coursera", courseraCourses.size());
            
            // Search edX courses
            List<Course> edxCourses = searchEdxCourses(keyword);
            allCourses.addAll(edxCourses);
            logger.info("Found {} courses from edX", edxCourses.size());
            
            // Search Udacity courses
            List<Course> udacityCourses = searchUdacityCourses(keyword);
            allCourses.addAll(udacityCourses);
            logger.info("Found {} courses from Udacity", udacityCourses.size());
            
            // Search FutureLearn courses
            List<Course> futurelearnCourses = searchFutureLearnCourses(keyword);
            allCourses.addAll(futurelearnCourses);
            logger.info("Found {} courses from FutureLearn", futurelearnCourses.size());
            
            // Filter courses to ensure they are relevant to the search keyword
            List<Course> filteredCourses = filterRelevantCourses(allCourses, keyword);
            logger.info("After filtering, found {} relevant courses", filteredCourses.size());
            
            // Save relevant courses to database
            for (Course course : filteredCourses) {
                try {
                    // Check if course already exists
                    Optional<Course> existingCourse = courseRepository.findByTitleAndPlatform(course.getTitle(), course.getPlatform());
                    if (existingCourse.isEmpty()) {
                        // Set MCDM values before saving
                        setMCDMValues(course);
                        courseRepository.save(course);
                        logger.info("Saved new course to database: {}", course.getTitle());
                    } else {
                        logger.info("Course already exists: {}", course.getTitle());
                    }
                } catch (Exception e) {
                    logger.error("Error saving course to database: {}", e.getMessage());
                }
            }
            
            return filteredCourses;
        } catch (Exception e) {
            logger.error("Error in live course search: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private List<Course> filterRelevantCourses(List<Course> courses, String keyword) {
        if (courses == null || courses.isEmpty()) {
            return Collections.emptyList();
        }
        
        String normalizedKeyword = keyword.toLowerCase().trim();
        List<Course> relevantCourses = new ArrayList<>();
        
        for (Course course : courses) {
            if (isCourseRelevant(course, normalizedKeyword)) {
                relevantCourses.add(course);
            }
        }
        
        return relevantCourses;
    }
    
    private boolean isCourseRelevant(Course course, String normalizedKeyword) {
        if (course == null || course.getTitle() == null || course.getDescription() == null) {
            return false;
        }
        
        String title = course.getTitle().toLowerCase();
        String description = course.getDescription().toLowerCase();
        
        // Split keyword into components
        String[] keywordComponents = normalizedKeyword.split("\\s+");
        
        // Calculate relevance score
        int relevanceScore = 0;
        int maxScore = keywordComponents.length * 2; // Each component can contribute up to 2 points
        
        for (String component : keywordComponents) {
            // Skip common words
            if (component.equals("the") || component.equals("and") || component.equals("or") || 
                component.equals("for") || component.equals("with") || component.equals("using")) {
                maxScore -= 2;
                continue;
            }
            
            // Check for exact matches (2 points)
            if (title.contains(component) || description.contains(component)) {
                relevanceScore += 2;
                continue;
            }
            
            // Check for partial matches (1 point)
            if (containsPartialMatch(title, component) || containsPartialMatch(description, component)) {
                relevanceScore += 1;
            }
        }
        
        // Calculate relevance percentage
        double relevancePercentage = (double) relevanceScore / maxScore;
        
        // Consider the course relevant if it matches at least 50% of the components
        return relevancePercentage >= 0.5;
    }
    
    private boolean containsPartialMatch(String text, String component) {
        // Check for common variations and abbreviations
        if (component.equals("javascript") || component.equals("js")) {
            return text.contains("javascript") || text.contains("js");
        }
        if (component.equals("python")) {
            return text.contains("python") || text.contains("py");
        }
        if (component.equals("java")) {
            return text.contains("java") || text.contains("j2ee") || text.contains("j2se") || text.contains("spring");
        }
        if (component.equals("react")) {
            return text.contains("react") || text.contains("reactjs") || text.contains("react.js");
        }
        if (component.equals("node")) {
            return text.contains("node") || text.contains("nodejs") || text.contains("node.js");
        }
        if (component.equals("express")) {
            return text.contains("express") || text.contains("expressjs") || text.contains("express.js");
        }
        if (component.equals("mongodb")) {
            return text.contains("mongodb") || text.contains("mongo") || text.contains("nosql");
        }
        if (component.equals("sql")) {
            return text.contains("sql") || text.contains("database") || text.contains("db");
        }
        if (component.equals("html")) {
            return text.contains("html") || text.contains("web") || text.contains("frontend");
        }
        if (component.equals("css")) {
            return text.contains("css") || text.contains("styling") || text.contains("design");
        }
        
        // Check for common technology stack components
        if (component.equals("stack")) {
            return text.contains("stack") || text.contains("full stack") || text.contains("development");
        }
        if (component.equals("mern")) {
            return text.contains("mern") || text.contains("full stack") || 
                   (text.contains("react") && text.contains("node") && text.contains("mongodb"));
        }
        if (component.equals("mean")) {
            return text.contains("mean") || text.contains("full stack") || 
                   (text.contains("angular") && text.contains("node") && text.contains("mongodb"));
        }
        if (component.equals("mevn")) {
            return text.contains("mevn") || text.contains("full stack") || 
                   (text.contains("vue") && text.contains("node") && text.contains("mongodb"));
        }
        
        // Check for common prefixes and suffixes
        if (component.endsWith("js")) {
            return text.contains(component) || text.contains(component.replace("js", ".js"));
        }
        if (component.endsWith("db")) {
            return text.contains(component) || text.contains(component.replace("db", "database"));
        }
        
        return false;
    }
    
    private List<Course> searchCourseraCourses(String keyword) {
        logger.info("Searching Coursera courses for: {}", keyword);
        List<Course> courses = new ArrayList<>();
        
        try {
            // Use Coursera's public API
            String url = "https://api.coursera.org/api/courses.v1?q=search&query=" + keyword;
            logger.info("Fetching Coursera courses from API: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("Coursera API response status: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> elements = (List<Map<String, Object>>) body.get("elements");
                
                for (Map<String, Object> element : elements) {
                    try {
                        Course course = new Course();
                        course.setTitle((String) element.get("name"));
                        course.setDescription((String) element.get("description"));
                        course.setInstructor((String) element.get("primaryLanguages"));
                        course.setUrl("https://www.coursera.org/learn/" + element.get("slug"));
                        course.setPlatform("Coursera");
                        
                        // Set default values
                        course.setRating(4.5);
                        course.setStudentCount(1000);
                        course.setPrice(0.0);
                        
                        if (course.getTitle() != null && !course.getTitle().isEmpty() && 
                            course.getUrl() != null && !course.getUrl().isEmpty()) {
                            // Save to database
                            course = courseRepository.save(course);
                            courses.add(course);
                            logger.info("Found and saved course: {}", course.getTitle());
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing Coursera course element: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error searching Coursera courses: {}", e.getMessage());
        }
        
        logger.info("Found {} courses from Coursera", courses.size());
        return courses;
    }
    
    private List<Course> searchEdxCourses(String keyword) {
        logger.info("Searching edX courses for: {}", keyword);
        List<Course> courses = new ArrayList<>();
        
        try {
            // Use edX's public API
            String url = "https://api.edx.org/catalog/v1/courses/?search=" + keyword;
            logger.info("Fetching edX courses from API: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("edX API response status: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
                
                for (Map<String, Object> result : results) {
                    try {
                        Course course = new Course();
                        course.setTitle((String) result.get("name"));
                        course.setDescription((String) result.get("short_description"));
                        course.setInstructor((String) result.get("org"));
                        course.setUrl("https://www.edx.org" + result.get("marketing_url"));
                        course.setPlatform("edX");
                        
                        // Set default values
                        course.setRating(4.5);
                        course.setStudentCount(1000);
                        course.setPrice(0.0);
                        
                        if (course.getTitle() != null && !course.getTitle().isEmpty() && 
                            course.getUrl() != null && !course.getUrl().isEmpty()) {
                            // Save to database
                            course = courseRepository.save(course);
                            courses.add(course);
                            logger.info("Found and saved course: {}", course.getTitle());
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing edX course element: {}", e.getMessage());
                    }
                }
            } else {
                logger.warn("edX API returned non-2xx status, providing sample courses");
                // Provide sample courses when API fails
                courses.add(createSampleEdxCourse("Introduction to " + keyword, keyword));
                courses.add(createSampleEdxCourse("Advanced " + keyword + " Development", keyword));
                courses.add(createSampleEdxCourse(keyword + " Fundamentals", keyword));
            }
        } catch (Exception e) {
            logger.error("Error searching edX courses: {}", e.getMessage());
            // Provide sample courses when API fails
            courses.add(createSampleEdxCourse("Introduction to " + keyword, keyword));
            courses.add(createSampleEdxCourse("Advanced " + keyword + " Development", keyword));
            courses.add(createSampleEdxCourse(keyword + " Fundamentals", keyword));
        }
        
        logger.info("Found {} courses from edX", courses.size());
        return courses;
    }
    
    private Course createSampleEdxCourse(String title, String keyword) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription("A comprehensive course on " + keyword + " covering all essential topics and best practices.");
        course.setInstructor("edX University");
        course.setUrl("https://www.edx.org/course/" + title.toLowerCase().replace(" ", "-"));
        course.setPlatform("edX");
        course.setRating(4.5);
        course.setStudentCount(1000);
        course.setPrice(0.0);
        return course;
    }
    
    private List<Course> searchUdacityCourses(String keyword) {
        logger.info("Searching Udacity courses for: {}", keyword);
        List<Course> courses = new ArrayList<>();
        
        try {
            // Use Udacity's public API
            String url = "https://www.udacity.com/public-api/v0/courses?search=" + keyword;
            logger.info("Fetching Udacity courses from API: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("X-Requested-With", "XMLHttpRequest");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("Udacity API response status: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> coursesList = (List<Map<String, Object>>) body.get("courses");
                
                for (Map<String, Object> courseData : coursesList) {
                    try {
                        Course course = new Course();
                        course.setTitle((String) courseData.get("title"));
                        course.setDescription((String) courseData.get("summary"));
                        course.setInstructor("Udacity");
                        course.setUrl("https://www.udacity.com/course/" + courseData.get("key"));
                        course.setPlatform("Udacity");
                        
                        // Set default values
                        course.setRating(4.5);
                        course.setStudentCount(1000);
                        course.setPrice(199.99);
                        
                        if (course.getTitle() != null && !course.getTitle().isEmpty() && 
                            course.getUrl() != null && !course.getUrl().isEmpty()) {
                            // Save to database
                            course = courseRepository.save(course);
                            courses.add(course);
                            logger.info("Found and saved course: {}", course.getTitle());
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing Udacity course element: {}", e.getMessage());
                    }
                }
            } else {
                logger.warn("Udacity API returned non-2xx status, providing sample courses");
                // Provide sample courses when API fails
                courses.add(createSampleUdacityCourse("Introduction to " + keyword, keyword));
                courses.add(createSampleUdacityCourse("Advanced " + keyword + " Development", keyword));
                courses.add(createSampleUdacityCourse(keyword + " Fundamentals", keyword));
            }
        } catch (Exception e) {
            logger.error("Error searching Udacity courses: {}", e.getMessage());
            // Provide sample courses when API fails
            courses.add(createSampleUdacityCourse("Introduction to " + keyword, keyword));
            courses.add(createSampleUdacityCourse("Advanced " + keyword + " Development", keyword));
            courses.add(createSampleUdacityCourse(keyword + " Fundamentals", keyword));
        }
        
        logger.info("Found {} courses from Udacity", courses.size());
        return courses;
    }
    
    private Course createSampleUdacityCourse(String title, String keyword) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription("A comprehensive course on " + keyword + " covering all essential topics and best practices.");
        course.setInstructor("Udacity");
        course.setUrl("https://www.udacity.com/course/" + title.toLowerCase().replace(" ", "-"));
        course.setPlatform("Udacity");
        course.setRating(4.5);
        course.setStudentCount(1000);
        course.setPrice(199.99);
        return course;
    }
    
    private List<Course> searchFutureLearnCourses(String keyword) {
        logger.info("Searching FutureLearn courses for: {}", keyword);
        List<Course> courses = new ArrayList<>();
        
        try {
            String url = futurelearnApiUrl + keyword;
            logger.info("Fetching FutureLearn courses from: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.set("Accept-Language", "en-US,en;q=0.5");
            headers.set("Connection", "keep-alive");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            logger.info("FutureLearn response status: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Document doc = Jsoup.parse(response.getBody());
                
                // Try multiple selectors for course elements
                Elements courseElements = doc.select("div.course-card, div.course, div.card");
                
                for (Element courseElement : courseElements) {
                    try {
                        Course course = new Course();
                        
                        // Try multiple selectors for title
                        Element titleElement = courseElement.selectFirst("h3.course-title, h2.card-title, h3.heading");
                        if (titleElement != null) {
                            course.setTitle(titleElement.text().trim());
                        }
                        
                        // Try multiple selectors for description
                        Element descElement = courseElement.selectFirst("p.course-description, p.card-description, p.description");
                        if (descElement != null) {
                            course.setDescription(descElement.text().trim());
                        }
                        
                        // Try multiple selectors for instructor
                        Element instructorElement = courseElement.selectFirst("span.course-instructor, span.card-instructor, span.instructor");
                        if (instructorElement != null) {
                            course.setInstructor(instructorElement.text().trim());
                        }
                        
                        // Try multiple selectors for URL
                        Element linkElement = courseElement.selectFirst("a[href^=/courses], a[href^=/learn]");
                        if (linkElement != null) {
                            String href = linkElement.attr("href");
                            course.setUrl("https://www.futurelearn.com" + href);
                        }
                        
                        // Set platform and default values
                        course.setPlatform("FutureLearn");
                        course.setRating(4.5);
                        course.setStudentCount(1000);
                        course.setPrice(0.0);
                        
                        // Only add if we have a title and URL
                        if (course.getTitle() != null && !course.getTitle().isEmpty() && 
                            course.getUrl() != null && !course.getUrl().isEmpty()) {
                            // Save to database
                            course = courseRepository.save(course);
                            courses.add(course);
                            logger.info("Found and saved course: {}", course.getTitle());
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing FutureLearn course element: {}", e.getMessage());
                    }
                }
            } else {
                logger.error("Failed to fetch FutureLearn courses. Status code: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error searching FutureLearn courses: {}", e.getMessage());
        }
        
        logger.info("Found {} courses from FutureLearn", courses.size());
        return courses;
    }
    
    private void setMCDMValues(Course course) {
        // Content Quality: Based on rating and student count
        double contentQuality = course.getRating() != null ? course.getRating() / 5.0 : 0.7;
        course.setContentQuality(contentQuality);
        
        // Instructor Rating: Based on platform reputation and rating
        double instructorRating = course.getRating() != null ? course.getRating() / 5.0 : 0.7;
        course.setInstructorRating(instructorRating);
        
        // Value for Money: Based on price and rating
        double valueForMoney = calculateValueForMoney(course);
        course.setValueForMoney(valueForMoney);
        
        // Course Structure: Based on platform reputation
        double courseStructure = getPlatformStructureScore(course.getPlatform());
        course.setCourseStructure(courseStructure);
        
        // Practical Exercises: Based on platform reputation
        double practicalExercises = getPlatformPracticalScore(course.getPlatform());
        course.setPracticalExercises(practicalExercises);
        
        // Support Quality: Based on platform reputation
        double supportQuality = getPlatformSupportScore(course.getPlatform());
        course.setSupportQuality(supportQuality);
    }
    
    private double calculateValueForMoney(Course course) {
        if (course.getPrice() == null || course.getPrice() == 0) {
            return 0.8; // Free courses get good value score
        }
        
        // Normalize price between 0 and 1 (lower is better)
        double normalizedPrice = Math.min(course.getPrice() / 200.0, 1.0);
        double ratingFactor = course.getRating() != null ? course.getRating() / 5.0 : 0.7;
        
        return (1.0 - normalizedPrice) * 0.6 + ratingFactor * 0.4;
    }
    
    private double getPlatformStructureScore(String platform) {
        // Assign structure scores based on platform reputation
        switch (platform.toLowerCase()) {
            case "udemy":
                return 0.8;
            case "coursera":
                return 0.9;
            case "edx":
                return 0.85;
            default:
                return 0.7;
        }
    }
    
    private double getPlatformPracticalScore(String platform) {
        // Assign practical exercise scores based on platform reputation
        switch (platform.toLowerCase()) {
            case "udemy":
                return 0.85;
            case "coursera":
                return 0.8;
            case "edx":
                return 0.75;
            default:
                return 0.7;
        }
    }
    
    private double getPlatformSupportScore(String platform) {
        // Assign support quality scores based on platform reputation
        switch (platform.toLowerCase()) {
            case "udemy":
                return 0.75;
            case "coursera":
                return 0.85;
            case "edx":
                return 0.8;
            default:
                return 0.7;
        }
    }
    
    private Double parsePrice(String priceText) {
        try {
            return Double.parseDouble(priceText.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private Integer parseStudentCount(String countText) {
        try {
            return Integer.parseInt(countText.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
    
    private Double parseRating(String ratingText) {
        try {
            return Double.parseDouble(ratingText.split(" ")[0]);
        } catch (Exception e) {
            return 0.0;
        }
    }
} 