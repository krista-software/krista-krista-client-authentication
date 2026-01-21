# Krista Client Authentication Extension for Krista

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

## Security Features

- 🔒 HttpOnly cookies to prevent XSS attacks
- 🔒 Secure flag for HTTPS-only transmission
- 🔒 SameSite attribute to prevent CSRF attacks
- 🔒 Session-based authentication with server-side validation
- 🔒 CORS support with credential handling



## License

This project is licensed under the **GNU General Public License v3.0**.

```
Krista Client Authentication Extension for Krista
Copyright (C) 2025 Krista Software

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

For the full license text, see the [LICENSE](LICENSE) file or visit https://www.gnu.org/licenses/gpl-3.0.html.
