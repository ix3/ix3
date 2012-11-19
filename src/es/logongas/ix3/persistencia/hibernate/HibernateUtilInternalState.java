package es.logongas.ix3.persistencia.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;


class HibernateUtilInternalState {
    SessionFactory sessionFactory;
    ThreadLocal<Session> threadLocalSession = new ThreadLocal<>();
}
