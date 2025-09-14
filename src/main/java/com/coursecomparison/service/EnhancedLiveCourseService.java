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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class EnhancedLiveCourseService {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedLiveCourseService.class);
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private CourseValidationService courseValidationService;
    
    @Value("${live.fetch.enabled:true}")
    private boolean liveFetchEnabled;
    
    /**
     * Enhanced search for live courses with multiple fallback strategies
     */
    public List<Course> searchLiveCoursesEnhanced(String keyword) {
        logger.info("Starting enhanced live course search for keyword: {}", keyword);
        
        if (!liveFetchEnabled) {
            logger.warn("Live fetch disabled by configuration. Returning no live courses.");
            return Collections.emptyList();
        }
        
        List<Course> allCourses = new ArrayList<>();
        
        try {
            // Try multiple strategies for each platform
            allCourses.addAll(searchCourseraEnhanced(keyword));
            allCourses.addAll(searchUdemyEnhanced(keyword));
            allCourses.addAll(searchEdXEnhanced(keyword));
            allCourses.addAll(searchUdacityEnhanced(keyword));
            allCourses.addAll(searchFutureLearnEnhanced(keyword));
            
            // Filter and validate courses
            List<Course> validCourses = allCourses.stream()
                .filter(course -> courseValidationService.validateCourseData(course))
                .collect(Collectors.toList());
            
            logger.info("Enhanced search completed. Found {} valid courses from {} total", 
                       validCourses.size(), allCourses.size());
            
            return validCourses;
            
        } catch (Exception e) {
            logger.error("Error in enhanced live course search: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Enhanced Coursera search with multiple strategies
     */
    private List<Course> searchCourseraEnhanced(String keyword) {
        List<Course> courses = new ArrayList<>();
        
        try {
            // Strategy 1: Try official API
            courses.addAll(searchCourseraAPI(keyword));
            
            // Strategy 2: If API fails, try web scraping
            if (courses.isEmpty()) {
                courses.addAll(searchCourseraWeb(keyword));
            }
            
            // Strategy 3: Try alternative search endpoints
            if (courses.isEmpty()) {
                courses.addAll(searchCourseraAlternative(keyword));
            }
            
        } catch (Exception e) {
            logger.warn("All Coursera strategies failed: {}", e.getMessage());
        }
        
        return courses;
    }
    
    private List<Course> searchCourseraAPI(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://api.coursera.org/api/courses.v1?q=search&query=" + encodedKeyword;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseCourseraAPIResponse(response.getBody(), keyword);
            }
            
        } catch (Exception e) {
            logger.debug("Coursera API failed: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }
    
    private List<Course> searchCourseraWeb(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://www.coursera.org/search?query=" + encodedKeyword;
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
            
            return parseCourseraWebResponse(doc, keyword);
            
        } catch (Exception e) {
            logger.debug("Coursera web scraping failed: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }
    
    private List<Course> searchCourseraAlternative(String keyword) {
        try {
            // Try different Coursera search endpoints
            String[] alternativeUrls = {
                "https://www.coursera.org/courses?query=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8),
                "https://www.coursera.org/search?query=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8) + "&indices=prod_all_products_term_optimization"
            };
            
            for (String url : alternativeUrls) {
                try {
                    Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(10000)
                        .get();
                    
                    List<Course> courses = parseCourseraWebResponse(doc, keyword);
                    if (!courses.isEmpty()) {
                        return courses;
                    }
                } catch (Exception e) {
                    logger.debug("Alternative Coursera URL failed: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.debug("Coursera alternative search failed: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Enhanced Udemy search with multiple strategies
     */
    private List<Course> searchUdemyEnhanced(String keyword) {
        List<Course> courses = new ArrayList<>();
        
        try {
            // Strategy 1: Try web scraping (most reliable for Udemy)
            courses.addAll(searchUdemyWeb(keyword));
            
            // Strategy 2: Try different search variations
            if (courses.isEmpty()) {
                courses.addAll(searchUdemyVariations(keyword));
            }
            
        } catch (Exception e) {
            logger.warn("All Udemy strategies failed: {}", e.getMessage());
        }
        
        return courses;
    }
    
    private List<Course> searchUdemyWeb(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://www.udemy.com/courses/search/?q=" + encodedKeyword;
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();
            
            return parseUdemyWebResponse(doc, keyword);
            
        } catch (Exception e) {
            logger.debug("Udemy web scraping failed: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }
    
    private List<Course> searchUdemyVariations(String keyword) {
        List<Course> courses = new ArrayList<>();
        
        // Try different keyword variations
        String[] variations = {
            keyword,
            keyword + " course",
            keyword + " tutorial",
            keyword + " training"
        };
        
        for (String variation : variations) {
            try {
                courses.addAll(searchUdemyWeb(variation));
                if (!courses.isEmpty()) {
                    break; // Found results, stop trying variations
                }
            } catch (Exception e) {
                logger.debug("Udemy variation '{}' failed: {}", variation, e.getMessage());
            }
        }
        
        return courses;
    }
    
    /**
     * Enhanced edX search
     */
    private List<Course> searchEdXEnhanced(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://www.edx.org/search?q=" + encodedKeyword;
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
            
            return parseEdXWebResponse(doc, keyword);
            
        } catch (Exception e) {
            logger.debug("edX search failed: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Enhanced Udacity search
     */
    private List<Course> searchUdacityEnhanced(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://www.udacity.com/courses/all?search=" + encodedKeyword;
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
            
            return parseUdacityWebResponse(doc, keyword);
            
        } catch (Exception e) {
            logger.debug("Udacity search failed: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Enhanced FutureLearn search
     */
    private List<Course> searchFutureLearnEnhanced(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://www.futurelearn.com/search?q=" + encodedKeyword;
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
            
            return parseFutureLearnWebResponse(doc, keyword);
            
        } catch (Exception e) {
            logger.debug("FutureLearn search failed: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }
    
    // Parsing methods for each platform
    private List<Course> parseCourseraAPIResponse(Map<String, Object> response, String keyword) {
        List<Course> courses = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> elements = (List<Map<String, Object>>) response.get("elements");
            
            if (elements != null) {
                for (Map<String, Object> element : elements) {
                    Course course = createCourseraCourseFromAPI(element);
                    if (course != null) {
                        courses.add(course);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.debug("Error parsing Coursera API response: {}", e.getMessage());
        }
        
        return courses;
    }
    
    private List<Course> parseCourseraWebResponse(Document doc, String keyword) {
        List<Course> courses = new ArrayList<>();
        
        try {
            Elements courseElements = doc.select("[data-testid='course-card'], .rc-CourseCard, .course-card");
            
            for (Element element : courseElements) {
                Course course = createCourseraCourseFromWeb(element);
                if (course != null) {
                    courses.add(course);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Error parsing Coursera web response: {}", e.getMessage());
        }
        
        return courses;
    }
    
    private List<Course> parseUdemyWebResponse(Document doc, String keyword) {
        List<Course> courses = new ArrayList<>();
        
        try {
            Elements courseElements = doc.select("[data-testid='course-card'], .course-card, .ud-component--course-card");
            
            for (Element element : courseElements) {
                Course course = createUdemyCourseFromWeb(element);
                if (course != null) {
                    courses.add(course);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Error parsing Udemy web response: {}", e.getMessage());
        }
        
        return courses;
    }
    
    private List<Course> parseEdXWebResponse(Document doc, String keyword) {
        List<Course> courses = new ArrayList<>();
        
        try {
            Elements courseElements = doc.select(".course-card, .discovery-card, [data-testid='course-card']");
            
            for (Element element : courseElements) {
                Course course = createEdXCourseFromWeb(element);
                if (course != null) {
                    courses.add(course);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Error parsing edX web response: {}", e.getMessage());
        }
        
        return courses;
    }
    
    private List<Course> parseUdacityWebResponse(Document doc, String keyword) {
        List<Course> courses = new ArrayList<>();
        
        try {
            Elements courseElements = doc.select(".course-card, .card, [data-testid='course-card']");
            
            for (Element element : courseElements) {
                Course course = createUdacityCourseFromWeb(element);
                if (course != null) {
                    courses.add(course);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Error parsing Udacity web response: {}", e.getMessage());
        }
        
        return courses;
    }
    
    private List<Course> parseFutureLearnWebResponse(Document doc, String keyword) {
        List<Course> courses = new ArrayList<>();
        
        try {
            Elements courseElements = doc.select(".course-card, .card, [data-testid='course-card']");
            
            for (Element element : courseElements) {
                Course course = createFutureLearnCourseFromWeb(element);
                if (course != null) {
                    courses.add(course);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Error parsing FutureLearn web response: {}", e.getMessage());
        }
        
        return courses;
    }
    
    // Course creation methods for each platform
    private Course createCourseraCourseFromAPI(Map<String, Object> element) {
        try {
            Course course = new Course();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> name = (Map<String, Object>) element.get("name");
            if (name != null) {
                course.setTitle((String) name.get("value"));
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> description = (Map<String, Object>) element.get("description");
            if (description != null) {
                course.setDescription((String) description.get("value"));
            }
            
            course.setPlatform("Coursera");
            course.setUrl("https://www.coursera.org/learn/" + element.get("slug"));
            course.setTopic("Programming"); // Default topic
            course.setDurationHours(40); // Default duration
            course.setRating(4.5); // Default rating
            course.setStudentCount(1000); // Default student count
            course.setPrice(0.0); // Default free
            course.setLanguage("English");
            course.setDifficultyLevel("Beginner");
            course.setIsActive(true);
            course.setLastUpdated(LocalDateTime.now());
            
            return course;
            
        } catch (Exception e) {
            logger.debug("Error creating Coursera course from API: {}", e.getMessage());
            return null;
        }
    }
    
    private Course createCourseraCourseFromWeb(Element element) {
        try {
            Course course = new Course();
            
            // Extract title
            Element titleElement = element.selectFirst("h3, .course-title, [data-testid='course-title']");
            if (titleElement != null) {
                course.setTitle(titleElement.text().trim());
            }
            
            // Extract description
            Element descElement = element.selectFirst(".course-description, .description, p");
            if (descElement != null) {
                course.setDescription(descElement.text().trim());
            }
            
            // Extract URL
            Element linkElement = element.selectFirst("a[href*='coursera.org']");
            if (linkElement != null) {
                String href = linkElement.attr("href");
                if (!href.startsWith("http")) {
                    href = "https://www.coursera.org" + href;
                }
                course.setUrl(href);
            }
            
            course.setPlatform("Coursera");
            course.setTopic("Programming");
            course.setDurationHours(40);
            course.setRating(4.5);
            course.setStudentCount(1000);
            course.setPrice(0.0);
            course.setLanguage("English");
            course.setDifficultyLevel("Beginner");
            course.setIsActive(true);
            course.setLastUpdated(LocalDateTime.now());
            
            return course;
            
        } catch (Exception e) {
            logger.debug("Error creating Coursera course from web: {}", e.getMessage());
            return null;
        }
    }
    
    private Course createUdemyCourseFromWeb(Element element) {
        try {
            Course course = new Course();
            
            // Extract title
            Element titleElement = element.selectFirst("h3, .course-title, [data-testid='course-title']");
            if (titleElement != null) {
                course.setTitle(titleElement.text().trim());
            }
            
            // Extract description
            Element descElement = element.selectFirst(".course-description, .description, p");
            if (descElement != null) {
                course.setDescription(descElement.text().trim());
            }
            
            // Extract URL
            Element linkElement = element.selectFirst("a[href*='udemy.com']");
            if (linkElement != null) {
                String href = linkElement.attr("href");
                if (!href.startsWith("http")) {
                    href = "https://www.udemy.com" + href;
                }
                course.setUrl(href);
            }
            
            course.setPlatform("Udemy");
            course.setTopic("Programming");
            course.setDurationHours(20);
            course.setRating(4.3);
            course.setStudentCount(500);
            course.setPrice(99.99);
            course.setLanguage("English");
            course.setDifficultyLevel("Beginner");
            course.setIsActive(true);
            course.setLastUpdated(LocalDateTime.now());
            
            return course;
            
        } catch (Exception e) {
            logger.debug("Error creating Udemy course from web: {}", e.getMessage());
            return null;
        }
    }
    
    private Course createEdXCourseFromWeb(Element element) {
        try {
            Course course = new Course();
            
            // Extract title
            Element titleElement = element.selectFirst("h3, .course-title, [data-testid='course-title']");
            if (titleElement != null) {
                course.setTitle(titleElement.text().trim());
            }
            
            // Extract description
            Element descElement = element.selectFirst(".course-description, .description, p");
            if (descElement != null) {
                course.setDescription(descElement.text().trim());
            }
            
            // Extract URL
            Element linkElement = element.selectFirst("a[href*='edx.org']");
            if (linkElement != null) {
                String href = linkElement.attr("href");
                if (!href.startsWith("http")) {
                    href = "https://www.edx.org" + href;
                }
                course.setUrl(href);
            }
            
            course.setPlatform("edX");
            course.setTopic("Programming");
            course.setDurationHours(60);
            course.setRating(4.4);
            course.setStudentCount(2000);
            course.setPrice(0.0);
            course.setLanguage("English");
            course.setDifficultyLevel("Beginner");
            course.setIsActive(true);
            course.setLastUpdated(LocalDateTime.now());
            
            return course;
            
        } catch (Exception e) {
            logger.debug("Error creating edX course from web: {}", e.getMessage());
            return null;
        }
    }
    
    private Course createUdacityCourseFromWeb(Element element) {
        try {
            Course course = new Course();
            
            // Extract title
            Element titleElement = element.selectFirst("h3, .course-title, [data-testid='course-title']");
            if (titleElement != null) {
                course.setTitle(titleElement.text().trim());
            }
            
            // Extract description
            Element descElement = element.selectFirst(".course-description, .description, p");
            if (descElement != null) {
                course.setDescription(descElement.text().trim());
            }
            
            // Extract URL
            Element linkElement = element.selectFirst("a[href*='udacity.com']");
            if (linkElement != null) {
                String href = linkElement.attr("href");
                if (!href.startsWith("http")) {
                    href = "https://www.udacity.com" + href;
                }
                course.setUrl(href);
            }
            
            course.setPlatform("Udacity");
            course.setTopic("Programming");
            course.setDurationHours(80);
            course.setRating(4.6);
            course.setStudentCount(1500);
            course.setPrice(399.0);
            course.setLanguage("English");
            course.setDifficultyLevel("Intermediate");
            course.setIsActive(true);
            course.setLastUpdated(LocalDateTime.now());
            
            return course;
            
        } catch (Exception e) {
            logger.debug("Error creating Udacity course from web: {}", e.getMessage());
            return null;
        }
    }
    
    private Course createFutureLearnCourseFromWeb(Element element) {
        try {
            Course course = new Course();
            
            // Extract title
            Element titleElement = element.selectFirst("h3, .course-title, [data-testid='course-title']");
            if (titleElement != null) {
                course.setTitle(titleElement.text().trim());
            }
            
            // Extract description
            Element descElement = element.selectFirst(".course-description, .description, p");
            if (descElement != null) {
                course.setDescription(descElement.text().trim());
            }
            
            // Extract URL
            Element linkElement = element.selectFirst("a[href*='futurelearn.com']");
            if (linkElement != null) {
                String href = linkElement.attr("href");
                if (!href.startsWith("http")) {
                    href = "https://www.futurelearn.com" + href;
                }
                course.setUrl(href);
            }
            
            course.setPlatform("FutureLearn");
            course.setTopic("Programming");
            course.setDurationHours(30);
            course.setRating(4.2);
            course.setStudentCount(800);
            course.setPrice(0.0);
            course.setLanguage("English");
            course.setDifficultyLevel("Beginner");
            course.setIsActive(true);
            course.setLastUpdated(LocalDateTime.now());
            
            return course;
            
        } catch (Exception e) {
            logger.debug("Error creating FutureLearn course from web: {}", e.getMessage());
            return null;
        }
    }
}
