# Using with Spring #

## Bean Declaration ##

Spring is a very popular dependency injection framework. It is easy to use JCROM with Spring, just construct a Jcrom instance with constructor based injection, supplying the classes to be mapped in the constructor. For example:

```
...
  <bean id="jcrom" class="org.jcrom.Jcrom">
    <constructor-arg type="java.util.Set">
      <set>
        <value type="java.lang.Class">com.mypackage.MyClass1</value>
        <value type="java.lang.Class">com.mypackage.MyClass2</value>
      </set>
    </constructor-arg>
  </bean>
...
```

## Spring Extension JCR ##

You could use [Spring Extension JCR](http://se-jcr.sourceforge.net/guide.html) with JCROM.

**Note:** SE-JCR [does not support JCR 2.0](https://jira.springsource.org/browse/SEJCR-21). But [Jörg Bellmann](https://github.com/jbellmann) did a great job to use JCR 2.0 with se-jcr. You can find the fork [here](https://github.com/jbellmann/jcr-springextension).

Firstly, create the following class to manage SessionFactory for se-jcr and JCROM:

```
import org.jcrom.SessionFactory;
import org.springframework.extensions.jcr.JcrSessionFactory;

public class JcromSessionFactory extends JcrSessionFactory implements SessionFactory {

}
```

And add in your Spring XML configuration:

```
<bean id="repository" class="org.apache.jackrabbit.core.TransientRepository">
    <constructor-arg index="0" type="java.lang.String" value="target/transient-repo/repository.xml" />
    <constructor-arg index="1" type="java.lang.String" value="target/transient-repo/repository"/>
</bean>

<bean id="jcrSessionFactory" class="my.jcr.JcromSessionFactory">
	<property name="repository" ref="repository" />
	<property name="credentials">
		<bean class="javax.jcr.SimpleCredentials">
			<constructor-arg index="0" value="admin" />
			<constructor-arg index="1" value="admin" />
		</bean>
	</property>
	<!-- register some bogus namespaces -->
	<!--
		<property name="namespaces"> <props> <prop
		key="foo">http://bar.com/jcr</prop> <prop
		key="hocus">http://pocus.com/jcr</prop> </props> </property>
	-->
	<!--
		register a simple listener <property name="eventListeners"> <list>
		<bean
		class="org.springframework.extensions.jcr.EventListenerDefinition">
		<property name="listener"> <bean
		class="org.springmodules.examples.jcr.DummyEventListener"/>
		</property> </bean> </list> </property>
	-->
</bean>

<bean id="jcrTemplate" class="org.springframework.extensions.jcr.JcrTemplate">
	<property name="sessionFactory" ref="jcrSessionFactory" />
	<property name="allowCreate" value="true" />
</bean>

<bean id="jcrom" class="org.jcrom.Jcrom">
	<constructor-arg index="0" type="boolean" value="true"/>
	<constructor-arg index="1" type="boolean" value="true"/>
	<constructor-arg index="2" type="java.util.Set">
	  <set>
		<value type="java.lang.Class">my.bean.Example</value>
		<value type="java.lang.Class">my.bean.ExampleChild</value>
		<value type="java.lang.Class">my.bean.ExampleParent</value>
	  </set>
	</constructor-arg>
	<property name="sessionFactory" ref="jcrSessionFactory" />
</bean>
```

If you want to use se-jcr transaction management, it must to use the same SessionFactory in the transaction manager:

```
<tx:annotation-driven transaction-manager="jcrTransactionManager"/>
    
<bean id="jcrTransactionManager" class="org.springframework.extensions.jcr.jackrabbit.LocalTransactionManager">
	<property name="sessionFactory" ref="jcrSessionFactory"/>
	<qualifier value="jcrTransactionManager" />
</bean>
```

You could use the following abstract class to use JCROM with Spring JcrTemplate:

```
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.jcrom.JcrMappingException;
import org.jcrom.Jcrom;
import org.jcrom.annotations.JcrNode;
import org.jcrom.util.JcrUtils;
import org.jcrom.util.PathUtils;
import org.jcrom.util.ReflectionUtils;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.support.JcrDaoSupport;

public abstract class AbstractJcrDaoSupport<T> extends JcrDaoSupport implements GenericJcrDAO<T> {

    private final Class<T> entityClass;
    private Jcrom jcrom;
    private String[] mixinTypes;
    private boolean isVersionable;

    public AbstractJcrDaoSupport() {
        this(null, new String[0]);
    }

    public AbstractJcrDaoSupport(Jcrom jcrom) {
        this(jcrom, new String[0]);
    }

    @SuppressWarnings("unchecked")
    public AbstractJcrDaoSupport(Jcrom jcrom, String[] mixinTypes) {
        Class<?> clazz = getClass();
        while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
            clazz = clazz.getSuperclass();
        }

        this.entityClass = (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];

        setJcrom(jcrom);
        setMixinTypes(mixinTypes);
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    protected void setJcrom(Jcrom jcrom) {
        this.jcrom = jcrom;
    }

    protected Jcrom getJcrom() {
        return jcrom;
    }

    protected void setMixinTypes(String[] mixinTypes) {
        this.mixinTypes = new String[mixinTypes.length];
        System.arraycopy(mixinTypes, 0, this.mixinTypes, 0, mixinTypes.length);
        this.isVersionable = checkIfVersionable();
    }

    protected String[] getMixinTypes() {
        return mixinTypes;
    }

    protected void setJcrTemplate(JcrTemplate jcrTemplate) {
        this.setTemplate(jcrTemplate);
    }

    protected JcrTemplate getJcrTemplate() {
        return this.getTemplate();
    }

    private boolean checkIfVersionable() {
        // check mixin type array
        for (String mixinType : getMixinTypes()) {
            if (mixinType.equals("mix:versionable") || mixinType.equals(NodeType.MIX_VERSIONABLE)) {
                return true;
            }
        }
        // check the class annotation
        JcrNode jcrNode = ReflectionUtils.getJcrNodeAnnotation(getEntityClass());
        if (jcrNode != null && jcrNode.mixinTypes() != null) {
            for (String mixinType : jcrNode.mixinTypes()) {
                if (mixinType.equals("mix:versionable") || mixinType.equals(NodeType.MIX_VERSIONABLE)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public T create(T entity) {
        return create(getParentPath(entity), entity);
    }

    private String getParentPath(final T entity) {
        return getTemplate().execute(new JcrCallback<String>() {
            @Override
            public String doInJcr(Session session) throws RepositoryException {
                // Set the parent path with a default value, the entity path if it is not null, otherwise the root path "/" 
                String entityPath = getJcrom().getPath(entity);
                String parentPath = entityPath != null ? entityPath : "/";

                // Search the parent object annotated with JcrParentNode
                Object parentObject = getJcrom().getParentObject(entity);
                if (parentObject != null) {
                    // Retrieve the parent path and check if the parent node exists
                    parentPath = getJcrom().getPath(parentObject);
                    if (!exists(parentPath)) {
                        throw new JcrMappingException("the parent with path '" + parentPath + "' is not created!");
                    }
                    Node parentNode = PathUtils.getNode(parentPath, session);
                    // Search a child container path annotated with JcrChildNode and the type is the same as 'entity' clazz
                    String childContainerPath = getJcrom().getChildContainerPath(entity, parentObject, parentNode);
                    if (childContainerPath != null) {
                        parentPath = childContainerPath;
                    }
                }
                return parentPath;
            }
        });
    }

    @Override
    public T create(final String parentNodePath, final T entity) {
        return getTemplate().execute(new JcrCallback<T>() {
            @Override
            public T doInJcr(Session session) throws RepositoryException {
                String entityName = getJcrom().getName(entity);
                if (entityName == null || entityName.equals("")) {
                    throw new JcrMappingException("The name of the entity being created is empty!");
                }
                if (parentNodePath == null || parentNodePath.equals("")) {
                    throw new JcrMappingException("The parent path of the entity being created is empty!");
                }

                Node parentNode = PathUtils.getNode(parentNodePath, session);
                if (isVersionable) {
                    // if this is a versionable node, then we need to check out parent before moving the node
                    if (JcrUtils.hasMixinType(parentNode, "mix:versionable") || JcrUtils.hasMixinType(parentNode, NodeType.MIX_VERSIONABLE)) {
                        JcrUtils.checkout(parentNode);
                    }
                }
                Node newNode = getJcrom().addNode(parentNode, entity, getMixinTypes());
                session.save();
                if (isVersionable) {
                    //newNode.checkin();
                    JcrUtils.checkinRecursively(newNode);
                    // check in the parent node
                    if ((JcrUtils.hasMixinType(parentNode, "mix:versionable") || JcrUtils.hasMixinType(parentNode, NodeType.MIX_VERSIONABLE)) && parentNode.isCheckedOut()) {
                        JcrUtils.checkin(parentNode);
                    }
                }
                return entity;
            }
        });
    }

    @Override
    public T update(T entity) {
        return update(entity, "*", -1);
    }

    @Override
    public T update(final T entity, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<T>() {
            @Override
            public T doInJcr(Session session) throws RepositoryException {
                Node node;
                try {
                    node = PathUtils.getNode(getJcrom().getPath(entity), session);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not update node", e);
                }
                return update(node, entity, childNameFilter, maxDepth);
            }
        });
    }

    @Override
    public T updateById(T entity, String id) {
        return updateById(entity, id, "*", -1);
    }

    @Override
    public T updateById(final T entity, final String id, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<T>() {
            @Override
            public T doInJcr(Session session) throws RepositoryException {
                Node node;
                try {
                    node = session.getNodeByIdentifier(id);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not update node", e);
                }
                return update(node, entity, childNameFilter, maxDepth);
            }
        });
    }

    protected T update(final Node node, final T entity, final String childNameFilter, final int maxDepth) {
        try {
            if (isVersionable) {
                //node.checkout();
                JcrUtils.checkoutRecursively(node);
            }
            Node updatedNode = getJcrom().updateNode(node, entity, childNameFilter, maxDepth);
            updatedNode.getSession().save();
            if (isVersionable) {
                //node.checkin();
                JcrUtils.checkinRecursively(updatedNode);
            }
            return entity;
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not update node", e);
        }
    }

    @Override
    public void move(final T entity, final String newParentPath) {
        getTemplate().execute(new JcrCallback<Object>() {
            @Override
            public Object doInJcr(Session session) throws RepositoryException {
                // if this is a versionable node, then we need to check out both
                // the old parent and the new parent before moving the node
                try {
                    String sourcePath = getJcrom().getPath(entity);
                    String entityName = getJcrom().getName(entity);
                    Node oldParent = null;
                    Node newParent = null;
                    if (isVersionable) {
                        oldParent = PathUtils.getNode(sourcePath, session).getParent();
                        newParent = PathUtils.getNode(newParentPath, session);

                        if (JcrUtils.hasMixinType(oldParent, "mix:versionable") || JcrUtils.hasMixinType(oldParent, NodeType.MIX_VERSIONABLE)) {
                            //oldParent.checkout();
                            JcrUtils.checkout(oldParent);
                        }
                        if (JcrUtils.hasMixinType(newParent, "mix:versionable") || JcrUtils.hasMixinType(newParent, NodeType.MIX_VERSIONABLE)) {
                            //newParent.checkout();
                            JcrUtils.checkout(newParent);
                        }
                    }

                    if (newParentPath.equals("/")) {
                        // special case, moving to root
                        session.move(sourcePath, newParentPath + entityName);
                    } else {
                        session.move(sourcePath, newParentPath + "/" + entityName);
                    }
                    session.save();

                    if (isVersionable) {
                        if ((JcrUtils.hasMixinType(oldParent, "mix:versionable") || JcrUtils.hasMixinType(oldParent, NodeType.MIX_VERSIONABLE)) && oldParent.isCheckedOut()) {
                            //oldParent.checkin();
                            JcrUtils.checkin(oldParent);
                        }
                        if ((JcrUtils.hasMixinType(newParent, "mix:versionable") || JcrUtils.hasMixinType(newParent, NodeType.MIX_VERSIONABLE)) && newParent.isCheckedOut()) {
                            //newParent.checkin();
                            JcrUtils.checkin(newParent);
                        }
                    }

                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not move node", e);
                }
                return null;
            }
        });
    }

    @Override
    public void remove(final String path) {
        getTemplate().execute(new JcrCallback<Object>() {
            @Override
            public Object doInJcr(Session session) throws RepositoryException {
                try {
                    Node parent = null;
                    if (isVersionable) {
                        parent = PathUtils.getNode(path, session).getParent();
                        if (JcrUtils.hasMixinType(parent, "mix:versionable") || JcrUtils.hasMixinType(parent, NodeType.MIX_VERSIONABLE)) {
                            //parent.checkout();
                            JcrUtils.checkout(parent);
                        }
                    }

                    PathUtils.getNode(path, session).remove();
                    session.save();

                    if (isVersionable) {
                        if ((JcrUtils.hasMixinType(parent, "mix:versionable") || JcrUtils.hasMixinType(parent, NodeType.MIX_VERSIONABLE)) && parent.isCheckedOut()) {
                            //parent.checkin();
                            JcrUtils.checkin(parent);
                        }
                    }

                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not remove node", e);
                }
                return null;
            }
        });
    }

    @Override
    public void removeById(final String id) {
        getTemplate().execute(new JcrCallback<Object>() {
            @Override
            public Object doInJcr(Session session) throws RepositoryException {
                try {
                    Node node = session.getNodeByIdentifier(id);

                    Node parent = null;
                    if (isVersionable) {
                        parent = node.getParent();
                        if (JcrUtils.hasMixinType(parent, "mix:versionable") || JcrUtils.hasMixinType(parent, NodeType.MIX_VERSIONABLE)) {
                            //parent.checkout();
                            JcrUtils.checkout(parent);
                        }
                    }

                    node.remove();
                    session.save();

                    if (isVersionable) {
                        if ((JcrUtils.hasMixinType(parent, "mix:versionable") || JcrUtils.hasMixinType(parent, NodeType.MIX_VERSIONABLE)) && parent.isCheckedOut()) {
                            //parent.checkin();
                            JcrUtils.checkin(parent);
                        }
                    }

                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not remove node", e);
                }
                return null;
            }
        });
    }

    @Override
    public boolean exists(final String path) {
        return getTemplate().execute(new JcrCallback<Boolean>() {
            @Override
            public Boolean doInJcr(Session session) throws RepositoryException {
                try {
                    return session.getRootNode().hasNode(PathUtils.relativePath(path));
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not check if node exists", e);
                }
            }
        });
    }

    @Override
    public T get(String path) {
        return get(path, "*", -1);
    }

    @Override
    public T get(final String path, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<T>() {
            @Override
            public T doInJcr(Session session) throws RepositoryException {
                if (exists(path)) {
                    Node node;
                    try {
                        node = PathUtils.getNode(path, session);
                    } catch (final RepositoryException e) {
                        throw new JcrMappingException("Could not get node", e);
                    }
                    return getJcrom().fromNode(getEntityClass(), node, childNameFilter, maxDepth);
                } else {
                    return null;
                }
            }
        });
    }

    @Override
    public List<T> getAll(String path) {
        return getAll(path, "*", -1);
    }

    @Override
    public List<T> getAll(String path, long startIndex, long resultSize) {
        return getAll(path, "*", -1, startIndex, resultSize);
    }

    @Override
    public List<T> getAll(final String path, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    return toList(PathUtils.getNodes(path, session), childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not find nodes", e);
                }
            }
        });
    }

    @Override
    public List<T> getAll(final String path, final String childNameFilter, final int maxDepth, final long startIndex, final long resultSize) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    NodeIterator nodeIterator = PathUtils.getNodes(path, session);
                    nodeIterator.skip(startIndex);
                    return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not find nodes", e);
                }
            }
        });
    }

    @Override
    public T loadById(String id) {
        return loadById(id, "*", -1);
    }

    @Override
    public T loadById(final String id, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<T>() {
            @Override
            public T doInJcr(Session session) throws RepositoryException {
                Node node;
                try {
                    node = session.getNodeByIdentifier(id);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not load node", e);
                }
                return getJcrom().fromNode(getEntityClass(), node, childNameFilter, maxDepth);
            }
        });
    }

    @Override
    public T getVersion(String path, String versionName) {
        return getVersion(path, versionName, "*", -1);
    }

    @Override
    public T getVersion(final String path, final String versionName, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<T>() {
            @Override
            public T doInJcr(Session session) throws RepositoryException {
                try {
                    return getVersion(PathUtils.getNode(path, session), versionName, childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version", e);
                }
            }
        });
    }

    @Override
    public T getVersionById(String id, String versionName) {
        return getVersionById(id, versionName, "*", -1);
    }

    @Override
    public T getVersionById(final String id, final String versionName, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<T>() {
            @Override
            public T doInJcr(Session session) throws RepositoryException {
                try {
                    Node node = session.getNodeByIdentifier(id);
                    return getVersion(node, versionName, childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version", e);
                }
            }
        });
    }

    protected T getVersion(final Node node, final String versionName, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<T>() {
            @Override
            public T doInJcr(Session session) throws RepositoryException {
                try {
                    //VersionHistory versionHistory = node.getVersionHistory();
                    VersionHistory versionHistory = JcrUtils.getVersionManager(session).getVersionHistory(node.getPath());
                    Version version = versionHistory.getVersion(versionName);
                    return getJcrom().fromNode(getEntityClass(), version.getNodes().nextNode(), childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version", e);
                }
            }
        });
    }

    @Override
    public void restoreVersion(String path, String versionName) {
        restoreVersion(path, versionName, true);
    }

    @Override
    public void restoreVersionById(String id, String versionName) {
        restoreVersionById(id, versionName, true);
    }

    @Override
    public void restoreVersion(final String path, final String versionName, final boolean removeExisting) {
        getTemplate().execute(new JcrCallback<Object>() {
            @Override
            public Object doInJcr(Session session) throws RepositoryException {
                try {
                    restoreVersion(PathUtils.getNode(path, session), versionName, removeExisting);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not restore version", e);
                }
                return null;
            }
        });
    }

    @Override
    public void restoreVersionById(final String id, final String versionName, final boolean removeExisting) {
        getTemplate().execute(new JcrCallback<Object>() {
            @Override
            public Object doInJcr(Session session) throws RepositoryException {
                try {
                    Node node = session.getNodeByIdentifier(id);
                    restoreVersion(node, versionName, removeExisting);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not restore version", e);
                }
                return null;
            }
        });
    }

    protected void restoreVersion(final Node node, final String versionName, final boolean removeExisting) {
        getTemplate().execute(new JcrCallback<Object>() {
            @Override
            public Object doInJcr(Session session) throws RepositoryException {
                try {
                    //node.checkout();
                    JcrUtils.checkout(node);
                    //node.restore(versionName, removeExisting);
                    JcrUtils.getVersionManager(session).restore(node.getPath(), versionName, removeExisting);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not restore version", e);
                }
                return null;
            }
        });
    }

    @Override
    public void removeVersion(final String path, final String versionName) {
        getTemplate().execute(new JcrCallback<Object>() {
            @Override
            public Object doInJcr(Session session) throws RepositoryException {
                try {
                    removeVersion(PathUtils.getNode(path, session), versionName);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not remove version", e);
                }
                return null;
            }
        });
    }

    @Override
    public void removeVersionById(final String id, final String versionName) {
        getTemplate().execute(new JcrCallback<Object>() {
            @Override
            public Object doInJcr(Session session) throws RepositoryException {
                try {
                    Node node = session.getNodeByIdentifier(id);
                    removeVersion(node, versionName);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not remove version", e);
                }
                return null;
            }
        });
    }

    protected void removeVersion(final Node node, final String versionName) {
        getTemplate().execute(new JcrCallback<Object>() {
            @Override
            public Object doInJcr(Session session) throws RepositoryException {
                try {
                    //VersionHistory versionHistory = node.getVersionHistory();
                    VersionHistory versionHistory = JcrUtils.getVersionManager(session).getVersionHistory(node.getPath());
                    versionHistory.removeVersion(versionName);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not remove version", e);
                }
                return null;
            }
        });
    }

    @Override
    public long getVersionSize(final String path) {
        return getTemplate().execute(new JcrCallback<Long>() {
            @Override
            public Long doInJcr(Session session) throws RepositoryException {
                try {
                    return getVersionSize(PathUtils.getNode(path, session));
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version history size", e);
                }
            }
        });
    }

    @Override
    public long getVersionSizeById(final String id) {
        return getTemplate().execute(new JcrCallback<Long>() {
            @Override
            public Long doInJcr(Session session) throws RepositoryException {
                try {
                    Node node = session.getNodeByIdentifier(id);
                    return getVersionSize(node);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version history size", e);
                }
            }
        });
    }

    protected long getVersionSize(final Node node) {
        return getTemplate().execute(new JcrCallback<Long>() {
            @Override
            public Long doInJcr(Session session) throws RepositoryException {
                try {
                    //VersionHistory versionHistory = node.getVersionHistory();
                    VersionHistory versionHistory = JcrUtils.getVersionManager(session).getVersionHistory(node.getPath());
                    return versionHistory.getAllVersions().getSize() - 1;
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version history size", e);
                }
            }
        });
    }

    @Override
    public List<T> getVersionList(final String path) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    return getVersionList(PathUtils.getNode(path, session), "*", -1);
                } catch (final RepositoryException e) {
                    throw new JcrMappingException("Could not get version list", e);
                }
            }
        });
    }

    @Override
    public List<T> getVersionList(final String path, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    return getVersionList(PathUtils.getNode(path, session), childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version list", e);
                }
            }
        });
    }

    @Override
    public List<T> getVersionList(final String path, final String childNameFilter, final int maxDepth, final long startIndex, final long resultSize) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    return getVersionList(PathUtils.getNode(path, session), childNameFilter, maxDepth, startIndex, resultSize);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version list", e);
                }
            }
        });
    }

    @Override
    public List<T> getVersionListById(final String id) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    Node node = session.getNodeByIdentifier(id);
                    return getVersionList(node, "*", -1);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version list", e);
                }
            }
        });
    }

    @Override
    public List<T> getVersionListById(final String id, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    Node node = session.getNodeByIdentifier(id);
                    return getVersionList(node, childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version list", e);
                }
            }
        });
    }

    @Override
    public List<T> getVersionListById(final String id, final String childNameFilter, final int maxDepth, final long startIndex, final long resultSize) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    Node node = session.getNodeByIdentifier(id);
                    return getVersionList(node, childNameFilter, maxDepth, startIndex, resultSize);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version list", e);
                }
            }
        });
    }

    protected List<T> getVersionList(final Node node, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    List<T> versionList = new ArrayList<T>();
                    //VersionHistory versionHistory = node.getVersionHistory();
                    VersionHistory versionHistory = JcrUtils.getVersionManager(session).getVersionHistory(node.getPath());
                    VersionIterator versionIterator = versionHistory.getAllVersions();
                    versionIterator.skip(1);
                    while (versionIterator.hasNext()) {
                        Version version = versionIterator.nextVersion();
                        NodeIterator nodeIterator = version.getNodes();
                        while (nodeIterator.hasNext()) {
                            T entityVersion = getJcrom().fromNode(getEntityClass(), nodeIterator.nextNode(), childNameFilter, maxDepth);
                            //Version baseVersion = node.getBaseVersion();
                            Version baseVersion = JcrUtils.getVersionManager(session).getBaseVersion(node.getPath());
                            getJcrom().setBaseVersionInfo(entityVersion, baseVersion.getName(), baseVersion.getCreated());
                            versionList.add(entityVersion);
                        }
                    }
                    return versionList;
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version list", e);
                }
            }
        });
    }

    protected List<T> getVersionList(final Node node, final String childNameFilter, final int maxDepth, final long startIndex, final long resultSize) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    List<T> versionList = new ArrayList<T>();
                    //VersionHistory versionHistory = node.getVersionHistory();
                    VersionHistory versionHistory = JcrUtils.getVersionManager(session).getVersionHistory(node.getPath());
                    VersionIterator versionIterator = versionHistory.getAllVersions();
                    versionIterator.skip(1 + startIndex);

                    long counter = 0;
                    while (versionIterator.hasNext()) {
                        if (counter == resultSize) {
                            break;
                        }
                        Version version = versionIterator.nextVersion();
                        NodeIterator nodeIterator = version.getNodes();
                        while (nodeIterator.hasNext()) {
                            versionList.add(getJcrom().fromNode(getEntityClass(), nodeIterator.nextNode(), childNameFilter, maxDepth));
                        }
                        counter++;
                    }
                    return versionList;
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get version list", e);
                }
            }
        });
    }

    @Override
    public long getSize(final String rootPath) {
        return getTemplate().execute(new JcrCallback<Long>() {
            @Override
            public Long doInJcr(Session session) throws RepositoryException {
                try {
                    NodeIterator nodeIterator = PathUtils.getNode(rootPath, session).getNodes();
                    return nodeIterator.getSize();
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not get list size", e);
                }
            }
        });
    }

    @Override
    public List<T> findAll(String rootPath) {
        return findAll(rootPath, "*", -1);
    }

    @Override
    public List<T> findAll(String rootPath, long startIndex, long resultSize) {
        return findAll(rootPath, "*", -1, startIndex, resultSize);
    }

    @Override
    public List<T> findAll(final String rootPath, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    return toList(PathUtils.getNode(rootPath, session).getNodes(), childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not find nodes", e);
                }
            }
        });
    }

    @Override
    public List<T> findAll(final String rootPath, final String childNameFilter, final int maxDepth, final long startIndex, final long resultSize) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    NodeIterator nodeIterator = PathUtils.getNode(rootPath, session).getNodes();
                    nodeIterator.skip(startIndex);
                    return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not find nodes", e);
                }
            }
        });
    }

    /**
     * Find JCR nodes that match the xpath supplied, and map to objects.
     * 
     * @param xpath the XPath for finding the nodes
     * @param childNameFilter comma separated list of names of child nodes to 
     * load ("*" loads all, "none" loads no children, and "-" at the beginning
     * makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no 
     * child nodes are loaded, while a negative value means that no 
     * restrictions are set on the depth).
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return a list of all objects found
     */
    protected List<T> findByXPath(final String xpath, final String childNameFilter, final int maxDepth, final long startIndex, final long resultSize) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    QueryResult result = query(xpath, session);
                    NodeIterator nodeIterator = result.getNodes();
                    nodeIterator.skip(startIndex);
                    return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not find nodes by XPath", e);
                }
            }
        });
    }

    /**
     * Find JCR nodes that match the xpath supplied, and map to objects.
     * 
     * @param xpath the XPath for finding the nodes
     * @param childNameFilter comma separated list of names of child nodes to 
     * load ("*" loads all, "none" loads no children, and "-" at the beginning
     * makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no 
     * child nodes are loaded, while a negative value means that no 
     * restrictions are set on the depth).
     * @return a list of all objects found
     */
    protected List<T> findByXPath(final String xpath, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    QueryResult result = query(xpath, session);
                    return toList(result.getNodes(), childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not find nodes by XPath", e);
                }
            }
        });
    }

    /**
     * Find JCR nodes that match the SQL supplied, and map to objects.
     * 
     * @param sql the SQL for finding the nodes
     * @param childNameFilter comma separated list of names of child nodes to 
     * load ("*" loads all, "none" loads no children, and "-" at the beginning
     * makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no 
     * child nodes are loaded, while a negative value means that no 
     * restrictions are set on the depth).
     * @param startIndex the zero based index of the first item to return
     * @param resultSize the number of items to return
     * @return a list of all objects found
     */
    protected List<T> findBySql(final String sql, final String childNameFilter, final int maxDepth, final long startIndex, final long resultSize) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    QueryResult result = query(sql, Query.JCR_SQL2, session);
                    NodeIterator nodeIterator = result.getNodes();
                    nodeIterator.skip(startIndex);
                    return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not find nodes by SQL2", e);
                }
            }
        });
    }

    /**
     * Find JCR nodes that match the SQL supplied, and map to objects.
     * 
     * @param sql the SQL for finding the nodes
     * @param childNameFilter comma separated list of names of child nodes to 
     * load ("*" loads all, "none" loads no children, and "-" at the beginning
     * makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no 
     * child nodes are loaded, while a negative value means that no 
     * restrictions are set on the depth).
     * @return a list of all objects found
     */
    protected List<T> findBySql(final String sql, final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    QueryResult result = query(sql, Query.JCR_SQL2, session);
                    return toList(result.getNodes(), childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not find nodes by SQL2", e);
                }
            }
        });
    }

    /**
     * Find JCR nodes with one or more selectors, and map to objects.
     * 
     * @param source the node-tuple source; non-null
     * @param constraint the constraint, or null if none
     * @param orderings zero or more orderings; null is equivalent to a zero-length array
     * @param columns  the columns; null is equivalent to a zero-length array
     * @param childNameFilter comma separated list of names of child nodes to 
     * load ("*" loads all, "none" loads no children, and "-" at the beginning
     * makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no 
     * child nodes are loaded, while a negative value means that no 
     * restrictions are set on the depth).
     * @return a list of objects mapped from the nodes
     */
    protected List<T> findByQOM(final Source source, final Constraint constraint, final Ordering orderings[], final Column columns[], final String childNameFilter, final int maxDepth) {
        return getTemplate().execute(new JcrCallback<List<T>>() {
            @Override
            public List<T> doInJcr(Session session) throws RepositoryException {
                try {
                    QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
                    Query query = factory.createQuery(source, constraint, orderings, columns);
                    QueryResult result = query.execute();
                    return toList(result.getNodes(), childNameFilter, maxDepth);
                } catch (RepositoryException e) {
                    throw new JcrMappingException("Could not find nodes by XPath", e);
                }
            }
        });
    }

    private QueryResult query(String statement, Session session) throws RepositoryException {
        return query(statement, null, session);
    }

    @SuppressWarnings("deprecation")
    private QueryResult query(String statement, String language, Session session) throws RepositoryException {
        // check language
        String lang = language;
        if (lang == null) {
            lang = Query.XPATH;
        }

        // get query manager
        QueryManager manager = session.getWorkspace().getQueryManager();

        Query query = manager.createQuery(statement, lang);

        return query.execute();
    }

    /**
     * Maps JCR nodes to a List of JcrEntity implementations.
     * 
     * @param nodeIterator the iterator pointing to the nodes
     * @param childNameFilter comma separated list of names of child nodes to 
     * load ("*" loads all, "none" loads no children, and "-" at the beginning
     * makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no 
     * child nodes are loaded, while a negative value means that no 
     * restrictions are set on the depth).
     * @return a list of objects mapped from the nodes
     */
    protected List<T> toList(NodeIterator nodeIterator, String childNameFilter, int maxDepth) {
        List<T> objects = new ArrayList<T>();
        while (nodeIterator.hasNext()) {
            objects.add(getJcrom().fromNode(getEntityClass(), nodeIterator.nextNode(), childNameFilter, maxDepth));
        }
        return objects;
    }

    /**
     * Maps JCR nodes to a List of JcrEntity implementations.
     * 
     * @param nodeIterator the iterator pointing to the nodes
     * @param childNameFilter comma separated list of names of child nodes to 
     * load ("*" loads all, "none" loads no children, and "-" at the beginning
     * makes it an exclusion filter)
     * @param maxDepth the maximum depth of loaded child nodes (0 means no 
     * child nodes are loaded, while a negative value means that no 
     * restrictions are set on the depth).
     * @param resultSize the number of items to retrieve from the iterator
     * @return a list of objects mapped from the nodes
     */
    protected List<T> toList(NodeIterator nodeIterator, String childNameFilter, int maxDepth, long resultSize) {
        List<T> objects = new ArrayList<T>();
        long counter = 0;
        while (nodeIterator.hasNext()) {
            if (counter == resultSize) {
                break;
            }
            objects.add(getJcrom().fromNode(getEntityClass(), nodeIterator.nextNode(), childNameFilter, maxDepth));
            counter++;
        }
        return objects;
    }

    @Override
    public T updateByUUID(T entity, String uuid) {
        return updateById(entity, uuid);
    }

    @Override
    public T updateByUUID(T entity, String uuid, String childNameFilter, int maxDepth) {
        return updateById(entity, uuid, childNameFilter, maxDepth);
    }

    @Override
    public void removeByUUID(String uuid) {
        removeById(uuid);
    }

    @Override
    public T loadByUUID(String uuid) {
        return loadById(uuid);
    }

    @Override
    public T loadByUUID(String uuid, String childNameFilter, int maxDepth) {
        return loadById(uuid, childNameFilter, maxDepth);
    }

    @Override
    public List<T> getVersionListByUUID(String uuid) {
        return getVersionListById(uuid);
    }

    @Override
    public List<T> getVersionListByUUID(String uuid, String childNameFilter, int maxDepth) {
        return getVersionListById(uuid, childNameFilter, maxDepth);
    }

    @Override
    public List<T> getVersionListByUUID(String uuid, String childNameFilter, int maxDepth, long startIndex, long resultSize) {
        return getVersionListById(uuid, childNameFilter, maxDepth, startIndex, resultSize);
    }

    @Override
    public long getVersionSizeByUUID(String uuid) {
        return getVersionSizeById(uuid);
    }

    @Override
    public T getVersionByUUID(String uuid, String versionName) {
        return getVersionById(uuid, versionName);
    }

    @Override
    public T getVersionByUUID(String uuid, String versionName, String childNameFilter, int maxDepth) {
        return getVersionById(uuid, versionName, childNameFilter, maxDepth);
    }

    @Override
    public void restoreVersionByUUID(String uuid, String versionName) {
        restoreVersionById(uuid, versionName);
    }

    @Override
    public void restoreVersionByUUID(String uuid, String versionName, boolean removeExisting) {
        restoreVersionById(uuid, versionName, removeExisting);
    }

    @Override
    public void removeVersionByUUID(String uuid, String versionName) {
        removeVersionById(uuid, versionName);
    }

}
```

Following is a DAO example which extends the previous Abstract class:

```
import java.util.List;

import javax.annotation.PostConstruct;

import org.jcrom.Jcrom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.stereotype.Repository;

import my.JcrUser;

@Repository
public class JcrUserDaoImpl extends AbstractJcrDaoSupport<JcrUser> implements JcrUserDao {

    @Autowired
    public JcrUserDaoImpl(Jcrom jcrom, JcrTemplate jcrTemplate) {
        super(jcrom);
        setJcrTemplate(jcrTemplate);
    }

    @PostConstruct
    public void init() throws Exception {
        if (this.getTemplate() == null) {
            throw new ApplicationContextException("Must set JCR Template bean property on " + getClass());
        }
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

-----------------------------------------------------------------------------------------

import org.jcrom.dao.JcrDAO;

public interface GenericJcrDAO<T> extends JcrDAO<T> {

}
```

Go [back](UserGuide.md) to the overview.