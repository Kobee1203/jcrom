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
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import org.jcrom.annotations.JcrReference;
import org.jcrom.util.NameFilter;
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles mappings of type @JcrReference
 * 
 * @author Olafur Gauti Gudmundsson
 */
class ReferenceMapper {
	
	private static String getPropertyName(Field field) {
		JcrReference jcrReference = field.getAnnotation(JcrReference.class);
		String name = field.getName();
		if (!jcrReference.name().equals(Mapper.DEFAULT_FIELDNAME)) {
			name = jcrReference.name();
		}
		return name;
	}
    
    private static String relativePath( String path ) {
        if ( path.charAt(0) == '/' ) {
            return path.substring(1);
        } else {
            return path;
        }
    }
	
	private static List<Value> getReferenceValues( List references, Session session, JcrReference jcrReference ) 
			throws IllegalAccessException, RepositoryException {
		List<Value> refValues = new ArrayList<Value>();
		for (int i = 0; i < references.size(); i++) {
            if ( jcrReference.byPath() ) {
                String referencePath = Mapper.getNodePath(references.get(i));
                if ( referencePath != null && !referencePath.equals("") ) {
                    if ( session.getRootNode().hasNode(relativePath(referencePath)) ) {
                        refValues.add(session.getValueFactory().createValue(referencePath));
                    }
                }
            } else {
                String referenceUUID = Mapper.getNodeUUID(references.get(i));
                if (referenceUUID != null && !referenceUUID.equals("")) {
                    Node referencedNode = session.getNodeByUUID(referenceUUID);
                    refValues.add(session.getValueFactory().createValue(referencedNode));
                }
            }
		}
		return refValues;
	}
	
	private static void addSingleReferenceToNode( Field field, Object obj, String propertyName, Node node ) 
			throws IllegalAccessException, RepositoryException {
		// extract the UUID from the object, load the node, and
		// add a reference to it
        JcrReference jcrReference = field.getAnnotation(JcrReference.class);
		Object referenceObject = field.get(obj);
        
		if (referenceObject != null) {
            mapSingleReference(jcrReference, referenceObject, node, propertyName);
		} else {
			// remove the reference
			node.setProperty(propertyName, (Value) null);
		}
	}
	
	private static void addMultipleReferencesToNode( Field field, Object obj, String propertyName, Node node ) 
			throws IllegalAccessException, RepositoryException {
        
        JcrReference jcrReference = field.getAnnotation(JcrReference.class);
		List references = (List) field.get(obj);
		if (references != null && !references.isEmpty()) {
			List<Value> refValues = getReferenceValues(references, node.getSession(), jcrReference);
			if (!refValues.isEmpty()) {
				node.setProperty(propertyName, (Value[]) refValues.toArray(new Value[refValues.size()]));
			} else {
				node.setProperty(propertyName, (Value) null);
			}
		} else {
			node.setProperty(propertyName, (Value) null);
		}
	}
    
    private static void mapSingleReference( JcrReference jcrReference, Object referenceObject, Node containerNode, String propertyName ) 
            throws IllegalAccessException, RepositoryException {
        
        if ( jcrReference.byPath() ) {
            String referencePath = Mapper.getNodePath(referenceObject);
            if ( referencePath != null && !referencePath.equals("") ) {
                if ( containerNode.getSession().getRootNode().hasNode(relativePath(referencePath)) ) {
                    containerNode.setProperty(propertyName, containerNode.getSession().getValueFactory().createValue(referencePath));
                }
            }
        } else {
            String referenceUUID = Mapper.getNodeUUID(referenceObject);
            if (referenceUUID != null && !referenceUUID.equals("")) {
                Node referencedNode = containerNode.getSession().getNodeByUUID(referenceUUID);
                containerNode.setProperty(propertyName, referencedNode);
            } else {
                // remove the reference
                containerNode.setProperty(propertyName, (Value) null);
            }
        }
    }
    
    /**
     * Maps a Map<String,Object> or Map<String,List<Object>> to a JCR Node.
     */
    private static void addMapOfReferencesToNode( Field field, Object obj, String containerName, Node node ) 
			throws IllegalAccessException, RepositoryException {
        
        JcrReference jcrReference = field.getAnnotation(JcrReference.class);
        
        // remove previous references if they exist
        if ( node.hasNode(containerName) ) {
            node.getNode(containerName).remove();
        }
        
        // create a reference container
        Node referenceContainer = node.addNode(containerName);
        
        // map the references as properties on the container node
        Map referenceMap = (Map) field.get(obj);
        if (referenceMap != null && !referenceMap.isEmpty()) {
            Class paramClass = ReflectionUtils.getParameterizedClass(field,1);
            Iterator it = referenceMap.keySet().iterator();
            while ( it.hasNext() ) {
                String key = (String)it.next();
                if ( ReflectionUtils.implementsInterface(paramClass, List.class) ) {
                    List references = (List) referenceMap.get(key);
                    List<Value> refValues = getReferenceValues(references, referenceContainer.getSession(), jcrReference);
                    if ( !refValues.isEmpty() ) {
                        referenceContainer.setProperty(key, (Value[]) refValues.toArray(new Value[refValues.size()]));
                    }
                
                } else {
                    Object referenceObject = referenceMap.get(key);
                    mapSingleReference(jcrReference, referenceObject, referenceContainer, key);
                }
            }
		}
    }
	
	private static void setReferenceProperties( Field field, Object obj, Node node, NameFilter nameFilter )
			throws IllegalAccessException, RepositoryException {

		String propertyName = getPropertyName(field);

		// make sure that the reference should be updated
		if ( nameFilter == null || nameFilter.isIncluded(field.getName()) ) {
			if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
				// multiple references in a List
				addMultipleReferencesToNode(field, obj, propertyName, node);
            } else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
                // multiple references in a Map
                addMapOfReferencesToNode(field, obj, propertyName, node);
			} else {
				// single reference object
				addSingleReferenceToNode(field, obj, propertyName, node);
			}
		}
	}

	static void addReferences( Field field, Object obj, Node node )
			throws IllegalAccessException, RepositoryException {
		setReferenceProperties(field, obj, node, null);
	}

	static void updateReferences( Field field, Object obj, Node node, NameFilter nameFilter ) 
			throws IllegalAccessException, RepositoryException {
		setReferenceProperties(field, obj, node, nameFilter);
	}
    
    private static Node getSingleReferencedNode( JcrReference jcrReference, Value value, Session session ) throws RepositoryException {
        if ( jcrReference.byPath() ) {
            if ( session.getRootNode().hasNode(relativePath(value.getString())) ) {
                return session.getRootNode().getNode(relativePath(value.getString()));
            }
        } else {
            return session.getNodeByUUID(value.getString());   
        }
        return null;
    }
	
	static Object createReferencedObject( Field field, Value value, Object obj, Session session, Class referenceObjClass, 
			int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		
        JcrReference jcrReference = field.getAnnotation(JcrReference.class);
        Node referencedNode = null;
        
        if ( jcrReference.byPath() ) {
            if ( session.getRootNode().hasNode(relativePath(value.getString())) ) {
                referencedNode = session.getRootNode().getNode(relativePath(value.getString()));
            }
        } else {
            referencedNode = session.getNodeByUUID(value.getString());   
        }
        
        if ( referencedNode != null ) {
            Object referencedObject = mapper.createInstanceForNode(referenceObjClass, referencedNode);
            if ( nameFilter.isIncluded(field.getName()) && ( maxDepth < 0 || depth < maxDepth ) ) {
                // load and map the object
                mapper.mapNodeToClass(referencedObject, referencedNode, nameFilter, maxDepth, obj, depth+1);
            } else {
                if ( jcrReference.byPath() ) {
                    // just store the path
                    Mapper.setNodePath(referencedObject, value.getString());
                } else {
                    // just store the UUID
                    Mapper.setUUID(referencedObject, value.getString());
                }
            }
            return referencedObject;
        } else {
            return null;
        }
	}
	
	static List getReferenceList( Field field, String propertyName, Class referenceObjClass, Node node, Object obj, 
			int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		
		List references = new ArrayList();
        if ( node.hasProperty(propertyName) ) {
            Value[] refValues = node.getProperty(propertyName).getValues();
            for ( Value value : refValues ) {
                Object referencedObject = createReferencedObject(field, value, obj, node.getSession(), referenceObjClass, depth, maxDepth, nameFilter, mapper);
                references.add(referencedObject);
            }
        }
		return references;
	}
    
    static Map getReferenceMap( Field field, String containerName, Class mapParamClass, Node node, Object obj, 
			int depth, int maxDepth, NameFilter nameFilter, Mapper mapper, JcrReference jcrReference ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
        
        Map references = new HashMap();
        if ( node.hasNode(containerName) ) {
            Node containerNode = node.getNode(containerName);
            PropertyIterator propertyIterator = containerNode.getProperties();
            while ( propertyIterator.hasNext() ) {
                Property p = propertyIterator.nextProperty();
                if ( !p.getName().startsWith("jcr:") ) {
                    if ( ReflectionUtils.implementsInterface(mapParamClass, List.class) ) {
                        if ( jcrReference.lazy() ) {
                            references.put(p.getName(), 
                                ProxyFactory.createReferenceListProxy(Object.class, obj, containerNode.getPath(), p.getName(), node.getSession(), 
                                    mapper, depth, maxDepth, nameFilter, field)
                                    );
                        } else {
                            references.put(p.getName(), 
                                    getReferenceList(field, p.getName(), Object.class, containerNode, obj, depth, maxDepth, nameFilter, mapper)
                                    );
                        }
                    } else {
                        if ( jcrReference.lazy() ) {
                            Node referencedNode = getSingleReferencedNode(jcrReference, p.getValue(), node.getSession());
                            references.put(p.getName(), 
                                    ProxyFactory.createReferenceProxy(mapper.findClassFromNode(Object.class, referencedNode), obj, containerNode.getPath(), p.getName(), node.getSession(), 
                                    mapper, depth, maxDepth, nameFilter, field));
                        } else {
                            references.put(p.getName(), createReferencedObject(field, p.getValue(), obj, containerNode.getSession(), Object.class, depth, maxDepth, nameFilter, mapper));
                        }
                    }
                }
            }
        }
        return references;
    }
	
	static void getReferencesFromNode( Field field, Node node, Object obj, int depth, int maxDepth, NameFilter nameFilter, Mapper mapper ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		
		String propertyName = getPropertyName(field);
		JcrReference jcrReference = field.getAnnotation(JcrReference.class);
		
        if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
            // multiple references in a List
            Class referenceObjClass = ReflectionUtils.getParameterizedClass(field);
            if ( jcrReference.lazy() ) {
                // lazy loading
                field.set(obj, 
                        ProxyFactory.createReferenceListProxy(referenceObjClass, obj, node.getPath(), propertyName, node.getSession(), 
                        mapper, depth, maxDepth, nameFilter, field));
            } else {
                // eager loading
                field.set(obj, getReferenceList(field, propertyName, referenceObjClass, node, obj, depth, maxDepth, nameFilter, mapper));
            }

        } else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
            // multiple references in a Map
            // lazy loading is applied to each value in the Map
            Class mapParamClass = ReflectionUtils.getParameterizedClass(field,1);
            field.set(obj, getReferenceMap(field, propertyName, mapParamClass, node, obj, depth, maxDepth, nameFilter, mapper, jcrReference));

        } else {
            // single reference
            if ( node.hasProperty(propertyName) ) {
                Class referenceObjClass = field.getType();
                if ( jcrReference.lazy() ) {
                    field.set(obj, 
                            ProxyFactory.createReferenceProxy(referenceObjClass, obj, node.getPath(), propertyName, node.getSession(), 
                            mapper, depth, maxDepth, nameFilter, field));
                } else {
                    field.set(obj, createReferencedObject(field, node.getProperty(propertyName).getValue(), obj, node.getSession(), referenceObjClass, depth, maxDepth, nameFilter, mapper));
                }
            }
        }
	}
}
