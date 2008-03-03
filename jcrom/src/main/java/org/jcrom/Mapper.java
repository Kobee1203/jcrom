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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import org.jcrom.annotations.JcrBaseVersionCreated;
import org.jcrom.annotations.JcrBaseVersionName;
import org.jcrom.annotations.JcrCheckedout;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrCreated;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrParentNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrReference;
import org.jcrom.annotations.JcrSerializedProperty;
import org.jcrom.annotations.JcrUUID;
import org.jcrom.annotations.JcrVersionCreated;
import org.jcrom.annotations.JcrVersionName;
import org.jcrom.util.NameFilter;
import org.jcrom.util.PathUtils;
import org.jcrom.util.ReflectionUtils;

/**
 * This class handles the heavy lifting of mapping a JCR node
 * to a JCR entity object, and vice versa.
 * 
 * @author Olafur Gauti Gudmundsson
 */
class Mapper {
	
	/** The Class that this instance maps to/from */
	private final Class entityClass;
	
	private final boolean cleanNames;
	
	/**
	 * Create a Mapper for a specific class.
	 * 
	 * @param entityClass the class that we will me mapping to/from
	 */
	Mapper( Class entityClass, boolean cleanNames ) {
		this.entityClass = entityClass;
		this.cleanNames = cleanNames;
	}
	
	Class getEntityClass() {
		return entityClass;
	}
	
	private String getCleanName( String name ) {
		if ( cleanNames ) {
			return PathUtils.createValidName(name);
		} else {
			return name;
		}
	}
	
	private Field findAnnotatedField( Object obj, Class annotationClass ) {
		for ( Field field : ReflectionUtils.getDeclaredAndInheritedFields(obj.getClass()) ) {
			if ( field.isAnnotationPresent(annotationClass) ) {
				field.setAccessible(true);
				return field;
			}
		}
		return null;
	}
	
	private Field findPathField( Object obj ) {
		return findAnnotatedField(obj, JcrPath.class);
	}
	
	private Field findNameField( Object obj ) {
		return findAnnotatedField(obj, JcrName.class);
	}
	
	private Field findUUIDField( Object obj ) {
		return findAnnotatedField(obj, JcrUUID.class);
	}
	
	String getNodeName( Object object ) throws Exception {
		return (String) findNameField(object).get(object);
	}
	
	String getNodePath( Object object ) throws Exception {
		return (String) findPathField(object).get(object);
	}
	
	String getNodeUUID( Object object ) throws Exception {
		return (String) findUUIDField(object).get(object);
	}
	
	void setBaseVersionInfo( Object object, String name, Calendar created ) throws Exception {
		Field baseName = findAnnotatedField(object, JcrBaseVersionName.class);
		if ( baseName != null ) {
			baseName.set(object, name);
		}
		Field baseCreated = findAnnotatedField(object, JcrBaseVersionCreated.class);
		if ( baseCreated != null ) {
			if ( baseCreated.getType() == Date.class ) {
				baseCreated.set(object, created.getTime());
			} else if ( baseCreated.getType() == Timestamp.class ) {
				baseCreated.set(object, new Timestamp(created.getTimeInMillis()));
			} else if ( baseCreated.getType() == Calendar.class ) {
				baseCreated.set(object, created);
			}
		}
	}
	
	private void setNodeName( Object object, String name ) throws Exception {
		findNameField(object).set(object, name);
	}
	
	private void setNodePath( Object object, String path ) throws Exception {
		findPathField(object).set(object, path);
	}
	
	private void setUUID( Object object, String uuid ) throws Exception {
		Field uuidField = findUUIDField(object);
		if ( uuidField != null ) {
			uuidField.set(object, uuid);
		}
	}

	/**
	 * Transforms the node supplied to an instance of the entity class
	 * that this Mapper was created for.
	 * 
	 * @param node the JCR node from which to create the object
	 * @param childNodeFilter comma separated list of names of child nodes to load 
	 * ("*" loads all, while "none" loads no children)
	 * @param maxDepth the maximum depth of loaded child nodes (0 means no child nodes are loaded,
	 * while a negative value means that no restrictions are set on the depth).
	 * @return an instance of the JCR entity class, mapped from the node
	 * @throws java.lang.Exception
	 */
	Object fromNode( Node node, String childNodeFilter, int maxDepth ) throws Exception {
		Object obj = entityClass.newInstance();
		mapNodeToClass(obj, entityClass, node, new NameFilter(childNodeFilter), maxDepth, null, 0);
		return obj;
	}
	
	/**
	 * 
	 * @param node
	 * @param entity
	 * @param childNodeFilter
	 * @param maxDepth
	 * @throws java.lang.Exception
	 */
	String updateNode( Node node, Object entity, String childNodeFilter, int maxDepth ) throws Exception {
		if ( entity.getClass() != entityClass ) {
			throw new JcrMappingException("This mapper was constructed for [" + entityClass.getName() + "] and cannot handle [" + entity.getClass().getName() + "]");
		}
		
		return updateNode(node, entity, entityClass, new NameFilter(childNodeFilter), maxDepth, 0);
	}
	
	private Object findEntityByName( List entities, String name ) throws Exception {
		for ( int i = 0; i < entities.size(); i++ ) {
			Object entity = (Object) entities.get(i);
			if ( getCleanName(getNodeName(entity)).equals(name) ) {
				return entity;
			}
		}
		return null;
	}
	
	private String updateNode( Node node, Object obj, Class objClass, NameFilter childNameFilter, int maxDepth, int depth ) throws Exception {
		
		// if name is different, then we move the node
		if ( !node.getName().equals(getCleanName(getNodeName(obj))) ) {
			node.getSession().move(node.getPath(), node.getParent().getPath() + "/" + getCleanName(getNodeName(obj)));
			
			// update the object name and path
			setNodeName(obj, node.getName());
			setNodePath(obj, node.getPath());
		}
		
		for ( Field field : ReflectionUtils.getDeclaredAndInheritedFields(objClass) ) {
			field.setAccessible(true);
		
			if ( field.isAnnotationPresent(JcrProperty.class) ) {
				mapFieldToProperty(field, obj, node);
				
			} else if ( field.isAnnotationPresent(JcrSerializedProperty.class) ) {
				mapSerializedFieldToProperty(field, obj, node);
				
			} else if ( field.isAnnotationPresent(JcrChildNode.class) 
					&& ( maxDepth < 0 || depth < maxDepth ) ) {
				//
				// CHILD NODES
				//
				JcrChildNode jcrChildNode = field.getAnnotation(JcrChildNode.class);
				String name = field.getName();
				if ( !jcrChildNode.name().equals("fieldName") ) {
					name = jcrChildNode.name();
				}
				
				// make sure that this child is supposed to be updated
				if ( childNameFilter.isIncluded(field.getName()) ) {
					if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
						// can expect multiple children
						boolean nullOrEmpty = field.get(obj) == null || ((List)field.get(obj)).isEmpty();
						if ( !nullOrEmpty ) {
							Class paramClass = ReflectionUtils.getParameterizedClass(field);
							List children = (List)field.get(obj);
							if ( node.hasNode(name) ) {
								// children exist, we must update
								Node childContainer = node.getNode(name);
								NodeIterator childNodes = childContainer.getNodes();
								while ( childNodes.hasNext() ) {
									Node child = childNodes.nextNode();
									Object childEntity = findEntityByName(children, child.getName());
									if ( childEntity == null ) {
										// this child was not found, so we remove it
										child.remove();
									} else {
										updateNode(child, childEntity, paramClass, childNameFilter, maxDepth, depth+1);
									}
								}
								// we must add new children, if any
								for ( int i = 0; i < children.size(); i++ ) {
									Object child = children.get(i);
									if ( !childContainer.hasNode(getCleanName(getNodeName(child))) ) {
										addNode(childContainer, child, paramClass, null);
									}
								}
							} else {
								// no children exist, we add
								Node childContainer = node.addNode(getCleanName(name));
								for ( int i = 0; i < children.size(); i++ ) {
									addNode(childContainer, children.get(i), paramClass, null);
								}
							}
						} else {
							// field list is now null (or empty), so we remove the child nodes
							NodeIterator nodeIterator = node.getNodes(name);
							while ( nodeIterator.hasNext() ) {
								nodeIterator.nextNode().remove();
							}
						}
						
					} else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
						// this is a Map child, where we map the key/value pairs as properties
						Map<String,Object> map = (Map<String,Object>)field.get(obj);
						boolean nullOrEmpty = map == null || map.isEmpty();
						// remove the child node
						NodeIterator nodeIterator = node.getNodes(name);
						while ( nodeIterator.hasNext() ) {
							nodeIterator.nextNode().remove();
						}
						// add the map as a child node
						if ( !nullOrEmpty ) {
							Node childContainer = node.addNode(getCleanName(name));
							for ( String key : map.keySet() ) {
								mapToProperty(key, ReflectionUtils.getParameterizedClass(field, 1), null, map.get(key), childContainer);
							}
						}
					} else {
						// single child
						if ( !node.hasNode(name) ) {
							if ( field.get(obj) != null ) {
								// add the node if it does not exist
								Node childContainer = node.addNode(getCleanName(name));
								addNode(childContainer, field.get(obj), field.getType(), null);
							}
						} else {
							if ( field.get(obj) != null ) {
								updateNode(node.getNode(name).getNodes().nextNode(), field.get(obj), field.getType(), childNameFilter, maxDepth, depth+1);
							} else {
								// field is now null, so we remove the child node
								NodeIterator nodeIterator = node.getNodes(name);
								while ( nodeIterator.hasNext() ) {
									nodeIterator.nextNode().remove();
								}
							}
						}
					}
				}
				
			} else if ( field.isAnnotationPresent(JcrReference.class) ) {
				//
				// REFERENCES
				//
				JcrReference jcrReference = field.getAnnotation(JcrReference.class);
				String name = field.getName();
				if ( !jcrReference.name().equals("fieldName") ) {
					name = jcrReference.name();
				}
				
				// make sure that the reference should be updated
				if ( childNameFilter.isIncluded(field.getName()) ) {
					if ( !node.hasProperty(name) ) {
						Object referencedObject = field.get(obj);
						String referencedUUID = referencedObject == null ? null : getNodeUUID(referencedObject);
						if ( referencedObject != null && referencedUUID != null && !referencedUUID.equals("") ) {
							// load the node and add a reference
							Node referencedNode = node.getSession().getNodeByUUID(referencedUUID);
							node.setProperty(name, referencedNode);
						}
					} else {
						// this reference already exists
						Object referencedObject = field.get(obj);
						String referencedUUID = referencedObject == null ? null : getNodeUUID(referencedObject);
						if ( referencedObject != null && referencedUUID != null && !referencedUUID.equals("") ) {
							// update the reference, but only if it changed
							if ( !node.getProperty(name).getString().equals(referencedUUID) ) {
								Node referencedNode = node.getSession().getNodeByUUID(referencedUUID);
								node.setProperty(name, referencedNode);
							}
						} else {
							// remove the reference
							node.setProperty(name, (Value)null);
						}
					}
				}
				
			} else if ( field.isAnnotationPresent(JcrFileNode.class) 
					&& ( maxDepth < 0 || depth < maxDepth ) ) {
				//
				// FILE NODES
				//
				JcrFileNode jcrFileNode = field.getAnnotation(JcrFileNode.class);
				String name = field.getName();
				if ( !jcrFileNode.name().equals("fieldName") ) {
					name = jcrFileNode.name();
				}
				
				// make sure that this child is supposed to be updated
				if ( childNameFilter.isIncluded(field.getName()) ) {
					if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
						// can expect multiple children
						boolean nullOrEmpty = field.get(obj) == null || ((List)field.get(obj)).isEmpty();
						if ( !nullOrEmpty ) {
							Class paramClass = ReflectionUtils.getParameterizedClass(field);
							List children = (List)field.get(obj);
							if ( node.hasNode(name) ) {
								// children exist, we must update
								Node childContainer = node.getNode(name);
								NodeIterator childNodes = childContainer.getNodes();
								while ( childNodes.hasNext() ) {
									Node child = childNodes.nextNode();
									JcrFile childEntity = (JcrFile)findEntityByName(children, child.getName());
									if ( childEntity == null ) {
										// this child was not found, so we remove it
										child.remove();
									} else {
										updateFileNode(child, childEntity, childNameFilter, maxDepth, depth);
									}
								}
								// we must add new children, if any
								for ( int i = 0; i < children.size(); i++ ) {
									Object child = children.get(i);
									if ( !childContainer.hasNode(getCleanName(getNodeName(child))) ) {
										addNode(childContainer, child, paramClass, null);
									}
								}
							} else {
								// no children exist, we add
								JcrNode jcrNode = getJcrNodeAnnotation(ReflectionUtils.getParameterizedClass(field));
								Node fileContainer = createFileFolderNode(jcrNode, name, node);
								for ( int i = 0; i < children.size(); i++ ) {
									addFileNode(jcrNode, fileContainer, (JcrFile)children.get(i));
								}
							}
						} else {
							// field list is now null (or empty), so we remove the child nodes
							NodeIterator nodeIterator = node.getNodes(name);
							while ( nodeIterator.hasNext() ) {
								nodeIterator.nextNode().remove();
							}
						}
					} else {
						// single child
						if ( !node.hasNode(name) ) {
							if ( field.get(obj) != null ) {
								JcrNode jcrNode = getJcrNodeAnnotation(field.getType());
								Node fileContainer = createFileFolderNode(jcrNode, name, node);
								addFileNode(jcrNode, fileContainer, (JcrFile)field.get(obj));
							}
						} else {
							if ( field.get(obj) != null ) {
								updateFileNode(node.getNode(name).getNodes().nextNode(), (JcrFile)field.get(obj), childNameFilter, maxDepth, depth);
							} else {
								// field is now null, so we remove the child node
								NodeIterator nodeIterator = node.getNodes(name);
								while ( nodeIterator.hasNext() ) {
									nodeIterator.nextNode().remove();
								}
							}
						}
					}
				}
			}
		}
		return node.getName();
	}
	
	private JcrNode getJcrNodeAnnotation( Class c ) throws Exception {
		
		if ( c.isAnnotationPresent(JcrNode.class) ) {
			return (JcrNode) c.getAnnotation(JcrNode.class);
		} else {
			// need to check all superclasses
			Class parent = c.getSuperclass();
			while ( parent != null && parent != Object.class ) {
				if ( parent.isAnnotationPresent(JcrNode.class) ) {
					return (JcrNode) parent.getAnnotation(JcrNode.class);
				}
				parent = parent.getSuperclass();
			}
			
			// ...and all implemented interfaces
			for ( Class interfaceClass : c.getInterfaces() ) {
				if ( interfaceClass.isAnnotationPresent(JcrNode.class) ) {
					return (JcrNode) interfaceClass.getAnnotation(JcrNode.class);
				}
			}
		}
		// no annotation found, use the defaults
		return null;
	}
	
	private Node createFileFolderNode( JcrNode jcrNode, String nodeName, Node parentNode ) throws Exception {
		if ( jcrNode != null && jcrNode.nodeType().equals("nt:unstructured") ) {
			return parentNode.addNode(getCleanName(nodeName));
		} else {
			// assume it is an nt:file or extension of that, 
			// so we create an nt:folder
			return parentNode.addNode(getCleanName(nodeName), "nt:folder");
		}
	}
	
	/**
	 * 
	 * @param parentNode
	 * @param entity
	 * @throws java.lang.Exception
	 */
	Node addNode( Node parentNode, Object entity, String[] mixinTypes ) throws Exception {
		if ( entity.getClass() != entityClass ) {
			throw new JcrMappingException("This mapper was constructed for [" + entityClass.getName() + "] and cannot handle [" + entity.getClass().getName() + "]");
		}
		return addNode(parentNode, entity, entityClass, mixinTypes);
	}
	
	private Node addNode( Node parentNode, Object entity, Class objClass, String[] mixinTypes ) throws Exception {
		return addNode(parentNode, entity, objClass, mixinTypes, true);
	}
	
	private Node addNode( Node parentNode, Object entity, Class objClass, String[] mixinTypes, boolean createNode ) throws Exception {
		
		// create the child node
		Node node = null;
		if ( createNode ) {
			// check if we should use a specific node type
			JcrNode jcrNode = getJcrNodeAnnotation(objClass);
			if ( jcrNode == null || jcrNode.nodeType().equals("nt:unstructured") ) {
				node = parentNode.addNode(getCleanName(getNodeName(entity)));
			} else {
				node = parentNode.addNode(getCleanName(getNodeName(entity)), jcrNode.nodeType());
			}
			// add the mixin types
			if ( mixinTypes != null ) {
				for ( String mixinType : mixinTypes ) {
					if ( node.canAddMixin(mixinType) ) {
						node.addMixin(mixinType);
					}
				}
			}
			
			// update the object name and path
			setNodeName(entity, node.getName());
			setNodePath(entity, node.getPath());
			if ( node.hasProperty("jcr:uuid") ) {
				setUUID(entity, node.getUUID());
			}
			
		} else {
			node = parentNode;
		}
		
		for ( Field field : ReflectionUtils.getDeclaredAndInheritedFields(objClass) ) {
			field.setAccessible(true);
			
			if ( field.isAnnotationPresent(JcrProperty.class) ) {
				mapFieldToProperty(field, entity, node);
				
			} else if ( field.isAnnotationPresent(JcrSerializedProperty.class) ) {
				mapSerializedFieldToProperty(field, entity, node);
				
			} else if ( field.isAnnotationPresent(JcrChildNode.class) ) {
				JcrChildNode jcrChildNode = field.getAnnotation(JcrChildNode.class);
				String name = field.getName();
				if ( !jcrChildNode.name().equals("fieldName") ) {
					name = jcrChildNode.name();
				}
				
				if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
					// we can expect more than one child object here
					Node childContainer = node.addNode(getCleanName(name), jcrChildNode.containerNodeType());
					List children = (List)field.get(entity);
					if ( children != null && !children.isEmpty() ) {
						for ( int i = 0; i < children.size(); i++ ) {
							addNode(childContainer, children.get(i), ReflectionUtils.getParameterizedClass(field), null);
						}
					}
					
				} else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
					// this is a Map child, where we map the key/value pairs as properties
					Map<String,Object> map = (Map<String,Object>)field.get(entity);
					boolean nullOrEmpty = map == null || map.isEmpty();
					// remove the child node
					NodeIterator nodeIterator = node.getNodes(name);
					while ( nodeIterator.hasNext() ) {
						nodeIterator.nextNode().remove();
					}
					// add the map as a child node
					if ( !nullOrEmpty ) {
						Node childContainer = node.addNode(getCleanName(name));
						for ( String key : map.keySet() ) {
							mapToProperty(key, ReflectionUtils.getParameterizedClass(field, 1), null, map.get(key), childContainer);
						}
					}
						
				} else {
					// make sure that the field value is not null
					if ( field.get(entity) != null ) {
						Node childContainer = node.addNode(getCleanName(name), jcrChildNode.containerNodeType());
						addNode(childContainer, field.get(entity), field.getType(), null);
					}
				}
				
			} else if ( field.isAnnotationPresent(JcrReference.class) ) {
				JcrReference jcrReference = field.getAnnotation(JcrReference.class);
				String name = field.getName();
				if ( !jcrReference.name().equals("fieldName") ) {
					name = jcrReference.name();
				}
				// extract the UUID from the object, load the node, and
				// add a reference to it
				Object referenceObject = field.get(entity);
				if ( referenceObject != null ) {
					String referenceUUID = getNodeUUID(referenceObject);
					if ( referenceUUID != null && !referenceUUID.equals("") ) {
						Node referencedNode = node.getSession().getNodeByUUID(referenceUUID);
						node.setProperty(name, referencedNode);
					}
				}
			
			} else if ( field.isAnnotationPresent(JcrFileNode.class) ) {
				JcrFileNode jcrFileNode = field.getAnnotation(JcrFileNode.class);
				String name = field.getName();
				if ( !jcrFileNode.name().equals("fieldName") ) {
					name = jcrFileNode.name();
				}
				
				if ( field.get(entity) != null ) {
					if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
						// we can expect more than one child object here
						List children = (List)field.get(entity);
						if ( !children.isEmpty() ) {
							JcrNode jcrNode = getJcrNodeAnnotation(ReflectionUtils.getParameterizedClass(field));
							Node fileContainer = createFileFolderNode(jcrNode, name, node);
							for ( int i = 0; i < children.size(); i++ ) {
								addFileNode(jcrNode, fileContainer, (JcrFile)children.get(i));
							}
						}
					} else {
						JcrNode jcrNode = getJcrNodeAnnotation(field.getType());
						Node fileContainer = createFileFolderNode(jcrNode, name, node);
						addFileNode(jcrNode, fileContainer, (JcrFile)field.get(entity));
					}
				}
			}
		}
		return node;
	}
	
	
	private <T extends JcrFile> void updateFileNode( Node fileNode, T file, NameFilter childNameFilter, int maxDepth, int depth ) throws Exception {
		Node contentNode = fileNode.getNode("jcr:content");
		setFileNodeProperties(contentNode, file);
	
		updateNode(fileNode, file, file.getClass(), childNameFilter, maxDepth, depth+1);
	}
	
	private <T extends JcrFile> void addFileNode( JcrNode jcrNode, Node parentNode, T file ) throws Exception {
		Node fileNode = null;
		if ( jcrNode == null || jcrNode.nodeType().equals("nt:unstructured") ) {
			fileNode = parentNode.addNode(getCleanName(file.getName()));
		} else {
			fileNode = parentNode.addNode(getCleanName(file.getName()), jcrNode.nodeType());
		}
		Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
		setFileNodeProperties(contentNode, file);
		
		addNode(fileNode, file, file.getClass(), null, false);
	}
	
	private <T extends JcrFile> void setFileNodeProperties( Node contentNode, T file ) throws Exception {
		contentNode.setProperty("jcr:mimeType", file.getMimeType());
		contentNode.setProperty("jcr:lastModified", file.getLastModified());
		if ( file.getEncoding() != null ) {
			contentNode.setProperty("jcr:encoding", file.getEncoding());
		}
		
		// add the file data
		JcrDataProvider dataProvider = file.getDataProvider();
		if ( dataProvider != null ) {
			if ( dataProvider.getType() == JcrDataProvider.TYPE.FILE && dataProvider.getFile() != null ) {
				contentNode.setProperty("jcr:data", new FileInputStream(dataProvider.getFile()));
			} else if ( dataProvider.getType() == JcrDataProvider.TYPE.BYTES && dataProvider.getBytes() != null ) {
				contentNode.setProperty("jcr:data", new ByteArrayInputStream(dataProvider.getBytes()));
			} else if ( dataProvider.getType() == JcrDataProvider.TYPE.STREAM && dataProvider.getInputStream() != null ) {
				contentNode.setProperty("jcr:data", dataProvider.getInputStream());
			}
		}
	}
	
	private void mapSerializedFieldToProperty( Field field, Object obj, Node node ) throws Exception {
		JcrSerializedProperty jcrProperty = field.getAnnotation(JcrSerializedProperty.class);
		String propertyName = field.getName();
		if ( !jcrProperty.name().equals("fieldName") ) {
			propertyName = jcrProperty.name();
		}
		
		Object fieldValue = field.get(obj);
		if ( fieldValue != null ) {
			// serialize and store
			node.setProperty(propertyName, new ByteArrayInputStream(serialize(fieldValue)));
		} else {
			// remove the value
			node.setProperty(propertyName, (Value)null);
		}
	}
	
	private void mapFieldToProperty( Field field, Object obj, Node node ) throws Exception {
		JcrProperty jcrProperty = field.getAnnotation(JcrProperty.class);
		String name = field.getName();
		if ( !jcrProperty.name().equals("fieldName") ) {
			name = jcrProperty.name();
		}
		
		Class paramClass = ReflectionUtils.implementsInterface(field.getType(), List.class) ? ReflectionUtils.getParameterizedClass(field) : null;
		mapToProperty(name, field.getType(), paramClass, field.get(obj), node);
	}
	
	private void mapToProperty( String propertyName, Class type, Class paramClass, Object propertyValue, Node node ) throws Exception {
		
		// make sure that the field value is not null
		if ( propertyValue != null ) {
		
			ValueFactory valueFactory = node.getSession().getValueFactory();

			if ( ReflectionUtils.implementsInterface(type, List.class) ) {
				// multi-value property List
				List fieldValues = (List)propertyValue;
				if ( !fieldValues.isEmpty() ) {
					Value[] values = new Value[fieldValues.size()];
					for ( int i = 0; i < fieldValues.size(); i++ ) {
						values[i] = createValue(paramClass, fieldValues.get(i), valueFactory);
					}
					node.setProperty(propertyName, values);
				}
				
			} else if ( type.isArray() && type.getComponentType() != byte.class ) {
				// multi-value property array
				if ( propertyValue != null ) {
					Value[] values = null;
					if ( type.getComponentType() == int.class ) {
						int[] ints = (int[]) propertyValue;
						values = new Value[ints.length];
						for ( int i = 0; i < ints.length; i++ ) {
							values[i] = createValue(int.class, ints[i], valueFactory);
						}
					} else if ( type.getComponentType() == long.class ) {
						long[] longs = (long[]) propertyValue;
						values = new Value[longs.length];
						for ( int i = 0; i < longs.length; i++ ) {
							values[i] = createValue(long.class, longs[i], valueFactory);
						}
					} else if ( type.getComponentType() == double.class ) {
						double[] doubles = (double[]) propertyValue;
						values = new Value[doubles.length];
						for ( int i = 0; i < doubles.length; i++ ) {
							values[i] = createValue(double.class, doubles[i], valueFactory);
						}
					} else if ( type.getComponentType() == boolean.class ) {
						boolean[] booleans = (boolean[]) propertyValue;
						values = new Value[booleans.length];
						for ( int i = 0; i < booleans.length; i++ ) {
							values[i] = createValue(boolean.class, booleans[i], valueFactory);
						}
					} else {
						// Object
						Object[] objects = (Object[]) propertyValue;
						values = new Value[objects.length];
						for ( int i = 0; i < objects.length; i++ ) {
							values[i] = createValue(type.getComponentType(), objects[i], valueFactory);
						}
						
					}
					node.setProperty(propertyName, values);
				}

			} else {
				// single-value property
				Value value = createValue(type, propertyValue, valueFactory);
				if ( value != null ) {
					node.setProperty(propertyName, value);
				}
			}
		} else {
			// remove the value
			node.setProperty(propertyName, (Value)null);
		}
	}
	
	private boolean isVersionable( Node node ) throws Exception {
		for ( NodeType mixinType : node.getMixinNodeTypes() ) {
			if ( mixinType.getName().equals("mix:versionable") ) {
				return true;
			}
		}
		return false;
	}
		
	private void mapNodeToClass( Object obj, Class objClass, Node node, NameFilter nameFilter, int maxDepth, Object parentObject, int depth ) throws Exception {
		
		if ( !ReflectionUtils.extendsClass(objClass, JcrFile.class) ) {
			// this does not apply for JcrFile extensions
			setNodeName(obj, node.getName());
		}
		
		for ( Field field : ReflectionUtils.getDeclaredAndInheritedFields(objClass) ) {
			field.setAccessible(true);
			
			if ( field.isAnnotationPresent(JcrProperty.class) ) {
				mapPropertyToField(obj, field, node);
				
			} else if ( field.isAnnotationPresent(JcrSerializedProperty.class) ) {
				mapSerializedPropertyToField(obj, field, node);
				
			} else if ( field.isAnnotationPresent(JcrUUID.class) ) {
				if ( node.hasProperty("jcr:uuid") ) {
					field.set(obj, node.getUUID());
				}
				
			} else if ( field.isAnnotationPresent(JcrBaseVersionName.class) ) {
				if ( isVersionable(node) ) {
					field.set(obj, node.getBaseVersion().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrBaseVersionCreated.class) ) {
				if ( isVersionable(node) ) {
					field.set(obj, getValue(field.getType(), node.getSession().getValueFactory().createValue(node.getBaseVersion().getCreated())));
				}
				
			} else if ( field.isAnnotationPresent(JcrVersionName.class) ) {
				if ( node.getParent() != null && node.getParent().isNodeType("nt:version") ) {
					field.set(obj, node.getParent().getName());
				} else if ( isVersionable(node) ) {
					// if we're not browsing version history, then this must be the base version
					field.set(obj, node.getBaseVersion().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrVersionCreated.class) ) {
				if ( node.getParent() != null && node.getParent().isNodeType("nt:version") ) {
					Version version = (Version) node.getParent();
					field.set(obj, getValue(field.getType(), node.getSession().getValueFactory().createValue(version.getCreated())));
				} else if ( isVersionable(node) ) {
					// if we're not browsing version history, then this must be the base version
					field.set(obj, getValue(field.getType(), node.getSession().getValueFactory().createValue(node.getBaseVersion().getCreated())));
				}
				
			} else if ( field.isAnnotationPresent(JcrCheckedout.class) ) {
				field.set(obj, node.isCheckedOut());
				
			} else if ( field.isAnnotationPresent(JcrCreated.class) ) {
				if ( node.hasProperty("jcr:created") ) {
					field.set(obj, getValue(field.getType(), node.getProperty("jcr:created").getValue()));
				}
			
			} else if ( field.isAnnotationPresent(JcrParentNode.class) ) {
				if ( parentObject != null ) {
					field.set(obj, parentObject);
				}
				
			} else if ( field.isAnnotationPresent(JcrChildNode.class) 
					&& ( maxDepth < 0 || depth < maxDepth ) ) {
				JcrChildNode jcrChildNode = field.getAnnotation(JcrChildNode.class);
				String name = field.getName();
				if ( !jcrChildNode.name().equals("fieldName") ) {
					name = jcrChildNode.name();
				}
				
				if ( node.hasNode(name) && nameFilter.isIncluded(field.getName()) ) {
					// child nodes are always stored inside a container node
					Node childrenContainer = node.getNode(name);
					if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
						// we can expect more than one child object here
						List children = new ArrayList();
						ParameterizedType ptype = (ParameterizedType) field.getGenericType();
						
						NodeIterator iterator = childrenContainer.getNodes();
						while ( iterator.hasNext() ) {
							Class childObjClass = (Class)ptype.getActualTypeArguments()[0];
							Object childObj = childObjClass.newInstance();
							mapNodeToClass(childObj, childObjClass, iterator.nextNode(), nameFilter, maxDepth, obj, depth+1);
							children.add(childObj);
						}
						field.set(obj, children);
						
					} else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
						// map of properties
						Class valueType = ReflectionUtils.getParameterizedClass(field, 1);
						PropertyIterator propIterator = childrenContainer.getProperties();
						mapPropertiesToMap(obj, field, valueType, propIterator);
						
					} else {
						// instantiate the field class
						Object childObj = field.getType().newInstance();
						mapNodeToClass(childObj, field.getType(), childrenContainer.getNodes().nextNode(), nameFilter, maxDepth, obj, depth+1);
						field.set(obj, childObj);
					}
				}
				
			} else if ( field.isAnnotationPresent(JcrReference.class) ) {
				JcrReference jcrReference = field.getAnnotation(JcrReference.class);
				String name = field.getName();
				if ( !jcrReference.name().equals("fieldName") ) {
					name = jcrReference.name();
				}
				if ( node.hasProperty(name) ) {
					Object referencedObject = field.getType().newInstance();
					if ( nameFilter.isIncluded(field.getName()) && ( maxDepth < 0 || depth < maxDepth ) ) {
						// load the object
						mapNodeToClass(referencedObject, field.getType(), node.getProperty(name).getNode(), nameFilter, maxDepth, obj, depth+1);
					} else {
						// just load the UUID
						setUUID(obj, node.getProperty(name).getString());
					}
					field.set(obj, referencedObject);
				}
				
			} else if ( field.isAnnotationPresent(JcrFileNode.class) 
					&& ( maxDepth < 0 || depth < maxDepth ) ) {
				JcrFileNode jcrFileNode = field.getAnnotation(JcrFileNode.class);
				String name = field.getName();
				if ( !jcrFileNode.name().equals("fieldName") ) {
					name = jcrFileNode.name();
				}
				
				if ( node.hasNode(name) && nameFilter.isIncluded(field.getName()) ) {
					// file nodes are always stored inside a folder node
					Node fileContainer = node.getNode(name);
					if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
						// we can expect more than one child object here
						List children = new ArrayList();
						ParameterizedType ptype = (ParameterizedType) field.getGenericType();
						
						NodeIterator iterator = fileContainer.getNodes();
						while ( iterator.hasNext() ) {
							Class childObjClass = (Class)ptype.getActualTypeArguments()[0];
							JcrFile fileObj = (JcrFile)childObjClass.newInstance();
							mapNodeToFileObject(jcrFileNode, fileObj, iterator.nextNode(), nameFilter, maxDepth, obj, depth);
							children.add(fileObj);
						}
						field.set(obj, children);
						
					} else {
						// instantiate the field class
						JcrFile fileObj = (JcrFile)field.getType().newInstance();
						mapNodeToFileObject(jcrFileNode, fileObj, fileContainer.getNodes().nextNode(), nameFilter, maxDepth, obj, depth);
						field.set(obj, fileObj);
					}
				}
			
			} else if ( field.getName().equals("path") ) {
				field.set(obj, node.getPath());
			
			}
		}
	}
	
	private <T extends JcrFile> void mapNodeToFileObject( JcrFileNode jcrFileNode, T fileObj, Node fileNode, NameFilter nameFilter, int maxDepth, Object parentObject, int depth ) throws Exception {
		Node contentNode = fileNode.getNode("jcr:content");
		fileObj.setName(fileNode.getName());
		fileObj.setPath(fileNode.getPath());
		fileObj.setMimeType( contentNode.getProperty("jcr:mimeType").getString() );
		fileObj.setLastModified( contentNode.getProperty("jcr:lastModified").getDate() );
		if ( contentNode.hasProperty("jcr:encoding") ) {
			fileObj.setEncoding(contentNode.getProperty("jcr:encoding").getString());
		}
		
		// file data
		if ( jcrFileNode.loadType() == JcrFileNode.LoadType.BYTES ) {
			JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(JcrDataProvider.TYPE.BYTES, readBytes(contentNode.getProperty("jcr:data").getStream()));
			fileObj.setDataProvider(dataProvider);
		} else if ( jcrFileNode.loadType() == JcrFileNode.LoadType.STREAM ) {
			JcrDataProviderImpl dataProvider = new JcrDataProviderImpl(JcrDataProvider.TYPE.STREAM, contentNode.getProperty("jcr:data").getStream());
			fileObj.setDataProvider(dataProvider);
		}
		
		// if this is a JcrFile subclass, it may contain custom properties and 
		// child nodes that need to be mapped
		mapNodeToClass(fileObj, fileObj.getClass(), fileNode, nameFilter, maxDepth, parentObject, depth+1);
	}
	
	private void mapPropertiesToMap( Object obj, Field field, Class valueType, PropertyIterator propIterator ) throws Exception {
		Map<String,Object> map = new HashMap<String,Object>();
		while ( propIterator.hasNext() ) {
			Property p = propIterator.nextProperty();
			// we ignore the read-only properties added by the repository
			if ( !p.getName().startsWith("jcr:") && !p.getName().startsWith("nt:") ) {
				if ( valueType.isArray() ) {
					if ( p.getDefinition().isMultiple() ) {
						map.put(p.getName(), valuesToArray(valueType.getComponentType(), p.getValues()));
					} else {
						Value[] values = new Value[1];
						values[0] = p.getValue();
						map.put(p.getName(), valuesToArray(valueType.getComponentType(), values));
					}
				} else {
					map.put(p.getName(), getValue(valueType, p.getValue()));
				}
			}
		}
		field.set(obj, map);
	}
	
	private Object[] valuesToArray( Class type, Value[] values ) throws Exception {
		Object[] arr = (Object[])Array.newInstance(type, values.length);
		for ( int i = 0; i < values.length; i++ ) {
			arr[i] = (Object) getValue(type, values[i]);
		}
		return arr;
	}
	
	private void mapSerializedPropertyToField( Object obj, Field field, Node node ) throws Exception {
		JcrSerializedProperty jcrProperty = field.getAnnotation(JcrSerializedProperty.class);
		String propertyName = field.getName();
		if ( !jcrProperty.name().equals("fieldName") ) {
			propertyName = jcrProperty.name();
		}
		if ( node.hasProperty(propertyName) ) {
			Property p = node.getProperty(propertyName);
			field.set(obj, deserialize(p.getStream()));
		}
	}
	
	private void mapPropertyToField( Object obj, Field field, Node node ) throws Exception {
		JcrProperty jcrProperty = field.getAnnotation(JcrProperty.class);
		String name = field.getName();
		if ( !jcrProperty.name().equals("fieldName") ) {
			name = jcrProperty.name();
		}
		if ( node.hasProperty(name) ) {
			Property p = node.getProperty(name);
			
			if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
				// multi-value property (List)
				List properties = new ArrayList();
				Class paramClass = ReflectionUtils.getParameterizedClass(field);
				for ( Value value : p.getValues() ) {
					properties.add(getValue(paramClass, value));
				}
				field.set(obj, properties);
			
			} else if ( field.getType().isArray() && field.getType().getComponentType() != byte.class ) {
				// multi-value property (array)
				Value[] values = p.getValues();
				if ( field.getType().getComponentType() == int.class ) {
					int[] arr = new int[values.length];
					for ( int i = 0; i < values.length; i++ ) {
						arr[i] = (int) values[i].getDouble();
					}
					field.set(obj, arr);
				} else if ( field.getType().getComponentType() == long.class ) {
					long[] arr = new long[values.length];
					for ( int i = 0; i < values.length; i++ ) {
						arr[i] = values[i].getLong();
					}
					field.set(obj, arr);
				} else if ( field.getType().getComponentType() == double.class ) {
					double[] arr = new double[values.length];
					for ( int i = 0; i < values.length; i++ ) {
						arr[i] = values[i].getDouble();
					}
					field.set(obj, arr);
				} else if ( field.getType().getComponentType() == boolean.class ) {
					boolean[] arr = new boolean[values.length];
					for ( int i = 0; i < values.length; i++ ) {
						arr[i] = values[i].getBoolean();
					}
					field.set(obj, arr);
				} else {
					Object[] arr = valuesToArray(field.getType().getComponentType(), values);
					field.set(obj, arr);
				}
				
			} else {
				// single-value property
				field.set(obj, getValue(field.getType(), p.getValue()));
			}
		}
	}
	
	private Value createValue( Class c, Object fieldValue, ValueFactory valueFactory ) throws Exception {		
		if ( c == String.class ) {
			return valueFactory.createValue((String) fieldValue);
		} else if ( c == Date.class ) {
			Calendar cal = Calendar.getInstance();
			cal.setTime((Date)fieldValue);
			return valueFactory.createValue(cal);
		} else if ( c == Timestamp.class ) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(((Timestamp)fieldValue).getTime());
			return valueFactory.createValue(cal);
		} else if ( c == Calendar.class ) {
			return valueFactory.createValue((Calendar) fieldValue);
		} else if ( c == InputStream.class ) {
			return valueFactory.createValue((InputStream)fieldValue);
		} else if ( c.isArray() && c.getComponentType() == byte.class ) {
			return valueFactory.createValue(new ByteArrayInputStream((byte[])fieldValue));
		} else if ( c == Integer.class || c == int.class ) {
			return valueFactory.createValue((Integer)fieldValue);
		} else if ( c == Long.class || c == long.class ) {
			return valueFactory.createValue((Long)fieldValue);
		} else if ( c == Double.class || c == double.class ) {
			return valueFactory.createValue((Double)fieldValue);
		} else if ( c == Boolean.class || c == boolean.class ) {
			return valueFactory.createValue((Boolean)fieldValue);
		}
		return null;
	}
	
	private Object getValue( Class c, Value value ) throws Exception {
		if ( c == String.class ) {
			return value.getString();
		} else if ( c == Date.class ) {
			return value.getDate().getTime();
		} else if ( c == Timestamp.class ) {
			return new Timestamp(value.getDate().getTimeInMillis());
		} else if ( c == Calendar.class ) {
			return value.getDate();
		} else if ( c == InputStream.class ) {
			return value.getStream();
		} else if ( c.isArray() && c.getComponentType() == byte.class ) {
			// byte array...we need to read from the stream
			return readBytes(value.getStream());
		} else if ( c == Integer.class || c == int.class ) {
			return (int) value.getDouble();
		} else if ( c == Long.class || c == long.class ) {
			return value.getLong();
		} else if ( c == Double.class || c == double.class ) {
			return value.getDouble(); 
		} else if ( c == Boolean.class || c == boolean.class ) {
			return value.getBoolean();
		}
		return null;
	}
	
	private byte[] readBytes( InputStream in ) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			in.close();
	        out.close();
		}
		return out.toByteArray();
	}
	
	/**
	 * Serialize an object to a byte array.
	 * 
	 * @param obj the object to be serialized
	 * @return the serialized object
	 * @throws java.lang.Exception
	 */
	private byte[] serialize( Object obj ) throws Exception {
        // Serialize to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        ObjectOutputStream out = new ObjectOutputStream(bos) ;
        out.writeObject(obj);
        out.close();
    
        // Get the bytes of the serialized object
        return bos.toByteArray();
	}
	
	/**
	 * Deserialize an object from a byte array.
	 * 
	 * @param bytes
	 * @return
	 * @throws java.lang.Exception
	 */
	private Object deserialize( InputStream byteStream ) throws Exception {
        // Deserialize from a byte array
        ObjectInputStream in = new ObjectInputStream(byteStream);
		try {
			return in.readObject();
		} finally {
			in.close();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj == null || !(obj instanceof Mapper) ) {
			return false;
		}
		Mapper other = (Mapper) obj;
		
		return other.entityClass == entityClass;
	}

	@Override
	public int hashCode() {
		return entityClass.hashCode();
	}
	
	
}
