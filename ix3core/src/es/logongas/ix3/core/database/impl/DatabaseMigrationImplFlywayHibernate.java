/*
 * Copyright 2014 Lorenzo González.
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
package es.logongas.ix3.core.database.impl;

import com.googlecode.flyway.core.Flyway;
import es.logongas.ix3.core.database.DatabaseMigration;
import java.util.List;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Lorenzo
 */
public class DatabaseMigrationImplFlywayHibernate implements DatabaseMigration {

    @Override
    public void migrate(List<String> locations) {
        String dataSourceName = getDataSourceNameFromHibernate();
        DataSource dataSource = getDataSource(dataSourceName);

        try {
            Flyway flyway = new Flyway();
            flyway.setOutOfOrder(true);
            flyway.setDataSource(dataSource);
            flyway.setLocations((String[]) locations.toArray(new String[0]));
            flyway.setEncoding("utf-8");
            flyway.migrate();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Obtiene un dataource a partir de su nombre
     *
     * @param dataSourceName
     * @return
     */
    public DataSource getDataSource(String dataSourceName) {

        try {
            InitialContext initialContext = new InitialContext();;
            DataSource dataSource = (DataSource) initialContext.lookup(dataSourceName);
            initialContext.close();

            return dataSource;

        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * Obtiene el nombre del datasource que usa hibernate
     *
     * @return
     */
    private String getDataSourceNameFromHibernate() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setValidating(false);
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(DatabaseMigrationImplFlywayHibernate.class.getResourceAsStream("/hibernate.cfg.xml"));

            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("property");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if ("connection.datasource".equalsIgnoreCase(element.getAttribute("name"))) {
                        return element.getTextContent();
                    }
                }
            }

            return null;

        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
