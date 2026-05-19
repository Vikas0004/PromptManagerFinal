# Prompt Manager App Final – Backend (Microservices | Python) | Front End (Svelte) 

A **cloud-native, microservices-based backend** for the **Prompt Manager Final Application**, that allows users to create, manage, and analyze AI prompts across tools like ChatGPT, Midjourney, Claude, and Gemini.

This backend is built using **Spring Boot**, **Python**, **Docker**, and **Docker Compose**, following best practices of **JWT-based security**, **service isolation**.

---

## 📁 Microservices Overview

| Service Name | Description | Tech Stack | Database |
|---------------|--------------|-------------|-----------|
| **user-service** | Handles authentication, registration, and JWT token validation | Spring Boot, JPA, JWT | MySQL (RDS) |
| **prompt-service** | Manages CRUD operations for prompts and user-specific filtering | Spring Boot, JPA | MySQL (RDS) |
| **analytics-service** | Tracks and reports prompt usage analytics (views, favorites, copies) | Spring Boot, JPA | MySQL (RDS) |
| **api-gateway** | Entry point for routing and token validation between services | Spring Cloud Gateway | - |
| **eureka-server** | Service Discovery |  - |
| **ml-service** | User for trainging the model on prompts and implemenatation of semantic search | Python,FastApi |  - |
| **preprocessing-service** | pre process the prompt data before storing to the database uses vector embedding and lammetization |  Python,FastApi |  - |
| **llm-service** | llm service uses the ml-service and rag-pipeline to create improved prompts with the help of ollama llama3 |   Python,FastApi |  - |





All services are **independently deployable Docker containers**, **Demo Using Docker Compose since AWS Free Tier credits are exhausted**.

---

## Architecture

The Prompt Manager App follows a cloud-native microservices architecture with centralized authentication, analytics tracking, AI-powered services, and asynchronous communication using RabbitMQ.

```text
                                      ┌──────────────────────────────┐
                                      │     Frontend (SvelteKit)     │
                                      │ AWS S3 Static Hosting /      │
                                      │     Docker Compose           │
                                      └──────────────┬───────────────┘
                                                     │
                                             REST API Calls
                                                     │
                                      ┌──────────────▼──────────────┐
                                      │         API Gateway          │
                                      │  JWT Validation & Routing    │
                                      └───────┬─────────┬───────────┘
                                              │         │
         ┌────────────────────────────────────┘         └──────────────────────────────────┐
         │                                                                                 │

┌────────▼─────────┐                                                         ┌────────────▼──────────┐
│   User Service   │                                                         │    Prompt Service     │
│ Authentication   │                                                         │ Prompt CRUD Operations│
│ JWT Generation   │                                                         │ Search & Filters      │
│ User Management  │                                                         │ Prompt Details        │
└────────┬─────────┘                                                         └───────┬──────┬───────┘
         │                                                                           │      │
         │                                                                           │      │
         │                                              ┌────────────────────────────┘      │
         │                                              │                                   │
         │                                              ▼                                   ▼

         │                             ┌──────────────────────────┐       ┌──────────────────────────┐
         │                             │  Preprocessing Service   │       │      ML/NLP Service      │
         │                             │  FastAPI + Python        │       │  FastAPI + Python        │
         │                             │ Stopword Removal         │       │ AI Tool Prediction       │
         │                             │ Lemmatization            │       │ Embedding Generation     │
         │                             │ Duplicate Detection      │       │ Semantic Search Support  │
         │                             └─────────────┬────────────┘       └─────────────┬────────────┘
         │                                           │                                  │
         │                                           │                                  │
         │                                           └──────────────┬───────────────────┘
         │                                                          │
         │                                                          ▼

         │                                           ┌──────────────────────────┐
         │                                           │       LLM Service         │
         │                                           │  RAG-based Prompt Assist  │
         │                                           │ Prompt Improvement        │
         │                                           │ Semantic Similarity       │
         │                                           │ Ollama / Local LLM        │
         │                                           └─────────────┬────────────┘
         │                                                         │
         │                                                         │
         │                                                         ▼

         │                                           ┌──────────────────────────┐
         │                                           │     Vector Store          │
         │                                           │ FAISS / ChromaDB          │
         │                                           │ Prompt Embeddings         │
         │                                           └──────────────────────────┘

         │
         │
         │                                 View / Copy / Favorite Events
         │                                                 │
         │                                                 ▼

         │                                 ┌──────────────────────────┐
         │                                 │    Analytics Service      │
         │                                 │ Views, Favorites, Copies  │
         │                                 │ Reports & Aggregations    │
         │                                 └─────────────┬────────────┘
         │                                               │
         │                               Async View Events via RabbitMQ
         │                                               │
         │                                               ▼

         │                                 ┌──────────────────────────┐
         │                                 │        RabbitMQ           │
         │                                 │   Event Message Broker    │
         │                                 └──────────────────────────┘

         │
         └─────────────────────────────────────────────────────────────────────────────┐
                                                                                      │
                                                                                      ▼

                                             ┌──────────────────────────────────────┐
                                             │         PostgreSQL Database          │
                                             │ Users, Prompts, Analytics Data       │
                                             │ Embeddings Metadata                  │
                                             └──────────────────────────────────────┘---

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

- Responsible for tracking prompt engagement activities such as:
  - Prompt views
  - Prompt copies
  - Prompt favorites / unfavorites

- Maintains analytics data for both individual users and global system-wide reporting.

- Supports role-based analytics:
  - Regular users can view analytics related to their own prompts.
  - Admin users can access global analytics and summary reports.

#### RabbitMQ Integration

- RabbitMQ is used for asynchronous event-driven processing of prompt view tracking.

- When a user opens a prompt, the Analytics Service publishes a `PromptViewedEvent` message to RabbitMQ instead of directly updating the database synchronously.

- A consumer listens to these events and processes analytics updates in the background.

#### Main Endpoints

##### User Analytics
- `GET /analytics/my`
- `GET /analytics/favorites`

##### Global/Admin Analytics
- `GET /analytics/global`
- `GET /analytics/summary`

##### Analytics Tracking
- `POST /analytics/increment/view/{promptId}`
- `POST /analytics/increment/copy/{promptId}`
- `POST /analytics/increment/favorite/{promptId}`
- `POST /analytics/decrement/favorite/{promptId}`

##### Prompt Validation & Cleanup
- `GET /analytics/check-favorite/{promptId}`
- `DELETE /analytics/delete/{promptId}`

##### Reporting Endpoints
- `GET /analytics/most-viewed`
- `GET /analytics/most-favorited`
- `GET /analytics/most-copied`

#### Additional Notes

- The Analytics Service implementation includes asynchronous RabbitMQ-based view event publishing through `AnalyticsEventProducer` and `PromptViewedEvent`.

- Copy and favorite actions are currently processed synchronously through the service layer.

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

## 🧪 Local Setup (Docker Compose) - Navigate to the service folder and run docker compose up | docker compose up -d
The initial container creation may take some time due to ollama size, if llama3 model is not already istalled that needs to be installed manually.

once all the containers are up and running go to
Entrypoint: [Prompt Manager](https://localhost:5173/login)


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
