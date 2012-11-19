package es.logongas.ix3.persistencia.hibernate;

import java.io.IOException;
import javax.servlet.*;

public class HibernateContextListenerAndFilter implements Filter,ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        HibernateUtil.buildSessionFactory();
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    } 
    

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HibernateUtil.openSessionAndAttachToThread();
            filterChain.doFilter(servletRequest,servletResponse);
        } finally {
            if (HibernateUtil.isSessionAttachToThread()) {
                HibernateUtil.closeSessionAndDeattachFromThread();
            }
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        HibernateUtil.closeSessionFactory();
    }

}
