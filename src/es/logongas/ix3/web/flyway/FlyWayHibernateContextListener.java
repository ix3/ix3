/**
 * FPempresa Copyright (C) 2014 Lorenzo González
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package es.logongas.ix3.web.flyway;

import com.googlecode.flyway.core.Flyway;
import javax.naming.InitialContext;
import javax.servlet.*;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FlyWayHibernateContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String dataSourceName=getDataSourceNameFromHibernate();
        DataSource dataSource=getDataSource(dataSourceName);
        
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(sce.getServletContext().getInitParameter("flyway.migration.location"));
        flyway.setEncoding("utf-8");
        flyway.migrate();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    /**
     * Obtiene un dataource a partir de su nombre
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
            Document document = documentBuilder.parse(FlyWayHibernateContextListener.class.getResourceAsStream("/hibernate.cfg.xml"));
            
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
