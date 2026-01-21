# Integration Guide

## Overview

This guide shows how to integrate Krista Client Authentication into your web or mobile applications.

## Web Application Integration

### Step 1: Obtain Client Session ID

First, register your device/client with Krista to obtain a session ID:

```javascript
// This is typically done through Krista's device registration API
// Contact your Krista administrator for the registration endpoint
const sessionId = await registerDevice();
```

### Step 2: Authenticate User

Call the login API with the session ID:

```javascript
async function login(clientSessionId) {
  const response = await fetch('https://your-workspace.krista.com/authn/login', {
    method: 'POST',
    credentials: 'include', // Important: include cookies
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ clientSessionId })
  });
  
  if (response.status === 202) {
    console.log('Authentication successful');
    return true;
  } else if (response.status === 401) {
    console.error('Invalid session ID');
    return false;
  }
}
```

### Step 3: Make Authenticated Requests

After successful login, the session cookie is automatically included:

```javascript
async function makeAuthenticatedRequest() {
  const response = await fetch('https://your-workspace.krista.com/api/data', {
    credentials: 'include' // Include session cookie
  });
  
  if (response.status === 401) {
    // Session expired, re-authenticate
    await login(newSessionId);
  }
  
  return response.json();
}
```

## Mobile Application Integration

### iOS Example (Swift)

```swift
func login(sessionId: String) async throws {
    let url = URL(string: "https://your-workspace.krista.com/authn/login")!
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    
    let body = ["clientSessionId": sessionId]
    request.httpBody = try JSONEncoder().encode(body)
    
    let (_, response) = try await URLSession.shared.data(for: request)
    
    guard let httpResponse = response as? HTTPURLResponse,
          httpResponse.statusCode == 202 else {
        throw AuthError.invalidSession
    }
}
```

### Android Example (Kotlin)

```kotlin
suspend fun login(sessionId: String): Boolean {
    val client = OkHttpClient()
    val json = JSONObject().put("clientSessionId", sessionId)
    
    val request = Request.Builder()
        .url("https://your-workspace.krista.com/authn/login")
        .post(json.toString().toRequestBody("application/json".toMediaType()))
        .build()
    
    val response = client.newCall(request).execute()
    return response.code == 202
}
```

## React Integration Example

```javascript
import React, { useState, useEffect } from 'react';

function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  
  useEffect(() => {
    // Check if already authenticated
    checkAuth();
  }, []);
  
  async function checkAuth() {
    try {
      const response = await fetch('/api/check-auth', {
        credentials: 'include'
      });
      setIsAuthenticated(response.ok);
    } catch (error) {
      setIsAuthenticated(false);
    }
  }
  
  async function login(sessionId) {
    const response = await fetch('/authn/login', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ clientSessionId: sessionId })
    });
    
    if (response.status === 202) {
      setIsAuthenticated(true);
      return true;
    }
    return false;
  }
  
  return (
    <AuthContext.Provider value={{ isAuthenticated, login }}>
      {children}
    </AuthContext.Provider>
  );
}
```

## Extension Integration

### Adding as Dependency

To use this authentication extension in another Krista extension:

1. Add dependency in your extension's `pom.xml`:
```xml
<dependency>
    <groupId>app.krista.extensions.authentication</groupId>
    <artifactId>krista-client-authentication</artifactId>
    <version>3.5.7</version>
</dependency>
```

2. Configure in your extension setup to use Krista authentication

3. Your extension will automatically use the RequestAuthenticator for session validation

## Error Handling Best Practices

```javascript
async function robustLogin(sessionId, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await fetch('/authn/login', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ clientSessionId: sessionId })
      });
      
      if (response.status === 202) return true;
      if (response.status === 401) {
        // Invalid session, don't retry
        throw new Error('Invalid session ID');
      }
      // Server error, retry
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
    } catch (error) {
      if (i === maxRetries - 1) throw error;
    }
  }
  return false;
}
```

## Session Management

### Handling Session Expiry

```javascript
// Intercept 401 responses globally
async function fetchWithAuth(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    credentials: 'include'
  });
  
  if (response.status === 401) {
    // Session expired, redirect to login
    window.location.href = '/login';
  }
  
  return response;
}
```

### Logout Implementation

```javascript
async function logout() {
  // Clear session cookie by calling logout endpoint (if available)
  // Or simply clear local state and redirect
  document.cookie = 'clientSessionId=; Max-Age=0; path=/';
  window.location.href = '/login';
}
```

## Testing Integration

### Unit Test Example

```javascript
describe('Authentication', () => {
  it('should authenticate with valid session ID', async () => {
    const sessionId = 'device_test_123';
    const result = await login(sessionId);
    expect(result).toBe(true);
  });
  
  it('should reject invalid session ID', async () => {
    const sessionId = 'invalid';
    const result = await login(sessionId);
    expect(result).toBe(false);
  });
});
```

## See Also

- [Login API](pages/LoginAPI.md) - API endpoint details
- [Authentication](pages/Authentication.md) - Authentication flow
- [Troubleshooting](pages/Troubleshooting.md) - Common issues

