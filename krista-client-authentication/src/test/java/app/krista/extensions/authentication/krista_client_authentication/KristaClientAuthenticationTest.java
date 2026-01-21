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

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.ksdk.authentication.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KristaClientAuthenticationExtension.
 */
@DisplayName("Krista Client Authentication Extension Tests")
class KristaClientAuthenticationTest {

    @Mock
    private Invoker mockInvoker;

    @Mock
    private SessionManager mockSessionManager;

    private KristaClientAuthenticationExtension extension;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extension = new KristaClientAuthenticationExtension(mockInvoker, mockSessionManager);
    }

    @Test
    @DisplayName("Should create extension successfully with required dependencies")
    void testConstructor_Success() {
        // Assert
        assertNotNull(extension, "Extension should be created successfully");
    }

    @Test
    @DisplayName("Should create RequestAuthenticator successfully")
    void testGetAuthenticatedAccountId_ReturnsAuthenticator() {
        // Act
        RequestAuthenticator authenticator = extension.getAuthenticatedAccountId();

        // Assert
        assertNotNull(authenticator, "Should return a RequestAuthenticator instance");
        assertTrue(authenticator instanceof KristaClientRequestAuthenticator,
                "Should return KristaClientRequestAuthenticator instance");
    }

    @Test
    @DisplayName("Should return custom tab with documentation link")
    void testCustomTab_ReturnsDocumentationTab() {
        // Act
        Map<String, String> customTabs = extension.customTab();

        // Assert
        assertNotNull(customTabs, "Custom tabs should not be null");
        assertEquals(1, customTabs.size(), "Should have exactly one custom tab");
        assertTrue(customTabs.containsKey("Documentation"), "Should contain Documentation tab");
        assertEquals("static/docs", customTabs.get("Documentation"),
                "Documentation tab should point to static/docs");
    }

    @Test
    @DisplayName("Should create multiple authenticators independently")
    void testGetAuthenticatedAccountId_MultipleInstances() {
        // Act
        RequestAuthenticator authenticator1 = extension.getAuthenticatedAccountId();
        RequestAuthenticator authenticator2 = extension.getAuthenticatedAccountId();

        // Assert
        assertNotNull(authenticator1, "First authenticator should not be null");
        assertNotNull(authenticator2, "Second authenticator should not be null");
        assertNotSame(authenticator1, authenticator2,
                "Each call should create a new authenticator instance");
    }

    @Test
    @DisplayName("Should accept null invoker without throwing exception")
    void testConstructor_NullInvoker() {
        // Act
        KristaClientAuthenticationExtension ext = new KristaClientAuthenticationExtension(null, mockSessionManager);

        // Assert
        assertNotNull(ext, "Extension should be created even with null invoker");
    }

    @Test
    @DisplayName("Should accept null session manager without throwing exception")
    void testConstructor_NullSessionManager() {
        // Act
        KristaClientAuthenticationExtension ext = new KristaClientAuthenticationExtension(mockInvoker, null);

        // Assert
        assertNotNull(ext, "Extension should be created even with null session manager");
    }
}
