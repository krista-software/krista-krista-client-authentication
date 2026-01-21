# Krista Client Authentication Extension

## Overview

The **Krista Client Authentication** extension provides secure session-based authentication capabilities for Krista Web and Mobile clients. It implements the `RequestAuthenticator` interface to enable seamless user authentication across Krista applications, including Krista Portal and custom extensions.

This extension operates with workspace admin rights, allowing it to create and manage user accounts and roles within the Krista platform.

## Key Features

- ✅ **Session-Based Authentication**: Secure cookie-based session management for web and mobile clients
- ✅ **RESTful API Endpoints**: Login and authenticator JavaScript delivery endpoints
- ✅ **Account Management**: Admin rights to create and manage user accounts
- ✅ **Role Management**: Create and assign user roles with granular permissions
- ✅ **Cross-Origin Support**: CORS-enabled for integration with external applications
- ✅ **Secure Cookie Handling**: HttpOnly, Secure, and SameSite cookie attributes
- ✅ **Extension Integration**: Can be added as a dependency to other Krista extensions

## Quick Start Guide

### 1. Install the Extension
Navigate to your Krista workspace and install the Krista Client Authentication extension from the Extensions marketplace.

### 2. Enable Trust Extension Flag ⚠️
**Critical:** Enable the Trust Extension flag in Setup → Extensions. The extension will NOT function without this flag enabled.

See [Setup Instructions](pages/setup.md) for detailed steps with screenshots.

### 3. Integrate with Your Application
Use the provided API endpoints to implement authentication in your web or mobile application.

### 4. Test Authentication
Verify the authentication flow by testing the login endpoint with a valid client session ID.

## Documentation Structure

### Getting Started
- [Setup Instructions](pages/setup.md) - Step-by-step setup guide with Trust Extension flag configuration
- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration parameters
- [Authentication](pages/Authentication.md) - Authentication modes, session management, and security
- [Integration Guide](pages/IntegrationGuide.md) - How to integrate with your applications

### API Endpoints
- [Login API](pages/LoginAPI.md) - POST /login endpoint for user authentication
- [Authenticator JavaScript](pages/AuthenticatorJavaScript.md) - GET /authenticator endpoint for client-side integration

### Reference
- [Dependencies](pages/dependency.md) - Required dependencies and prerequisites
- [Release Notes](pages/release-notes.md) - Version history and changes
- [Troubleshooting](pages/Troubleshooting.md) - Common issues and solutions

## Support & Resources

- **Version**: 3.5.7
- **Domain**: Authentication
- **Ecosystem**: Krista
- **JAX-RS ID**: `authn`
- **Requires**: Workspace Admin Rights

## Architecture

The extension uses:
- **SessionManager**: Manages user sessions and account lookups
- **Invoker**: Executes authentication requests and routing
- **RequestAuthenticator**: Implements custom authentication logic
- **JAX-RS Resources**: Provides RESTful API endpoints

## Security Features

- 🔒 HttpOnly cookies to prevent XSS attacks
- 🔒 Secure flag for HTTPS-only transmission
- 🔒 SameSite attribute to prevent CSRF attacks
- 🔒 Session-based authentication with server-side validation
- 🔒 CORS support with credential handling
