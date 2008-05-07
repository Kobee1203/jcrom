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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
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
	
	private static void removeChildren( Node containerNode ) throws RepositoryException {
		NodeIterator nodeIterator = containerNode.getNodes();
		while ( nodeIterator.hasNext() ) {
			nodeIterator.nextNode().remove();
		}
	}
    
    private static Node createChildNodeContainer( Node node, String containerName, JcrChildNode jcrChildNode, Mapper mapper ) 
            throws RepositoryException {
        
        if ( !node.hasNode(mapper.getCleanName(containerName)) ) {
            return node.addNode(mapper.getCleanName(containerName), jcrChildNode.containerNodeType());
        } else {
            return node.getNode(mapper.getCleanName(containerName));
        }
    }
	
	private static void addSingleChildToNode( Field field, JcrChildNode jcrChildNode, Object obj, String nodeName, Node node, 
			Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
        Node childContainer = createChildNodeContainer(node, nodeName, jcrChildNode, mapper);
		if ( !childContainer.hasNodes() ) {
			if ( field.get(obj) != null ) {
				// add the node if it does not exist
				mapper.addNode(childContainer, field.get(obj), null);
			}
		} else {
			if ( field.get(obj) != null ) {
				mapper.updateNode(childContainer.getNodes().nextNode(), field.get(obj), field.getType(), nameFilter, maxDepth, depth+1);
			} else {
				// field is now null, so we remove the child node
				removeChildren(childContainer);
			}
		}
	}

	private static void addMultipleChildrenToNode( Field field, JcrChildNode jcrChildNode, Object obj, String nodeName, Node node, 
			Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {

        Node childContainer = createChildNodeContainer(node, nodeName, jcrChildNode, mapper);
		List children = (List)field.get(obj);
		if ( children != null && !children.isEmpty() ) {
			Class paramClass = ReflectionUtils.getParameterizedClass(field);
			if ( childContainer.hasNodes() ) {
				// children exist, we must update
				NodeIterator childNodes = childContainer.getNodes();
				while ( childNodes.hasNext() ) {
					Node child = childNodes.nextNode();
					Object childEntity = Mapper.findEntityByPath(children, child.getPath());
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
                    String childPath = Mapper.getNodePath(child);
					if ( childPath == null || childPath.equals("") || !childContainer.hasNode(mapper.getCleanName(Mapper.getNodeName(child))) ) {
						mapper.addNode(childContainer, child, null);
					}
				}
			} else {
				// no children exist, we add
				for ( int i = 0; i < children.size(); i++ ) {
					mapper.addNode(childContainer, children.get(i), null);
				}
			}
		} else {
			// field list is now null (or empty), so we remove the child nodes
			removeChildren(childContainer);
		}
	}
    
    /**
     * Maps a Map<String,Object> or Map<String,List<Object>> to a JCR Node.
     */
	private static void addMapOfChildrenToNode( Field field, JcrChildNode jcrChildNode, Object obj, String nodeName, Node node, 
			Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException, IOException {
        
        Node childContainer = createChildNodeContainer(node, nodeName, jcrChildNode, mapper);
        Map childMap = (Map)field.get(obj);
        if ( childMap != null && !childMap.isEmpty() ) {
            Class paramClass = ReflectionUtils.getParameterizedClass(field,1);
            if ( childContainer.hasNodes() ) {
                // nodes already exist, we need to update
                Map mapWithCleanKeys = new HashMap();
                Iterator it = childMap.keySet().iterator();
                while ( it.hasNext() ) {
                    String key = (String)it.next();
                    String cleanKey = mapper.getCleanName(key);
                    if ( childContainer.hasNode(cleanKey) ) {
                        if ( ReflectionUtils.implementsInterface(paramClass, List.class) ) {
                            // lists are hard to update, so we just recreate it
                            childContainer.getNode(cleanKey).remove();
                            Node listContainer = childContainer.addNode(cleanKey);
                            List childList = (List) childMap.get(key);
                            for ( int i = 0; i < childList.size(); i++ ) {
                                mapper.addNode(listContainer, childList.get(i), null);
                            }
                        } else {
                            // update the child
                            mapper.updateNode(childContainer.getNode(cleanKey), childMap.get(key), paramClass, nameFilter, maxDepth, depth+1);
                        }
                    } else {
                        // this child does not exist, so we add it
                        addMapChild(paramClass, childContainer, childMap, key, cleanKey, mapper);
                    }
                    mapWithCleanKeys.put(cleanKey, "1");
                }
                
                // remove nodes that no longer exist
				NodeIterator childNodes = childContainer.getNodes();
				while ( childNodes.hasNext() ) {
					Node child = childNodes.nextNode();
                    if ( !mapWithCleanKeys.containsKey(child.getName()) ) {
                        child.remove();
                    }
                }
            } else {
                // no children exist, we simply add all
                Iterator it = childMap.keySet().iterator();
                while ( it.hasNext() ) {
                    String key = (String)it.next();
                    String cleanKey = mapper.getCleanName(key);
                    addMapChild(paramClass, childContainer, childMap, key, cleanKey, mapper);
                }
            }
		} else {
			// map is now null (or empty), so we remove the child nodes
			removeChildren(childContainer);
        }
    }
    
    private static void addMapChild( Class paramClass, Node childContainer, Map childMap, String key, String cleanKey, Mapper mapper ) 
            throws IllegalAccessException, RepositoryException, IOException {
        if ( ReflectionUtils.implementsInterface(paramClass, List.class) ) {
            List childList = (List) childMap.get(key);
            // create a container for the List
            Node listContainer = childContainer.addNode(cleanKey);
            for ( int i = 0; i < childList.size(); i++ ) {
                mapper.addNode(listContainer, childList.get(i), null);
            }
        } else {
            mapper.addNode(childContainer, childMap.get(key), null);
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
                // multiple children in a Map
                addMapOfChildrenToNode(field, jcrChildNode, obj, nodeName, node, mapper, depth, maxDepth, nameFilter);
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
	
	static List getChildrenList( Class childObjClass, Node childrenContainer, Object parentObj, Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		List children = new ArrayList();
		NodeIterator iterator = childrenContainer.getNodes();
		while ( iterator.hasNext() ) {
			Node childNode = iterator.nextNode();
			Object childObj = mapper.createInstanceForNode(childObjClass, childNode);
			mapper.mapNodeToClass(childObj, childNode, nameFilter, maxDepth, parentObj, depth+1);
			children.add(childObj);
		}
		return children;
	}
    
    private static Map getChildrenMap( Class mapParamClass, Node childrenContainer, Object parentObj, Mapper mapper, int depth, int maxDepth, NameFilter nameFilter, JcrChildNode jcrChildNode ) 
            throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {

        Map children = new HashMap();
        NodeIterator iterator = childrenContainer.getNodes();
        while ( iterator.hasNext() ) {
            Node childNode = iterator.nextNode();
            if ( ReflectionUtils.implementsInterface(mapParamClass, List.class) ) {
                // each value in the map is a list of child nodes
				if ( jcrChildNode.lazy() ) {
					// lazy loading
					children.put(childNode.getName(), ProxyFactory.createChildNodeListProxy(Object.class, parentObj, childNode.getSession(), childNode.getPath(), 
							mapper, depth, maxDepth, nameFilter)
                            );
				} else {
					// eager loading
					children.put(childNode.getName(), getChildrenList(Object.class, childNode, parentObj, mapper, depth, maxDepth, nameFilter));
				}
            } else {
                // each value in the map is a child node
				if ( jcrChildNode.lazy() ) {
					// lazy loading
					children.put(childNode.getName(), 
							ProxyFactory.createChildNodeProxy(mapper.findClassFromNode(Object.class,childNode), parentObj, childNode.getSession(), childNode.getPath(), 
							mapper, depth, maxDepth, nameFilter, false));
				} else {
					// eager loading
					children.put(childNode.getName(), getSingleChild(Object.class, childNode, parentObj, mapper, depth, maxDepth, nameFilter));
				}
            }
        }
        return children;
    }
	
	static Object getSingleChild( Class childObjClass, Node childNode, Object obj, Mapper mapper, int depth, int maxDepth, NameFilter nameFilter ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		Object childObj = mapper.createInstanceForNode(childObjClass, childNode);
		mapper.mapNodeToClass(childObj, childNode, nameFilter, maxDepth, obj, depth+1);
		return childObj;
	}
	
	static void getChildrenFromNode( Field field, Node node, Object obj, int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		
		String nodeName = getNodeName(field);
		JcrChildNode jcrChildNode = field.getAnnotation(JcrChildNode.class);

		if ( node.hasNode(nodeName) && node.getNode(nodeName).hasNodes() && nameFilter.isIncluded(field.getName()) ) {
			// child nodes are always stored inside a container node
            Node childrenContainer = node.getNode(nodeName);
			if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
				// we can expect more than one child object here
				Class childObjClass = ReflectionUtils.getParameterizedClass(field);
				List children;
				if ( jcrChildNode.lazy() ) {
					// lazy loading
					children = ProxyFactory.createChildNodeListProxy(childObjClass, obj, node.getSession(), childrenContainer.getPath(), 
							mapper, depth, maxDepth, nameFilter);
				} else {
					// eager loading
					children = getChildrenList(childObjClass, childrenContainer, obj, mapper, depth, maxDepth, nameFilter);
				}
				field.set(obj, children);

			} else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
                // dynamic map of child nodes
                // lazy loading is applied to each value in the Map
                Class mapParamClass = ReflectionUtils.getParameterizedClass(field,1);
                field.set(obj, getChildrenMap(mapParamClass, childrenContainer, obj, mapper, depth, maxDepth, nameFilter, jcrChildNode));
            
            } else {
				// instantiate the field class
				Class childObjClass = field.getType();
                if ( childrenContainer.hasNodes() ) {
                    if ( jcrChildNode.lazy() ) {
                        // lazy loading
                        field.set(obj, 
                                ProxyFactory.createChildNodeProxy(childObjClass, obj, node.getSession(), childrenContainer.getPath(), 
                                mapper, depth, maxDepth, nameFilter, true));
                    } else {
                        // eager loading
                        field.set(obj, getSingleChild(childObjClass, childrenContainer.getNodes().nextNode(), obj, mapper, depth, maxDepth, nameFilter));
                    }
                }
			}
		}
	}
}
