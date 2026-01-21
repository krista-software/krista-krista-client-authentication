# Krista Client Authentication Extension - Architecture Documentation

**Version:** 3.5.7  
**Last Updated:** 2026-01-16  
**Extension Type:** Authentication Provider  
**Java Version:** 21

---

## Table of Contents

1. [Overview](#overview)
2. [5-Layer Architecture](#5-layer-architecture)
3. [Component Architecture](#component-architecture)
4. [Authentication Flow](#authentication-flow)
5. [Request Processing Flow](#request-processing-flow)
6. [Error Handling Architecture](#error-handling-architecture)
7. [Performance Characteristics](#performance-characteristics)
8. [Security Architecture](#security-architecture)
9. [Deployment Architecture](#deployment-architecture)
10. [Limitations and Constraints](#limitations-and-constraints)

---

## Overview

The Krista Client Authentication extension provides session-based authentication for Krista Web and Mobile clients. It implements the `RequestAuthenticator` interface to intercept and authenticate incoming HTTP requests using cookie-based session management.

### Key Architectural Principles

- **Layered Architecture**: Clear separation of concerns across 5 distinct layers
- **Dependency Injection**: HK2-based DI for loose coupling
- **Stateless Design**: No local state; all session data managed by platform SessionManager
- **Protocol Agnostic**: Designed for HTTP but extensible to other protocols
- **Security First**: HttpOnly cookies, HTTPS support, CORS validation

### Extension Metadata

```yaml
Name: Krista Client Authentication
Domain ID: catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7
Domain Name: Authentication
Ecosystem: Krista
JAX-RS ID: authn
Admin Rights: Required
Java Version: 21
```

---

## 5-Layer Architecture

The extension follows a strict 5-layer architecture pattern consistent with Krista extension design principles.

```mermaid
graph TB
    subgraph "Layer 1: Extension Layer"
        EXT[KristaClientAuthenticationExtension<br/>@Extension @Domain @StaticResource]
    end
    
    subgraph "Layer 2: Application Layer"
        APP[KristaClientApplication<br/>@ApplicationPath JAX-RS Config]
    end
    
    subgraph "Layer 3: API/Resource Layer"
        API[AuthenticationResource<br/>@Path REST Endpoints]
    end
    
    subgraph "Layer 4: Service/Logic Layer"
        SVC[KristaClientRequestAuthenticator<br/>implements RequestAuthenticator]
    end
    
    subgraph "Layer 5: Utility/Helper Layer"
        UTIL1[ApiConstants<br/>Constants]
        UTIL2[ApiUtility<br/>Cookie Parsing]
    end
    
    subgraph "External Dependencies"
        SM[SessionManager<br/>Platform Service]
        INV[Invoker<br/>Platform Service]
    end
    
    EXT -->|provides| SVC
    EXT -->|configures| APP
    APP -->|registers| API
    API -->|uses| SM
    API -->|uses| INV
    SVC -->|uses| SM
    SVC -->|uses| INV
    SVC -->|uses| UTIL2
    API -->|uses| UTIL1
    API -->|uses| UTIL2
    
    style EXT fill:#e1f5ff
    style APP fill:#fff4e1
    style API fill:#ffe1f5
    style SVC fill:#e1ffe1
    style UTIL1 fill:#f5f5f5
    style UTIL2 fill:#f5f5f5
    style SM fill:#ffe1e1
    style INV fill:#ffe1e1
```

### Layer 1: Extension Layer

**Class:** `KristaClientAuthenticationExtension`

**Responsibilities:**
- Bootstrap the authentication extension
- Register with Krista platform
- Provide `RequestAuthenticator` implementation via `@InvokerRequest`
- Configure static resources (documentation)
- Define custom tabs for UI

**Key Annotations:**
```java
@Extension(
    implementingDomainIds = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7",
    jaxrsId = "authn",
    requireWorkspaceAdminRights = true,
    name = "Krista Client Authentication",
    version = "3.5.7"
)
@Domain(id = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7", name = "Authentication")
@StaticResource(path = "docs", file = "docs")
```

**Dependencies:**
- `Invoker` (injected)
- `SessionManager` (injected)

**Invoker Requests:**
1. `AUTHENTICATOR` - Returns `KristaClientRequestAuthenticator` instance
2. `CUSTOM_TABS` - Returns documentation tab configuration

---

### Layer 2: Application Layer

**Class:** `KristaClientApplication`

**Responsibilities:**
- Configure JAX-RS application
- Register REST resource classes
- Configure message body handlers (JSON serialization)

**Key Annotations:**
```java
@Service
@ContractsProvided(Application.class)
@ApplicationPath("/")
```

**Configuration:**
- **Resources:** `AuthenticationResource.class`
- **Singletons:** `GsonMessageBodyHandler` for JSON processing
- **Base Path:** `/` (combined with JAX-RS ID becomes `/authn`)

---

### Layer 3: API/Resource Layer

**Class:** `AuthenticationResource`

**Responsibilities:**
- Handle HTTP authentication requests
- Implement REST API endpoints
- Manage CORS headers
- Deliver client-side JavaScript authenticator

**Endpoints:**

| Method | Path | Purpose | Response |
|--------|------|---------|----------|
| POST | `/login` | Authenticate user with session ID | 202 Accepted + Set-Cookie |
| OPTIONS | `/login` | CORS preflight handler | 200 OK + CORS headers |
| GET | `/authenticator` | Deliver JavaScript client | 200 OK + JavaScript code |

**Dependencies:**
- `SessionManager` (injected)
- `Invoker` (injected)

**Key Features:**
- CORS support with dynamic origin handling
- Cookie creation with security attributes
- JavaScript template substitution
- Cache control for static resources

---

### Layer 4: Service/Logic Layer

**Class:** `KristaClientRequestAuthenticator`

**Responsibilities:**
- Implement `RequestAuthenticator` interface
- Extract and validate session cookies
- Authenticate incoming requests
- Handle authentication failures with redirects

**Interface Methods:**

| Method | Return Type | Purpose |
|--------|-------------|---------|
| `getScheme()` | String | Returns "Krista" |
| `getSupportedProtocols()` | Set\<String\> | Returns ["HTTP"] |
| `getAuthenticatedAccountId()` | String | Extract account ID from request |
| `getMustAuthenticateResponse()` | ProtoResponse | Return 302 redirect to login |
| `setServiceAuthorization()` | boolean | Not supported (returns false) |
| `getAttributeFields()` | Map | No custom attributes (returns empty) |

**Authentication Algorithm:**
```
1. Extract Cookie header from HTTP request
2. Parse X-Krista-Context cookie using ApiUtility
3. Extract clientSessionId from cookie JSON
4. Call SessionManager.lookupAccountId(clientSessionId)
5. If account found → return account ID
6. If null and login API requested → extract from request body
7. Otherwise → return null (triggers MustAuthenticateException)
```

**Dependencies:**
- `Invoker` (for routing info)
- `SessionManager` (for session validation)
- `Gson` (for JSON parsing)

---

### Layer 5: Utility/Helper Layer

#### ApiConstants

**Purpose:** Define application-wide constants

**Constants:**
```java
X_KRISTA_SESSION_ID = "X-Krista-Session-Id"
X_KRISTA_ORIGINAL_URL = "X-Krista-Original-URI"
X_KRISTA_CONTEXT = "X-Krista-Context"
COOKIE = "Cookie"
LOGIN_API = "/login"
CLIENTSESSIONID = "clientSessionId"
BASE_URI_KEYWORD_TO_BE_REPLACED = "__BASE_URI__"
```

#### ApiUtility

**Purpose:** Cookie parsing and session ID extraction

**Key Methods:**

| Method | Parameters | Returns | Purpose |
|--------|------------|---------|---------|
| `getClientSessionIdFromCookie()` | String cookie | String | Extract session ID from cookie |
| `parseClientSessionIdFromCookie()` | String cookie | String | Parse and decode cookie value |
| `decodeRequestContext()` | String encoded | JsonObject | URL decode and parse JSON |

**Cookie Parsing Algorithm:**
```
1. Check if cookie contains "clientSessionId"
2. Parse cookies using CookiesParser.parseCookies()
3. Extract "X-Krista-Context" cookie
4. URL decode the cookie value
5. Parse JSON using Gson
6. Extract "clientSessionId" field
7. Return session ID or null
```

---

## Component Architecture

### Component Interaction Diagram

```mermaid
sequenceDiagram
    participant Client
    participant Platform as Krista Platform
    participant Ext as Extension Layer
    participant App as Application Layer
    participant API as API Layer
    participant Auth as Authenticator
    participant SM as SessionManager
    participant Util as ApiUtility

    Note over Client,Util: Extension Initialization
    Platform->>Ext: Load Extension
    Ext->>Ext: @Inject Invoker, SessionManager
    Ext->>Platform: Register AUTHENTICATOR InvokerRequest
    Ext->>App: Configure JAX-RS Application
    App->>API: Register AuthenticationResource

    Note over Client,Util: Authentication Request Flow
    Client->>Platform: POST /authn/login {clientSessionId}
    Platform->>API: Route to AuthenticationResource.login()
    API->>SM: lookupAccountId(clientSessionId)
    SM-->>API: accountId or null
    alt Valid Session
        API->>API: Create session cookie (Base64 encoded)
        API-->>Client: 202 Accepted + Set-Cookie
    else Invalid Session
        API-->>Client: 401 Unauthorized
    end

    Note over Client,Util: Subsequent Request Authentication
    Client->>Platform: GET /api/data (with Cookie)
    Platform->>Auth: getAuthenticatedAccountId(request)
    Auth->>Util: getClientSessionIdFromCookie(cookie)
    Util->>Util: Parse X-Krista-Context cookie
    Util->>Util: URL decode + JSON parse
    Util-->>Auth: clientSessionId
    Auth->>SM: lookupAccountId(clientSessionId)
    SM-->>Auth: accountId or null
    alt Authenticated
        Auth-->>Platform: accountId
        Platform->>Client: Process request
    else Not Authenticated
        Auth-->>Platform: null
        Platform->>Auth: getMustAuthenticateResponse()
        Auth-->>Platform: 302 Redirect to /authn/login
        Platform-->>Client: 302 Redirect
    end
```

### Dependency Graph

```mermaid
graph LR
    subgraph "Extension Components"
        EXT[Extension]
        APP[Application]
        API[Resource]
        AUTH[Authenticator]
        CONST[Constants]
        UTIL[Utility]
    end

    subgraph "Platform Services"
        SM[SessionManager]
        INV[Invoker]
    end

    subgraph "External Libraries"
        GSON[Gson]
        JAXRS[JAX-RS]
        HK2[HK2]
        SLF4J[SLF4J]
    end

    EXT -->|injects| SM
    EXT -->|injects| INV
    EXT -->|creates| AUTH
    API -->|injects| SM
    API -->|injects| INV
    API -->|uses| CONST
    API -->|uses| UTIL
    AUTH -->|uses| SM
    AUTH -->|uses| INV
    AUTH -->|uses| UTIL
    AUTH -->|uses| GSON
    UTIL -->|uses| GSON
    UTIL -->|uses| CONST
    API -->|uses| JAXRS
    EXT -->|uses| HK2
    API -->|uses| SLF4J
    AUTH -->|uses| SLF4J

    style EXT fill:#e1f5ff
    style SM fill:#ffe1e1
    style INV fill:#ffe1e1
```

---

## Authentication Flow

### Initial Login Flow

```mermaid
flowchart TD
    Start([Client Application]) --> A[Obtain clientSessionId<br/>from Krista Device Registration]
    A --> B[POST /authn/login<br/>Body: {clientSessionId}]
    B --> C{Cookie Header<br/>Present?}
    C -->|Yes| D[Extract from Cookie]
    C -->|No| E[Extract from Request Body]
    D --> F[SessionManager.lookupAccountId]
    E --> F
    F --> G{Account ID<br/>Found?}
    G -->|Yes| H[Base64 Encode Session ID]
    G -->|No| I[Return 401 Unauthorized]
    H --> J[Create Session Cookie<br/>HttpOnly, Secure, SameSite]
    J --> K[Add CORS Headers<br/>Allow-Origin, Allow-Credentials]
    K --> L[Return 202 Accepted<br/>+ Set-Cookie Header]
    L --> M([Client Stores Cookie])
    I --> N([Authentication Failed])

    style Start fill:#e1f5ff
    style M fill:#e1ffe1
    style N fill:#ffe1e1
```

### Request Authentication Flow

```mermaid
flowchart TD
    Start([Incoming HTTP Request]) --> A[Platform Intercepts Request]
    A --> B[Call RequestAuthenticator.<br/>getAuthenticatedAccountId]
    B --> C[Extract Cookie Header]
    C --> D{Cookie<br/>Contains<br/>clientSessionId?}
    D -->|No| E{Is Login<br/>API Request?}
    D -->|Yes| F[ApiUtility.getClientSessionIdFromCookie]
    F --> G[Parse X-Krista-Context Cookie]
    G --> H[URL Decode Cookie Value]
    H --> I[JSON Parse to Extract clientSessionId]
    I --> J[SessionManager.lookupAccountId]
    J --> K{Account ID<br/>Found?}
    K -->|Yes| L[Return Account ID]
    K -->|No| E
    E -->|Yes| M[Extract from Request Body/Query]
    E -->|No| N[Return null]
    M --> J
    L --> O([Request Authenticated<br/>Proceed with Processing])
    N --> P[Platform Throws<br/>MustAuthenticateException]
    P --> Q[Call getMustAuthenticateResponse]
    Q --> R[Extract X-Krista-Original-URI]
    R --> S[URL Encode Original URI]
    S --> T[Build Login URL<br/>/authn/login?X-Krista-Original-URI=...]
    T --> U[Return 302 Redirect]
    U --> V([Client Redirected to Login])

    style Start fill:#e1f5ff
    style O fill:#e1ffe1
    style V fill:#fff4e1
```

### Cookie Structure and Lifecycle

```mermaid
stateDiagram-v2
    [*] --> DeviceRegistration: User Registers Device
    DeviceRegistration --> SessionCreated: Platform Creates Session
    SessionCreated --> LoginRequest: Client Sends clientSessionId
    LoginRequest --> SessionValidation: SessionManager.lookupAccountId()

    state SessionValidation {
        [*] --> CheckSession
        CheckSession --> Valid: Account Found
        CheckSession --> Invalid: Account Not Found
        Valid --> [*]
        Invalid --> [*]
    }

    SessionValidation --> CookieCreated: Valid Session
    SessionValidation --> AuthFailed: Invalid Session

    CookieCreated --> CookieSet: Set-Cookie Header Sent
    CookieSet --> ActiveSession: Browser Stores Cookie

    state ActiveSession {
        [*] --> RequestSent
        RequestSent --> CookieExtracted: Extract Cookie Header
        CookieExtracted --> CookieParsed: Parse X-Krista-Context
        CookieParsed --> SessionLookup: Extract clientSessionId
        SessionLookup --> Authenticated: Valid Session
        SessionLookup --> Expired: Session Timeout
        Authenticated --> RequestSent: Next Request
    }

    ActiveSession --> SessionExpired: Timeout/Invalidation
    SessionExpired --> [*]
    AuthFailed --> [*]

    note right of CookieCreated
        Cookie Attributes:
        - HttpOnly: true
        - Secure: true (HTTPS)
        - SameSite: None (HTTPS)
        - Path: /
        - Value: Base64(clientSessionId)
    end note
```

---

## Request Processing Flow

### Complete Request Lifecycle

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant B as Browser/HTTP Client
    participant P as Krista Platform
    participant A as Authenticator
    participant R as AuthenticationResource
    participant S as SessionManager
    participant U as ApiUtility

    Note over C,U: Phase 1: Initial Authentication
    C->>B: Initiate Login
    B->>P: POST /authn/login<br/>{clientSessionId: "device_xxx"}
    P->>R: Route to login()
    R->>S: lookupAccountId(clientSessionId)
    S-->>R: accountId or null
    alt Valid Session
        R->>R: Base64.encode(clientSessionId)
        R->>R: Create cookie with security attributes
        R-->>P: 202 + Set-Cookie header
        P-->>B: 202 Accepted
        B->>B: Store cookie automatically
        B-->>C: Login Success
    else Invalid Session
        R-->>P: 401 Unauthorized
        P-->>B: 401 Unauthorized
        B-->>C: Login Failed
    end

    Note over C,U: Phase 2: Authenticated Request
    C->>B: Make API Request
    B->>P: GET /api/data<br/>Cookie: X-Krista-Context=...
    P->>A: getAuthenticatedAccountId(request)
    A->>U: getClientSessionIdFromCookie(cookie)
    U->>U: Parse cookies
    U->>U: Extract X-Krista-Context
    U->>U: URL decode
    U->>U: JSON parse
    U-->>A: clientSessionId
    A->>S: lookupAccountId(clientSessionId)
    S-->>A: accountId
    A-->>P: accountId
    P->>P: Process request with accountId
    P-->>B: 200 OK + Response Data
    B-->>C: Data Received

    Note over C,U: Phase 3: Unauthenticated Request
    C->>B: Request without cookie
    B->>P: GET /api/protected
    P->>A: getAuthenticatedAccountId(request)
    A->>U: getClientSessionIdFromCookie(null)
    U-->>A: null
    A-->>P: null
    P->>P: Throw MustAuthenticateException
    P->>A: getMustAuthenticateResponse(exception, request)
    A->>A: Extract X-Krista-Original-URI
    A->>A: Build login URL
    A-->>P: 302 Redirect to /authn/login
    P-->>B: 302 Redirect
    B->>P: GET /authn/login?X-Krista-Original-URI=...
    B-->>C: Show Login Page
```

### JavaScript Authenticator Delivery Flow

```mermaid
flowchart LR
    A[Client Request] --> B[GET /authn/authenticator?key=MyAuth]
    B --> C[AuthenticationResource.getAuthenticator]
    C --> D[Load authenticator.js from resources]
    D --> E{Resource<br/>Found?}
    E -->|No| F[Return 404 Not Found]
    E -->|Yes| G[Read file content]
    G --> H[Extract workspace URL from headers]
    H --> I[Prepare template substitutions]
    I --> J[Replace __BASE_URI__<br/>with workspace URL]
    J --> K[Replace __JSAuthFn__<br/>with function name]
    K --> L[Replace __JSWindowVariable__<br/>with window variable]
    L --> M[Set Cache-Control:<br/>private, max-age=604800]
    M --> N[Set Content-Type:<br/>text/javascript]
    N --> O[Return 200 OK + JavaScript]
    O --> P[Client Executes JavaScript]
    P --> Q[window.MyAuth.login available]

    style A fill:#e1f5ff
    style F fill:#ffe1e1
    style Q fill:#e1ffe1
```

---

## Error Handling Architecture

### Error Handling Strategy

```mermaid
graph TB
    subgraph "Error Categories"
        E1[Authentication Errors]
        E2[Session Errors]
        E3[CORS Errors]
        E4[Resource Errors]
        E5[Platform Errors]
    end

    subgraph "Error Handlers"
        H1[AuthenticationResource<br/>Exception Handling]
        H2[RequestAuthenticator<br/>Null Returns]
        H3[Platform Exception<br/>Handlers]
    end

    subgraph "Error Responses"
        R1[401 Unauthorized]
        R2[302 Redirect to Login]
        R3[404 Not Found]
        R4[500 Internal Server Error]
        R5[200 OK with CORS]
    end

    E1 --> H1
    E2 --> H2
    E3 --> H1
    E4 --> H1
    E5 --> H3

    H1 --> R1
    H2 --> R2
    H1 --> R3
    H1 --> R5
    H3 --> R4

    style E1 fill:#ffe1e1
    style E2 fill:#ffe1e1
    style E3 fill:#fff4e1
    style E4 fill:#fff4e1
    style E5 fill:#ffe1e1
```

### Error Scenarios and Handling

| Error Scenario | Detection Point | Handler | Response | Recovery |
|----------------|----------------|---------|----------|----------|
| **Invalid Session ID** | `SessionManager.lookupAccountId()` returns null | `AuthenticationResource.login()` | 401 Unauthorized | Client obtains new session ID |
| **Expired Session** | `SessionManager.lookupAccountId()` returns null | `KristaClientRequestAuthenticator` | null → 302 Redirect | Client re-authenticates |
| **Missing Cookie** | `ApiUtility.getClientSessionIdFromCookie()` returns null | `KristaClientRequestAuthenticator` | null → 302 Redirect | Client authenticates |
| **Malformed Cookie** | JSON parse exception in `ApiUtility` | Caught, logged, returns null | null → 302 Redirect | Client clears cookies, re-authenticates |
| **CORS Preflight Failure** | Missing Origin header | `AuthenticationResource.loginOptions()` | 200 OK with CORS headers | Client retries with proper headers |
| **Resource Not Found** | `authenticator.js` not in classpath | `AuthenticationResource.getAuthenticator()` | 404 Not Found | Reinstall extension |
| **SessionManager Unavailable** | Platform service error | Exception propagates | 500 Internal Server Error | Check platform services |
| **Invalid Request Body** | JSON parse error | `AuthenticationResource.login()` | 401 Unauthorized | Client sends valid JSON |

### Error Flow Diagram

```mermaid
flowchart TD
    Start([Request Received]) --> A{Request Type}

    A -->|Login Request| B[AuthenticationResource.login]
    A -->|Authenticated Request| C[RequestAuthenticator.getAuthenticatedAccountId]
    A -->|Authenticator JS| D[AuthenticationResource.getAuthenticator]

    B --> B1{Session Valid?}
    B1 -->|Yes| B2[Create Cookie]
    B1 -->|No| B3[Log Error]
    B2 --> B4[202 Accepted]
    B3 --> B5[401 Unauthorized]

    C --> C1{Cookie Present?}
    C1 -->|Yes| C2[Parse Cookie]
    C1 -->|No| C3[Return null]
    C2 --> C4{Parse Success?}
    C4 -->|Yes| C5[Validate Session]
    C4 -->|No| C6[Log Error + Return null]
    C5 --> C7{Session Valid?}
    C7 -->|Yes| C8[Return Account ID]
    C7 -->|No| C3
    C3 --> C9[Platform Throws Exception]
    C6 --> C9
    C9 --> C10[getMustAuthenticateResponse]
    C10 --> C11[302 Redirect]

    D --> D1{Resource Exists?}
    D1 -->|Yes| D2[Load and Process]
    D1 -->|No| D3[404 Not Found]
    D2 --> D4[200 OK + JS]

    B4 --> End([Success])
    B5 --> End2([Error Response])
    C8 --> End
    C11 --> End3([Redirect])
    D4 --> End
    D3 --> End2

    style Start fill:#e1f5ff
    style End fill:#e1ffe1
    style End2 fill:#ffe1e1
    style End3 fill:#fff4e1
```

---

## Performance Characteristics

### Performance Metrics

| Operation | Typical Latency | Bottleneck | Optimization Strategy |
|-----------|----------------|------------|----------------------|
| **POST /login** | < 100ms | SessionManager lookup | Platform-level session caching |
| **GET /authenticator** | < 50ms | File I/O | HTTP cache (max-age=604800) |
| **Request Authentication** | < 10ms | Cookie parsing + JSON | Efficient Gson parsing, minimal allocations |
| **Session Validation** | < 20ms | SessionManager query | Platform connection pooling |
| **Cookie Parsing** | < 5ms | String operations | Optimized regex-free parsing |
| **Redirect Generation** | < 5ms | URL encoding | Minimal string operations |

### Performance Architecture

```mermaid
graph TB
    subgraph "Performance Layers"
        L1[HTTP Cache Layer<br/>604800s for JS]
        L2[Platform Session Cache<br/>SessionManager]
        L3[Efficient Parsing<br/>Gson + CookiesParser]
        L4[Minimal Allocations<br/>Reuse objects]
    end

    subgraph "Bottlenecks"
        B1[SessionManager Lookup<br/>~20ms]
        B2[File I/O<br/>~10ms first request]
        B3[JSON Parsing<br/>~5ms]
        B4[Cookie Parsing<br/>~5ms]
    end

    subgraph "Optimizations"
        O1[Browser Cache<br/>Reduces JS requests]
        O2[Platform Caching<br/>Reduces DB queries]
        O3[Stateless Design<br/>No local cache needed]
        O4[Connection Pooling<br/>Reuse connections]
    end

    L1 -.mitigates.-> B2
    L2 -.mitigates.-> B1
    L3 -.mitigates.-> B3
    L3 -.mitigates.-> B4

    O1 --> L1
    O2 --> L2
    O3 --> L4
    O4 --> B1

    style B1 fill:#ffe1e1
    style B2 fill:#ffe1e1
    style B3 fill:#fff4e1
    style B4 fill:#fff4e1
    style O1 fill:#e1ffe1
    style O2 fill:#e1ffe1
    style O3 fill:#e1ffe1
    style O4 fill:#e1ffe1
```

### Scalability Characteristics

```mermaid
graph LR
    subgraph "Scalability Factors"
        S1[Concurrent Logins]
        S2[Active Sessions]
        S3[Request Rate]
        S4[Cookie Size]
    end

    subgraph "Scaling Limits"
        L1[Platform-Dependent<br/>SessionManager Capacity]
        L2[~4KB Browser Limit<br/>Minimal cookie data]
        L3[Stateless Design<br/>Horizontal Scaling]
        L4[No Local State<br/>No Synchronization]
    end

    S1 --> L1
    S2 --> L1
    S3 --> L3
    S4 --> L2

    L3 --> L4

    style S1 fill:#e1f5ff
    style S2 fill:#e1f5ff
    style S3 fill:#e1f5ff
    style S4 fill:#e1f5ff
    style L3 fill:#e1ffe1
    style L4 fill:#e1ffe1
```

### Performance Best Practices

1. **Client-Side Caching**
   - Cache `GET /authenticator` response for 7 days (604800 seconds)
   - Reduces server load and improves client performance
   - Browser automatically handles cache validation

2. **Session Management**
   - Platform SessionManager handles session caching
   - No local caching needed in extension
   - Stateless design enables horizontal scaling

3. **Efficient Parsing**
   - Use Gson for JSON parsing (optimized for performance)
   - CookiesParser for cookie parsing (JAX-RS standard)
   - Minimal string allocations

4. **Connection Pooling**
   - Platform manages connection pooling to SessionManager
   - Reuse connections for session lookups
   - Reduces connection overhead

5. **Monitoring Metrics**
   - Track SessionManager lookup latency
   - Monitor authentication failure rates
   - Alert on high redirect rates (indicates session issues)

---

## Security Architecture

### Security Layers

```mermaid
graph TB
    subgraph "Layer 1: Transport Security"
        T1[HTTPS Required<br/>Production]
        T2[TLS 1.2+<br/>Encryption]
    end

    subgraph "Layer 2: Cookie Security"
        C1[HttpOnly Flag<br/>XSS Protection]
        C2[Secure Flag<br/>HTTPS Only]
        C3[SameSite=None<br/>CSRF Protection]
        C4[Base64 Encoding<br/>Safe Transmission]
    end

    subgraph "Layer 3: Session Security"
        S1[Platform SessionManager<br/>Centralized Control]
        S2[Session Timeout<br/>Auto Expiration]
        S3[Session Validation<br/>Every Request]
    end

    subgraph "Layer 4: Access Control"
        A1[Workspace Admin Rights<br/>Required]
        A2[Account Validation<br/>SessionManager]
        A3[No Password Storage<br/>Session IDs Only]
    end

    subgraph "Layer 5: CORS Security"
        O1[Origin Validation<br/>Whitelist Check]
        O2[Credentials Required<br/>No Wildcards]
        O3[Limited Methods<br/>POST, OPTIONS]
    end

    T1 --> C2
    T2 --> C2
    C1 --> S3
    C2 --> S3
    C3 --> S3
    S1 --> A2
    S2 --> S3
    O1 --> O2

    style T1 fill:#e1ffe1
    style C1 fill:#e1ffe1
    style C2 fill:#e1ffe1
    style C3 fill:#e1ffe1
    style S1 fill:#e1ffe1
    style S3 fill:#e1ffe1
    style A1 fill:#e1ffe1
    style O1 fill:#e1ffe1
```

### Threat Model and Mitigations

```mermaid
flowchart LR
    subgraph "Threats"
        T1[Session Hijacking]
        T2[CSRF Attacks]
        T3[XSS Attacks]
        T4[MITM Attacks]
        T5[Brute Force]
        T6[Session Fixation]
    end

    subgraph "Mitigations"
        M1[HttpOnly + Secure Cookies]
        M2[SameSite=None + CORS]
        M3[HttpOnly Flag]
        M4[HTTPS Required]
        M5[Platform Rate Limiting]
        M6[Platform Session Generation]
    end

    subgraph "Residual Risk"
        R1[Low<br/>Requires HTTPS Compromise]
        R2[Low<br/>Requires Whitelist Bypass]
        R3[Low<br/>Cookie Not Accessible]
        R4[Low<br/>TLS Protection]
        R5[Low<br/>Platform Managed]
        R6[Low<br/>Secure Generation]
    end

    T1 --> M1 --> R1
    T2 --> M2 --> R2
    T3 --> M3 --> R3
    T4 --> M4 --> R4
    T5 --> M5 --> R5
    T6 --> M6 --> R6

    style T1 fill:#ffe1e1
    style T2 fill:#ffe1e1
    style T3 fill:#ffe1e1
    style T4 fill:#ffe1e1
    style T5 fill:#ffe1e1
    style T6 fill:#ffe1e1
    style R1 fill:#e1ffe1
    style R2 fill:#e1ffe1
    style R3 fill:#e1ffe1
    style R4 fill:#e1ffe1
    style R5 fill:#e1ffe1
    style R6 fill:#e1ffe1
```

### Security Configuration

| Security Feature | Configuration | Purpose | Impact |
|------------------|---------------|---------|--------|
| **HttpOnly Cookie** | Always enabled | Prevent JavaScript access to cookie | Mitigates XSS attacks |
| **Secure Cookie** | Enabled on HTTPS | HTTPS-only transmission | Mitigates MITM attacks |
| **SameSite Attribute** | None (HTTPS) or not set (HTTP) | Explicit cross-site intent | Mitigates CSRF attacks |
| **CORS Validation** | Origin whitelist required | Restrict cross-origin access | Prevents unauthorized origins |
| **Session Timeout** | Platform-managed | Auto-expire inactive sessions | Reduces exposure window |
| **Admin Rights** | Required for extension | Controlled account management | Limits privilege escalation |
| **No Password Storage** | Session IDs only | Avoid credential exposure | Reduces attack surface |
| **Base64 Encoding** | Cookie values | Safe cookie transmission | Prevents special char issues |

### Security Best Practices

1. **Always Use HTTPS in Production**
   - Secure flag requires HTTPS
   - Prevents cookie interception
   - Enables SameSite=None for cross-origin

2. **Whitelist CORS Origins**
   - Never use wildcard (*) with credentials
   - Validate origin against platform whitelist
   - Log unauthorized origin attempts

3. **Monitor Authentication Activity**
   - Track failed authentication attempts
   - Alert on unusual session patterns
   - Monitor SessionManager errors

4. **Regular Security Audits**
   - Review workspace admin access
   - Audit session timeout policies
   - Validate CORS configuration

5. **Input Validation**
   - Validate all request parameters
   - Sanitize session IDs before logging
   - Handle malformed cookies gracefully

---

## Deployment Architecture

### Deployment Model

```mermaid
graph TB
    subgraph "Krista Platform"
        P[Platform Core]
        SM[SessionManager Service]
        INV[Invoker Service]
        EXT_MGR[Extension Manager]
    end

    subgraph "Extension Deployment"
        JAR[Extension JAR<br/>krista-client-authentication-3.5.7.jar]

        subgraph "Extension Components"
            E[Extension Class]
            A[Application Class]
            R[Resource Class]
            AUTH[Authenticator Class]
            U[Utility Classes]
        end

        subgraph "Static Resources"
            JS[authenticator.js]
            DOCS[Documentation]
        end
    end

    subgraph "Runtime Environment"
        JVM[Java 21 JVM]
        HK2_RT[HK2 Runtime]
        JAXRS_RT[JAX-RS Runtime]
    end

    EXT_MGR -->|loads| JAR
    JAR -->|contains| E
    JAR -->|contains| A
    JAR -->|contains| R
    JAR -->|contains| AUTH
    JAR -->|contains| U
    JAR -->|contains| JS
    JAR -->|contains| DOCS

    E -->|registers with| P
    E -->|uses| SM
    E -->|uses| INV
    A -->|registers| R
    R -->|uses| SM
    AUTH -->|uses| SM

    P -->|runs on| JVM
    E -->|uses| HK2_RT
    R -->|uses| JAXRS_RT

    style JAR fill:#e1f5ff
    style P fill:#ffe1e1
    style JVM fill:#fff4e1
```

### Installation Flow

```mermaid
sequenceDiagram
    participant Admin
    participant UI as Krista UI
    participant EM as Extension Manager
    participant Platform
    participant Ext as Extension

    Admin->>UI: Navigate to Extensions
    Admin->>UI: Search "Krista Client Authentication"
    Admin->>UI: Click Install
    UI->>EM: Install Extension Request
    EM->>EM: Download Extension JAR
    EM->>EM: Validate Extension Signature
    EM->>Platform: Load Extension Class
    Platform->>Ext: Initialize Extension
    Ext->>Platform: Register @InvokerRequest(AUTHENTICATOR)
    Ext->>Platform: Register @InvokerRequest(CUSTOM_TABS)
    Ext->>Platform: Register JAX-RS Application
    Platform->>Platform: Configure /authn endpoint
    Platform-->>EM: Extension Loaded
    EM-->>UI: Installation Complete
    UI-->>Admin: Show Extension Active

    Admin->>UI: Enable "Trust Extension" Flag
    UI->>EM: Update Extension Permissions
    EM->>Platform: Grant Workspace Admin Rights
    Platform-->>EM: Permissions Updated
    EM-->>UI: Trust Flag Enabled
    UI-->>Admin: Extension Ready
```

### Runtime Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Browser]
        MOBILE[Mobile App]
        EXT_CLIENT[Other Extensions]
    end

    subgraph "Platform Layer"
        GATEWAY[API Gateway]
        AUTH_INT[Authentication Interceptor]
        ROUTER[Request Router]
    end

    subgraph "Extension Layer"
        AUTHN_EXT[Krista Client Authentication<br/>Extension]

        subgraph "Request Handlers"
            LOGIN[POST /authn/login]
            OPTIONS[OPTIONS /authn/login]
            JS[GET /authn/authenticator]
        end

        subgraph "Authentication"
            REQ_AUTH[RequestAuthenticator<br/>Implementation]
        end
    end

    subgraph "Platform Services"
        SM[SessionManager]
        ACCT[Account Service]
        ROLE[Role Service]
    end

    WEB -->|HTTPS| GATEWAY
    MOBILE -->|HTTPS| GATEWAY
    EXT_CLIENT -->|Internal| ROUTER

    GATEWAY --> AUTH_INT
    AUTH_INT -->|/authn/*| ROUTER
    AUTH_INT -->|other requests| REQ_AUTH

    ROUTER --> LOGIN
    ROUTER --> OPTIONS
    ROUTER --> JS

    REQ_AUTH -->|validate session| SM
    LOGIN -->|validate session| SM

    SM --> ACCT
    SM --> ROLE

    style WEB fill:#e1f5ff
    style MOBILE fill:#e1f5ff
    style AUTHN_EXT fill:#e1ffe1
    style SM fill:#ffe1e1
```

---

## Limitations and Constraints

### Architectural Limitations

```mermaid
mindmap
    root((Limitations))
        Authentication
            No Token Refresh
            Single Session per Client
            No Custom Attributes
            No Service Authorization
        Protocol Support
            HTTP Only
            No WebSocket Support
            No gRPC Support
        Authorization
            No Authorization Handling
            getMustAuthorizeResponse returns null
        Session Management
            Platform-Dependent Timeout
            No Local Session Cache
            No Session Refresh API
        Deployment
            Requires Admin Rights
            Trust Flag Mandatory
            Single Workspace Only
        Performance
            SessionManager Latency
            No Request Batching
            No Async Processing
```

### Detailed Limitations

| Category | Limitation | Impact | Workaround |
|----------|-----------|--------|------------|
| **Session Management** | No token refresh mechanism | Users must re-authenticate on session expiry | Implement session monitoring in client |
| **Session Management** | Single session per client ID | Multiple sessions require multiple client IDs | Use different client IDs for different contexts |
| **Authentication** | No custom attribute fields | `getAttributeFields()` returns empty map | Store attributes in platform account service |
| **Authentication** | No service authorization | `setServiceAuthorization()` returns false | Use platform service accounts |
| **Protocol Support** | HTTP protocol only | `getSupportedProtocols()` returns HTTP only | Not applicable for current use cases |
| **Authorization** | No authorization handling | `getMustAuthorizeResponse()` returns null | Implement authorization in application layer |
| **Deployment** | Requires workspace admin rights | Cannot function without trust flag | Must enable trust flag in extension settings |
| **Performance** | SessionManager lookup latency | ~20ms per request | Platform-level optimization only |
| **Scalability** | Platform-dependent session storage | Limited by platform capacity | Scale platform infrastructure |
| **Cookie Size** | ~4KB browser limit | Minimal data in cookie (session ID only) | Use session ID as reference, store data server-side |

### Constraint Matrix

```mermaid
graph TB
    subgraph "Hard Constraints"
        H1[Java 21 Required]
        H2[Krista Platform 3.5.7+]
        H3[Workspace Admin Rights]
        H4[Trust Flag Enabled]
        H5[SessionManager Available]
    end

    subgraph "Soft Constraints"
        S1[HTTPS Recommended]
        S2[CORS Whitelist Configured]
        S3[Session Timeout Policy]
        S4[Browser Cookie Support]
    end

    subgraph "Design Constraints"
        D1[Stateless Design]
        D2[HTTP Protocol Only]
        D3[No Local Caching]
        D4[Platform Session Management]
    end

    subgraph "Operational Constraints"
        O1[Single Workspace Deployment]
        O2[No Cross-Workspace Sessions]
        O3[Platform-Dependent Performance]
        O4[No Offline Support]
    end

    H1 --> D1
    H2 --> H5
    H3 --> H4
    S1 --> S4
    D1 --> D3
    D4 --> O3
    O1 --> O2

    style H1 fill:#ffe1e1
    style H2 fill:#ffe1e1
    style H3 fill:#ffe1e1
    style H4 fill:#ffe1e1
    style H5 fill:#ffe1e1
    style S1 fill:#fff4e1
    style D1 fill:#e1f5ff
    style O3 fill:#fff4e1
```

### Feature Support Matrix

| Feature | Supported | Implementation | Notes |
|---------|-----------|----------------|-------|
| **Session-Based Auth** | ✅ Yes | Cookie-based sessions | Primary authentication method |
| **Token-Based Auth** | ❌ No | Not implemented | Use session IDs instead |
| **OAuth 2.0** | ❌ No | Not implemented | Platform-level feature |
| **SAML** | ❌ No | Not implemented | Platform-level feature |
| **Multi-Factor Auth** | ❌ No | Not implemented | Platform-level feature |
| **Session Refresh** | ❌ No | Not implemented | Re-authenticate on expiry |
| **Custom Attributes** | ❌ No | Returns empty map | Use platform account service |
| **Service Authorization** | ❌ No | Returns false | Use platform service accounts |
| **HTTP Protocol** | ✅ Yes | Full support | Primary protocol |
| **WebSocket** | ❌ No | Not supported | Use HTTP polling |
| **gRPC** | ❌ No | Not supported | Use HTTP REST |
| **CORS Support** | ✅ Yes | Dynamic origin handling | Requires whitelist configuration |
| **Cookie Security** | ✅ Yes | HttpOnly, Secure, SameSite | Full security attributes |
| **Redirect Handling** | ✅ Yes | 302 redirect to login | Automatic unauthenticated handling |
| **JavaScript Client** | ✅ Yes | Delivered via GET /authenticator | Cached for 7 days |
| **Mobile Support** | ✅ Yes | Cookie-based auth | Requires cookie storage |
| **Extension Dependency** | ✅ Yes | @Dependency annotation | Can be used by other extensions |
| **Custom Tabs** | ✅ Yes | Documentation tab | Static resource serving |
| **Horizontal Scaling** | ✅ Yes | Stateless design | No local state |
| **Session Clustering** | ⚠️ Platform | SessionManager handles | Platform-dependent |

### Performance Constraints

```mermaid
graph LR
    subgraph "Latency Constraints"
        L1[SessionManager Lookup<br/>~20ms]
        L2[Cookie Parsing<br/>~5ms]
        L3[JSON Parsing<br/>~5ms]
        L4[Total Request Auth<br/>~30ms]
    end

    subgraph "Throughput Constraints"
        T1[Concurrent Logins<br/>Platform-Dependent]
        T2[Request Rate<br/>Stateless = High]
        T3[Session Validation<br/>Platform-Dependent]
    end

    subgraph "Resource Constraints"
        R1[Memory<br/>Minimal - No Caching]
        R2[CPU<br/>Low - Simple Parsing]
        R3[Network<br/>SessionManager Calls]
        R4[Storage<br/>None - Stateless]
    end

    L1 --> L4
    L2 --> L4
    L3 --> L4

    T1 --> T3
    T2 --> R2
    T3 --> R3

    style L1 fill:#fff4e1
    style L4 fill:#ffe1e1
    style T2 fill:#e1ffe1
    style R1 fill:#e1ffe1
    style R4 fill:#e1ffe1
```

### Operational Constraints

1. **Deployment Constraints**
   - Must be deployed to Krista workspace
   - Cannot run standalone
   - Requires platform services (SessionManager, Invoker)
   - Single workspace deployment only

2. **Configuration Constraints**
   - JAX-RS ID cannot be changed after deployment
   - Domain ID is fixed
   - Admin rights requirement cannot be disabled
   - Static resource path is fixed

3. **Runtime Constraints**
   - Depends on platform SessionManager availability
   - No graceful degradation if SessionManager unavailable
   - No offline mode support
   - No request queuing or batching

4. **Integration Constraints**
   - Other extensions must use @Dependency annotation
   - Cannot be used outside Krista platform
   - CORS origins must be whitelisted at platform level
   - No custom authentication schemes

### Mitigation Strategies

```mermaid
flowchart TB
    subgraph "Limitations"
        L1[No Session Refresh]
        L2[SessionManager Latency]
        L3[Single Session per Client]
        L4[No Custom Attributes]
    end

    subgraph "Mitigations"
        M1[Client-Side Session Monitoring<br/>Proactive Re-auth]
        M2[Platform-Level Caching<br/>Connection Pooling]
        M3[Multiple Client IDs<br/>Per Context]
        M4[Platform Account Service<br/>Store Attributes]
    end

    subgraph "Best Practices"
        B1[Monitor Session Expiry<br/>Refresh Before Timeout]
        B2[Implement Retry Logic<br/>Exponential Backoff]
        B3[Use Separate Sessions<br/>For Different Apps]
        B4[Leverage Platform APIs<br/>For Extended Data]
    end

    L1 --> M1 --> B1
    L2 --> M2 --> B2
    L3 --> M3 --> B3
    L4 --> M4 --> B4

    style L1 fill:#ffe1e1
    style L2 fill:#ffe1e1
    style L3 fill:#ffe1e1
    style L4 fill:#ffe1e1
    style M1 fill:#fff4e1
    style M2 fill:#fff4e1
    style M3 fill:#fff4e1
    style M4 fill:#fff4e1
    style B1 fill:#e1ffe1
    style B2 fill:#e1ffe1
    style B3 fill:#e1ffe1
    style B4 fill:#e1ffe1
```

---

## Appendix

### Technology Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Language** | Java | 21 | Primary implementation language |
| **Build Tool** | Gradle | 8.5+ | Build automation |
| **DI Framework** | HK2 | 3.0.5 | Dependency injection |
| **REST Framework** | JAX-RS | 3.1.0 | REST API implementation |
| **JSON Library** | Gson | 2.10.1 | JSON parsing and serialization |
| **Logging** | SLF4J | 2.0.9 | Logging abstraction |
| **Testing** | JUnit Jupiter | 5.10.1 | Unit testing |
| **Mocking** | Mockito | 5.7.0 | Test mocking |
| **Code Coverage** | JaCoCo | 0.8.11 | Coverage reporting |
| **Platform APIs** | Krista APIs | 1.0.121-rc1 | Platform integration |

### Key Interfaces and Classes

```java
// Extension Entry Point
@Extension
public class KristaClientAuthenticationExtension {
    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getAuthenticatedAccountId();
}

// Request Authenticator Implementation
public class KristaClientRequestAuthenticator implements RequestAuthenticator {
    String getScheme();
    Set<String> getSupportedProtocols();
    String getAuthenticatedAccountId(ProtoRequest request);
    ProtoResponse getMustAuthenticateResponse(MustAuthenticateException cause, ProtoRequest request);
}

// REST API Resource
@Path("/")
public class AuthenticationResource {
    @POST @Path("/login")
    Response login(Map<String, String> loginInput);

    @OPTIONS @Path("/login")
    Response loginOptions();

    @GET @Path("/authenticator")
    Response getAuthenticator(@QueryParam("key") String key);
}

// Utility Classes
public class ApiUtility {
    static String getClientSessionIdFromCookie(String cookie);
    static String parseClientSessionIdFromCookie(String cookie);
}

public class ApiConstants {
    static final String X_KRISTA_SESSION_ID;
    static final String X_KRISTA_ORIGINAL_URL;
    static final String X_KRISTA_CONTEXT;
    static final String CLIENTSESSIONID;
}
```

### Configuration Reference

```yaml
# Extension Configuration
extension:
  name: "Krista Client Authentication"
  version: "3.5.7"
  domain_id: "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7"
  domain_name: "Authentication"
  jaxrs_id: "authn"
  admin_rights: true
  java_version: 21

# Endpoints
endpoints:
  - method: POST
    path: /authn/login
    auth_required: false
  - method: OPTIONS
    path: /authn/login
    auth_required: false
  - method: GET
    path: /authn/authenticator
    auth_required: false

# Security
security:
  cookie:
    http_only: true
    secure: true  # HTTPS only
    same_site: "None"  # HTTPS, or not set for HTTP
    path: "/"
  cors:
    allow_credentials: true
    allow_methods: ["POST", "OPTIONS"]
    allow_headers: ["Content-Type", "Accept"]

# Performance
performance:
  cache:
    authenticator_js: 604800  # 7 days
  timeouts:
    session_lookup: 5000  # 5 seconds
```

### Glossary

- **SessionManager**: Platform service that manages user sessions and account lookups
- **Invoker**: Platform service that executes requests and provides routing information
- **RequestAuthenticator**: Interface for implementing custom authentication logic
- **InvokerRequest**: Extension capability provided to the platform
- **clientSessionId**: Unique identifier for a client session, obtained from device registration
- **X-Krista-Context**: Cookie name containing session context information
- **CORS**: Cross-Origin Resource Sharing - mechanism for cross-domain requests
- **HttpOnly**: Cookie attribute preventing JavaScript access
- **Secure**: Cookie attribute requiring HTTPS transmission
- **SameSite**: Cookie attribute controlling cross-site request behavior
- **Trust Extension Flag**: Platform setting granting workspace admin rights to extension

---

## Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-16 | Architecture Team | Initial architecture documentation with Mermaid diagrams |

---

**End of Architecture Documentation**

