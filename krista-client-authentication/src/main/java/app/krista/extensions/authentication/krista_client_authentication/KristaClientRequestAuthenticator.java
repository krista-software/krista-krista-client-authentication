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
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extension.request.protos.http.HttpResponse;
import app.krista.extensions.authentication.krista_client_authentication.api.constants.ApiConstants;
import app.krista.extensions.authentication.krista_client_authentication.api.constants.ApiUtility;
import app.krista.ksdk.authentication.SessionManager;
import app.krista.model.field.NamedField;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kristasoft.common.io.Unicodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.HttpHeaders;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static app.krista.extension.request.RoutingInfo.Type.APPLIANCE;

@SuppressWarnings("deprecation")
public class KristaClientRequestAuthenticator implements RequestAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(KristaClientRequestAuthenticator.class);

    private final Invoker invoker;
    private final SessionManager sessionManager;

    private final Gson gson;

    public KristaClientRequestAuthenticator(Invoker invoker, SessionManager sessionManager) {
        this(invoker, sessionManager, new GsonBuilder().create());
    }

    private KristaClientRequestAuthenticator(Invoker invoker, SessionManager sessionManager, Gson gson) {
        this.invoker = invoker;
        this.sessionManager = sessionManager;
        this.gson = gson;
    }

    @Override
    public String getScheme() {
        return "Krista";
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return Set.of(HttpProtocol.PROTOCOL_NAME);
    }

    /**
     * Returns the id of the account that is authenticated for the given request
     *
     * @param request the request potentially containing an authentication
     * @return accountId
     */
    @Override
    public String getAuthenticatedAccountId(ProtoRequest request) {
        HttpRequest httpRequest = (HttpRequest) request;
        String cookie = httpRequest.getHeader(ApiConstants.COOKIE);
        String clientSessionId = ApiUtility.getClientSessionIdFromCookie(cookie);
        if(clientSessionId == null){
            return null;
        }

        String accountId = sessionManager.lookupAccountId(clientSessionId);
        if (!Objects.isNull(accountId)) {
            return accountId;
        }

        if (isLoginApiRequested(httpRequest)) {
            return getAccountId(httpRequest);
        }
        return null;
    }

    @Override
    public boolean setServiceAuthorization(String authorizationToken) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Map.of();
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException cause, ProtoRequest request) {
        String uri =
                ((HttpRequest) request).getHeader(ApiConstants.X_KRISTA_ORIGINAL_URL);
        String encodedUri = URLEncoder.encode(uri, StandardCharsets.UTF_8);
        return new HttpResponse(302,
                Map.of(HttpHeaders.LOCATION, getLoginUrl(encodedUri)),
                new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException cause) {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException cause, ProtoRequest request) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        return null;
    }

    private String getLoginUrl(String encodedUri) {
        return invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, APPLIANCE)
                + "/authn/login?" + ApiConstants.X_KRISTA_ORIGINAL_URL + "=" + encodedUri;
    }

    private String getAccountId(HttpRequest httpRequest) {
        try {
            if ("GET".equalsIgnoreCase(httpRequest.getMethod())) {
                List<String> clientSessionId = httpRequest.getQueryParameters().get(ApiConstants.CLIENTSESSIONID);
                if (!Objects.isNull(clientSessionId) && !clientSessionId.isEmpty()) {
                    return sessionManager.lookupAccountId(clientSessionId.get(0));
                }
            } else if (("POST".equalsIgnoreCase(httpRequest.getMethod()))) {
                httpRequest.bufferBody();
                InputStream body = httpRequest.getBody();
                String requestPayload = Unicodes.fromUTF8(body);
                JsonObject jsonObject = gson.fromJson(requestPayload, JsonObject.class);
                JsonElement jsonElement = jsonObject.get(ApiConstants.CLIENTSESSIONID);
                String clientSessionId = jsonElement != null ? jsonElement.getAsString() : "";
                return sessionManager.lookupAccountId(clientSessionId);
            }
            return null;
        } catch (IOException ex) {
            logger.error("error occurred while getting clientSessionId: {}", ex.getCause(), ex);
            return null;
        }
    }

    private boolean isLoginApiRequested(HttpRequest httpRequest) {
        return Objects.equals(ApiConstants.LOGIN_API, httpRequest.getUri().getPath());
    }

}
