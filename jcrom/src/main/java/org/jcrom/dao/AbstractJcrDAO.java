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
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.jcrom.JcrMappingException;
import org.jcrom.Jcrom;
import org.jcrom.util.PathUtils;

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
 * @author Olafur Gauti Gudmundsson
 */
public abstract class AbstractJcrDAO<T> implements JcrDAO<T> {

	protected final boolean saveAfterMod;
	protected final String rootPath;
	protected final Jcrom jcrom;
	protected final Session session;
	protected final Class<T> entityClass;
	protected final String[] mixinTypes;
	
	/**
	 * Constructor.
	 * 
	 * @param entityClass the class handled by this DAO implementation
	 * @param rootPath the JCR root path under which entities should be created
	 * @param session the current JCR session
	 * @param jcrom the Jcrom instance to use for object mapping
	 */
	public AbstractJcrDAO( Class<T> entityClass, String rootPath, Session session, Jcrom jcrom ) {
		this(entityClass, rootPath, session, jcrom, true, new String[0]);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param entityClass the class handled by this DAO implementation
	 * @param rootPath the JCR root path under which entities should be created
	 * @param session the current JCR session
	 * @param jcrom the Jcrom instance to use for object mapping
	 * @param saveAfterMod specifies whether to call session.save() after write methods
	 * @param mixinTypes an array of mixin types to apply to new nodes
	 */
	public AbstractJcrDAO( Class<T> entityClass, String rootPath, Session session, Jcrom jcrom, boolean saveAfterMod, String[] mixinTypes ) {
		this.entityClass = entityClass;
		this.rootPath = rootPath;
		this.session = session;
		this.jcrom = jcrom;
		this.saveAfterMod = saveAfterMod;
		this.mixinTypes = mixinTypes;
	}

	private String fullPath( String name ) {
		String validName = PathUtils.createValidName(name);
		if ( rootPath == null || rootPath.equals("") ) {
			return "/" + validName;
		} else if ( !rootPath.endsWith("/") ) {
			return rootPath + "/" + validName;
		} else {
			return rootPath + validName;
		}
	}
	
	public Node create( T entity ) throws Exception {
		String entityName = jcrom.getName(entity);
		if ( entityName == null || entityName.equals("") ) {
			throw new JcrMappingException("The name of the entity being created is empty!");
		}
		Node parentNode = session.getRootNode().getNode(rootPath);
		Node newNode = jcrom.addNode(parentNode, entity, mixinTypes);
		if ( saveAfterMod ) {
			session.save();
		}
		return newNode;
	}
	
	public String update( T entity ) throws Exception {
		return update(entity, "*", -1);
	}
	
	public String update( T entity, String childNodeFilter, int maxDepth ) throws Exception {
		Node node = session.getRootNode().getNode(fullPath(jcrom.getName(entity)));
		String name = jcrom.updateNode(node, entity, childNodeFilter, maxDepth);
		if ( saveAfterMod ) {
			session.save();
		}
		return name;
	}
	
	public String update( T entity, String oldName ) throws Exception {
		return update(entity, oldName, "*", -1);
	}
	
	public String update( T entity, String oldName, String childNodeFilter, int maxDepth ) throws Exception {
		Node node = session.getRootNode().getNode(fullPath(oldName));
		String name = jcrom.updateNode(node, entity, childNodeFilter, maxDepth);
		if ( saveAfterMod ) {
			session.save();
		}
		return name;
	}
	
	public String updateByUUID( T entity, String uuid ) throws Exception {
		return updateByUUID(entity, uuid, "*", -1);
	}
	
	public String updateByUUID( T entity, String uuid, String childNodeFilter, int maxDepth ) throws Exception {
		Node node = session.getNodeByUUID(uuid);
		String name = jcrom.updateNode(node, entity, childNodeFilter, maxDepth);
		if ( saveAfterMod ) {
			session.save();
		}
		return name;
	}
	
	public void delete( String name ) throws Exception {
		session.getRootNode().getNode(fullPath(name)).remove();
		if ( saveAfterMod ) {
			session.save();
		}
	}
	
	public void deleteByUUID( String uuid ) throws Exception {
		session.getNodeByUUID(uuid).remove();
		if ( saveAfterMod ) {
			session.save();
		}
	}
	
	public boolean exists( String name ) throws Exception {
		return session.getRootNode().hasNode(fullPath(name));
	}
	
	public T get( String name ) throws Exception {
		return get(name, "*", -1);
	}
	
	public T get( String name, String childNodeFilter, int maxDepth ) throws Exception {
		if ( exists(name) ) {
			Node node = session.getRootNode().getNode(fullPath(name));
			return (T)jcrom.fromNode(entityClass, node, childNodeFilter, maxDepth);
		} else {
			return null;
		}
	}
	
	public T loadByUUID( String uuid ) throws Exception {
		return loadByUUID(uuid, "*", -1);
	}
	
	public T loadByUUID( String uuid, String childNodeFilter, int maxDepth ) throws Exception {
		Node node = session.getNodeByUUID(uuid);
		return (T)jcrom.fromNode(entityClass, node, childNodeFilter, maxDepth);
	}
	
	public List<T> findAll() throws Exception {
		return findAll("*", -1);
	}
	
	public List<T> findAll( long startIndex, long resultSize ) throws Exception {
		return findAll("*", -1, startIndex, resultSize);
	}
	
	public List<T> findAll( String childNameFilter, int maxDepth ) throws Exception {
		return toList(session.getRootNode().getNode(rootPath).getNodes(), childNameFilter, maxDepth);
	}
	
	public List<T> findAll( String childNameFilter, int maxDepth, long startIndex, long resultSize ) throws Exception {
		NodeIterator nodeIterator = session.getRootNode().getNode(rootPath).getNodes();
		nodeIterator.skip(startIndex);
		return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
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
	 * @throws java.lang.Exception
	 */
	protected List<T> findByXPath( String xpath, String childNameFilter, int maxDepth, long startIndex, long resultSize ) throws Exception {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(xpath, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator nodeIterator = result.getNodes();
		nodeIterator.skip(startIndex);
		return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
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
	 * @throws java.lang.Exception
	 */
	protected List<T> findByXPath( String xpath, String childNameFilter, int maxDepth ) throws Exception {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(xpath, Query.XPATH);
		QueryResult result = query.execute();
		return toList(result.getNodes(), childNameFilter, maxDepth);
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
	 * @throws java.lang.Exception
	 */
	protected List<T> toList( NodeIterator nodeIterator, String childNameFilter, int maxDepth ) throws Exception {
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
	 * @throws java.lang.Exception
	 */
	protected List<T> toList( NodeIterator nodeIterator, String childNameFilter, int maxDepth, long resultSize ) throws Exception {
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
