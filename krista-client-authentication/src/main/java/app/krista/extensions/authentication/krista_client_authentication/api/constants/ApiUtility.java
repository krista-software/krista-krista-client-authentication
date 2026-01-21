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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.glassfish.jersey.message.internal.CookiesParser;

public class ApiUtility {

    private static final Gson GSON = new Gson();

    private ApiUtility() {

    }

    public static String getClientSessionIdFromCookie(String cookie) {
        String clientSessionId = null;
        if (cookie != null && cookie.contains(ApiConstants.CLIENTSESSIONID)) {
            clientSessionId = parseClientSessionIdFromCookie(cookie);
        }
        return clientSessionId;
    }

    public static String parseClientSessionIdFromCookie(String cookie) {
        String clientSessionId = null;
        if (cookie != null && !cookie.isBlank()) {
            Map<String, Cookie> cookies = CookiesParser.parseCookies(cookie);
            Cookie xKristaContextCookie = cookies.get("X-Krista-Context");
            String encodedRequestContext;
            if (xKristaContextCookie != null) {
                encodedRequestContext = xKristaContextCookie.getValue();
                if (encodedRequestContext != null && !encodedRequestContext.isBlank()) {
                    JsonObject jsonObject = decodeRequestContext(encodedRequestContext);
                    clientSessionId = jsonObject.get("clientSessionId").getAsString();
                }
            }
        }
        return clientSessionId;
    }

    private static JsonObject decodeRequestContext(String encodedRequestContext) {
        String requestContextString = URLDecoder.decode(encodedRequestContext, StandardCharsets.UTF_8);
        return GSON.fromJson(requestContextString, JsonObject.class);
    }

}
