# 📚 TopLearn - Online Course Comparison Platform

TopLearn is a web-based platform built using **Spring Boot** and **Thymeleaf** that allows users to compare online courses from different e-learning platforms using a Multi-Criteria Decision-Making (MCDM) approach, specifically the **TOPSIS** algorithm.

---

## 🔍 Features

- 🔎 **Search & Compare Courses** from multiple platforms.
- 📊 **TOPSIS-based ranking** based on user-defined criteria (price, flexibility, quality).
- 📝 Dynamic course listings and details.
- 🌐 Responsive web UI using **Thymeleaf** templates.
- ⚙️ Pre-loaded data using a `DataLoader`.

---

## 🏗️ Project Structure

src/
├── main/
│ ├── java/com/coursecomparison/
│ │ ├── controller/ # Handles web and REST requests
│ │ ├── service/ # Business logic & TOPSIS algorithm
│ │ ├── repository/ # Spring Data JPA for Course entity
│ │ ├── model/ # Course model
│ │ └── CourseComparisonApplication.java # Main Spring Boot app
│ └── resources/
│ ├── templates/ # Thymeleaf HTML templates
│ ├── static/css/ # Stylesheet
│ └── application.properties

yaml
Copy
Edit

---

## ⚙️ Technologies Used

- Java 17+
- Spring Boot
- Spring Data JPA (Hibernate)
- Thymeleaf
- Maven
- H2 (or other DB, as configured)
- TOPSIS for MCDM

---

## 🚀 Getting Started

### 1. Clone and Import

```bash
git clone https://github.com/yourusername/toplearn.git
cd toplearn
2. Build and Run
bash
Copy
Edit
mvn spring-boot:run
Or use your IDE to run CourseComparisonApplication.java.

3. Access App
Open your browser at:

arduino
Copy
Edit
http://localhost:8080/
🧠 MCDM Algorithm (TOPSIS-based)
The project uses the TOPSIS algorithm under MCDMService.java to rank courses based on multiple criteria such as:

📈 Course quality

💵 Price

🕒 Flexibility or duration

How it works:
Normalize the decision matrix.

Apply weights to each criterion.

Determine ideal (best) and negative-ideal (worst) solutions.

Calculate distances to ideal and negative-ideal solutions.

Compute a similarity score and rank courses accordingly.

📁 Sample Pages
/ – Homepage

/courses – Course list

/courses/compare – Compare courses

/about – About page

/search – Search results

✅ To Do / Improvements
Add authentication & user profiles

Fetch real-time data via APIs (Udemy, Coursera, etc.)

Make criteria user-customizable

Add unit tests and validation
