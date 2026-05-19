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
         │                                                          │──────────────────────────────────
         │                                                          ▼                                 │
         │                                           ┌──────────────────────────┐                     │
         │                                           │       LLM Service         │                    │
         │                                           │  RAG-based Prompt Assist  │                    │
         │                                           │ Prompt Improvement        │                    │
         │                                           │ Semantic Similarity       │                    │
         │                                           │ Ollama / Local LLM        │                    │
         │                                           └─────────────┬────────────┘                     │
                                                                                                      │
         │                                 View / Copy / Favorite Events                              │                 
         │                                                 │                                          │
         │                                                 ▼                                          │
         │                                                                                            │
         │                                 ┌──────────────────────────┐                               │
         │                                 │    Analytics Service      │                              │
         │                                 │ Views, Favorites, Copies  │                              │
         │                                 │ Reports & Aggregations    │                              │
         │                                 └─────────────┬────────────┘                               │
         │                                               │                                            │
         │                               Async View Events via RabbitMQ                               |
         │                                               │                                            │
         │                                               ▼                                            │
         │                                 ┌──────────────────────────┐                               │
         │                                 │        RabbitMQ           │                              │
         │                                 │   Event Message Broker    │                              │
         │                                 └──────────────────────────┘                               │
         │                                                                                            │
         └─────────────────────────────────────────────────────────────────────────────┐              │
                                                                                       │              │
                                                                                       ▼              ▼

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
- **Docker Compose**

---

## 🧪 Local Setup (Docker Compose) - Navigate to the service folder and run docker compose up --build | docker compose up 
The initial container creation may take some time due to ollama size, if llama3 model is not already istalled that needs to be installed manually.

once all the containers are up and running go to
Entrypoint: [Prompt Manager](https://localhost:5173/login)




# Prompt Manager App – Frontend (SvelteKit UI)

A modern **SvelteKit-based frontend** for the **Prompt Manager App**, enabling users to securely manage, analyze, search, and improve AI prompts.

The frontend communicates with a cloud-native backend built using **Spring Boot microservices**, **Python FastAPI AI services**, **RabbitMQ**, and **PostgreSQL**, all orchestrated locally using **Docker Compose**.

---

# ⚙️ Tech Stack

| Layer | Technology |
|--------|-------------|
| **Frontend Framework** | [SvelteKit 2.x](https://kit.svelte.dev) |
| **Language** | TypeScript |
| **Styling** | Tailwind CSS |
| **Build Tool** | Vite |
| **State Management** | Svelte Stores |
| **API Communication** | REST APIs (`fetch` + JWT Authentication) |
| **Containerization** | Docker |
| **Local Deployment** | Docker Compose |

---

# 🎯 Core Features

| Module | Description |
|--------|--------------|
| **Authentication** | Register and login using JWT tokens via `user-service`. |
| **Prompt Management (CRUD)** | Create, edit, view, search, and delete AI prompts securely. |
| **Favorites & Analytics** | Mark prompts as favorites and track prompt engagement statistics. |
| **Admin Analytics Dashboard** | View most viewed, copied, and favorited prompts globally. |
| **Search & Filtering** | Filter prompts by AI tool, keyword, favorites, and semantic similarity. |
| **Semantic Search** | Search prompts using natural language queries powered by vector embeddings. |
| **Prompt Improvement Assistant** | Improve prompts using a RAG-based AI assistant powered by local LLMs. |
| **AI Tool Recommendation** | Automatically suggest the most suitable AI tool for a prompt. |
| **Responsive UI** | Mobile-first responsive design using TailwindCSS. |

---

# AI-Powered Features

## Semantic Search

Users can search prompts using natural language instead of exact keywords.

### Examples
- "Find prompts for writing formal emails"
- "Show prompts related to Python code generation"
- "Prompts for summarizing documents"

The frontend integrates with the backend ML and LLM services to display semantically relevant prompts ranked by similarity.

---

## Prompt Improvement Assistant

Users can click **"Improve with AI"** on any prompt.

The system:
1. Retrieves semantically similar high-performing prompts.
2. Uses a local LLM (Ollama) with Retrieval-Augmented Generation (RAG).
3. Returns:
   - Improved prompt version
   - Explanation of improvements
   - Reference prompts used as context

---

## AI Tool Auto-Suggestion

While creating prompts, the frontend displays suggested AI tools such as:
- ChatGPT
- Gemini
- Claude
- Midjourney
- Perplexity

Suggestions are generated using the backend ML/NLP classifier service.

---

# 🏗️ Frontend Architecture

```text
SvelteKit Frontend
       │
       ▼
API Gateway
       │
 ┌─────┼─────────────────────────────────────────────┐
 │     │                     │                       │
 ▼     ▼                     ▼                       ▼

User Service        Prompt Service        Analytics Service
(JWT Auth)          (CRUD + Search)       (Views/Favorites)

                             │
                             ▼

                  Preprocessing Service
            (Cleaning + Lemmatization)

                             │
                             ▼

                      ML/NLP Service
                (Embeddings + Prediction)

                             │─────────────────────────────────────────────┐
                             ▼                                             ▼

                        LLM Service                         Cleaned Prompt + Embedings stored in DB
               (RAG + Prompt Improvement)                               (Postgres)

                             │
                             ▼

                    Vector Store (FAISS)
