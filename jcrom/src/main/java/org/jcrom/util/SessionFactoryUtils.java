package org.jcrom.util;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.jcrom.JcrMappingException;
import org.jcrom.SessionFactory;

/**
 * Helper class featuring methods for JCR Session handling.
 * 
 * @author Nicolas Dos Santos
 */
public final class SessionFactoryUtils {

	private SessionFactoryUtils() {
	}
	
    /**
     * Get a JCR Session from the SessionFactory
     * @param sessionFactory JCR Repository to create session with
     * @throws RepositoryException
     * @return {@link Session}
     */
    public static Session getSession(SessionFactory sessionFactory) throws JcrMappingException {
        try {
            if (sessionFactory == null) {
                throw new IllegalArgumentException("No sessionFactory specified");
            }

            Session session = sessionFactory.getSession();

            return session;
        } catch (RepositoryException ex) {
            throw new JcrMappingException("Could not open Jcr Session", ex);
        }
    }

    /**
     * Close the given JCR Session
     * @param session the Session to close
     */
    public static void releaseSession(Session session) {
        if (session == null) {
            return;
        }
        session.logout();
    }

}