🧩 Tech Stack & AWS Services Used
Backend: Java, Spring Boot
Cloud Platform: AWS (DynamoDB, Cognito, Elastic Beanstalk, IAM)

☁️ AWS Integrations
🗂️ DynamoDB
Used as the primary NoSQL database for managing data related to Organizations and Volunteers.

Supports registration, login, profile management, and task handling for Organizations.

Allows Volunteers to sign up for tasks and submit ratings and reviews.

🔐 Cognito
Integrated for secure user authentication and authorization.

Email verification is required upon registration for both Volunteers and Organizations.

Generates JWT tokens on login, which are validated via Spring Security for secure API access.

🔑 IAM (Identity and Access Management)
Configured IAM users and roles to define fine-grained access policies for services like DynamoDB, Elastic Beanstalk, and SNS, ensuring secure and controlled access.

🚀 Elastic Beanstalk
Used to deploy the full-stack Spring Boot application.

Provides automated provisioning, load balancing, and scaling

Simplifies environment and deployment management for the web app.
