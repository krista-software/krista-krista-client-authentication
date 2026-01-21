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

package app.krista.extensions.authentication.krista_client_authentication.api.constants;

public class ApiConstants {

    private ApiConstants() {
    }

    public static final String X_KRISTA_SESSION_ID = "X-Krista-Session-Id";

    public static final String X_KRISTA_ORIGINAL_URL = "X-Krista-Original-URI";

    public static final String X_KRISTA_CONTEXT = "X-Krista-Context";
    public static final String COOKIE = "Cookie";
    public static final Object LOGIN_API = "/login";
    public static final String HOST = "host";
    public static final String BASE_URI_KEYWORD_TO_BE_REPLACED = "__BASE_URI__";

    public static final String CLIENTSESSIONID = "clientSessionId";

}
