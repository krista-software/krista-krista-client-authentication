# Login API

## Overview

The Login API endpoint authenticates users by validating their client session ID and establishing an authenticated session with a secure cookie.

## Request Details

- **Endpoint**: `POST /authn/login`
- **Content-Type**: `application/json`
- **Authentication**: Not required (this endpoint establishes authentication)
- **CORS**: Enabled with credentials support

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| clientSessionId | String | Yes | Unique client session identifier obtained from Krista device registration | `"device_bbafe0cf-38e1-490b-9f00-d31aafadfe"` |

### Parameter Details

#### clientSessionId
- **Type**: String
- **Format**: Alphanumeric with hyphens
- **Source**: Obtained from Krista device registration or session creation
- **Validation**: Must be a valid, non-expired session ID registered in Krista
- **Alternative**: Can also be provided in Cookie header as `clientSessionId=<value>`

## Output Parameters

### Success Response (HTTP 202)

| Header Name | Type | Description |
|-------------|------|-------------|
| Access-Control-Allow-Credentials | Boolean | Set to `true` to allow credentials |
| Access-Control-Allow-Origin | String | Requesting origin (from request header) |
| Access-Control-Allow-Methods | String | `POST,OPTIONS` |
| Access-Control-Allow-Headers | String | `Content-Type, Accept` |
| Set-Cookie | String | Session cookie with encoded client session ID |

**Cookie Format**:
```
clientSessionId=<Base64EncodedSessionId>;HttpOnly;path=/;SameSite=None;Secure
```

**Response Body**: Empty (202 Accepted)

### Error Responses

| HTTP Code | Description | Cause |
|-----------|-------------|-------|
| 400 | Bad Request | Missing or empty clientSessionId |
| 401 | Unauthorized | Invalid or expired client session ID |
| 500 | Internal Server Error | Server-side error during authentication |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| clientSessionId is null or empty | 400 Bad Request | Provide a valid clientSessionId in request body or Cookie header |
| clientSessionId not found in SessionManager | 401 Unauthorized | Verify the session ID is valid and not expired |
| SessionManager lookup fails | 401 Unauthorized | Check SessionManager configuration and session validity |

## Error Handling

### Input Errors (HTTP 400)

**Cause**: Missing or invalid input parameters

**Common Scenarios**:
- clientSessionId not provided in request body
- clientSessionId is empty string
- Request body is malformed JSON

**Resolution**:
1. Verify request body contains `clientSessionId` field
2. Ensure clientSessionId is not empty
3. Validate JSON syntax

### Authentication Errors (HTTP 401)

**Cause**: Invalid or expired session ID

**Common Scenarios**:
- Session ID has expired
- Session ID was never registered
- Session was invalidated by administrator
- SessionManager cannot find the session

**Resolution**:
1. Obtain a new client session ID from Krista
2. Verify the session ID is correctly formatted
3. Check that the session hasn't been revoked
4. Ensure SessionManager is properly configured

### System Errors (HTTP 500)

**Cause**: Internal server error during authentication

**Common Scenarios**:
- SessionManager unavailable
- Database connection failure
- Unexpected exception during processing

**Resolution**:
1. Check server logs for detailed error information
2. Verify Krista services are running
3. Check database connectivity
4. Contact support if issue persists

## Usage Examples

### Example 1: Successful Login

**Input**:
```json
POST /authn/login
Content-Type: application/json
Origin: https://your-app.com

{
  "clientSessionId": "device_bbafe0cf-38e1-490b-9f00-d31aafadfe"
}
```

**Output**:
```
HTTP/1.1 202 Accepted
Access-Control-Allow-Credentials: true
Access-Control-Allow-Origin: https://your-app.com
Access-Control-Allow-Methods: POST,OPTIONS
Access-Control-Allow-Headers: Content-Type, Accept
Set-Cookie: clientSessionId=ZGV2aWNlX2JiYWZlMGNmLTM4ZTEtNDkwYi05ZjAwLWQzMWFhZmFkZmU=;HttpOnly;path=/;SameSite=None;Secure
```

**Result**: User is authenticated, session cookie is set, subsequent requests will be authenticated

### Example 2: Login with Cookie Header

**Input**:
```json
POST /authn/login
Content-Type: application/json
Cookie: clientSessionId=device_bbafe0cf-38e1-490b-9f00-d31aafadfe
Origin: https://your-app.com

{}
```

**Output**:
```
HTTP/1.1 202 Accepted
Set-Cookie: clientSessionId=ZGV2aWNlX2JiYWZlMGNmLTM4ZTEtNDkwYi05ZjAwLWQzMWFhZmFkZmU=;HttpOnly;path=/;SameSite=None;Secure
```

**Result**: Session ID extracted from Cookie header, authentication successful

### Example 3: Invalid Session ID

**Input**:
```json
POST /authn/login
Content-Type: application/json

{
  "clientSessionId": "invalid_session_id"
}
```

**Output**:
```
HTTP/1.1 401 Unauthorized
```

**Result**: Authentication failed, no cookie set, user must obtain valid session ID

## Business Rules

1. **Session ID Validation**: All session IDs must be validated against SessionManager before authentication
2. **Cookie Encoding**: Session IDs are Base64-encoded before being set in cookies
3. **CORS Handling**: Origin from request header is echoed in Access-Control-Allow-Origin
4. **Secure Cookies**: Secure flag is only set for HTTPS connections
5. **HttpOnly Enforcement**: All session cookies must have HttpOnly flag
6. **Path Scope**: Cookies are scoped to root path (/) for workspace-wide access

## Limitations

1. **Single Session Per Client**: Each client can have only one active session at a time
2. **CORS Required**: Cross-origin requests require proper CORS configuration
3. **HTTPS Recommended**: Secure flag requires HTTPS; HTTP should only be used in development
4. **Session Expiration**: Sessions expire based on workspace configuration
5. **No Token Refresh**: Extension does not support token refresh; users must re-authenticate

## Best Practices

### 1. **Handle CORS Preflight**
Always handle OPTIONS requests before POST:
```javascript
// Browser automatically sends OPTIONS request
// Extension handles it with loginOptions() method
```

### 2. **Store Cookies Securely**
```javascript
// Browser automatically stores HttpOnly cookies
// Don't try to access via JavaScript
// Cookie is sent automatically with subsequent requests
```

### 3. **Handle Authentication Errors**
```javascript
fetch('/authn/login', {
  method: 'POST',
  credentials: 'include',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({clientSessionId: sessionId})
})
.then(response => {
  if (response.status === 202) {
    // Authentication successful
  } else if (response.status === 401) {
    // Invalid session, obtain new session ID
  }
});
```

## Common Use Cases

### 1. Web Application Login
```
Scenario: User opens web application
Action: 
  1. Application obtains client session ID from Krista
  2. Sends POST request to /authn/login
  3. Receives session cookie
  4. Makes authenticated requests
Result: User is logged in and can access protected resources
```

### 2. Mobile Application Authentication
```
Scenario: Mobile app needs to authenticate user
Action:
  1. App registers device with Krista
  2. Obtains client session ID
  3. Calls login API with session ID
  4. Stores session cookie
Result: Mobile app can make authenticated API calls
```

### 3. Re-authentication After Session Expiry
```
Scenario: User's session has expired
Action:
  1. Application receives 401 Unauthorized
  2. Obtains new client session ID
  3. Calls login API again
  4. Receives new session cookie
Result: User is re-authenticated without losing context
```

## Related API Endpoints

- [Authenticator JavaScript](pages/AuthenticatorJavaScript.md) - Client-side authentication helper
- OPTIONS /authn/login - CORS preflight handler (automatic)

## Technical Implementation

### Helper Class
- **Class**: AuthenticationResource
- **Package**: app.krista.extensions.authentication.krista_client_authentication.api
- **Method**: `login(HttpHeaders, Map<String, String>, HttpHeaders)`

### Validation Flow
1. Extract clientSessionId from request body or Cookie header
2. Validate session ID is not null
3. Look up account ID using SessionManager
4. If not found, throw AuthenticationException
5. Encode session ID in Base64
6. Create session cookie with security attributes
7. Return 202 Accepted with Set-Cookie header

### Service Delegation
- **SessionManager.lookupAccountId()**: Validates session and returns account ID
- **Base64.getEncoder()**: Encodes session ID for cookie storage

## Troubleshooting

### Issue: CORS Error in Browser

**Cause**: Origin not allowed or CORS headers missing

**Solution**:
1. Verify origin is whitelisted in Krista
2. Check that request includes Origin header
3. Ensure credentials: 'include' is set in fetch options

### Issue: Cookie Not Set

**Cause**: Browser blocking third-party cookies

**Solution**:
1. Use same-origin requests if possible
2. Ensure SameSite=None and Secure are set
3. Check browser cookie settings
4. Test in different browser

### Issue: 401 on Valid Session ID

**Cause**: Session expired or SessionManager issue

**Solution**:
1. Verify session hasn't expired
2. Check SessionManager logs
3. Obtain fresh session ID
4. Verify Krista services are running

## See Also

- [Authentication](pages/Authentication.md) - Authentication flow and session management
- [Authenticator JavaScript](pages/AuthenticatorJavaScript.md) - Client-side integration
- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Troubleshooting](pages/Troubleshooting.md) - Common issues and solutions

