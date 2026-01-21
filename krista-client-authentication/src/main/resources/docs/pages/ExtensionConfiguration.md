# Extension Configuration

## Overview

The Krista Client Authentication extension requires minimal configuration to get started. This page describes the configuration parameters, setup steps, and best practices for deploying the extension in your Krista workspace.

## Configuration Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| Domain ID | UUID | Yes | Unique identifier for the Authentication domain | `catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7` |
| JAX-RS ID | Text | Yes | REST API path prefix for authentication endpoints | `authn` |
| Workspace Admin Rights | Boolean | Yes | Grants admin privileges for account/role management | `true` |
| Ecosystem ID | UUID | Yes | Krista ecosystem identifier | `catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9` |
| Ecosystem Version | UUID | Yes | Version identifier for the Krista ecosystem | `3e7e09ed-688f-41fa-ab7c-ff879e750011` |

## Step-by-Step Setup

### Step 1: Install the Extension

1. Log in to your Krista workspace with administrator credentials
2. Navigate to **Extensions** → **Marketplace**
3. Search for "Krista Client Authentication"
4. Click **Install** and wait for the installation to complete
5. Verify the extension appears in your installed extensions list

### Step 2: Enable Trust Extension

The extension requires the "Trust Extension" flag to be enabled:

1. Navigate to **Setup** → **Extensions**
2. Locate "Krista Client Authentication" in the extensions list
3. Enable the **Trust Extension** toggle
4. Save the configuration

> **⚠️ Warning**: The Trust Extension flag grants the extension workspace admin rights. Only enable this for trusted extensions.

### Step 3: Verify Installation

After installation, verify the extension is working correctly:

1. Check that the extension status shows as "Active"
2. Verify the JAX-RS endpoint is accessible at `<workspace-url>/authn`
3. Test the authenticator JavaScript endpoint: `<workspace-url>/authn/authenticator`

### Step 4: Configure CORS (Optional)

If you're integrating with external applications, configure CORS settings:

1. Ensure your application's origin is whitelisted in Krista
2. The extension automatically handles CORS headers for allowed origins
3. Test cross-origin requests to verify CORS configuration

## Authentication Type Selection

The extension supports session-based authentication using client session IDs:

### Session-Based Authentication
- **Use Case**: Web and mobile applications requiring persistent sessions
- **Mechanism**: Cookie-based session management with server-side validation
- **Security**: HttpOnly, Secure, and SameSite cookie attributes
- **Expiration**: Managed by Krista's SessionManager

## Security Considerations

### 1. **HTTPS Required**
Always use HTTPS in production environments. The extension sets the `Secure` cookie flag for HTTPS connections.

### 2. **Cookie Security**
- **HttpOnly**: Prevents JavaScript access to session cookies
- **Secure**: Ensures cookies are only transmitted over HTTPS
- **SameSite**: Protects against CSRF attacks

### 3. **Session Management**
- Sessions are validated server-side on every request
- Invalid or expired sessions are rejected with 401 Unauthorized
- Session IDs are Base64-encoded for safe transmission

### 4. **Admin Rights**
The extension requires workspace admin rights to:
- Create user accounts
- Assign roles and permissions
- Manage authentication sessions

> **📝 Note**: Admin rights are necessary for the extension to function properly. Ensure you trust the extension before installation.

## Troubleshooting

### Extension Not Appearing After Installation

**Cause**: Extension installation may not have completed successfully

**Resolution**:
1. Check the extension installation logs
2. Verify all dependencies are installed
3. Restart the Krista workspace if necessary
4. Contact support if the issue persists

### Trust Extension Flag Not Available

**Cause**: Insufficient permissions or workspace configuration issue

**Resolution**:
1. Verify you have workspace administrator privileges
2. Check that the workspace allows trust extensions
3. Review workspace security policies

### CORS Errors When Accessing Endpoints

**Cause**: Origin not whitelisted or CORS headers not properly configured

**Resolution**:
1. Verify the requesting origin is allowed in Krista
2. Check browser console for specific CORS error messages
3. Ensure the extension is properly installed and active
4. Test with a simple CORS request to isolate the issue

## Best Practices

### 1. **Use HTTPS in Production**
Always deploy the extension in HTTPS-enabled environments to ensure secure cookie transmission.

### 2. **Monitor Session Activity**
Regularly review session logs to detect unusual authentication patterns or potential security issues.

### 3. **Keep Extension Updated**
Install updates promptly to benefit from security patches and new features.

### 4. **Test in Staging First**
Always test extension updates in a staging environment before deploying to production.

### 5. **Document Integration Points**
Maintain documentation of all applications and extensions that depend on this authentication extension.

## Next Steps

After configuring the extension:

1. Review the [Authentication](pages/Authentication.md) documentation to understand authentication flows
2. Follow the [Integration Guide](pages/IntegrationGuide.md) to integrate with your applications
3. Test the [Login API](pages/LoginAPI.md) endpoint with sample requests
4. Implement the [Authenticator JavaScript](pages/AuthenticatorJavaScript.md) in your client applications

## See Also

- [Authentication](pages/Authentication.md) - Authentication modes and session management
- [Login API](pages/LoginAPI.md) - Login endpoint documentation
- [Dependencies](pages/dependency.md) - Required dependencies
- [Troubleshooting](pages/Troubleshooting.md) - Common issues and solutions

