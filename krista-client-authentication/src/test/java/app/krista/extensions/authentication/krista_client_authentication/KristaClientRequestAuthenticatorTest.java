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

package app.krista.extensions.authentication.krista_client_authentication;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extension.request.protos.http.HttpResponse;
import app.krista.extensions.authentication.krista_client_authentication.api.constants.ApiConstants;
import app.krista.ksdk.authentication.SessionManager;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KristaClientRequestAuthenticator.
 */
@DisplayName("Krista Client Request Authenticator Tests")
class KristaClientRequestAuthenticatorTest {

    @Mock
    private Invoker mockInvoker;

    @Mock
    private SessionManager mockSessionManager;

    @Mock
    private HttpRequest mockHttpRequest;

    @Mock
    private RoutingInfo mockRoutingInfo;

    private KristaClientRequestAuthenticator authenticator;
    private Gson gson;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gson = new Gson();
        authenticator = new KristaClientRequestAuthenticator(mockInvoker, mockSessionManager);
    }

    @Test
    @DisplayName("Should return 'Krista' as authentication scheme")
    void testGetScheme_ReturnsKrista() {
        // Act
        String scheme = authenticator.getScheme();

        // Assert
        assertEquals("Krista", scheme, "Scheme should be 'Krista'");
    }

    @Test
    @DisplayName("Should support HTTP protocol")
    void testGetSupportedProtocols_ReturnsHttpProtocol() {
        // Act
        Set<String> protocols = authenticator.getSupportedProtocols();

        // Assert
        assertNotNull(protocols, "Supported protocols should not be null");
        assertEquals(1, protocols.size(), "Should support exactly one protocol");
        assertTrue(protocols.contains(HttpProtocol.PROTOCOL_NAME),
                "Should support HTTP protocol");
    }

    @Test
    @DisplayName("Should return null when cookie header is missing")
    void testGetAuthenticatedAccountId_NoCookie_ReturnsNull() {
        // Arrange
        when(mockHttpRequest.getHeader(ApiConstants.COOKIE)).thenReturn(null);

        // Act
        String accountId = authenticator.getAuthenticatedAccountId(mockHttpRequest);

        // Assert
        assertNull(accountId, "Should return null when cookie is missing");
        verify(mockHttpRequest).getHeader(ApiConstants.COOKIE);
    }

    @Test
    @DisplayName("Should return null when cookie does not contain X-Krista-Context")
    void testGetAuthenticatedAccountId_SimpleCookie_ReturnsNull() {
        // Arrange
        String clientSessionId = "test-session-id";
        String cookie = ApiConstants.CLIENTSESSIONID + "=" + clientSessionId;

        when(mockHttpRequest.getHeader(ApiConstants.COOKIE)).thenReturn(cookie);

        // Act
        String accountId = authenticator.getAuthenticatedAccountId(mockHttpRequest);

        // Assert
        assertNull(accountId, "Should return null when cookie doesn't have X-Krista-Context format");
    }

    @Test
    @DisplayName("Should return empty map for attribute fields")
    void testGetAttributeFields_ReturnsEmptyMap() {
        // Act
        Map<String, app.krista.model.field.NamedField> fields = authenticator.getAttributeFields();

        // Assert
        assertNotNull(fields, "Attribute fields should not be null");
        assertTrue(fields.isEmpty(), "Attribute fields should be empty");
    }

    @Test
    @DisplayName("Should return false for setServiceAuthorization")
    void testSetServiceAuthorization_ReturnsFalse() {
        // Act
        boolean result = authenticator.setServiceAuthorization("test-token");

        // Assert
        assertFalse(result, "setServiceAuthorization should return false");
    }

    @Test
    @DisplayName("Should return redirect response for must authenticate exception")
    void testGetMustAuthenticateResponse_ReturnsRedirect() {
        // Arrange
        String originalUrl = "https://example.com/protected";
        MustAuthenticateException exception = new MustAuthenticateException("Not authenticated");

        when(mockHttpRequest.getHeader(ApiConstants.X_KRISTA_ORIGINAL_URL)).thenReturn(originalUrl);
        when(mockInvoker.getRoutingInfo()).thenReturn(mockRoutingInfo);
        when(mockRoutingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn("https://krista.example.com");

        // Act
        HttpResponse response = (HttpResponse) authenticator.getMustAuthenticateResponse(exception, mockHttpRequest);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(302, response.getStatusCode(), "Should return 302 redirect");
        assertTrue(response.getHeaders().containsKey("Location"), "Should contain Location header");
    }

    @Test
    @DisplayName("Should return null for getMustAuthenticateResponse without request")
    void testGetMustAuthenticateResponse_NoRequest_ReturnsNull() {
        // Arrange
        MustAuthenticateException exception = new MustAuthenticateException("Not authenticated");

        // Act
        var response = authenticator.getMustAuthenticateResponse(exception);

        // Assert
        assertNull(response, "Should return null when no request is provided");
    }
}
