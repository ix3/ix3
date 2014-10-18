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
package es.logongas.ix3.web.database;

import es.logongas.ix3.core.database.DatabaseMigration;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class DatabaseMigrateContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        List<String> locations = new ArrayList<String>();
        locations.add(sce.getServletContext().getInitParameter("databasemigration.location"));

        WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
        DatabaseMigration databaseMigration = springContext.getBean(DatabaseMigration.class);
        databaseMigration.migrate(locations);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
