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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.naming.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import app.krista.extension.authorization.AuthorizationException;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.authentication.krista_client_authentication.api.constants.ApiConstants;
import app.krista.extensions.authentication.krista_client_authentication.api.constants.ApiUtility;
import app.krista.ksdk.authentication.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
@Path("/")
public class AuthenticationResource {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationResource.class);
    private final SessionManager sessionManager;
    private final Invoker invoker;

    @Inject
    public AuthenticationResource(SessionManager sessionManager, Invoker invoker) {
        this.sessionManager = sessionManager;
        this.invoker = invoker;
    }

    @GET
    @Path("/authenticator")
    public Response getAuthenticator(@Context HttpHeaders httpHeaders, @QueryParam("key") String key)
            throws IOException {

        String jsAuthFn = "Auth";
        String jSWindowVariable = "Authenticator";
        if (key != null) {
            jsAuthFn = "_" + key + "_";
            jSWindowVariable = key;
        }

        String path = httpHeaders.getHeaderString(ApiConstants.X_KRISTA_ORIGINAL_URL);
        System.out.println("Client Authenticator header path - " + path);
        String uriToSubstitute = !Objects.isNull(path)
                ? path.substring(0, path.indexOf("authn") - 1)
                : this.invoker.getRoutingInfo()
                        .getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("authenticator.js");
        if (resourceAsStream == null) {
            throw new NotFoundException();
        }
        String cacheControl = "private, max-age=604800";
        String contentType = "text/javascript";
        String page = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> translations = Map.of(
                "__BASE_URI__", uriToSubstitute.replace("http://", "https://"),
                "__JSAuthFn__", jsAuthFn,
                "__JSWindowVariable__", jSWindowVariable
        );
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            page = page.replace(entry.getKey(), entry.getValue());
        }
        return page.isEmpty()
                ? Response.noContent().build()
                : Response.ok(page)
                        .header(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, contentType)
                        .header("Cache-Control", cacheControl)
                        .build();
    }

    @OPTIONS
    @Path("/login")
    public Response loginOptions(@Context HttpHeaders httpHeaders) {
        String originUrl = httpHeaders.getRequestHeader("origin").get(0);
        return Response.status(200)
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Origin", originUrl)
                .header("Access-Control-Allow-Methods", "POST,OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept")
                .build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context HttpHeaders headers, Map<String, String> loginInput,
            @Context HttpHeaders httpHeaders)
            throws AuthorizationException {

        String clientSessionId = loginInput.get(ApiConstants.CLIENTSESSIONID);
        String messageToLog = "Client Session id :" + clientSessionId;

        if (clientSessionId == null) {
            List<String> cookies = headers.getRequestHeaders().get("Cookie");
            if (cookies != null && !cookies.isEmpty()) {
                String cookie = cookies.get(0);
                clientSessionId = ApiUtility.getClientSessionIdFromCookie(cookie);
            }
        }

        logger.info(messageToLog);
        try {
            if (sessionManager.lookupAccountId(clientSessionId) == null) {
                throw new AuthenticationException("Unauthenticated User");
            }
            String encodedClientSessionId = Base64.getEncoder().encodeToString(clientSessionId.getBytes());
            String originUrl = httpHeaders.getRequestHeader("origin").get(0);
            URI originalUri = URI.create(originUrl);

            return Response.status(202)
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Origin", originUrl)
                    .header("Access-Control-Allow-Methods", "POST,OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Accept")
                    .header("Set-Cookie", createSessionIdCookie(encodedClientSessionId, originalUri))
                    .build();
        } catch (Exception ex) {
            return Response.status(401).build();
        }
    }

    private String createSessionIdCookie(String sessionInfo, URI originalUri)
            throws MalformedURLException {
        String cookieOptions = ";HttpOnly;path=/";
        if (originalUri.toURL().getProtocol().equals("https")) {
            cookieOptions += ";SameSite=None;Secure";
        }
        return ApiConstants.CLIENTSESSIONID + "=" + sessionInfo + cookieOptions;
    }

}
