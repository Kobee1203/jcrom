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
package org.jcrom;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.util.NameFilter;
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles mappings of type @JcrChildNode
 * 
 * @author Olafur Gauti Gudmundsson
 */
class ChildNodeMapper {

	private static String getNodeName(Field field) {
		JcrChildNode jcrChildNode = field.getAnnotation(JcrChildNode.class);
		String name = field.getName();
		if ( !jcrChildNode.name().equals(Mapper.DEFAULT_FIELDNAME) ) {
			name = jcrChildNode.name();
		}
		return name;
	}
	
	private static void removeChildren( Node node, String childNodeName ) throws RepositoryException {
		NodeIterator nodeIterator = node.getNodes(childNodeName);
		while ( nodeIterator.hasNext() ) {
			nodeIterator.nextNode().remove();
		}
	}
	
	private static void addSingleChildToNode( Field field, JcrChildNode jcrChildNode, Object obj, String nodeName, Node node, 
			Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		if ( !node.hasNode(nodeName) ) {
			if ( field.get(obj) != null ) {
				// add the node if it does not exist
				Node childContainer = node.addNode(mapper.getCleanName(nodeName), jcrChildNode.containerNodeType());
				mapper.addNode(childContainer, field.get(obj), null);
			}
		} else {
			if ( field.get(obj) != null ) {
				mapper.updateNode(node.getNode(nodeName).getNodes().nextNode(), field.get(obj), field.getType(), nameFilter, maxDepth, depth+1);
			} else {
				// field is now null, so we remove the child node
				removeChildren(node, nodeName);
			}
		}
	}
	
	private static void addMultipleChildrenToNode( Field field, JcrChildNode jcrChildNode, Object obj, String nodeName, Node node, 
			Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {

		List children = (List)field.get(obj);
		if ( children != null && !children.isEmpty() ) {
			Class paramClass = ReflectionUtils.getParameterizedClass(field);
			if ( node.hasNode(nodeName) ) {
				// children exist, we must update
				Node childContainer = node.getNode(nodeName);
				NodeIterator childNodes = childContainer.getNodes();
				while ( childNodes.hasNext() ) {
					Node child = childNodes.nextNode();
					Object childEntity = mapper.findEntityByName(children, child.getName());
					if ( childEntity == null ) {
						// this child was not found, so we remove it
						child.remove();
					} else {
						mapper.updateNode(child, childEntity, paramClass, nameFilter, maxDepth, depth+1);
					}
				}
				// we must add new children, if any
				for ( int i = 0; i < children.size(); i++ ) {
					Object child = children.get(i);
					if ( !childContainer.hasNode(mapper.getCleanName(Mapper.getNodeName(child))) ) {
						mapper.addNode(childContainer, child, null);
					}
				}
			} else {
				// no children exist, we add
				Node childContainer = node.addNode(mapper.getCleanName(nodeName), jcrChildNode.containerNodeType());
				for ( int i = 0; i < children.size(); i++ ) {
					mapper.addNode(childContainer, children.get(i), null);
				}
			}
		} else {
			// field list is now null (or empty), so we remove the child nodes
			removeChildren(node, nodeName);
		}
	}
	
	private static void addChildMap(Field field, JcrChildNode jcrChildNode, Object obj, Node node, String nodeName, Mapper mapper) 
			throws RepositoryException, IllegalAccessException {
		
		Map<String,Object> map = (Map<String,Object>)field.get(obj);
		boolean nullOrEmpty = map == null || map.isEmpty();
		// remove the child node
		NodeIterator nodeIterator = node.getNodes(nodeName);
		while ( nodeIterator.hasNext() ) {
			nodeIterator.nextNode().remove();
		}
		// add the map as a child node
		if ( !nullOrEmpty ) {
			Node childContainer = node.addNode(mapper.getCleanName(nodeName), jcrChildNode.containerNodeType());
			for ( Map.Entry<String,Object> entry : map.entrySet() ) {
				PropertyMapper.mapToProperty(entry.getKey(), ReflectionUtils.getParameterizedClass(field, 1), null, entry.getValue(), childContainer);
			}
		}
	}
	
	private static void setChildren( Field field, Object obj, Node node, 
			int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		JcrChildNode jcrChildNode = field.getAnnotation(JcrChildNode.class);
		String nodeName = getNodeName(field);

		// make sure that this child is supposed to be updated
		if ( nameFilter == null || nameFilter.isIncluded(field.getName()) ) {
			if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
				// multiple children in a List
				addMultipleChildrenToNode(field, jcrChildNode, obj, nodeName, node, mapper, depth, maxDepth, nameFilter);

			} else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
				// this is a Map child, where we map the key/value pairs as properties
				addChildMap(field, jcrChildNode, obj, node, nodeName, mapper);
				
			} else {
				// single child
				addSingleChildToNode(field, jcrChildNode, obj, nodeName, node, mapper, depth, maxDepth, nameFilter);
			}
		}
	}
	
	static void addChildren( Field field, Object entity, Node node, Mapper mapper ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		setChildren(field, entity, node, -1, -1, null, mapper);
	}
	
	static void updateChildren( Field field, Object obj, Node node, int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		setChildren(field, obj, node, depth, maxDepth, nameFilter, mapper);
	}
	
	static void getChildrenFromNode( Field field, Node node, Object obj, int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		
		String nodeName = getNodeName(field);

		if ( node.hasNode(nodeName) && nameFilter.isIncluded(field.getName()) ) {
			// child nodes are always stored inside a container node
			Node childrenContainer = node.getNode(nodeName);
			if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
				// we can expect more than one child object here
				List children = new ArrayList();
				Class childObjClass = ReflectionUtils.getParameterizedClass(field);

				NodeIterator iterator = childrenContainer.getNodes();
				while ( iterator.hasNext() ) {
					Node childNode = iterator.nextNode();
					Object childObj = mapper.createInstanceForNode(childObjClass, childNode);
					mapper.mapNodeToClass(childObj, childNode, nameFilter, maxDepth, obj, depth+1);
					children.add(childObj);
				}
				field.set(obj, children);

			} else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
				// map of properties
				Class valueType = ReflectionUtils.getParameterizedClass(field, 1);
				PropertyIterator propIterator = childrenContainer.getProperties();
				PropertyMapper.mapPropertiesToMap(obj, field, valueType, propIterator);

			} else {
				// instantiate the field class
				Class childObjClass = field.getType();
				Node childNode = childrenContainer.getNodes().nextNode();
				Object childObj = mapper.createInstanceForNode(childObjClass, childNode);
				mapper.mapNodeToClass(childObj, childNode, nameFilter, maxDepth, obj, depth+1);
				field.set(obj, childObj);
			}
		}
	}
}
