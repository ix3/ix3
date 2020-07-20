/*
 * ix3 Copyright 2020 Lorenzo González.
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
package es.logongas.ix3.web.database;

import es.logongas.ix3.core.database.DatabaseMigration;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class DatabaseMigrateContextListener implements ServletContextListener {

    @Autowired
    DatabaseMigration databaseMigration;
    
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContextEvent.getServletContext());
        AutowireCapableBeanFactory autowireCapableBeanFactory=webApplicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(this);
        
        //Permitirmos varias "locations" en el parámetro separados por "\n"
        String[] rawLocations = (servletContextEvent.getServletContext().getInitParameter("databasemigration.location")+"").split("\\n");
        List<String> locations = new ArrayList<String>();
        for(String location : rawLocations) {
            if ((location!=null) && (location.trim().isEmpty()==false)) {
                locations.add(location);
            }
        }

        databaseMigration.migrate(locations);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
