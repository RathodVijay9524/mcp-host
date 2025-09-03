# API Reference Guide - Spring Boot MCP Application

## 🚀 Quick Start

**Base URL**: `http://localhost:8080`

## 📋 Core Chat Endpoints

### 1. Process Chat Request (Regular)
```http
POST /api/ai/chat
Content-Type: application/json

{
  "message": "Hello, how can you help me?",
  "provider": "gemini",
  "model": "gemini-1.5-flash",
  "apiKey": "optional-custom-key",
  "baseUrl": "optional-custom-url",
  "conversationId": "optional-conversation-id"
}
```

**Response:**
```json
{
  "provider": "gemini",
  "model": "gemini-1.5-flash",
  "answer": "Hello! I'm an AI coding assistant with access to 43 tools..."
}
```

### 2. Process Chat Request (Async with Virtual Threads)
```http
POST /api/ai/chat/async
Content-Type: application/json

{
  "message": "Hello, how can you help me?",
  "provider": "gemini",
  "model": "gemini-1.5-flash",
  "apiKey": "optional-custom-key",
  "baseUrl": "optional-custom-url",
  "conversationId": "optional-conversation-id"
}
```

**Response:**
```json
{
  "provider": "gemini",
  "model": "gemini-1.5-flash",
  "answer": "Hello! I'm an AI coding assistant with access to 43 tools..."
}
```

**Note:** The `/chat/async` endpoint uses Virtual Threads for better performance and can handle thousands of concurrent requests.

### 3. Get Available Providers
```http
GET /api/ai/providers
```

**Response:**
```json
["gemini", "ollama"]
```

### 4. Get Available Models
```http
GET /api/ai/models?provider=gemini
```

**Response:**
```json
["gemini-1.5-flash", "gemini-1.5-pro"]
```

## 🧵 Virtual Threads Endpoints

### 1. Process Chat with Virtual Threads
```http
POST /api/virtual-threads/chat
Content-Type: application/json

{
  "message": "Hello, how can you help me?",
  "provider": "gemini",
  "model": "gemini-1.5-flash"
}
```

**Response:**
```json
{
  "provider": "gemini",
  "model": "gemini-1.5-flash",
  "response": "Hello! I'm an AI coding assistant with access to 43 tools..."
}
```

### 2. Process Multiple Chats Concurrently
```http
POST /api/virtual-threads/chat/batch
Content-Type: application/json

[
  {
    "message": "What tools do you have?",
    "provider": "gemini",
    "model": "gemini-1.5-flash"
  },
  {
    "message": "How can you help with coding?",
    "provider": "gemini",
    "model": "gemini-1.5-flash"
  }
]
```

**Response:**
```json
{
  "totalRequests": 2,
  "responses": [
    {
      "provider": "gemini",
      "model": "gemini-1.5-flash",
      "response": "I have 43 tools available..."
    },
    {
      "provider": "gemini",
      "model": "gemini-1.5-flash",
      "response": "I can help with coding in many ways..."
    }
  ],
  "conversationIds": ["uuid1", "uuid2"],
  "message": "All requests processed concurrently with Virtual Threads"
}
```

### 3. Get Virtual Thread Information
```http
GET /api/virtual-threads/info
```

**Response:**
```json
{
  "virtualThreadInfo": "Virtual Thread Info:\n- Thread Name: VirtualThread-1\n- Is Virtual: true\n- Thread ID: 12345\n- Thread Group: main\n- Priority: 5\n- State: RUNNABLE",
  "currentThread": "http-nio-8080-exec-1",
  "isVirtual": false,
  "timestamp": 1756886259915
}
```

### 4. Test Virtual Threads Concurrency
```http
POST /api/virtual-threads/test/concurrency?requestCount=50
```

**Response:**
```json
{
  "requestCount": 50,
  "duration": "2500 ms",
  "averageTimePerRequest": "50.0 ms",
  "successfulRequests": 50,
  "message": "Virtual Threads concurrency test completed"
}
```

## 📊 Monitoring Endpoints

### 1. System Health
```http
GET /api/health
```

**Response:**
```json
{
  "circuitBreakers": {
    "ai-provider": {
      "numberOfSuccessfulCalls": 2,
      "failureRate": -1.0,
      "numberOfFailedCalls": 1,
      "state": "CLOSED",
      "numberOfBufferedCalls": 3
    }
  },
  "status": "UP",
  "timestamp": 1756886259915
}
```

### 2. Metrics Summary
```http
GET /api/metrics/summary
```

**Response:**
```json
{
  "chatRequests": 0.0,
  "chatSuccess": 0.0,
  "chatErrors": 0.0,
  "successRate": 0.0,
  "cacheHits": 0.0,
  "cacheMisses": 0.0,
  "cacheHitRate": 0.0,
  "mcpToolCalls": 0.0,
  "responseTimeStats": {
    "count": 0,
    "mean": 0.0,
    "max": 0.0,
    "p95": "NaN",
    "p99": "NaN"
  }
}
```

### 3. JVM Metrics
```http
GET /api/metrics/jvm
```

**Response:**
```json
{
  "activeThreads": 45,
  "usedMemory": 55531624,
  "totalMemory": 111149056,
  "availableProcessors": 8,
  "memoryUsagePercent": 1.31,
  "maxMemory": 4248829952,
  "freeMemory": 55617432
}
```

### 4. Metrics by Provider
```http
GET /api/metrics/by-provider
```

**Response:**
```json
{
  "gemini_gemini-1.5-flash": {
    "chat.requests.by_provider": 5.0,
    "chat.responses.success.by_provider": 4.0,
    "chat.response.time.by_provider": 3500.0
  }
}
```

## 🔧 Tool Endpoints

### 1. Get Available Tools
```http
GET /api/tools
```

**Response:**
```json
[
  {
    "name": "fullanalysis",
    "description": "Comprehensive project analysis - detailed overview including structure, languages, dependencies, quality metrics",
    "example": "Try using tool: fullanalysis"
  },
  {
    "name": "searchcode",
    "description": "Search for code patterns, functions, or specific implementations across the project",
    "example": "Try using tool: searchcode"
  }
  // ... 43 total tools
]
```

### 2. Get Tool Help
```http
GET /api/tools/help
```

**Response:**
```json
{
  "totalTools": 43,
  "categories": {
    "analysis": ["fullanalysis", "quickanalysis"],
    "search": ["searchcode", "searchfiles"],
    "generation": ["generatecode", "createtest"]
  },
  "usage": "Use tools by mentioning them in your chat messages"
}
```

## 🔍 Actuator Endpoints

### 1. Available Endpoints
```http
GET /actuator
```

### 2. Spring Boot Health
```http
GET /actuator/health
```

### 3. Spring Boot Metrics
```http
GET /actuator/metrics
```

### 4. Prometheus Metrics
```http
GET /actuator/prometheus
```

## 🧪 Testing Examples

### cURL Commands

```bash
# Test health
curl http://localhost:8080/api/health

# Test metrics
curl http://localhost:8080/api/metrics/summary

# Test providers
curl http://localhost:8080/api/ai/providers

# Test models
curl http://localhost:8080/api/ai/models?provider=gemini

# Test tools
curl http://localhost:8080/api/tools

# Test chat (example)
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello, what tools do you have?",
    "provider": "gemini",
    "model": "gemini-1.5-flash"
  }'
```

### PowerShell Commands

```powershell
# Test health
Invoke-WebRequest -Uri "http://localhost:8080/api/health" -UseBasicParsing

# Test metrics
Invoke-WebRequest -Uri "http://localhost:8080/api/metrics/summary" -UseBasicParsing

# Test with CORS
Invoke-WebRequest -Uri "http://localhost:8080/api/health" -UseBasicParsing -Headers @{"Origin"="http://localhost:3000"}
```

## 📈 Response Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request (validation error) |
| 500 | Internal Server Error |
| 503 | Service Unavailable (circuit breaker open) |

## 🔒 CORS Configuration

The application supports CORS for the following origins:
- `http://localhost:3000`
- `http://localhost:5173`

## 📊 Monitoring Dashboard URLs

- **Health Dashboard**: `http://localhost:8080/api/health`
- **Metrics Dashboard**: `http://localhost:8080/api/metrics/summary`
- **JVM Dashboard**: `http://localhost:8080/api/metrics/jvm`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

## 🚨 Error Responses

### Validation Error
```json
{
  "error": "Validation Error",
  "message": "Message cannot be empty",
  "details": {
    "field": "message",
    "rejectedValue": ""
  },
  "timestamp": "2025-09-03T13:00:00Z"
}
```

### Service Error
```json
{
  "error": "Chat Service Error",
  "message": "Failed to process chat request: Connection timeout",
  "timestamp": "2025-09-03T13:00:00Z"
}
```

### Circuit Breaker Open
```json
{
  "error": "Service Unavailable",
  "message": "Circuit breaker is OPEN for ai-provider",
  "timestamp": "2025-09-03T13:00:00Z"
}
```

## 🔧 Configuration

### Environment Variables
- `SERVER_PORT`: Application port (default: 8080)
- `SPRING_PROFILES_ACTIVE`: Active profile (dev/prod)
- `REDIS_HOST`: Redis host (optional)
- `REDIS_PORT`: Redis port (optional)

### Application Properties
See `src/main/resources/application.yml` for full configuration options.

---

*API Reference generated on: 2025-09-03*
*Application Version: 0.0.1-SNAPSHOT*
