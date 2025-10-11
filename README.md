# 🧠 Prompt Manager App – Backend (Microservices)

A **cloud-native, microservices-based backend** for the **Prompt Manager App**, a platform that allows users to create, manage, and analyze AI prompts across tools like ChatGPT, Midjourney, Claude, and Gemini.

This backend is built using **Spring Boot**, **Docker**, and **AWS Fargate**, following best practices of **JWT-based security**, **service isolation**, and **observability**.

---

## 📁 Microservices Overview

| Service Name | Description | Tech Stack | Database |
|---------------|--------------|-------------|-----------|
| **user-service** | Handles authentication, registration, and JWT token validation | Spring Boot, JPA, JWT | MySQL (RDS) |
| **prompt-service** | Manages CRUD operations for prompts and user-specific filtering | Spring Boot, JPA | MySQL (RDS) |
| **analytics-service** | Tracks and reports prompt usage analytics (views, favorites, copies) | Spring Boot, JPA | MySQL (RDS) |
| **api-gateway** | Entry point for routing and token validation between services | Spring Cloud Gateway | - |
| **eureka-server** | Service Discovery |  - |

All services are **independently deployable Docker containers**, **AWS ECS Fargate (production)**.

---

## 🧩 Architecture

         ┌──────────────────────────────┐
         │        Frontend (SvelteKit)  │
         │  Hosted on AWS S3 (Static)   │
         └──────────────┬───────────────┘
                        │ REST API Calls
               ┌────────▼────────┐
               │   API Gateway   │
               │ (JWT Validation)│
               └───────┬─────────┘
      ┌────────────────┼────────────────┐
      │                │                │
┌────────▼───────┐ ┌──────▼────────┐ ┌─────▼─────────┐
│ user-service │ │ prompt-service │ │ analytics-svc │
│ Auth, JWT, IAM │ │ CRUD, Filters │ │ Stats, Reports│
└───────────────┬┘ └───────────────┬┘ └──────────────┘
│ │
▼ ▼
AWS RDS (MySQL) CloudWatch Logs


---

## ⚙️ Features

### 1. User Service
- Register new users with role-based access (default `USER`)
- JWT issuance and validation
- Endpoints:
  - `POST /user/register` → register a new user  
  - `POST /user/login` → returns JWT + role  
  - `POST /user/validate` → validates JWT token

### 2. Prompt Service
- CRUD for user-specific prompts
- Filters and search (by title or description)
- Enforces ownership and role-based updates/deletes
- Endpoints:
  - `POST /prompts/add` → create new prompt
  - `GET /prompts/my` → get prompts of logged-in user
  - `PUT /prompts/{id}` → update prompt
  - `DELETE /prompts/{id}` → delete prompt
  - `GET /prompts/search?query=` → search prompts

### 3. Analytics Service
- Tracks prompt views, copies, favorites
- Aggregates reports:
  - Most viewed prompts
  - Most favorited prompts
  - Most copied prompts
- Endpoints:
  - `/analytics/global/most-viewed`
  - `/analytics/global/most-favorited`
  - `/analytics/global/most-copied`
  - `/analytics/summary`

---

## 🛠️ Tech Stack

- **Java 17 + Spring Boot 3.x**
- **Spring Data JPA + Hibernate**
- **MySQL (AWS RDS)**
- **JWT Auth (io.jsonwebtoken)**
- **Docker**
- **AWS ECS Fargate**
- **AWS CloudWatch (logs and alerts)**

---

## 🧪 Local Setup (Docker Compose) - Since the app is deployed on AWS, local setup could not be completed.

# 🧠 Prompt Manager App – Frontend (SvelteKit UI)

A modern **SvelteKit-based frontend** for the **Prompt Manager App**, enabling users to securely manage, analyze, and interact with AI prompts.  
The app integrates with a cloud-native backend (Spring Boot microservices) deployed on **AWS ECS Fargate** and **RDS**.

---

## ⚙️ Tech Stack

| Layer | Technology |
|--------|-------------|
| **Frontend Framework** | [SvelteKit 2.x](https://kit.svelte.dev) |
| **Language** | TypeScript |
| **Styling** | Tailwind CSS |
| **Build Tool** | Vite |
| **State Management** | Svelte Stores |
| **API Communication** | REST (via `fetch` + JWT) |
| **Deployment** | AWS S3 (Static Hosting) + CloudFront (optional) |

---

## 🎯 Core Features

| Module | Description |
|--------|--------------|
| **Authentication** | Register and login using JWT tokens via `user-service`. |
| **Prompt Management (CRUD)** | Create, edit, view, and delete AI prompts with ownership and access control. |
| **Favorites & Analytics** | Mark prompts as favorites, view stats (views, copies, favorites). |
| **Admin Dashboard** | Access real-time analytics reports: most viewed, copied, and favorited prompts. |
| **Search & Filter** | Client-side search by title, description, or AI tool. |
| **Responsive UI** | Mobile-first design using TailwindCSS and adaptive layouts. |