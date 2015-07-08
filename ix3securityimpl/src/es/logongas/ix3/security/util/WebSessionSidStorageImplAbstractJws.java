/*
 * Copyright 2015 logongas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.logongas.ix3.security.util;

import es.logongas.ix3.security.jwt.Jws;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author logongas
 */
public abstract class WebSessionSidStorageImplAbstractJws implements WebSessionSidStorage {

    @Autowired
    Jws jws;

    private String jwsCookieName = "XSRF-TOKEN";
    private String jwsHeaderName = "X-XSRF-TOKEN";
    private boolean checkHeader = false;

    @Override
    public void setSid(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Serializable sid) {
        String payload = serialize(sid);

        String jwsCompact = jws.getJwsCompactSerialization(payload, getPassword(sid));
        Cookie cookie = new Cookie(jwsCookieName, jwsCompact);
        cookie.setHttpOnly(false);
        cookie.setPath(httpServletRequest.getContextPath() + "/");
        httpServletResponse.addCookie(cookie);
    }

    @Override
    public void deleteSid(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Cookie cookie = new Cookie(jwsCookieName, "");
        cookie.setHttpOnly(false);
        cookie.setPath(httpServletRequest.getContextPath() + "/");
        httpServletResponse.addCookie(cookie);
    }

    @Override
    public Serializable getSid(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String jwsCompactCookie = getJwsCompactInCookie(httpServletRequest);

        if ((jwsCompactCookie == null) || (jwsCompactCookie.trim().equals(""))) {

            return null;
        }

        if (checkHeader == true) {
            String jwsCompactHeader = getJwsCompactInHeader(httpServletRequest);
            if ((jwsCompactHeader == null) || (jwsCompactHeader.trim().equals(""))) {
                return null;
            }
            if (jwsCompactCookie.equals(jwsCompactHeader) == false) {
                return null;
            }
        }

        String payload = jws.getUnverifiedPayloadFromJwsCompactSerialization(jwsCompactCookie);

        Serializable sid;
        try {
            sid = unserialize(payload);
        } catch (Exception ex) {
            Logger.getLogger(WebSessionSidStorageImplAbstractJws.class.getName()).log(Level.SEVERE, "Fallo al desserializar el token", ex);
            sid = null;
        }

        if (sid == null) {
            return null;
        }

        if ((jws.verifyJwsCompactSerialization(jwsCompactCookie, getPassword(sid)) == false)) {
            return null;
        }

        return sid;
    }

    abstract protected byte[] getPassword(Serializable sid);

    private String getJwsCompactInCookie(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (jwsCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private String getJwsCompactInHeader(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader(jwsHeaderName);
    }

    private String serialize(Serializable serializable) {
        try {
            Base64 base64 = new Base64();
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(outputStream);
            so.writeObject(serializable);
            so.flush();
            
            return base64.encodeAsString(outputStream.toByteArray());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Serializable unserialize(String s) {
        try {
            Base64 base64 = new Base64();
            byte b[] = base64.decode(s);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            Serializable serializable = (Serializable) si.readObject();

            return serializable;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return the jwsCookieName
     */
    public String getJwsCookieName() {
        return jwsCookieName;
    }

    /**
     * @param jwsCookieName the jwsCookieName to set
     */
    public void setJwsCookieName(String jwsCookieName) {
        this.jwsCookieName = jwsCookieName;
    }

    /**
     * @return the jwsHeaderName
     */
    public String getJwsHeaderName() {
        return jwsHeaderName;
    }

    /**
     * @param jwsHeaderName the jwsHeaderName to set
     */
    public void setJwsHeaderName(String jwsHeaderName) {
        this.jwsHeaderName = jwsHeaderName;
    }

    /**
     * @return the checkHeader
     */
    public boolean isCheckHeader() {
        return checkHeader;
    }

    /**
     * @param checkHeader the checkHeader to set
     */
    public void setCheckHeader(boolean checkHeader) {
        this.checkHeader = checkHeader;
    }

}
