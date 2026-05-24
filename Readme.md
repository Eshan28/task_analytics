# Personal Task Analytics App with AI Agent

A full-stack task management app with an AI assistant that understands natural language, creates tasks, shows analytics, and remembers your conversation.

**Stack**: Spring Boot 4 + MySQL + React 18 + LLaMA 3.3 (Groq)

---

## Quick Demo

- Say **"Add task to finish report due tomorrow under Work"** → AI creates it
- Say **"Mark all Health tasks complete and show my stats"** → AI does both
- Say **"Show my pending tasks"** → AI fetches real data from your database

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Spring Boot | 4.0.6 |
| Language | Java | 18 |
| Database | MySQL | 8.0.41 |
| ORM | Hibernate | 7.2.12 |
| AI Model | LLaMA 3.3-70B via Groq | Free |
| HTTP Client | OkHttp | 4.12.0 |
| Frontend | React | 18 |
| Charts | Recharts | Latest |
| Routing | React Router | v6 |
| HTTP | Axios | Latest |

---

## Project Structure

```
C:\DemoEshan\
├── task-analytics/                        (Backend)
│   ├── src/main/java/com/rbhu/taskanalytics/
│   │   ├── config/
│   │   │   ├── JacksonConfig.java         Jackson 3 bean
│   │   │   └── WebConfig.java             CORS settings
│   │   ├── model/
│   │   │   ├── Task.java                  Task entity
│   │   │   └── ChatMessage.java           Chat entity
│   │   ├── repository/
│   │   │   ├── TaskRepository.java        Task DB queries
│   │   │   └── ChatMessageRepository.java Chat DB queries
│   │   ├── dto/
│   │   │   ├── TaskRequest.java
│   │   │   ├── TaskUpdateRequest.java
│   │   │   ├── TaskResponse.java
│   │   │   ├── AnalyticsResponse.java
│   │   │   ├── ChatRequest.java
│   │   │   └── ChatResponse.java
│   │   ├── service/
│   │   │   └── TaskService.java           Business logic
│   │   ├── controller/
│   │   │   ├── TaskController.java        /tasks endpoints
│   │   │   ├── AgentController.java       /agent/chat endpoint
│   │   │   └── GlobalExceptionHandler.java
│   │   └── agent/
│   │       ├── ToolDefinitions.java       6 tool definitions for AI
│   │       ├── ToolExecutor.java          Runs AI-chosen tools
│   │       └── AgentService.java          Groq API + agentic loop
│   └── src/main/resources/
│       └── application.properties
│
└── task-analytics-frontend/               (Frontend)
    └── src/
        ├── api/api.js                     All HTTP calls
        ├── pages/
        │   ├── TaskList.js                View + filter tasks
        │   ├── AddTask.js                 Create task form
        │   ├── Analytics.js               Charts + stats
        │   └── Chat.js                    AI chat interface
        ├── components/Navbar.js
        ├── App.js
        └── index.js
```

---

## Database Schema

```sql
-- Auto-created by Hibernate on first run

CREATE TABLE tasks (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(255) NOT NULL,
    category    VARCHAR(50) DEFAULT 'Other',
    status      ENUM('pending', 'completed') DEFAULT 'pending',
    due_date    DATE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE chat_messages (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id  VARCHAR(100) NOT NULL,
    role        ENUM('user', 'assistant'),
    content     TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX (session_id)
);
```

---

## Setup Instructions

### Prerequisites

Before starting, install these:

- **Java 18+** → [oracle.com/java/technologies/downloads](https://www.oracle.com/java/technologies/downloads/#java18)
- **Node.js 16+** → [nodejs.org](https://nodejs.org)
- **MySQL 8.0+** → [dev.mysql.com/downloads/mysql](https://dev.mysql.com/downloads/mysql/)
- **Git** → [git-scm.com/download/win](https://git-scm.com/download/win)
- **Groq API Key (FREE)** → [console.groq.com](https://console.groq.com) → Sign up → API Keys → Create Key → Copy `gsk_...`

---

### Step 1 — Create MySQL Database

Open Command Prompt and run:

```bash
mysql -u root -p
```

Then inside MySQL:

```sql
CREATE DATABASE task_analytics;
exit
```

---

### Step 2 — Configure Backend

Open this file:
```
C:\DemoEshan\task-analytics\src\main\resources\application.properties
```

Replace entire content with:

```properties
spring.application.name=task-analytics
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/task_analytics?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=put_MySQL_PIN 
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

groq.api.key=gsk_PUT_YOUR_API_KEY
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.model=llama-3.3-70b-versatile

logging.level.com.rbhu=DEBUG
```
You can put your MySQL password and Groq api key 

---

### Step 3 — Run Backend

Open project in **IntelliJ** → Click green **Run** button

Wait for this in console:
```
Started TaskAnalyticsApplication in X seconds
```

Verify: Open browser → go to `http://localhost:8080/tasks` → should show `[]`

---

### Step 4 — Run Frontend

Open **VS Code terminal** and run:

```bash
cd C:\DemoEshan\task-analytics-frontend
npm install
npm start
```

Browser opens at `http://localhost:3000` automatically.

---

## API Reference

### Tasks

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/tasks` | Create task |
| GET | `/tasks` | Get all tasks |
| GET | `/tasks?category=Work&status=pending` | Get filtered tasks |
| PUT | `/tasks/{id}` | Update task |
| DELETE | `/tasks/{id}` | Delete task |

**Create Task — Request:**
```json
POST /tasks
{
  "title": "Fix login bug",
  "category": "Work",
  "dueDate": "2026-05-30"
}
```

**Create Task — Response:**
```json
{
  "id": 1,
  "title": "Fix login bug",
  "category": "Work",
  "status": "pending",
  "dueDate": "2026-05-30",
  "createdAt": "2026-05-24T10:30:00",
  "updatedAt": "2026-05-24T10:30:00"
}
```

**Update Task — Request:**
```json
PUT /tasks/1
{
  "status": "completed"
}
```

---

### Analytics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/analytics` | Get productivity stats |

**Response:**
```json
{
  "total_tasks": 10,
  "completed_tasks": 6,
  "pending_tasks": 4,
  "completion_rate": 60.0,
  "category_breakdown": {
    "Work": 5,
    "Health": 3,
    "Learning": 2
  }
}
```

---

### AI Agent

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/agent/chat` | Send message to AI |
| DELETE | `/agent/history/{sessionId}` | Clear chat history |

**Request:**
```json
POST /agent/chat
{
  "message": "Add task to buy groceries due tomorrow",
  "sessionId": "session-001"
}
```

**Response:**
```json
{
  "reply": "I've created 'buy groceries' in Other category due 2026-05-25.",
  "actions": [
    {
      "tool": "create_task",
      "args": "{\"title\":\"buy groceries\",\"due_date\":\"2026-05-25\"}",
      "result": "Task created: {\"id\":8,...}"
    }
  ]
}
```

---

## AI Agent Design

### How It Works

```
User message
    ↓
Load full chat history from MySQL
    ↓
Send to Groq API with:
  - Conversation history
  - 6 tool definitions
  - System instructions
    ↓
LLaMA 3.3 decides which tools to call
    ↓
ToolExecutor runs real DB operations
    ↓
Results sent back to LLaMA
    ↓
LLaMA writes natural language reply
    ↓
Save reply to DB → return to frontend
```

### Available Tools

| Tool | What It Does |
|------|-------------|
| `create_task` | Creates task (parses natural dates) |
| `get_tasks` | Fetches tasks with optional filters |
| `update_task` | Updates status/title/category |
| `delete_task` | Removes task permanently |
| `get_analytics` | Gets completion stats |
| `mark_all_completed_by_category` | Batch marks category done |

### Grounding Strategy

The agent never hallucinates task data because:

1. **Real tool execution** — Calls actual database, not simulated
2. **Result feedback** — LLaMA reads actual DB response before replying
3. **History context** — Full conversation sent every request (stateless API)
4. **Session isolation** — Each browser gets UUID stored in localStorage

### Multi-step Example

```
User: "Mark all Work done and show my analytics"

Agent loop:
  Iteration 1 → calls mark_all_completed_by_category("Work")
  Iteration 2 → calls get_analytics()
  Iteration 3 → LLaMA reads both results → writes reply

Reply: "Marked 3 Work tasks complete.
        Completion rate is now 75% (6 of 8 tasks done)"
```

---

## Frontend Pages

### Tasks Page (`/`)
- All tasks in card layout
- Category badges (Work=Blue, Health=Green, Learning=Orange, Other=Purple)
- Click checkbox to toggle complete/pending
- Click trash to delete
- Filter by category + status
- Task count display

### Add Task Page (`/add`)
- Title input (validated — required)
- Category dropdown
- Optional date picker
- Redirects to task list after save

### Analytics Page (`/analytics`)
- **4 stat cards**: Total, Completed, Pending, Completion Rate
- **Bar chart**: Tasks per category
- **Donut chart**: Completed vs Pending
- All data from real DB

### AI Chat Page (`/chat`)
- Conversational interface
- Shows "Actions taken" after each AI response
- Scrolls to latest message
- Clear chat button
- Enter = send, Shift+Enter = new line
- Session ID persisted in localStorage

---

## Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| `Connection refused` on start | MySQL not running | Start MySQL service |
| `Access denied for root@localhost` | Wrong password | Fix `spring.datasource.password` |
| `Unknown database task_analytics` | DB not created | Run `CREATE DATABASE task_analytics;` |
| `Could not resolve placeholder 'groq.api.key'` | Missing key in properties | Add `groq.api.key=...` to properties file |
| Frontend shows blank page | Backend not running | Start backend on port 8080 first |
| `CORS error` in browser | CORS misconfigured | Verify WebConfig.java exists |
| `401 Unauthorized` from Groq | Wrong API key | Get new key from console.groq.com |
| `429 Too Many Requests` from Groq | Rate limit hit | Wait 1 minute and retry |
| Analytics shows 0s | No tasks in DB | Create at least 1 task first |
| Charts not rendering | No category data | Add tasks with different categories |

---

## Push to GitHub

### Step 1 — Create Repository on GitHub

1. Go to [github.com](https://github.com) → Sign in
2. Click **+** (top right) → **New repository**
3. Name: `task-analytics`
4. Visibility: **Public**
5. Click **Create repository**

---

### Step 2 — Create .gitignore

Create file `C:\DemoEshan\.gitignore`:

```
# Java
target/
*.class
*.jar
.idea/
*.iml

# Node
node_modules/
npm-debug.log

# Secrets - IMPORTANT
application.properties
.env
```

> ⚠️ **Important**: `application.properties` is in `.gitignore` to protect your database password and API key. Share credentials separately with your teacher.

---

### Step 3 — Push Code

Open Command Prompt (not PowerShell):

```bash
cd C:\DemoEshan

git init
git add .
git commit -m "Complete task analytics app with AI agent"
git remote add origin https://github.com/YOUR_USERNAME/task-analytics.git
git branch -M main
git push -u origin main
```

Replace `YOUR_USERNAME` with your actual GitHub username.

---

### Step 4 — Verify

Visit: `https://github.com/YOUR_USERNAME/task-analytics`

You should see all your files. ✅

---

## Assignment Requirements

| Requirement | Status | Implementation |
|-------------|--------|---------------|
| POST /tasks | ✅ Done | TaskController.java |
| GET /tasks with filters | ✅ Done | ?category= and ?status= params |
| PUT /tasks/{id} | ✅ Done | Partial update (all fields optional) |
| DELETE /tasks/{id} | ✅ Done | Returns 204 No Content |
| GET /analytics | ✅ Done | total, completed, rate, breakdown |
| AI Agent with tool calling | ✅ Done | 6 real tools, LLaMA 3.3 via Groq |
| Multi-step tool chaining | ✅ Done | Agent loops until end_turn |
| React frontend | ✅ Done | 4 pages with full navigation |
| Real-time updates | ✅ Done | DB operations → UI refresh |
| Conversation memory | ✅ Done | MySQL chat history per session |

---

## Running the App (Both Together)

**Terminal 1 — IntelliJ:**
```
Click Run → Wait for "Started TaskAnalyticsApplication"
```

**Terminal 2 — VS Code:**
```bash
cd C:\DemoEshan\task-analytics-frontend
npm start
```

Open browser → `http://localhost:3000` ✅

---

## Sample AI Conversations

```
"Add task to buy groceries"
→ Creates task

"Show all pending tasks"
→ Lists pending tasks from DB

"Mark task 1 as complete"
→ Updates status in DB

"How many tasks have I completed?"
→ Calls get_analytics, reports real number

"Create a workout task due Friday and show my health tasks"
→ create_task + get_tasks (2 tool calls, 1 message)

"Mark all Work tasks done and show my completion rate"
→ mark_all_completed_by_category + get_analytics
```

---

*Built with Spring Boot 4 + React 18 + LLaMA 3.3 | Status: Complete ✅*
EOF
Output

exit code 0
Done