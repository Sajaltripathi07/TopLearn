/**
 * Course Comparison Platform - Main JavaScript
 * Enhanced functionality for modern user experience
 */

// Global configuration and state
const App = {
    config: {
        apiBaseUrl: '/api',
        animationDuration: 300,
        debounceDelay: 300
    },
    
    state: {
        currentUser: null,
        wishlist: new Set(),
        searchHistory: [],
        filters: {},
        currentPage: 1,
        isLoading: false
    },
    
    // Initialize the application
    init() {
        this.setupEventListeners();
        this.initializeComponents();
        this.loadUserPreferences();
        console.log('Course Comparison Platform initialized');
    },
    
    // Setup global event listeners
    setupEventListeners() {
        // Search functionality
        this.setupSearch();
        
        // Filter functionality
        this.setupFilters();
        
        // Wishlist functionality
        this.setupWishlist();
        
        // Course comparison
        this.setupComparison();
        
        // Responsive navigation
        this.setupResponsiveNav();
        
        // Smooth scrolling
        this.setupSmoothScrolling();
        
        // Intersection observer for animations
        this.setupAnimations();
    },
    
    // Initialize UI components
    initializeComponents() {
        // Initialize tooltips
        this.initTooltips();
        
        // Initialize modals
        this.initModals();
        
        // Initialize dropdowns
        this.initDropdowns();
        
        // Initialize progress bars
        this.initProgressBars();
        
        // Initialize charts if Chart.js is available
        if (typeof Chart !== 'undefined') {
            this.initCharts();
        }
    },
    
    // Search functionality
    setupSearch() {
        const searchForm = document.querySelector('.search-form');
        const searchInput = document.querySelector('.search-input');
        
        if (searchForm && searchInput) {
            // Debounced search
            let searchTimeout;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    this.performSearch(e.target.value);
                }, this.config.debounceDelay);
            });
            
            // Form submission
            searchForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.performSearch(searchInput.value);
            });
            
            // Search suggestions
            this.setupSearchSuggestions(searchInput);
        }
    },
    
    // Perform search with API call
    async performSearch(query) {
        if (!query.trim()) return;
        
        try {
            this.showLoading();
            
            const response = await fetch(`${this.config.apiBaseUrl}/courses/search?keyword=${encodeURIComponent(query)}`);
            const courses = await response.json();
            
            this.displaySearchResults(courses);
            this.addToSearchHistory(query);
            
        } catch (error) {
            console.error('Search error:', error);
            this.showError('Search failed. Please try again.');
        } finally {
            this.hideLoading();
        }
    },
    
    // Display search results
    displaySearchResults(courses) {
        const resultsContainer = document.querySelector('.search-results');
        if (!resultsContainer) return;
        
        if (courses.length === 0) {
            resultsContainer.innerHTML = `
                <div class="text-center py-5">
                    <i class="fas fa-search text-muted mb-3" style="font-size: 3rem;"></i>
                    <h4>No courses found</h4>
                    <p class="text-muted">Try adjusting your search terms or browse our course categories.</p>
                </div>
            `;
            return;
        }
        
        const coursesHTML = courses.map(course => this.renderCourseCard(course)).join('');
        resultsContainer.innerHTML = coursesHTML;
        
        // Add animation to new cards
        this.animateNewElements(resultsContainer.querySelectorAll('.course-card'));
    },
    
    // Render course card HTML
    renderCourseCard(course) {
        return `
            <div class="col-md-4 mb-4">
                <div class="card course-card h-100" data-course-id="${course.id}">
                    <img src="${course.courseImageUrl || '/images/course-placeholder.jpg'}" 
                         class="card-image" alt="${course.title}">
                    <div class="card-body">
                        <div class="course-header">
                            <span class="course-platform">${course.platform}</span>
                            ${course.difficultyLevel ? `<span class="course-difficulty">${course.difficultyLevel}</span>` : ''}
                        </div>
                        <h5 class="course-title">${course.title}</h5>
                        <p class="course-instructor">${course.instructor}</p>
                        <p class="course-description">${course.description}</p>
                        <div class="course-meta">
                            <div class="course-rating">
                                <i class="fas fa-star"></i>
                                <span>${course.rating}</span>
                            </div>
                            <div class="course-students">${course.studentCount} students</div>
                            <div class="course-price">$${course.price}</div>
                        </div>
                        <div class="course-actions">
                            <a href="${course.url}" class="btn btn-primary" target="_blank" rel="noopener noreferrer">
                                <i class="fas fa-external-link-alt me-2"></i>View on ${course.platform}
                            </a>
                            <a href="${course.url}" class="btn btn-outline-primary btn-sm" target="_blank" rel="noopener noreferrer">
                                <i class="fas fa-info-circle me-2"></i>View Details
                            </a>
                            <button class="btn btn-outline wishlist-btn" onclick="App.toggleWishlist(${course.id})">
                                <i class="fas fa-heart me-2"></i>Wishlist
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    },
    
    // Filter functionality
    setupFilters() {
        const filterForm = document.querySelector('.filters-form');
        if (!filterForm) return;
        
        // Price range slider
        const priceRange = document.querySelector('.price-range');
        if (priceRange) {
            this.initPriceRangeSlider(priceRange);
        }
        
        // Filter form submission
        filterForm.addEventListener('submit', (e) => {
            e.preventDefault();
            this.applyFilters();
        });
        
        // Real-time filter updates
        filterForm.querySelectorAll('input, select').forEach(input => {
            input.addEventListener('change', () => {
                this.debounce(() => this.applyFilters(), this.config.debounceDelay);
            });
        });
    },
    
    // Apply filters to courses
    async applyFilters() {
        const filterForm = document.querySelector('#filtersForm') || document.querySelector('.filters-form');
        if (!filterForm) {
            console.warn('No filter form found');
            return;
        }
        
        console.log('Applying filters...');
        
        const formData = new FormData(filterForm);
        const filters = {};
        
        // Convert FormData to object and handle special cases
        for (let [key, value] of formData.entries()) {
            if (value && value.trim() !== '') {
                filters[key] = value;
            }
        }
        
        // Handle price range
        const priceRange = document.getElementById('priceRange');
        const maxPrice = document.getElementById('maxPrice');
        if (priceRange && maxPrice && priceRange.value) {
            filters.maxPrice = priceRange.value;
        }
        
        // Handle free checkbox
        const isFree = document.getElementById('isFree');
        if (isFree && isFree.checked) {
            filters.maxPrice = '0';
        }
        
        console.log('Filters to apply:', filters);
        
        try {
            this.showLoading();
            
            // Build query string
            const queryParams = new URLSearchParams();
            Object.entries(filters).forEach(([key, value]) => {
                if (value !== '' && value !== null && value !== undefined) {
                    queryParams.append(key, value);
                }
            });
            
            const url = `${this.config.apiBaseUrl}/courses/filter?${queryParams.toString()}`;
            console.log('Filter URL:', url);
            
            const response = await fetch(url);
            
            console.log('Filter response status:', response.status);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('Filter response error:', errorText);
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const courses = await response.json();
            console.log('Filter results:', courses);
            this.displayFilteredResults(courses);
            
        } catch (error) {
            console.error('Filter error:', error);
            this.showError('Failed to apply filters. Please try again.');
        } finally {
            this.hideLoading();
        }
    },
    
    // Display filtered results
    displayFilteredResults(courses) {
        const resultsContainer = document.querySelector('.search-results') || 
                                document.querySelector('.row') || 
                                document.querySelector('.col-lg-9 .row');
        
        if (!resultsContainer) {
            console.warn('No results container found');
            return;
        }
        
        if (courses.length === 0) {
            resultsContainer.innerHTML = `
                <div class="col-12 text-center py-5">
                    <i class="fas fa-search text-muted mb-3" style="font-size: 3rem;"></i>
                    <h4>No courses found</h4>
                    <p class="text-muted">Try adjusting your filters or search terms.</p>
                </div>
            `;
            return;
        }
        
        const coursesHTML = courses.map(course => this.renderCourseCard(course)).join('');
        resultsContainer.innerHTML = coursesHTML;
        
        // Add animation to new cards
        this.animateNewElements(resultsContainer.querySelectorAll('.course-card'));
    },
    
    // Wishlist functionality
    setupWishlist() {
        // Load wishlist from localStorage
        this.loadWishlist();
        
        // Wishlist button event delegation
        document.addEventListener('click', (e) => {
            if (e.target.closest('.wishlist-btn')) {
                const courseId = e.target.closest('.course-card').dataset.courseId;
                this.toggleWishlist(courseId);
            }
        });
    },
    
    // Toggle wishlist item
    toggleWishlist(courseId) {
        const button = event.target.closest('.wishlist-btn');
        const icon = button.querySelector('i');
        
        if (this.state.wishlist.has(courseId)) {
            this.state.wishlist.delete(courseId);
            icon.classList.remove('fas');
            icon.classList.add('far');
            button.classList.remove('btn-primary');
            button.classList.add('btn-outline');
            button.innerHTML = '<i class="far fa-heart me-2"></i>Wishlist';
        } else {
            this.state.wishlist.add(courseId);
            icon.classList.remove('far');
            icon.classList.add('fas');
            button.classList.remove('btn-outline');
            button.classList.add('btn-primary');
            button.innerHTML = '<i class="fas fa-heart me-2"></i>Added';
        }
        
        this.saveWishlist();
        this.updateWishlistCount();
    },
    
    // Load wishlist from localStorage
    loadWishlist() {
        const saved = localStorage.getItem('courseWishlist');
        if (saved) {
            this.state.wishlist = new Set(JSON.parse(saved));
        }
    },
    
    // Save wishlist to localStorage
    saveWishlist() {
        localStorage.setItem('courseWishlist', JSON.stringify([...this.state.wishlist]));
    },
    
    // Update wishlist count display
    updateWishlistCount() {
        const countElement = document.querySelector('.wishlist-count');
        if (countElement) {
            countElement.textContent = this.state.wishlist.size;
        }
    },
    
    // Course comparison functionality
    setupComparison() {
        const compareButtons = document.querySelectorAll('.compare-btn');
        compareButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                const courseId = e.target.closest('.course-card').dataset.courseId;
                this.addToComparison(courseId);
            });
        });
        
        // Comparison modal
        this.setupComparisonModal();
    },
    
    // Add course to comparison
    addToComparison(courseId) {
        const comparison = JSON.parse(localStorage.getItem('courseComparison') || '[]');
        
        if (comparison.length >= 4) {
            this.showError('You can compare up to 4 courses at a time.');
            return;
        }
        
        if (!comparison.includes(courseId)) {
            comparison.push(courseId);
            localStorage.setItem('courseComparison', JSON.stringify(comparison));
            this.updateComparisonCount();
            this.showSuccess('Course added to comparison');
        }
    },
    
    // Setup comparison modal
    setupComparisonModal() {
        const modal = document.getElementById('comparisonModal');
        if (!modal) return;
        
        const compareBtn = document.querySelector('.compare-courses-btn');
        if (compareBtn) {
            compareBtn.addEventListener('click', () => {
                this.showComparisonModal();
            });
        }
    },
    
    // Show comparison modal
    async showComparisonModal() {
        const comparison = JSON.parse(localStorage.getItem('courseComparison') || '[]');
        
        if (comparison.length < 2) {
            this.showError('Please select at least 2 courses to compare.');
            return;
        }
        
        try {
            this.showLoading();
            
            const response = await fetch(`${this.config.apiBaseUrl}/courses/compare?courseIds=${comparison.join(',')}`);
            const comparisonData = await response.json();
            
            this.displayComparison(comparisonData);
            
        } catch (error) {
            console.error('Comparison error:', error);
            this.showError('Failed to load comparison data.');
        } finally {
            this.hideLoading();
        }
    },
    
    // Responsive navigation
    setupResponsiveNav() {
        const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
        const mobileMenu = document.querySelector('.mobile-menu');
        
        if (mobileMenuToggle && mobileMenu) {
            mobileMenuToggle.addEventListener('click', () => {
                mobileMenu.classList.toggle('active');
                mobileMenuToggle.classList.toggle('active');
            });
        }
        
        // Close mobile menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.mobile-menu') && !e.target.closest('.mobile-menu-toggle')) {
                mobileMenu?.classList.remove('active');
                mobileMenuToggle?.classList.remove('active');
            }
        });
    },
    
    // Smooth scrolling
    setupSmoothScrolling() {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });
    },
    
    // Animation setup
    setupAnimations() {
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-fade-in-up');
                }
            });
        }, observerOptions);
        
        // Observe all cards and sections
        document.querySelectorAll('.card, .section, .feature-item').forEach(element => {
            observer.observe(element);
        });
    },
    
    // Initialize tooltips
    initTooltips() {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    },
    
    // Initialize modals
    initModals() {
        const modalTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="modal"]'));
        modalTriggerList.map(function (modalTriggerEl) {
            return new bootstrap.Modal(modalTriggerEl);
        });
    },
    
    // Initialize dropdowns
    initDropdowns() {
        const dropdownTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="dropdown"]'));
        dropdownTriggerList.map(function (dropdownTriggerEl) {
            return new bootstrap.Dropdown(dropdownTriggerEl);
        });
    },
    
    // Initialize progress bars
    initProgressBars() {
        const progressBars = document.querySelectorAll('.progress-bar');
        progressBars.forEach(bar => {
            const width = bar.getAttribute('aria-valuenow') || 0;
            setTimeout(() => {
                bar.style.width = `${width}%`;
            }, 100);
        });
    },
    
    // Initialize charts
    initCharts() {
        // Course rating distribution chart
        const ratingChart = document.getElementById('ratingChart');
        if (ratingChart) {
            new Chart(ratingChart, {
                type: 'doughnut',
                data: {
                    labels: ['5 Stars', '4 Stars', '3 Stars', '2 Stars', '1 Star'],
                    datasets: [{
                        data: [45, 30, 15, 7, 3],
                        backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#ef4444', '#6b7280']
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        }
                    }
                }
            });
        }
        
        // Platform comparison chart
        const platformChart = document.getElementById('platformChart');
        if (platformChart) {
            new Chart(platformChart, {
                type: 'bar',
                data: {
                    labels: ['Udemy', 'Coursera', 'edX', 'LinkedIn Learning'],
                    datasets: [{
                        label: 'Average Rating',
                        data: [4.6, 4.7, 4.5, 4.4],
                        backgroundColor: ['#6366f1', '#10b981', '#3b82f6', '#f59e0b']
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 5
                        }
                    }
                }
            });
        }
    },
    
    // Utility functions
    showLoading() {
        this.state.isLoading = true;
        const spinner = document.getElementById('loadingSpinner');
        if (spinner) spinner.style.display = 'block';
    },
    
    hideLoading() {
        this.state.isLoading = false;
        const spinner = document.getElementById('loadingSpinner');
        if (spinner) spinner.style.display = 'none';
    },
    
    showSuccess(message) {
        this.showNotification(message, 'success');
    },
    
    showError(message) {
        this.showNotification(message, 'error');
    },
    
    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
                <span>${message}</span>
                <button class="notification-close">&times;</button>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Auto-remove after 5 seconds
        setTimeout(() => {
            notification.remove();
        }, 5000);
        
        // Close button functionality
        notification.querySelector('.notification-close').addEventListener('click', () => {
            notification.remove();
        });
    },
    
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },
    
    animateNewElements(elements) {
        elements.forEach((element, index) => {
            setTimeout(() => {
                element.classList.add('animate-fade-in-up');
            }, index * 100);
        });
    },
    
    // Load user preferences
    loadUserPreferences() {
        const preferences = localStorage.getItem('userPreferences');
        if (preferences) {
            this.state.filters = JSON.parse(preferences);
        }
    },
    
    // Save user preferences
    saveUserPreferences() {
        localStorage.setItem('userPreferences', JSON.stringify(this.state.filters));
    },
    
    // Add to search history
    addToSearchHistory(query) {
        if (!this.state.searchHistory.includes(query)) {
            this.state.searchHistory.unshift(query);
            this.state.searchHistory = this.state.searchHistory.slice(0, 10);
            localStorage.setItem('searchHistory', JSON.stringify(this.state.searchHistory));
        }
    }
};

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});

// Export for global access
window.App = App;
