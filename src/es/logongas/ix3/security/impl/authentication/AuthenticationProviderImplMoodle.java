/*
 * Copyright 2013 Lorenzo González.
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
package es.logongas.ix3.security.impl.authentication;

import es.logongas.ix3.model.Identity;
import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.DAOFactory;
import es.logongas.ix3.persistence.services.dao.GenericDAO;
import es.logongas.ix3.security.services.authentication.AuthenticationProvider;
import es.logongas.ix3.security.services.authentication.Credential;
import es.logongas.ix3.security.services.authentication.Principal;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Autenticar a un usuario mediante el usuario y contraseña de moodle
 *
 * @author Lorenzo González
 */
public class AuthenticationProviderImplMoodle implements AuthenticationProvider {

    private String moodleLoginURL="http://www.fpmislata.com/moodle/login/index.php";
    @Autowired
    DAOFactory daoFactory;

    @Override
    public Principal authenticate(Credential credential) {
        try {
            if ((credential instanceof CredentialImplLoginPassword) == false) {
                return null;
            }
            CredentialImplLoginPassword credentialImplLoginPassword = (CredentialImplLoginPassword) credential;

            DefaultHttpClient httpClientPost = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(moodleLoginURL);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("username", credentialImplLoginPassword.getLogin()));
            nvps.add(new BasicNameValuePair("password", credentialImplLoginPassword.getPassword()));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            HttpResponse response1 = httpClientPost.execute(httpPost);
            InputStream inputStream = response1.getEntity().getContent();
            Document document = Jsoup.parse(inputStreamToString(inputStream));

            Elements divElements = document.getElementsByClass("logininfo");
            if (divElements.size() == 0) {
                return null;
            }
            Element divElement = divElements.get(0);
            Elements aElements = divElement.getElementsByTag("a");
            if (aElements.size() == 0) {
                return null;
            }
            Element aElement = aElements.get(aElements.size() - 1);
            if (aElement.attr("href").indexOf("logout") < 0) {
                return null;
            }

            DefaultHttpClient httpclient2 = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(aElement.attr("href"));

            httpclient2.execute(httpGet);

            GenericDAO<Identity,Integer> genericDAO=daoFactory.getDAO(Identity.class);
            Identity identity=genericDAO.readByNaturalKey(credentialImplLoginPassword.getLogin());

            return identity;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Principal getPrincipalBySID(Serializable sid) throws BusinessException {
        Integer idIdentity=(Integer)sid;
        GenericDAO<Identity,Integer> genericDAO=daoFactory.getDAO(Identity.class);

        return genericDAO.read(idIdentity);
    }

    private String inputStreamToString(InputStream inputStream) {
        try {
            String line;
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while (null != (line = reader.readLine())) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return the moodleLoginURL
     */
    public String getMoodleLoginURL() {
        return moodleLoginURL;
    }

    /**
     * @param moodleLoginURL the moodleLoginURL to set
     */
    public void setMoodleLoginURL(String moodleLoginURL) {
        this.moodleLoginURL = moodleLoginURL;
    }
}
