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
import org.jcrom.JcrEntity;
import org.jcrom.Jcrom;
import org.jcrom.util.PathUtils;

/**
 * An abstract implementation of the JcrDAO interface.
 *
 * @author Olafur Gauti Gudmundsson
 */
public abstract class AbstractJcrDAO<T extends JcrEntity> implements JcrDAO<T> {

	protected String rootPath; // annotation instead?
	protected Jcrom jcrom;
	protected Session session;
	protected Class<T> entityClass;
	
	
	public AbstractJcrDAO( Class<T> entityClass, String rootPath, Session session, Jcrom jcrom ) {
		this.entityClass = entityClass;
		this.rootPath = rootPath;
		this.session = session;
		this.jcrom = jcrom;
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
		Node parentNode = session.getRootNode().getNode(rootPath);
		return jcrom.addNode(parentNode, entity);
	}
	
	public String update( T entity ) throws Exception {
		return update(entity, "*", -1);
	}
	
	public String update( T entity, String childNodeFilter, int maxDepth ) throws Exception {
		Node node = session.getRootNode().getNode(fullPath(entity.getName()));
		return jcrom.updateNode(node, entity, childNodeFilter, maxDepth);
	}
	
	public void delete( String name ) throws Exception {
		session.getRootNode().getNode(fullPath(name)).remove();
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
	
	protected List<T> findByXPath( String xpath, String childNameFilter, int maxDepth, long startIndex, long resultSize ) throws Exception {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(xpath, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator nodeIterator = result.getNodes();
		nodeIterator.skip(startIndex);
		return toList(nodeIterator, childNameFilter, maxDepth, resultSize);
	}
	
	protected List<T> findByXPath( String xpath, String childNameFilter, int maxDepth ) throws Exception {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(xpath, Query.XPATH);
		QueryResult result = query.execute();
		return toList(result.getNodes(), childNameFilter, maxDepth);
	}
	
	protected List<T> toList( NodeIterator nodeIterator, String childNameFilter, int maxDepth ) throws Exception {
		List<T> objects = new ArrayList<T>();
		while ( nodeIterator.hasNext() ) {
			objects.add( (T)jcrom.fromNode(entityClass, nodeIterator.nextNode(), childNameFilter, maxDepth) );
		}
		return objects;
	}
	
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
