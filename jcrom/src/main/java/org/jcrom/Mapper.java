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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
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
	
	static final String DEFAULT_FIELDNAME = "fieldName";
	
	/** Set of classes that have been validated for mapping by this mapper */
	private final CopyOnWriteArraySet<Class> mappedClasses = new CopyOnWriteArraySet<Class>();
	/** Specifies whether to clean up the node names */
	private final boolean cleanNames;
	/** Specifies whether to retrieve mapped class name from node property */
	private final boolean dynamicInstantiation;
	
	/**
	 * Create a Mapper for a specific class.
	 * 
	 * @param entityClass the class that we will me mapping to/from
	 */
	Mapper( boolean cleanNames, boolean dynamicInstantiation ) {
		this.cleanNames = cleanNames;
		this.dynamicInstantiation = dynamicInstantiation;
	}
	
	boolean isMapped( Class c ) {
		return mappedClasses.contains(c);
	}
	
	void addMappedClass( Class c ) {
		mappedClasses.add(c);
	}
	
	CopyOnWriteArraySet<Class> getMappedClasses() {
		return mappedClasses;
	}

	boolean isCleanNames() {
		return cleanNames;
	}

	boolean isDynamicInstantiation() {
		return dynamicInstantiation;
	}
	
	String getCleanName( String name ) {
		if ( cleanNames ) {
			return PathUtils.createValidName(name);
		} else {
			return name;
		}
	}
	
	Object findEntityByName( List entities, String name ) throws IllegalAccessException {
		for ( int i = 0; i < entities.size(); i++ ) {
			Object entity = (Object) entities.get(i);
			if ( getCleanName(getNodeName(entity)).equals(name) ) {
				return entity;
			}
		}
		return null;
	}
	
	static Field findAnnotatedField( Object obj, Class annotationClass ) {
		for ( Field field : ReflectionUtils.getDeclaredAndInheritedFields(obj.getClass()) ) {
			if ( field.isAnnotationPresent(annotationClass) ) {
				field.setAccessible(true);
				return field;
			}
		}
		return null;
	}
	
	static Field findPathField( Object obj ) {
		return findAnnotatedField(obj, JcrPath.class);
	}
	
	static Field findNameField( Object obj ) {
		return findAnnotatedField(obj, JcrName.class);
	}
	
	static Field findUUIDField( Object obj ) {
		return findAnnotatedField(obj, JcrUUID.class);
	}
	
	static String getNodeName( Object object ) throws IllegalAccessException {
		return (String) findNameField(object).get(object);
	}
	
	static String getNodePath( Object object ) throws IllegalAccessException {
		return (String) findPathField(object).get(object);
	}
	
	static String getNodeUUID( Object object ) throws IllegalAccessException {
		return (String) findUUIDField(object).get(object);
	}
	
	static void setBaseVersionInfo( Object object, String name, Calendar created ) throws IllegalAccessException {
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
	
	static void setNodeName( Object object, String name ) throws IllegalAccessException {
		findNameField(object).set(object, name);
	}
	
	static void setNodePath( Object object, String path ) throws IllegalAccessException {
		findPathField(object).set(object, path);
	}
	
	static void setUUID( Object object, String uuid ) throws IllegalAccessException {
		Field uuidField = findUUIDField(object);
		if ( uuidField != null ) {
			uuidField.set(object, uuid);
		}
	}
	
	Object createInstanceForNode( Class objClass, Node node ) 
			throws RepositoryException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		if ( dynamicInstantiation ) {
			// first we try to locate the class name from node property
			String classNameProperty = "className";
			JcrNode jcrNode = getJcrNodeAnnotation(objClass);
			if ( jcrNode != null && !jcrNode.classNameProperty().equals("none") ) {
				classNameProperty = jcrNode.classNameProperty();
			}
			
			if ( node.hasProperty(classNameProperty) ) {
				String className = node.getProperty(classNameProperty).getString();
				Class c = Class.forName(className);
				if ( isMapped(c) ) {
					return c.newInstance();
				} else {
					throw new JcrMappingException("Trying to instantiate unmapped class: " + c.getName());
				}
			} else {
				// use default class
				return objClass.newInstance();
			}
		} else {
			// use default class
			return objClass.newInstance();
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
	Object fromNode( Class entityClass, Node node, String childNodeFilter, int maxDepth ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		Object obj = createInstanceForNode(entityClass, node);
		mapNodeToClass(obj, node, new NameFilter(childNodeFilter), maxDepth, null, 0);
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
	String updateNode( Node node, Object entity, String childNodeFilter, int maxDepth ) 
			throws RepositoryException, IllegalAccessException, IOException {
		return updateNode(node, entity, entity.getClass(), new NameFilter(childNodeFilter), maxDepth, 0);
	}
	
	String updateNode( Node node, Object obj, Class objClass, NameFilter childNameFilter, int maxDepth, int depth )
			throws RepositoryException, IllegalAccessException, IOException {
		
		// if name is different, then we move the node
		if ( !node.getName().equals(getCleanName(getNodeName(obj))) ) {
			if ( node.getParent().getPath().equals("/") ) {
				// special case: moving a root node
				node.getSession().move(node.getPath(), node.getParent().getPath() + getCleanName(getNodeName(obj)));
			} else {
				node.getSession().move(node.getPath(), node.getParent().getPath() + "/" + getCleanName(getNodeName(obj)));node.getSession().move(node.getPath(), node.getParent().getPath() + "/" + getCleanName(getNodeName(obj)));
			}
			
			// update the object name and path
			setNodeName(obj, node.getName());
			setNodePath(obj, node.getPath());
		}
		
		// map the class name to a property
		JcrNode jcrNode = getJcrNodeAnnotation(objClass);
		if ( jcrNode != null && !jcrNode.classNameProperty().equals("none") ) {
			node.setProperty(jcrNode.classNameProperty(), obj.getClass().getCanonicalName());
		}
		
		for ( Field field : ReflectionUtils.getDeclaredAndInheritedFields(objClass) ) {
			field.setAccessible(true);
		
			if ( field.isAnnotationPresent(JcrProperty.class) ) {
				PropertyMapper.mapFieldToProperty(field, obj, node);
				
			} else if ( field.isAnnotationPresent(JcrSerializedProperty.class) ) {
				PropertyMapper.mapSerializedFieldToProperty(field, obj, node);
				
			} else if ( field.isAnnotationPresent(JcrChildNode.class) 
					&& ( maxDepth < 0 || depth < maxDepth ) ) {
				// child nodes
				ChildNodeMapper.updateChildren(field, obj, node, depth, maxDepth, childNameFilter, this);
				
			} else if ( field.isAnnotationPresent(JcrReference.class) ) {
				// references
				ReferenceMapper.updateReferences(field, obj, node, childNameFilter);
				
			} else if ( field.isAnnotationPresent(JcrFileNode.class) 
					&& ( maxDepth < 0 || depth < maxDepth ) ) {
				// file nodes
				FileNodeMapper.updateFiles(field, obj, node, this, depth, maxDepth, childNameFilter);
			}
		}
		return node.getName();
	}
	
	/**
	 * 
	 * @param parentNode
	 * @param entity
	 * @throws java.lang.Exception
	 */
	Node addNode( Node parentNode, Object entity, String[] mixinTypes ) 
			throws IllegalAccessException, RepositoryException, IOException {
		return addNode(parentNode, entity, mixinTypes, true);
	}
	
	Node addNode( Node parentNode, Object entity, String[] mixinTypes, boolean createNode ) 
			throws IllegalAccessException, RepositoryException, IOException {
		
		// create the child node
		Node node;
		JcrNode jcrNode = getJcrNodeAnnotation(entity.getClass());
		if ( createNode ) {
			// check if we should use a specific node type
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
			// add annotated mixin types
			if ( jcrNode != null && jcrNode.mixinTypes() != null ) {
				for ( String mixinType : jcrNode.mixinTypes() ) {
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
		
		// map the class name to a property
		if ( jcrNode != null && !jcrNode.classNameProperty().equals("none") ) {
			node.setProperty(jcrNode.classNameProperty(), entity.getClass().getCanonicalName());
		}
		
		for ( Field field : ReflectionUtils.getDeclaredAndInheritedFields(entity.getClass()) ) {
			field.setAccessible(true);
			
			if ( field.isAnnotationPresent(JcrProperty.class) ) {
				PropertyMapper.mapFieldToProperty(field, entity, node);
				
			} else if ( field.isAnnotationPresent(JcrSerializedProperty.class) ) {
				PropertyMapper.mapSerializedFieldToProperty(field, entity, node);
				
			} else if ( field.isAnnotationPresent(JcrChildNode.class) ) {
				ChildNodeMapper.addChildren(field, entity, node, this);
				
			} else if ( field.isAnnotationPresent(JcrReference.class) ) {
				ReferenceMapper.addReferences(field, entity, node);
			
			} else if ( field.isAnnotationPresent(JcrFileNode.class) ) {
				FileNodeMapper.addFiles(field, entity, node, this);
			}
		}
		return node;
	}
	
	private boolean isVersionable( Node node ) throws RepositoryException {
		for ( NodeType mixinType : node.getMixinNodeTypes() ) {
			if ( mixinType.getName().equals("mix:versionable") ) {
				return true;
			}
		}
		return false;
	}
	
	static JcrNode getJcrNodeAnnotation( Class c ) {
		
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
		
	void mapNodeToClass( Object obj, Node node, NameFilter nameFilter, int maxDepth, Object parentObject, int depth ) 
			throws ClassNotFoundException, InstantiationException, RepositoryException, IllegalAccessException, IOException {
		
		if ( !ReflectionUtils.extendsClass(obj.getClass(), JcrFile.class) ) {
			// this does not apply for JcrFile extensions
			setNodeName(obj, node.getName());
		}
		
		for ( Field field : ReflectionUtils.getDeclaredAndInheritedFields(obj.getClass()) ) {
			field.setAccessible(true);
			
			if ( field.isAnnotationPresent(JcrProperty.class) ) {
				PropertyMapper.mapPropertyToField(obj, field, node);
				
			} else if ( field.isAnnotationPresent(JcrSerializedProperty.class) ) {
				PropertyMapper.mapSerializedPropertyToField(obj, field, node);
				
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
					field.set(obj, PropertyMapper.getValue(field.getType(), node.getSession().getValueFactory().createValue(node.getBaseVersion().getCreated())));
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
					field.set(obj, PropertyMapper.getValue(field.getType(), node.getSession().getValueFactory().createValue(version.getCreated())));
				} else if ( isVersionable(node) ) {
					// if we're not browsing version history, then this must be the base version
					field.set(obj, PropertyMapper.getValue(field.getType(), node.getSession().getValueFactory().createValue(node.getBaseVersion().getCreated())));
				}
				
			} else if ( field.isAnnotationPresent(JcrCheckedout.class) ) {
				field.set(obj, node.isCheckedOut());
				
			} else if ( field.isAnnotationPresent(JcrCreated.class) ) {
				if ( node.hasProperty("jcr:created") ) {
					field.set(obj, PropertyMapper.getValue(field.getType(), node.getProperty("jcr:created").getValue()));
				}
			
			} else if ( field.isAnnotationPresent(JcrParentNode.class) ) {
				if ( parentObject != null ) {
					field.set(obj, parentObject);
				}
				
			} else if ( field.isAnnotationPresent(JcrChildNode.class) 
					&& ( maxDepth < 0 || depth < maxDepth ) ) {
				ChildNodeMapper.getChildrenFromNode(field, node, obj, depth, maxDepth, nameFilter, this);
				
			} else if ( field.isAnnotationPresent(JcrReference.class) ) {
				ReferenceMapper.getReferencesFromNode(field, node, obj, depth, maxDepth, nameFilter, this);
				
			} else if ( field.isAnnotationPresent(JcrFileNode.class) 
					&& ( maxDepth < 0 || depth < maxDepth ) ) {
				FileNodeMapper.getFilesFromNode(field, node, obj, depth, maxDepth, nameFilter, this);
			
			} else if ( field.getName().equals("path") ) {
				field.set(obj, node.getPath());
			
			}
		}
	}
	
	static byte[] readBytes( InputStream in ) throws IOException {
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
	
}
