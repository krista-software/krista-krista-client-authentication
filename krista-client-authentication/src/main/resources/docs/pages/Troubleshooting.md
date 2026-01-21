# Troubleshooting

## Common Issues and Solutions

### Authentication Issues

#### Issue: 401 Unauthorized on Login Request

**Symptoms**: Login request returns 401 status code

**Possible Causes**:
- Invalid or expired client session ID
- Session not registered in Krista
- SessionManager configuration issue

**Solutions**:
1. Verify the client session ID is valid and not expired
2. Obtain a new session ID from Krista device registration
3. Check SessionManager logs for errors
4. Ensure the extension is properly installed and active

#### Issue: Session Cookie Not Set After Successful Login

**Symptoms**: Login returns 202 but no cookie in browser

**Possible Causes**:
- Browser blocking third-party cookies
- CORS configuration issue
- HTTPS/HTTP mismatch

**Solutions**:
1. Check browser cookie settings and allow cookies
2. Verify CORS headers are present in response
3. Use HTTPS in production (required for Secure cookies)
4. Check browser console for cookie warnings
5. Ensure `credentials: 'include'` is set in fetch options

#### Issue: Session Expires Too Quickly

**Symptoms**: User logged out after short period

**Possible Causes**:
- Session timeout configured too short
- Idle timeout triggering
- Session invalidated by administrator

**Solutions**:
1. Review workspace session timeout settings
2. Adjust idle timeout configuration
3. Implement session refresh mechanism
4. Check for session invalidation events in logs

### CORS Issues

#### Issue: CORS Error in Browser Console

**Symptoms**: "Access-Control-Allow-Origin" error in console

**Possible Causes**:
- Origin not whitelisted in Krista
- Missing CORS headers
- Preflight request failing

**Solutions**:
1. Verify requesting origin is allowed in Krista configuration
2. Check that Origin header is sent with request
3. Ensure extension handles OPTIONS requests
4. Test with same-origin request first

#### Issue: Credentials Not Included in Cross-Origin Request

**Symptoms**: Cookie not sent with cross-origin requests

**Possible Causes**:
- Missing `credentials: 'include'` in fetch
- SameSite cookie attribute blocking request
- Browser security policy

**Solutions**:
1. Add `credentials: 'include'` to all fetch calls
2. Ensure SameSite=None and Secure are set for HTTPS
3. Use same-origin requests if possible
4. Check browser cookie policy settings

### Installation Issues

#### Issue: Extension Not Appearing After Installation

**Symptoms**: Extension not visible in installed extensions list

**Possible Causes**:
- Installation failed
- Workspace restart required
- Dependency missing

**Solutions**:
1. Check installation logs for errors
2. Restart Krista workspace
3. Verify all dependencies are installed
4. Reinstall the extension
5. Contact support if issue persists

#### Issue: Trust Extension Flag Not Available

**Symptoms**: Cannot enable Trust Extension toggle

**Possible Causes**:
- Insufficient permissions
- Workspace configuration restricts trust extensions

**Solutions**:
1. Verify you have workspace administrator privileges
2. Check workspace security policies
3. Contact workspace administrator
4. Review Krista documentation for trust extension requirements

### Integration Issues

#### Issue: Authenticator JavaScript Not Loading

**Symptoms**: 404 error when loading /authn/authenticator

**Possible Causes**:
- Extension not active
- JAX-RS endpoint not registered
- Resource file missing

**Solutions**:
1. Verify extension is installed and active
2. Check that JAX-RS ID is correctly configured
3. Restart the extension
4. Check server logs for errors

#### Issue: Authentication Works in Browser but Not in Mobile App

**Symptoms**: Login succeeds in browser but fails in mobile app

**Possible Causes**:
- Cookie handling differences
- CORS configuration
- HTTPS certificate issues

**Solutions**:
1. Verify mobile app properly stores and sends cookies
2. Check CORS configuration for mobile origin
3. Ensure HTTPS certificate is valid
4. Test with browser user agent first
5. Review mobile app HTTP client configuration

### Performance Issues

#### Issue: Slow Login Response

**Symptoms**: Login request takes long time to complete

**Possible Causes**:
- SessionManager performance issue
- Database connection slow
- Network latency

**Solutions**:
1. Check SessionManager performance metrics
2. Verify database connectivity and performance
3. Review network latency between client and server
4. Check server resource utilization
5. Review server logs for slow queries

### Security Issues

#### Issue: Session Hijacking Concerns

**Symptoms**: Unauthorized access to user sessions

**Possible Causes**:
- Session ID exposed in logs or URLs
- Insecure transmission (HTTP instead of HTTPS)
- XSS vulnerability in application

**Solutions**:
1. Always use HTTPS in production
2. Never log session IDs in plain text
3. Ensure HttpOnly flag is set on cookies
4. Implement XSS protection in application
5. Monitor for suspicious session activity

## Diagnostic Steps

### Step 1: Verify Extension Status

```bash
# Check if extension is installed and active
# Navigate to Krista workspace → Extensions
# Verify "Krista Client Authentication" shows as "Active"
```

### Step 2: Test Login Endpoint

```bash
curl -X POST https://your-workspace.krista.com/authn/login \
  -H "Content-Type: application/json" \
  -d '{"clientSessionId":"your-session-id"}' \
  -v
```

Expected response: HTTP 202 with Set-Cookie header

### Step 3: Check CORS Headers

```bash
curl -X OPTIONS https://your-workspace.krista.com/authn/login \
  -H "Origin: https://your-app.com" \
  -H "Access-Control-Request-Method: POST" \
  -v
```

Expected response: HTTP 200 with CORS headers

### Step 4: Verify Session Cookie

```javascript
// In browser console after successful login
document.cookie
// Should show: clientSessionId=<encoded-value>
```

### Step 5: Review Server Logs

Check Krista server logs for:
- Authentication errors
- SessionManager errors
- Extension initialization errors
- CORS-related warnings

## Getting Help

If you've tried the solutions above and still have issues:

1. **Collect Information**:
   - Extension version
   - Krista workspace version
   - Browser/client details
   - Error messages and logs
   - Steps to reproduce

2. **Check Documentation**:
   - [Extension Configuration](pages/ExtensionConfiguration.md)
   - [Authentication](pages/Authentication.md)
   - [Login API](pages/LoginAPI.md)

3. **Contact Support**:
   - Provide collected information
   - Include relevant log excerpts
   - Describe expected vs actual behavior

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication flow
- [Login API](pages/LoginAPI.md) - API endpoint details
- [Integration Guide](pages/IntegrationGuide.md) - Integration examples

