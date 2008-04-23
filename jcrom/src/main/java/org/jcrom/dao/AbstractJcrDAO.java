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

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import org.jcrom.JcrMappingException;
import org.jcrom.Jcrom;
import org.jcrom.annotations.JcrNode;
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
 */
public abstract class AbstractJcrDAO<T> implements JcrDAO<T> {

	protected final Jcrom jcrom;
	protected final Session session;
	protected final Class<T> entityClass;
	protected final String[] mixinTypes;
	protected final boolean isVersionable;
	
    /**
     * Use this constructor when you intend to override the getSession()
     * method to provide your own session management (for example via
     * Guice providers). You can also use the other constructors and just
     * pass null as the session.
     * 
     * @param entityClass the class handled by this DAO implementation
     * @param jcrom the Jcrom instance to use for object mapping
     */
	public AbstractJcrDAO( Class<T> entityClass, Jcrom jcrom ) {
		this(entityClass, null, jcrom, new String[0]);
	}
    
	/**
	 * Constructor.
	 * 
	 * @param entityClass the class handled by this DAO implementation
	 * @param session the current JCR session
	 * @param jcrom the Jcrom instance to use for object mapping
	 */
	public AbstractJcrDAO( Class<T> entityClass, Session session, Jcrom jcrom ) {
		this(entityClass, session, jcrom, new String[0]);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param entityClass the class handled by this DAO implementation
	 * @param session the current JCR session
	 * @param jcrom the Jcrom instance to use for object mapping
	 * @param mixinTypes an array of mixin types to apply to new nodes
	 */
	public AbstractJcrDAO( Class<T> entityClass, Session session, Jcrom jcrom, String[] mixinTypes ) {
		this.entityClass = entityClass;
		this.session = session;
		this.jcrom = jcrom;
		this.mixinTypes = new String[mixinTypes.length];
		System.arraycopy(mixinTypes, 0, this.mixinTypes, 0, mixinTypes.length);
		this.isVersionable = checkIfVersionable();
	}
	
    protected Session getSession() {
        return session;
    }
    
	private boolean checkIfVersionable() {
        // check mixin type array
		for ( String mixinType : mixinTypes ) {
			if ( mixinType.equals("mix:versionable") ) {
				return true;
			}
		}
        // check the class annotation
        JcrNode jcrNode = ReflectionUtils.getJcrNodeAnnotation(entityClass);
        if ( jcrNode != null && jcrNode.mixinTypes() != null ) {
            for ( String mixinType : jcrNode.mixinTypes() ) {
                if ( mixinType.equals("mix:versionable") ) {
                    return true;
                }
            }
        }
		return false;
	}
	
	protected String relativePath( String absolutePath ) {
		 if ( absolutePath.startsWith("/") ) {
			 return absolutePath.substring(1);
		 } else {
			 return absolutePath;
		 }
	}
	
	public T create( T entity ) {
		return create(jcrom.getPath(entity), entity);
	}

	public T create(String parentNodePath, T entity) {
		try {
			String entityName = jcrom.getName(entity);
			if ( entityName == null || entityName.equals("") ) {
				throw new JcrMappingException("The name of the entity being created is empty!");
			}
			if ( parentNodePath == null || parentNodePath.equals("") ) {
				throw new JcrMappingException("The parent path of the entity being created is empty!");
			}

			Node parentNode;
			if ( parentNodePath.equals("/") ) {
				// special case, add directly to the root node
				parentNode = getSession().getRootNode();
			} else {
				parentNode = getSession().getRootNode().getNode(relativePath(parentNodePath));
			}
			Node newNode = jcrom.addNode(parentNode, entity, mixinTypes);
			getSession().save();
			if ( isVersionable ) {
				newNode.checkin();
			}
			return (T)jcrom.fromNode(entityClass, newNode);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not create node", e);
		}
	}
	
	public String update( T entity ) {
		return update(entity, "*", -1);
	}
	
	public String update( T entity, String childNodeFilter, int maxDepth ) {
		Node node;
		try {
			node = getSession().getRootNode().getNode(relativePath(jcrom.getPath(entity)));
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not update node", e);
		}
		return update(node, entity, childNodeFilter, maxDepth);
	}
	
	public String updateByUUID( T entity, String uuid ) {
		return updateByUUID(entity, uuid, "*", -1);
	}
	
	public String updateByUUID( T entity, String uuid, String childNodeFilter, int maxDepth ) {
		Node node;
		try {
			node = getSession().getNodeByUUID(uuid);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not update node", e);
		}
		return update(node, entity, childNodeFilter, maxDepth);
	}
	
	protected String update( Node node, T entity, String childNodeFilter, int maxDepth ) {
		try {
			if ( isVersionable ) {
				node.checkout();
			}
			String name = jcrom.updateNode(node, entity, childNodeFilter, maxDepth);
			getSession().save();
			if ( isVersionable ) {
				node.checkin();
			}
			return name;
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not update node", e);
		}
	}
	
	public void delete( String path ) {
		try {
			getSession().getRootNode().getNode(relativePath(path)).remove();
			getSession().save();
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not delete node", e);
		}
	}
	
	public void deleteByUUID( String uuid ) {
		try {
			getSession().getNodeByUUID(uuid).remove();
			getSession().save();
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not delete node", e);
		}
	}
	
	public boolean exists( String path ) {
		try {
			return getSession().getRootNode().hasNode(relativePath(path));
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not check if node exists", e);
		}
	}
	
	public T get( String path ) {
		return get(path, "*", -1);
	}
	
	public T get( String path, String childNodeFilter, int maxDepth ) {
		if ( exists(path) ) {
			Node node;
			try {
				node = getSession().getRootNode().getNode(relativePath(path));
			} catch ( RepositoryException e ) {
				throw new JcrMappingException("Could not get node", e);
			}
			return (T)jcrom.fromNode(entityClass, node, childNodeFilter, maxDepth);
		} else {
			return null;
		}
	}
	
	public T loadByUUID( String uuid ) {
		return loadByUUID(uuid, "*", -1);
	}
	
	public T loadByUUID( String uuid, String childNodeFilter, int maxDepth ) {
		Node node;
		try {
			node = getSession().getNodeByUUID(uuid);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not load node", e);
		}
		return (T)jcrom.fromNode(entityClass, node, childNodeFilter, maxDepth);
	}
	
	public T getVersion( String path, String versionName ) {
		return getVersion(path, versionName, "*", -1);
	}
	public T getVersion( String path, String versionName, String childNodeFilter, int maxDepth ) {
		try {
			return getVersion(getSession().getRootNode().getNode(relativePath(path)), versionName, childNodeFilter, maxDepth);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version", e);
		}
	}
	
	public T getVersionByUUID( String uuid, String versionName ) {
		return getVersionByUUID(uuid, versionName, "*", -1);
	}
	public T getVersionByUUID( String uuid, String versionName, String childNodeFilter, int maxDepth ) {
		try {
			return getVersion(getSession().getNodeByUUID(uuid), versionName, childNodeFilter, maxDepth);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version", e);
		}
	}
	
	protected T getVersion( Node node, String versionName, String childNodeFilter, int maxDepth ) {
		try {
			VersionHistory versionHistory = node.getVersionHistory();
			Version version = versionHistory.getVersion(versionName);
			return (T)jcrom.fromNode(entityClass, version.getNodes().nextNode(), childNodeFilter, maxDepth);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version", e);
		}
	}
	
	public void restoreVersion( String path, String versionName ) {
		try {
			restoreVersion(getSession().getRootNode().getNode(relativePath(path)), versionName);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not restore version", e);
		}
	}
	public void restoreVersionByUUID( String uuid, String versionName ) {
		try {
			restoreVersion(getSession().getNodeByUUID(uuid), versionName);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not restore version", e);
		}
	}
	protected void restoreVersion( Node node, String versionName ) {
		try {
			node.checkout();
			node.restore(versionName, true);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not restore version", e);
		}
	}
	
	public void removeVersion( String path, String versionName ) {
		try {
			removeVersion(getSession().getRootNode().getNode(relativePath(path)), versionName);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not remove version", e);
		}
	}
	public void removeVersionByUUID( String uuid, String versionName ) {
		try {
			removeVersion(getSession().getNodeByUUID(uuid), versionName);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not remove version", e);
		} 
	}
	protected void removeVersion( Node node, String versionName ) {
		try {
			node.getVersionHistory().removeVersion(versionName);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not remove version", e);
		}
	}
	
	public long getVersionSize( String path ) {
		try {
			return getVersionSize(getSession().getRootNode().getNode(relativePath(path)));
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version history size", e);
		}
	}
	
	public long getVersionSizeByUUID( String uuid ) {
		try {
			return getVersionSize(getSession().getNodeByUUID(uuid));
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version history size", e);
		}
	}
	
	protected long getVersionSize( Node node ) {
		try {
			VersionHistory versionHistory = node.getVersionHistory();
			return versionHistory.getAllVersions().getSize()-1;
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version history size", e);
		}
	}
	
	public List<T> getVersionList( String path ) {
		try {
			return getVersionList(getSession().getRootNode().getNode(relativePath(path)), "*", -1);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version list", e);
		}
	}
	
	public List<T> getVersionList( String path, String childNameFilter, int maxDepth ) {
		try {
			return getVersionList(getSession().getRootNode().getNode(relativePath(path)), childNameFilter, maxDepth);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version list", e);
		}
	}
	
	public List<T> getVersionList( String path, String childNameFilter, int maxDepth, long startIndex, long resultSize ) {
		try {
			return getVersionList(getSession().getRootNode().getNode(relativePath(path)), childNameFilter, maxDepth, startIndex, resultSize);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version list", e);
		}
	}
	
	public List<T> getVersionListByUUID( String uuid ) {
		try {
			return getVersionList(getSession().getNodeByUUID(uuid), "*", -1);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version list", e);
		}
	}
	
	public List<T> getVersionListByUUID( String uuid, String childNameFilter, int maxDepth ) {
		try {
			return getVersionList(getSession().getNodeByUUID(uuid), childNameFilter, maxDepth);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version list", e);
		}
	}
	
	public List<T> getVersionListByUUID( String uuid, String childNameFilter, int maxDepth, long startIndex, long resultSize ) {
		try {
			return getVersionList(getSession().getNodeByUUID(uuid), childNameFilter, maxDepth, startIndex, resultSize);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version list", e);
		}
	}
	
	protected List<T> getVersionList( Node node, String childNameFilter, int maxDepth ) {
		try {
			List<T> versionList = new ArrayList<T>();
			VersionHistory versionHistory = node.getVersionHistory();
			VersionIterator versionIterator = versionHistory.getAllVersions();
			versionIterator.skip(1);
			while ( versionIterator.hasNext() ) {
				Version version = versionIterator.nextVersion();
				NodeIterator nodeIterator = version.getNodes();
				while ( nodeIterator.hasNext() ) {
					T entityVersion = (T)jcrom.fromNode(entityClass, nodeIterator.nextNode(), childNameFilter, maxDepth);
					jcrom.setBaseVersionInfo(entityVersion, node.getBaseVersion().getName(), node.getBaseVersion().getCreated());
					versionList.add(entityVersion);
				}
			}
			return versionList;
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version list", e);
		}
	}
	
	protected List<T> getVersionList( Node node, String childNameFilter, int maxDepth, long startIndex, long resultSize ) {
		try {
			List<T> versionList = new ArrayList<T>();
			VersionHistory versionHistory = node.getVersionHistory();
			VersionIterator versionIterator = versionHistory.getAllVersions();
			versionIterator.skip(1 + startIndex);

			long counter = 0;
			while ( versionIterator.hasNext() ) {
				if ( counter == resultSize ) {
					break;
				}
				Version version = versionIterator.nextVersion();
				NodeIterator nodeIterator = version.getNodes();
				while ( nodeIterator.hasNext() ) {
					versionList.add((T)jcrom.fromNode(entityClass, nodeIterator.nextNode(), childNameFilter, maxDepth));
				}
				counter++;
			}
			return versionList;
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get version list", e);
		}
	}
	
	
	public long getSize( String rootPath ) {
		try {
			NodeIterator nodeIterator = getSession().getRootNode().getNode(relativePath(rootPath)).getNodes();
			return nodeIterator.getSize();
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not get list size", e);
		}
	}
	
	public List<T> findAll( String rootPath ) {
		return findAll(rootPath, "*", -1);
	}
	
	public List<T> findAll( String rootPath, long startIndex, long resultSize ) {
		return findAll(rootPath, "*", -1, startIndex, resultSize);
	}
	
	public List<T> findAll( String rootPath, String childNameFilter, int maxDepth ) {
		try {
			return toList(getSession().getRootNode().getNode(relativePath(rootPath)).getNodes(), childNameFilter, maxDepth);
		} catch ( RepositoryException e ) {
			throw new JcrMappingException("Could not find nodes", e);
		}
	}
	
	public List<T> findAll( String rootPath, String childNameFilter, int maxDepth, long startIndex, long resultSize ) {
		try {
			NodeIterator nodeIterator = getSession().getRootNode().getNode(relativePath(rootPath)).getNodes();
			nodeIterator.skip(startIndex);
			return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
		} catch ( RepositoryException e ) {
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
	protected List<T> findByXPath( String xpath, String childNameFilter, int maxDepth, long startIndex, long resultSize ) {
		try {
			QueryManager queryManager = getSession().getWorkspace().getQueryManager();
			Query query = queryManager.createQuery(xpath, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator nodeIterator = result.getNodes();
			nodeIterator.skip(startIndex);
			return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
		} catch ( RepositoryException e ) {
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
	protected List<T> findByXPath( String xpath, String childNameFilter, int maxDepth ) {
		try {
			QueryManager queryManager = getSession().getWorkspace().getQueryManager();
			Query query = queryManager.createQuery(xpath, Query.XPATH);
			QueryResult result = query.execute();
			return toList(result.getNodes(), childNameFilter, maxDepth);
		} catch ( RepositoryException e ) {
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
	protected List<T> toList( NodeIterator nodeIterator, String childNameFilter, int maxDepth ) {
		List<T> objects = new ArrayList<T>();
		while ( nodeIterator.hasNext() ) {
			objects.add( (T)jcrom.fromNode(entityClass, nodeIterator.nextNode(), childNameFilter, maxDepth) );
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
	protected List<T> toList( NodeIterator nodeIterator, String childNameFilter, int maxDepth, long resultSize ) {
		List<T> objects = new ArrayList<T>();
		long counter = 0;
		while ( nodeIterator.hasNext() ) {
			if ( counter == resultSize ) {
				break;
			}
			objects.add( (T)jcrom.fromNode(entityClass, nodeIterator.nextNode(), childNameFilter, maxDepth) );
			counter++;
		}
		return objects;
	}
}
