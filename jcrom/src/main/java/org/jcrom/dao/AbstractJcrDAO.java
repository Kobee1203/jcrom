/**
 * Copyright (C) Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jcrom.dao;

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

/**
 * An abstract implementation of the JcrDAO interface. This should be extended
 * for specific entity implementations.
 * This class implements all the methods defined in the JcrDAO interface, and
 * provides a few protected methods that are useful for implementing custom
 * finder methods.
 * <br/><br/>
 * 
 * The constructor takes a JCR session, so an instance should be created per
 * session. The constructor also takes a Jcrom instance that can be shared
 * across multiple DAOs.
 * <br/><br/>
 * 
 * This implementation encapsulates exceptions in JcrMappingException, which
 * is a RuntimeException.
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public abstract class AbstractJcrDAO<T> implements JcrDAO<T> {

    protected final Jcrom jcrom;
    protected final Session session;
    protected final Class<T> entityClass;
    protected final String[] mixinTypes;
    protected final boolean isVersionable;

    /**
     * <p>
     * Use this constructor when you intend to override the getSession()
     * method to provide your own session management (for example via
     * Guice providers). You can also use the other constructors and just
     * pass null as the session.
     * </p>
     * <p>The entityClass is retrieved from {@link ParameterizedType} in the superclass.</p>
     * 
     * @param jcrom the Jcrom instance to use for object mapping
     */
    public AbstractJcrDAO(Jcrom jcrom) {
        this(null, null, jcrom, new String[0]);
    }

    /**
     * Constructor. The entityClass is retrieved from {@link ParameterizedType} in the superclass.
     * 
     * @param session the current JCR session
     * @param jcrom the Jcrom instance to use for object mapping
     */
    public AbstractJcrDAO(Session session, Jcrom jcrom) {
        this(null, session, jcrom, new String[0]);
    }

    /**
     * Use this constructor when you intend to override the getSession()
     * method to provide your own session management (for example via
     * Guice providers). You can also use the other constructors and just
     * pass null as the session.
     * 
     * @param entityClass the class handled by this DAO implementation
     * @param jcrom the Jcrom instance to use for object mapping
     */
    public AbstractJcrDAO(Class<T> entityClass, Jcrom jcrom) {
        this(entityClass, null, jcrom, new String[0]);
    }

    /**
     * Constructor.
     * 
     * @param entityClass the class handled by this DAO implementation
     * @param session the current JCR session
     * @param jcrom the Jcrom instance to use for object mapping
     */
    public AbstractJcrDAO(Class<T> entityClass, Session session, Jcrom jcrom) {
        this(entityClass, session, jcrom, new String[0]);
    }

    /**
     * Constructor.
     * 
     * @param entityClass the class handled by this DAO implementation. If entityClass is <code>null</code>, the entityClass is retrieved from {@link ParameterizedType} in the superclass
     * @param session the current JCR session
     * @param jcrom the Jcrom instance to use for object mapping
     * @param mixinTypes an array of mixin types to apply to new nodes
     */
    @SuppressWarnings("unchecked")
    public AbstractJcrDAO(Class<T> entityClass, Session session, Jcrom jcrom, String[] mixinTypes) {
        if (entityClass == null) {
            Class<?> clazz = getClass();
            while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
                clazz = clazz.getSuperclass();
            }
            this.entityClass = (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
        } else {
            this.entityClass = entityClass;
        }
        this.session = session;
        this.jcrom = jcrom;
        this.mixinTypes = new String[mixinTypes.length];
        System.arraycopy(mixinTypes, 0, this.mixinTypes, 0, mixinTypes.length);
        this.isVersionable = checkIfVersionable();
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    protected Session getSession() {
        return session;
    }

    protected Jcrom getJcrom() {
        return jcrom;
    }

    protected String[] getMixinTypes() {
        return mixinTypes;
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

    protected Node getNodeById(String id) throws RepositoryException {
        // return getSession().getNodeByUUID(uuid);
        return getSession().getNodeByIdentifier(id);
    }

    protected Node getNode(String absolutePath) throws RepositoryException {
        return PathUtils.getNode(absolutePath, getSession());
    }

    protected NodeIterator getNodes(String absolutePath) throws RepositoryException {
        return PathUtils.getNodes(absolutePath, getSession());
    }

    @Override
    public T create(T entity) {
        return create(getParentPath(entity), entity);
    }

    private String getParentPath(T entity) {
        try {
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
                Node parentNode = getNode(parentPath);
                // Search a child container path annotated with JcrChildNode and the type is the same as 'entity' clazz
                String childContainerPath = getJcrom().getChildContainerPath(entity, parentObject, parentNode);
                if (childContainerPath != null) {
                    parentPath = childContainerPath;
                }
            }
            return parentPath;
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not retrieve parent path", e);
        }
    }

    @Override
    public T create(String parentNodePath, T entity) {
        try {
            String entityName = getJcrom().getName(entity);
            if (entityName == null || entityName.equals("")) {
                throw new JcrMappingException("The name of the entity being created is empty!");
            }
            if (parentNodePath == null || parentNodePath.equals("")) {
                throw new JcrMappingException("The parent path of the entity being created is empty!");
            }

            Node parentNode = getNode(parentNodePath);
            if (isVersionable) {
                // if this is a versionable node, then we need to check out parent before moving the node
                if (JcrUtils.hasMixinType(parentNode, "mix:versionable") || JcrUtils.hasMixinType(parentNode, NodeType.MIX_VERSIONABLE)) {
                    JcrUtils.checkout(parentNode);
                }
            }
            Node newNode = getJcrom().addNode(parentNode, entity, getMixinTypes());
            newNode.getSession().save();
            if (isVersionable) {
                //newNode.checkin();
                JcrUtils.checkinRecursively(newNode);
                // check in the parent node
                if ((JcrUtils.hasMixinType(parentNode, "mix:versionable") || JcrUtils.hasMixinType(parentNode, NodeType.MIX_VERSIONABLE)) && parentNode.isCheckedOut()) {
                    JcrUtils.checkin(parentNode);
                }
            }
            //return (T)getJcrom().fromNode(getEntityClass(), newNode);
            return entity;
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not create node", e);
        }
    }

    @Override
    public T update(T entity) {
        return update(entity, "*", -1);
    }

    @Override
    public T update(T entity, String childNameFilter, int maxDepth) {
        Node node;
        try {
            node = getNode(getJcrom().getPath(entity));
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not update node", e);
        }
        return update(node, entity, childNameFilter, maxDepth);
    }

    @Override
    @Deprecated
    public T updateByUUID(T entity, String uuid) {
        return updateById(entity, uuid, "*", -1);
    }

    @Override
    public T updateById(T entity, String id) {
        return updateById(entity, id, "*", -1);
    }

    @Override
    @Deprecated
    public T updateByUUID(T entity, String uuid, String childNameFilter, int maxDepth) {
        return updateById(entity, uuid, childNameFilter, maxDepth);
    }

    @Override
    public T updateById(T entity, String id, String childNameFilter, int maxDepth) {
        Node node;
        try {
            node = getNodeById(id);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not update node", e);
        }
        return update(node, entity, childNameFilter, maxDepth);
    }

    protected T update(Node node, T entity, String childNameFilter, int maxDepth) {
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
    public void move(T entity, String newParentPath) {
        // if this is a versionable node, then we need to check out both
        // the old parent and the new parent before moving the node
        try {
            String sourcePath = getJcrom().getPath(entity);
            String entityName = getJcrom().getName(entity);
            Node oldParent = null;
            Node newParent = null;
            if (isVersionable) {
                oldParent = getNode(sourcePath).getParent();
                newParent = getNode(newParentPath);

                if (JcrUtils.hasMixinType(oldParent, "mix:versionable") || JcrUtils.hasMixinType(oldParent, NodeType.MIX_VERSIONABLE)) {
                    //oldParent.checkout();
                    JcrUtils.checkout(oldParent);
                }
                if (JcrUtils.hasMixinType(newParent, "mix:versionable") || JcrUtils.hasMixinType(newParent, NodeType.MIX_VERSIONABLE)) {
                    //newParent.checkout();
                    JcrUtils.checkout(newParent);
                }
            }

            Session session = getSession();
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
    }

    @Override
    public void remove(String path) {
        try {
            Node parent = null;
            if (isVersionable) {
                parent = getNode(path).getParent();
                if (JcrUtils.hasMixinType(parent, "mix:versionable") || JcrUtils.hasMixinType(parent, NodeType.MIX_VERSIONABLE)) {
                    //parent.checkout();
                    JcrUtils.checkout(parent);
                }
            }

            Node node = getNode(path);
            node.remove();
            node.getSession().save();

            if (isVersionable) {
                if ((JcrUtils.hasMixinType(parent, "mix:versionable") || JcrUtils.hasMixinType(parent, NodeType.MIX_VERSIONABLE)) && parent.isCheckedOut()) {
                    //parent.checkin();
                    JcrUtils.checkin(parent);
                }
            }

        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not remove node", e);
        }
    }

    @Override
    @Deprecated
    public void removeByUUID(String uuid) {
        removeById(uuid);
    }

    @Override
    public void removeById(String id) {
        try {
            Node node = getNodeById(id);

            Node parent = null;
            if (isVersionable) {
                parent = node.getParent();
                if (JcrUtils.hasMixinType(parent, "mix:versionable") || JcrUtils.hasMixinType(parent, NodeType.MIX_VERSIONABLE)) {
                    //parent.checkout();
                    JcrUtils.checkout(parent);
                }
            }

            node.remove();
            node.getSession().save();

            if (isVersionable) {
                if ((JcrUtils.hasMixinType(parent, "mix:versionable") || JcrUtils.hasMixinType(parent, NodeType.MIX_VERSIONABLE)) && parent.isCheckedOut()) {
                    //parent.checkin();
                    JcrUtils.checkin(parent);
                }
            }

        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not remove node", e);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            return getSession().getRootNode().hasNode(PathUtils.relativePath(path));
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not check if node exists", e);
        }
    }

    @Override
    public T get(String path) {
        return get(path, "*", -1);
    }

    @Override
    public T get(String path, String childNameFilter, int maxDepth) {
        if (exists(path)) {
            Node node;
            try {
                node = getNode(path);
            } catch (RepositoryException e) {
                throw new JcrMappingException("Could not get node", e);
            }
            return getJcrom().fromNode(getEntityClass(), node, childNameFilter, maxDepth);
        } else {
            return null;
        }
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
    public List<T> getAll(String path, String childNameFilter, int maxDepth) {
        try {
            return toList(getNodes(path), childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get nodes", e);
        }
    }

    @Override
    public List<T> getAll(String path, String childNameFilter, int maxDepth, long startIndex, long resultSize) {
        try {
            NodeIterator nodeIterator = getNodes(path);
            nodeIterator.skip(startIndex);
            return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get nodes", e);
        }
    }

    @Override
    @Deprecated
    public T loadByUUID(String uuid) {
        return loadById(uuid, "*", -1);
    }

    @Override
    public T loadById(String id) {
        return loadById(id, "*", -1);
    }

    @Override
    @Deprecated
    public T loadByUUID(String uuid, String childNameFilter, int maxDepth) {
        return loadById(uuid, childNameFilter, maxDepth);
    }

    @Override
    public T loadById(String id, String childNameFilter, int maxDepth) {
        Node node;
        try {
            node = getNodeById(id);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not load node", e);
        }
        return getJcrom().fromNode(getEntityClass(), node, childNameFilter, maxDepth);
    }

    @Override
    public T getVersion(String path, String versionName) {
        return getVersion(path, versionName, "*", -1);
    }

    @Override
    public T getVersion(String path, String versionName, String childNameFilter, int maxDepth) {
        try {
            return getVersion(getNode(path), versionName, childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version", e);
        }
    }

    @Override
    @Deprecated
    public T getVersionByUUID(String uuid, String versionName) {
        return getVersionById(uuid, versionName, "*", -1);
    }

    @Override
    public T getVersionById(String id, String versionName) {
        return getVersionById(id, versionName, "*", -1);
    }

    @Override
    @Deprecated
    public T getVersionByUUID(String uuid, String versionName, String childNameFilter, int maxDepth) {
        return getVersionById(uuid, versionName, childNameFilter, maxDepth);
    }

    @Override
    public T getVersionById(String id, String versionName, String childNameFilter, int maxDepth) {
        try {
            Node node = getNodeById(id);
            return getVersion(node, versionName, childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version", e);
        }
    }

    protected T getVersion(Node node, String versionName, String childNameFilter, int maxDepth) {
        try {
            //VersionHistory versionHistory = node.getVersionHistory();
            VersionHistory versionHistory = JcrUtils.getVersionManager(node.getSession()).getVersionHistory(node.getPath());
            Version version = versionHistory.getVersion(versionName);
            return getJcrom().fromNode(getEntityClass(), version.getNodes().nextNode(), childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version", e);
        }
    }

    @Override
    public void restoreVersion(String path, String versionName) {
        restoreVersion(path, versionName, true);
    }

    @Override
    @Deprecated
    public void restoreVersionByUUID(String uuid, String versionName) {
        restoreVersionById(uuid, versionName, true);
    }

    @Override
    public void restoreVersionById(String id, String versionName) {
        restoreVersionById(id, versionName, true);
    }

    @Override
    public void restoreVersion(String path, String versionName, boolean removeExisting) {
        try {
            restoreVersion(getNode(path), versionName, removeExisting);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not restore version", e);
        }
    }

    @Override
    @Deprecated
    public void restoreVersionByUUID(String uuid, String versionName, boolean removeExisting) {
        restoreVersionById(uuid, versionName, removeExisting);
    }

    @Override
    public void restoreVersionById(String id, String versionName, boolean removeExisting) {
        try {
            Node node = getNodeById(id);
            restoreVersion(node, versionName, removeExisting);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not restore version", e);
        }
    }

    protected void restoreVersion(Node node, String versionName, boolean removeExisting) {
        try {
            //node.checkout();
            JcrUtils.checkout(node);
            //node.restore(versionName, removeExisting);
            JcrUtils.getVersionManager(node.getSession()).restore(node.getPath(), versionName, removeExisting);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not restore version", e);
        }
    }

    @Override
    public void removeVersion(String path, String versionName) {
        try {
            removeVersion(getNode(path), versionName);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not remove version", e);
        }
    }

    @Override
    @Deprecated
    public void removeVersionByUUID(String uuid, String versionName) {
        removeVersionById(uuid, versionName);
    }

    @Override
    public void removeVersionById(String id, String versionName) {
        try {
            Node node = getNodeById(id);
            removeVersion(node, versionName);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not remove version", e);
        }
    }

    protected void removeVersion(Node node, String versionName) {
        try {
            //VersionHistory versionHistory = node.getVersionHistory();
            VersionHistory versionHistory = JcrUtils.getVersionManager(node.getSession()).getVersionHistory(node.getPath());
            versionHistory.removeVersion(versionName);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not remove version", e);
        }
    }

    @Override
    public long getVersionSize(String path) {
        try {
            return getVersionSize(getNode(path));
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version history size", e);
        }
    }

    @Override
    @Deprecated
    public long getVersionSizeByUUID(String uuid) {
        return getVersionSizeById(uuid);
    }

    @Override
    public long getVersionSizeById(String id) {
        try {
            Node node = getNodeById(id);
            return getVersionSize(node);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version history size", e);
        }
    }

    protected long getVersionSize(Node node) {
        try {
            //VersionHistory versionHistory = node.getVersionHistory();
            VersionHistory versionHistory = JcrUtils.getVersionManager(node.getSession()).getVersionHistory(node.getPath());
            return versionHistory.getAllVersions().getSize() - 1;
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version history size", e);
        }
    }

    @Override
    public List<T> getVersionList(String path) {
        try {
            return getVersionList(getNode(path), "*", -1);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version list", e);
        }
    }

    @Override
    public List<T> getVersionList(String path, String childNameFilter, int maxDepth) {
        try {
            return getVersionList(getNode(path), childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version list", e);
        }
    }

    @Override
    public List<T> getVersionList(String path, String childNameFilter, int maxDepth, long startIndex, long resultSize) {
        try {
            return getVersionList(getNode(path), childNameFilter, maxDepth, startIndex, resultSize);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version list", e);
        }
    }

    @Override
    @Deprecated
    public List<T> getVersionListByUUID(String uuid) {
        return getVersionListById(uuid);
    }

    @Override
    public List<T> getVersionListById(String id) {
        try {
            Node node = getNodeById(id);
            return getVersionList(node, "*", -1);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version list", e);
        }
    }

    @Override
    @Deprecated
    public List<T> getVersionListByUUID(String uuid, String childNameFilter, int maxDepth) {
        return getVersionListById(uuid, childNameFilter, maxDepth);
    }

    @Override
    public List<T> getVersionListById(String uuid, String childNameFilter, int maxDepth) {
        try {
            Node node = getNodeById(uuid);
            return getVersionList(node, childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version list", e);
        }
    }

    @Override
    @Deprecated
    public List<T> getVersionListByUUID(String uuid, String childNameFilter, int maxDepth, long startIndex, long resultSize) {
        return getVersionListById(uuid, childNameFilter, maxDepth, startIndex, resultSize);
    }

    @Override
    public List<T> getVersionListById(String id, String childNameFilter, int maxDepth, long startIndex, long resultSize) {
        try {
            Node node = getNodeById(id);
            return getVersionList(node, childNameFilter, maxDepth, startIndex, resultSize);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version list", e);
        }
    }

    protected List<T> getVersionList(Node node, String childNameFilter, int maxDepth) {
        try {
            List<T> versionList = new ArrayList<T>();
            //VersionHistory versionHistory = node.getVersionHistory();
            VersionHistory versionHistory = JcrUtils.getVersionManager(node.getSession()).getVersionHistory(node.getPath());
            VersionIterator versionIterator = versionHistory.getAllVersions();
            versionIterator.skip(1);
            while (versionIterator.hasNext()) {
                Version version = versionIterator.nextVersion();
                NodeIterator nodeIterator = version.getNodes();
                while (nodeIterator.hasNext()) {
                    T entityVersion = getJcrom().fromNode(getEntityClass(), nodeIterator.nextNode(), childNameFilter, maxDepth);
                    //Version baseVersion = node.getBaseVersion();
                    Version baseVersion = JcrUtils.getVersionManager(node.getSession()).getBaseVersion(node.getPath());
                    getJcrom().setBaseVersionInfo(entityVersion, baseVersion.getName(), baseVersion.getCreated());
                    versionList.add(entityVersion);
                }
            }
            return versionList;
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get version list", e);
        }
    }

    protected List<T> getVersionList(Node node, String childNameFilter, int maxDepth, long startIndex, long resultSize) {
        try {
            List<T> versionList = new ArrayList<T>();
            //VersionHistory versionHistory = node.getVersionHistory();
            VersionHistory versionHistory = JcrUtils.getVersionManager(node.getSession()).getVersionHistory(node.getPath());
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

    @Override
    public long getSize(String rootPath) {
        try {
            NodeIterator nodeIterator = getNode(rootPath).getNodes();
            return nodeIterator.getSize();
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not get list size", e);
        }
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
    public List<T> findAll(String rootPath, String childNameFilter, int maxDepth) {
        try {
            return toList(getNode(rootPath).getNodes(), childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not find nodes", e);
        }
    }

    @Override
    public List<T> findAll(String rootPath, String childNameFilter, int maxDepth, long startIndex, long resultSize) {
        try {
            NodeIterator nodeIterator = getNode(rootPath).getNodes();
            nodeIterator.skip(startIndex);
            return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not find nodes", e);
        }
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
    protected List<T> findByXPath(String xpath, String childNameFilter, int maxDepth, long startIndex, long resultSize) {
        try {
            QueryManager queryManager = getSession().getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(xpath, Query.XPATH);
            QueryResult result = query.execute();
            NodeIterator nodeIterator = result.getNodes();
            nodeIterator.skip(startIndex);
            return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not find nodes by XPath", e);
        }
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
    protected List<T> findByXPath(String xpath, String childNameFilter, int maxDepth) {
        try {
            QueryManager queryManager = getSession().getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(xpath, Query.XPATH);
            QueryResult result = query.execute();
            return toList(result.getNodes(), childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not find nodes by XPath", e);
        }
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
    protected List<T> findBySql(String sql, String childNameFilter, int maxDepth, long startIndex, long resultSize) {
        try {
            QueryManager queryManager = getSession().getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(sql, Query.JCR_SQL2);
            QueryResult result = query.execute();
            NodeIterator nodeIterator = result.getNodes();
            nodeIterator.skip(startIndex);
            return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not find nodes by XPath", e);
        }
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
    protected List<T> findBySql(String sql, String childNameFilter, int maxDepth) {
        try {
            QueryManager queryManager = getSession().getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(sql, Query.JCR_SQL2);
            QueryResult result = query.execute();
            return toList(result.getNodes(), childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not find nodes by XPath", e);
        }
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
    protected List<T> findByQOM(Source source, Constraint constraint, Ordering orderings[], Column columns[], String childNameFilter, int maxDepth) {
        try {
            QueryObjectModelFactory factory = getSession().getWorkspace().getQueryManager().getQOMFactory();
            Query query = factory.createQuery(source, constraint, orderings, columns);
            QueryResult result = query.execute();
            return toList(result.getNodes(), childNameFilter, maxDepth);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not find nodes by XPath", e);
        }
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
}
