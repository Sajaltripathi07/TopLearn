package com.coursecomparison.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
        if (isApiRequest(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Endpoint not found");
            response.put("message", "The requested URL does not exist.");
            response.put("available_endpoints", "/api/courses/search, /api/courses/platform/{platform}, /api/courses/topics, /api/courses/rank");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Page Not Found");
            mav.addObject("message", "The page you are looking for does not exist.");
            mav.addObject("details", "URL: " + ex.getRequestURL());
            return mav;
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingParameter(MissingServletRequestParameterException ex, WebRequest request) {
        if (isApiRequest(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Missing parameter");
            response.put("message", "Required parameter '" + ex.getParameterName() + "' is missing");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Missing Parameter");
            mav.addObject("message", "A required parameter is missing from your request.");
            mav.addObject("details", "Parameter: " + ex.getParameterName());
            return mav;
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        if (isApiRequest(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid parameter type");
            response.put("message", "Parameter '" + ex.getName() + "' must be of type " + ex.getRequiredType().getSimpleName());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Invalid Parameter Type");
            mav.addObject("message", "One of the parameters in your request has an invalid type.");
            mav.addObject("details", "Parameter: " + ex.getName() + ", Expected type: " + ex.getRequiredType());
            return mav;
        }
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(Exception ex, WebRequest request) {
        if (isApiRequest(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Internal server error");
            response.put("message", "An unexpected error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Internal Server Error");
            mav.addObject("message", "An unexpected error occurred while processing your request.");
            mav.addObject("details", ex.getMessage());
            return mav;
        }
    }

    private boolean isApiRequest(WebRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && 
               (acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE) || 
                request.getHeader("X-Requested-With") != null);
    }
} 