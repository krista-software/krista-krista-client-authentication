# Authenticator JavaScript

## Overview

The Authenticator JavaScript endpoint delivers a client-side JavaScript file that provides authentication helper functions for web applications integrating with Krista Client Authentication.

## Request Details

- **Endpoint**: `GET /authn/authenticator`
- **Content-Type**: `application/javascript`
- **Authentication**: Not required
- **CORS**: Enabled

## Response

### Success Response (HTTP 200)

**Headers**:
```
Content-Type: application/javascript
Access-Control-Allow-Origin: <requesting-origin>
Access-Control-Allow-Credentials: true
```

**Body**: JavaScript file content from `authenticator.js` resource

## Usage Example

### Include in HTML
```html
<script src="https://your-workspace.krista.com/authn/authenticator"></script>
```

### Use in Application
```javascript
// The authenticator.js provides helper functions for authentication
// Exact functions depend on the authenticator.js implementation
```

## Technical Implementation

- **Class**: AuthenticationResource
- **Method**: `getAuthenticator(HttpHeaders)`
- **Resource File**: `authenticator.js` (bundled in extension)

## CORS Support

The endpoint automatically handles CORS:
- Echoes requesting origin in Access-Control-Allow-Origin
- Sets Access-Control-Allow-Credentials to true
- Allows cross-origin script loading

## Common Use Cases

### 1. Web Application Integration
Load the authenticator script to access authentication helper functions in your web application.

### 2. Single Page Applications
Include the script in your SPA to handle authentication flows client-side.

### 3. Mobile Web Views
Load the script in mobile web views for consistent authentication behavior.

## See Also

- [Login API](pages/LoginAPI.md) - Authentication endpoint
- [Authentication](pages/Authentication.md) - Authentication flow
- [Integration Guide](pages/IntegrationGuide.md) - Integration examples

