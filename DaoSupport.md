# DAO support #

It is often wise to abstract the underlying persistence strategy away from the calling code, by encapsulating the persistence calls within Data Access Objects (DAOs). That means that your controller (or action) classes can have a DAO object injected, and use it, without having to worry whether the object is being persisted to a database, XML, or JCR. Also, session management can be kept outside the scope of controllers.

JCROM supports this methodology by providing a DAO interface, and an abstract DAO implementation that uses JCROM to map objects to/from JCR. Look at the [javadoc for JcrDAO](http://jcrom.googlecode.com/svn/trunk/apidocs/org/jcrom/dao/JcrDAO.html) to see which methods are implemented.

JCROM offers an abstract implementation of this interface, AbstractJcrDAO, that implements all the methods (you can extend and add custom finder methods). Note that the finder methods can accept startIndex and resultSize arguments - this is useful when paginating over large result sets.

Since release 2.1.0, methods with "childNameFilter String, int maxDepth" in parameters have been deprecated. You must use the equivalent methods with NodeFilter as a parameter.
This allows to benefit from improvements made at the NodeFilter object (eg the new "filterDepth" parameter) and future improvements if any.

```
update(T entity, String childNameFilter, int maxDepth) -> update(T entity, NodeFilter nodeFilter)

myDao.update(myEntity, "*", -1) -> myDao.update(myEntity, new NodeFilter("*", -1))
```

Let's look at how we could use the DAO support to simplify our WeblogEntry management. First of all, we would need to create a DAO class that extends AbstractJcrDAO:

```
import javax.jcr.Node;
import javax.jcr.Session;
import org.jcrom.Jcrom;
import org.jcrom.dao.AbstractJcrDAO;

public class WeblogEntryDAO extends AbstractJcrDAO<WeblogEntry> {
	
	public WeblogEntryDAO( Session session, Jcrom jcrom ) {
		super(WeblogEntry.class, session, jcrom);
	}
}
```

Note that AbstractJcrDAO uses generics to define a parameter class. This means you don't have to do any casting when using the DAO methods.

Since all the methods are implemented for us, the only thing we need to do is implement a constructor. The constructor passes information on to the AbstractJcrDAO superclass (there are a few different constructors, refer to the javadoc for description of the arguments). Now, we can use this class to persist and load weblog entries:

```
Jcrom jcrom = ...;
Session session = ...;
String weblogEntryPath = ...;
try {
    WeblogEntryDAO weblogEntryDAO = new WeblogEntryDAO(session, jcrom);

    WeblogEntry weblogEntry = weblogEntryDAO.get(weblogEntryPath);
    weblogEntry.setExcerpt("This is the excerpt");

    weblogEntryDAO.update(weblogEntry);

} finally {
    session.logout();
}
```

In a web application environment, we would probably use a dependency injection framework (like Guice or Spring) to inject the Session and the Jcrom instance into the DAO, and then inject the DAO into a controller, so the controller would never directly deal with Jcrom or the Session.

I highly recommend using this approach, as it keeps your code very tidy and less error prone.

## Custom finder methods ##

Although the abstract DAO implementation has findAll() methods, we sometimes need custom finder methods to filter the data and apply custom sorting. The AbstractJcrDAO class provides a set of protected methods to make custom finder methods easier to create (findByXPath(...), findBySql(...), findByQOM(...), ...).

So, if we wanted to create a finder method that returns all posts where the title starts with a particular String, we could add the following method to the WeblogEntryDAO:

```
...
public List<WeblogEntry> findByStartChars( String chars ) throws Exception {
    return super.findByXPath("/jcr:root/" 
        + ROOT_PATH + "/*[jcr:like(@title, '" + chars + "%')]", "*", -1);
}
...
```

As you can see, adding custom finder methods is really easy.

## Versioning ##

The DAO implementation handles versioning transparently. If "mix:versionable" is one of the mixin types, then the AbstractJcrDAO class will automatically perform checkout and checkin on all write operations. Retrieving all versions of a node using the JCR API is a bit complex and needs many lines of code. The DAO simplifies this task with methods that retrieve the versions and automatically map to objects:

```
Jcrom jcrom = ...;
Session session = ...;
try {
    VersionedDAO versionedDao = new VersionedDAO(session, jcrom);
    
    VersionedEntity entity = new VersionedEntity();
    entity.setBody("first");
    // set other values
    ...

    versionedDao.create(entity); // this creates the first version

    entity.setBody("second");
    versionedDao.update(entity); // this creates a new version

    // we can get a list of all versions
    List<VersionedEntity> versions = versionedDao.getVersionList(entity.getPath());

    // we can also get one specific version
    VersionedEntity oldEntity = versionedDao.getVersion(entity.getPath(), "1.0");

    // and we can restore a version
    versionedDao.restoreVersion(entity.getPath(), "1.0");
 
} finally {
    session.logout();
}
```

## Custom session management ##

In some cases, when using a dependency injection framework (like Guice or Spring) to manage your DAOs, you don't want to inject the Session via the constructor.

For custom session management in your DAO, you just do the following:

  * Provide no session to the AbstractJcrDAO constructor (use [this constructor](http://jcrom.googlecode.com/svn/trunk/jcrom/apidocs/org/jcrom/dao/AbstractJcrDAO.html#AbstractJcrDAO(java.lang.Class,%20org.jcrom.Jcrom)))
  * Override the getSession() method to provide your own Session.

For example, if you want to use a Guice provider for your session, you could do:

```
import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.jcr.Session;
import org.jcrom.Jcrom;
import org.jcrom.dao.AbstractJcrDAO;
import my.User;

class UserJcrDAO extends AbstractJcrDAO<User> {

    private final Provider<Session> sessions;
    
    @Inject
    UserJcrDAO( Jcrom jcrom, Provider<Session> sessions ) {
        super(User.class, jcrom);
        this.sessions = sessions;
    }

    @Override
    protected Session getSession() {
        return sessions.get();
    }
}
```

For example, if you want to use a Spring JCR [SessionFactory](http://se-jcr.sourceforge.net/guide.html), you could use the following AbstractGenericJcrDAO class :

```
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.jcrom.Jcrom;
import org.jcrom.dao.AbstractJcrDAO;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionFactoryUtils;

public abstract class AbstractGenericJcrDAO<T> extends AbstractJcrDAO<T> implements GenericJcrDAO<T> {

    private SessionFactory jcrSessionFactory;

    public AbstractGenericJcrDAO(SessionFactory jcrSessionFactory, Jcrom jcrom) throws RepositoryException {
        super(jcrom);
        this.jcrSessionFactory = jcrSessionFactory;
    }

    @Override
    protected Session getSession() {
        if (jcrSessionFactory != null) {
            return SessionFactoryUtils.getSession(this.jcrSessionFactory, true);
        } else {
            return super.getSession();
        }
    }

    public final SessionFactory getSessionFactory() {
        return jcrSessionFactory;
    }

    public void setJcrSessionFactory(SessionFactory jcrSessionFactory) {
        this.jcrSessionFactory = jcrSessionFactory;
    }

}

-----------------------------------------------------------------------------------------

import org.jcrom.dao.JcrDAO;

public interface GenericJcrDAO<T> extends JcrDAO<T> {

}
```

```
import java.util.List;

import org.jcrom.Jcrom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import my.JcrUser;

@Repository
public class JcrUserDaoImpl extends AbstractGenericJcrDAO<JcrUser> implements JcrUserDao {

    @Autowired
    public JcrUserDaoImpl(Jcrom jcrom, SessionFactory jcrSessionFactory) throws RepositoryException {
        super(jcrSessionFactory, jcrom);
    }

    @Override
    public JcrUser findByUsername(String username) {
        List<JcrUser> list = super.findByXPath("/jcr:root/*[@title = '" + username + "']", "*", -1);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

}

-----------------------------------------------------------------------------------------

import my.JcrUser;

public interface JcrUserDao extends GenericJcrDAO<JcrUser> {

    JcrUser findByUsername(String username);

}
```

Go [back](UserGuide.md) to the overview.