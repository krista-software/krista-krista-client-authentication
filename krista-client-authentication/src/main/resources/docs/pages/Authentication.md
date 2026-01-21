# Authentication

## Overview

The Krista Client Authentication extension provides session-based authentication for Krista Web and Mobile clients. It implements the `RequestAuthenticator` interface to validate user sessions and manage authentication state across requests.

## Authentication Mode

### Session-Based Authentication

The extension uses cookie-based session management with server-side validation:

- **Client Session ID**: Unique identifier for each client session
- **Cookie Storage**: Session ID stored in HttpOnly cookies
- **Server Validation**: SessionManager validates session on every request
- **Account Lookup**: Maps session ID to Krista account ID

**When to Use**:
- Web applications requiring persistent login sessions
- Mobile applications with session management
- Applications integrated with Krista Portal
- Extensions requiring user authentication

## Authentication Scheme

The extension implements the "Krista" authentication scheme:

```java
@Override
public String getScheme() {
    return "Krista";
}
```

**Supported Protocols**: HTTP/HTTPS

## Authentication Flow

### 1. Initial Authentication Request

```
Client Application
       ↓
   POST /authn/login
   {clientSessionId: "device_xxx"}
       ↓
   SessionManager validates session
       ↓
   Returns 202 Accepted + Set-Cookie
       ↓
   Client stores session cookie
```

### 2. Subsequent Authenticated Requests

```
Client sends request with Cookie header
       ↓
   RequestAuthenticator extracts session ID
       ↓
   SessionManager looks up account ID
       ↓
   Request proceeds with authenticated account
```

### 3. Unauthenticated Request Handling

```
Request without valid session
       ↓
   RequestAuthenticator returns null
       ↓
   MustAuthenticateException thrown
       ↓
   302 Redirect to /authn/login
```

## Session Management

### Session Creation

Sessions are created and managed by Krista's SessionManager:

1. Client obtains a session ID from Krista (device registration)
2. Client sends session ID to `/authn/login` endpoint
3. Extension validates session with SessionManager
4. If valid, session cookie is set with encoded session ID
5. Cookie is returned to client for subsequent requests

### Session Validation

On every request, the extension:

1. Extracts session ID from Cookie header
2. Decodes Base64-encoded session ID
3. Looks up account ID using SessionManager
4. Returns account ID if session is valid
5. Returns null if session is invalid or expired

### Session Storage

- **Client-Side**: Session ID stored in HttpOnly cookie
- **Server-Side**: Session-to-account mapping in SessionManager
- **Encoding**: Base64 encoding for safe transmission
- **Security**: HttpOnly, Secure, SameSite attributes

## Cookie Attributes

### HttpOnly
```
HttpOnly=true
```
- Prevents JavaScript access to cookies
- Protects against XSS attacks
- Cookie only accessible via HTTP(S) requests

### Secure
```
Secure=true (HTTPS only)
```
- Cookie only transmitted over HTTPS
- Automatically set for HTTPS connections
- Not set for HTTP (development only)

### SameSite
```
SameSite=None (HTTPS)
SameSite not set (HTTP)
```
- Prevents CSRF attacks
- Allows cross-site requests with credentials
- Requires Secure flag when set to None

### Path
```
path=/
```
- Cookie available for all paths in the domain
- Ensures authentication works across the application

## Session Expiration

### Session Lifetime

Session expiration is managed by Krista's SessionManager:

- **Default Lifetime**: Configured in Krista workspace settings
- **Idle Timeout**: Sessions may expire after period of inactivity
- **Absolute Timeout**: Maximum session duration regardless of activity

### Expiration Handling

When a session expires:

1. SessionManager returns null for account lookup
2. RequestAuthenticator returns null for authentication
3. Application throws MustAuthenticateException
4. User is redirected to login page
5. User must re-authenticate with new session ID

### Re-authentication

Users must re-authenticate when:

- Session expires due to timeout
- Session is invalidated by administrator
- User logs out explicitly
- Session ID is invalid or corrupted

## Security Best Practices

### 1. **Always Use HTTPS in Production**
```
✅ https://your-workspace.krista.com/authn/login
❌ http://your-workspace.krista.com/authn/login
```

### 2. **Validate Session on Every Request**
The extension automatically validates sessions, but ensure your application:
- Handles 401 Unauthorized responses
- Redirects to login on authentication failure
- Clears local state on logout

### 3. **Protect Session IDs**
- Never log session IDs in plain text
- Don't expose session IDs in URLs
- Use secure transmission channels only

### 4. **Implement Logout**
Provide a logout mechanism that:
- Clears the session cookie
- Invalidates the session server-side
- Redirects to login page

### 5. **Monitor Session Activity**
- Log authentication attempts
- Track failed login attempts
- Alert on suspicious patterns

## Troubleshooting Authentication

### Issue: 401 Unauthorized on Login

**Cause**: Invalid or expired client session ID

**Resolution**:
1. Verify the client session ID is valid
2. Check that the session hasn't expired
3. Ensure SessionManager is properly configured
4. Review session creation process

### Issue: Session Cookie Not Set

**Cause**: CORS or cookie configuration issue

**Resolution**:
1. Verify CORS headers are properly configured
2. Check that origin is whitelisted
3. Ensure browser allows third-party cookies
4. Verify HTTPS is used if Secure flag is set

### Issue: Session Expires Too Quickly

**Cause**: Session timeout configuration

**Resolution**:
1. Review workspace session timeout settings
2. Adjust idle timeout if necessary
3. Implement session refresh mechanism
4. Consider using longer-lived sessions for mobile apps

### Issue: Authentication Works in Browser but Not in Mobile App

**Cause**: Cookie handling differences

**Resolution**:
1. Verify mobile app properly stores cookies
2. Check that cookies are sent with requests
3. Ensure CORS is configured for mobile origin
4. Test with browser user agent first

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Login API](pages/LoginAPI.md) - Login endpoint details
- [Authenticator JavaScript](pages/AuthenticatorJavaScript.md) - Client-side integration
- [Troubleshooting](pages/Troubleshooting.md) - Common issues and solutions

