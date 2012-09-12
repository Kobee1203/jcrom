package org.jcrom;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Session;

import net.sf.cglib.proxy.LazyLoader;

import org.jcrom.util.SessionFactoryUtils;

public abstract class AbstractLazyLoader implements LazyLoader {

    private static final Logger logger = Logger.getLogger(AbstractLazyLoader.class.getName());

    private final Session session;
    private final Mapper mapper;

    public AbstractLazyLoader(Session session, Mapper mapper) {
        this.session = session;
        this.mapper = mapper;
    }

    private Session getSession() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Getting the session");
        }
        Session sessionToUse = Jcrom.getCurrentSession() != null ? Jcrom.getCurrentSession() : session;
        if (sessionToUse == null || !sessionToUse.isLive()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Creating a new session");
            }
            SessionFactory sessionFactory = mapper.getJcrom().getSessionFactory();
            sessionToUse = SessionFactoryUtils.getSession(sessionFactory);
        }
        return sessionToUse;
    }

    private void releaseSession(Session session) {
        if (session != null) {
            Session sessionToUse = Jcrom.getCurrentSession() != null ? Jcrom.getCurrentSession() : this.session;
            if (!sessionToUse.equals(session)) {
                SessionFactoryUtils.releaseSession(session);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Closing the newly created session");
                }
            }
        }
    }

    @Override
    public final Object loadObject() throws Exception {
        // Retrieve the session. If the session is closed, create a new session
        Session sessionToUse = getSession();
        // Load object
        Object obj = doLoadObject(sessionToUse, mapper);
        // Close only the newly created session
        releaseSession(sessionToUse);
        return obj;
    }

    protected abstract Object doLoadObject(Session session, Mapper mapper) throws Exception;
}
