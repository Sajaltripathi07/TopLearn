package com.coursecomparison.controller;

import com.coursecomparison.model.Course;
import com.coursecomparison.repository.CourseRepository;
import com.coursecomparison.service.MCDMService;
import com.coursecomparison.service.CourseService;
import com.coursecomparison.service.CourseValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    private static final List<String> TOPICS = Arrays.asList(
        "Java", "Python", "JavaScript", "C++", "Web Development",
        "Data Science", "Machine Learning", "Cloud Computing",
        "Mobile Development", "Database", "DevOps", "Cybersecurity"
    );

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MCDMService mcdmService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseValidationService courseValidationService;

    @GetMapping("/")
    public String home(Model model) {
        logger.info("Loading home page");
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("searchTerm", "");
        model.addAttribute("topics", TOPICS);
        return "index";
    }

    @GetMapping("/search")
    public String searchCourses(@RequestParam(required = false) String keyword, Model model) {
        logger.info("Searching courses with keyword: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/";
        }
        
        List<Course> courses = courseService.searchCourses(keyword,null);
        logger.info("Found {} courses for keyword: {}", courses.size(), keyword);
        
        model.addAttribute("courses", courses);
        model.addAttribute("searchTerm", keyword);
        model.addAttribute("topics", TOPICS);
        return "search-results";
    }

    @GetMapping("/platform/{platform}")
    public String platformCourses(@PathVariable String platform, Model model) {
        logger.info("Loading courses for platform: {}", platform);
        List<Course> courses = courseService.getCoursesByPlatform(platform,null);
        model.addAttribute("courses", courses);
        model.addAttribute("searchTerm", platform);
        model.addAttribute("topics", TOPICS);
        return "search-results";
    }

    @GetMapping("/course/{id}")
    public String viewCourseDetails(@PathVariable Long id, Model model) {
        logger.info("Viewing course details for ID: {}", id);
        
        try {
            Optional<Course> courseOptional = courseService.getCourseById(id);
            if (courseOptional.isPresent()) {
                Course course = courseOptional.get();
                // If the course has a valid external URL, redirect to the platform directly
                if (course.getUrl() != null && course.getUrl().startsWith("http") &&
                        courseValidationService.validateCourseUrl(course)) {
                    return "redirect:" + course.getUrl();
                }
                model.addAttribute("course", course);
                return "course-details";
            } else {
                logger.warn("Course not found with ID: {}", id);
                return "redirect:/";
            }
        } catch (Exception e) {
            logger.error("Error viewing course details: {}", e.getMessage());
            return "redirect:/";
        }
    }
    
    @GetMapping("/criteria")
    public String customizeCriteria(Model model) {
        logger.info("Loading criteria customization page");
        return "criteria-customization";
    }

    @GetMapping("/compare")
    public String comparePage(Model model) {
        logger.info("Loading compare page");
        return "compare";
    }
    
    @GetMapping("/api-docs")
    public String apiDocs(Model model) {
        logger.info("Loading API documentation page");
        return "api-docs";
    }
} 