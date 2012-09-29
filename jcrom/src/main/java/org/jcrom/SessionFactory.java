package org.jcrom;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Session Factory interface. This interface describes a simplfied contract for retrieving a session.
 * 
 * @author Nicolas Dos Santos
 */
public interface SessionFactory {

    /**
     * Returns a Session using the credentials and workspace on this SessionFactory implementation. The session
     * factory doesn't allow specification of a different workspace name because:
     * <p>
     *" Each Session object is associated one-to-one with a Workspace object. The Workspace object represents
     * a `view` of an actual repository workspace entity as seen through the authorization settings of its
     * associated Session." (quote from javax.jcr.Session javadoc).
     * </p>
     * @return the session.
     * @throws RepositoryException
     */
    Session getSession() throws RepositoryException;

}