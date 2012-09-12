package org.jcrom;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.jcrom.dao.AbstractJcrDAO;
import org.jcrom.util.SessionFactoryUtils;

public class TreeDAO extends AbstractJcrDAO<Tree> {

    private boolean flag;

    private Session currentSession;

    public TreeDAO(Jcrom jcrom) throws RepositoryException {
        super(jcrom);
    }

    @Override
    protected Session getSession() {
        if (!flag) {
            Session session = super.getSession();
            currentSession = session != null ? session : SessionFactoryUtils.getSession(getJcrom().getSessionFactory());
            flag = true;
        }
        return currentSession;
    }

    protected void releaseSession(Session session) {
        SessionFactoryUtils.releaseSession(session);
        currentSession = null;
        flag = false;
    }

    @Override
    public Tree create(Tree entity) {
        Session session = getSession();
        Tree result = super.create(entity);
        releaseSession(session);
        return result;
    }

    @Override
    public Tree loadById(String id) {
        Session session = getSession();
        Tree result = super.loadById(id);
        releaseSession(session);
        return result;
    }

    @Override
    public Tree update(Tree entity) {
        Session session = getSession();
        Tree result = super.update(entity);
        releaseSession(session);
        return result;
    }
}
