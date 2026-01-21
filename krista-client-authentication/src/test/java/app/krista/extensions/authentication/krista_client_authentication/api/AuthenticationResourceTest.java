/*
 * Krista Client Authentication Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.authentication.krista_client_authentication.api;

import app.krista.extension.executor.Invoker;
import app.krista.ksdk.authentication.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationResource.
 */
@DisplayName("Authentication Resource Tests")
class AuthenticationResourceTest {

    @Mock
    private SessionManager mockSessionManager;

    @Mock
    private Invoker mockInvoker;

    @Mock
    private HttpHeaders mockHttpHeaders;

    private AuthenticationResource authenticationResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationResource = new AuthenticationResource(mockSessionManager, mockInvoker);
    }

    @Test
    @DisplayName("Should create AuthenticationResource successfully")
    void testConstructor_Success() {
        assertNotNull(authenticationResource, "AuthenticationResource should be created successfully");
    }

    @Test
    @DisplayName("Should return 200 OK for login OPTIONS request")
    void testLoginOptions_Returns200() {
        String originUrl = "https://example.com";
        when(mockHttpHeaders.getRequestHeader("origin")).thenReturn(List.of(originUrl));

        Response response = authenticationResource.loginOptions(mockHttpHeaders);

        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatus(), "Should return 200 OK");
    }

    @Test
    @DisplayName("Should return 202 for successful login with valid session")
    void testLogin_ValidSession_Returns202() throws Exception {
        String clientSessionId = "valid-session-id";
        String accountId = "account-123";
        String originUrl = "https://example.com";

        Map<String, String> loginInput = new HashMap<>();
        loginInput.put("clientSessionId", clientSessionId);

        when(mockHttpHeaders.getRequestHeader("origin")).thenReturn(List.of(originUrl));
        when(mockSessionManager.lookupAccountId(clientSessionId)).thenReturn(accountId);

        Response response = authenticationResource.login(mockHttpHeaders, loginInput, mockHttpHeaders);

        assertNotNull(response, "Response should not be null");
        assertEquals(202, response.getStatus(), "Should return 202 Accepted");
        verify(mockSessionManager).lookupAccountId(clientSessionId);
    }

    @Test
    @DisplayName("Should return 401 for login with invalid session")
    void testLogin_InvalidSession_Returns401() throws Exception {
        String clientSessionId = "invalid-session-id";
        String originUrl = "https://example.com";

        Map<String, String> loginInput = new HashMap<>();
        loginInput.put("clientSessionId", clientSessionId);

        when(mockHttpHeaders.getRequestHeader("origin")).thenReturn(List.of(originUrl));
        when(mockSessionManager.lookupAccountId(clientSessionId)).thenReturn(null);

        Response response = authenticationResource.login(mockHttpHeaders, loginInput, mockHttpHeaders);

        assertNotNull(response, "Response should not be null");
        assertEquals(401, response.getStatus(), "Should return 401 Unauthorized");
    }
}

