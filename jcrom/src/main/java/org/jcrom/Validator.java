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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrParentNode;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrUUID;
import org.jcrom.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by Jcrom to validate that the classes being mapped
 * are valid JcrEntity implementations, and are correctly annotated.
 * 
 * @author Olafur Gauti Gudmundsson
 */
class Validator {
	
	private static final Logger logger = LoggerFactory.getLogger(Validator.class);

	/**
	 * Takes a class that extends JcrEntity, validates it, and adds it to a Set
	 * which is then returned. All JcrEntity implementations referenced from
	 * this class (e.g. child nodes) are also validated and added to the set.
	 * Throws a JcrMappingException if invalid mapping is found.
	 * 
	 * @param c the Class to be validated
	 * @return a Set of the input class and referenced classes, validated
	 * and ready for mapping
	 */
	static Set<Class<? extends JcrEntity>> validate( Class<? extends JcrEntity> c ) {
		Set<Class<? extends JcrEntity>> validClasses = new HashSet<Class<? extends JcrEntity>>();
		validateInternal(c, validClasses);
		return validClasses;
	}
	
	private static void validateInternal( Class<? extends JcrEntity> c, Set<Class<? extends JcrEntity>> validClasses ) {
		if ( validClasses.contains(c) ) {
			return;
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug("Processing class: " + c.getName());
		}
		
		validateFields(c, ReflectionUtils.getDeclaredAndInheritedFields(c), validClasses);
		validClasses.add(c);
	}

	
	private static boolean implementsJcrEntity( Class<?> type ) {
		return ReflectionUtils.implementsInterface(type, JcrEntity.class);
	}
	
	
	private static void validateFields( Class c, Field[] fields, Set<Class<? extends JcrEntity>> validClasses ) {
		for ( Field field : fields ) {
			field.setAccessible(true);
			if ( logger.isDebugEnabled() ) {
				logger.debug("In [" + c.getName() + "]: Processing field: " + field.getName());
			}
			
			if ( field.isAnnotationPresent(JcrProperty.class) ) {
				// make sure that the property type is supported
				if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
					if ( !ReflectionUtils.isFieldParameterizedWithPropertyType(field) ) {
						throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is a List annotated as @JcrProperty is not parameterized with a property type.");
					}
					
				} else if ( !ReflectionUtils.isPropertyType(field.getType()) ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrProperty is not a valid JCR property (type is " + field.getType().getName() + ").");
				}
			} else if ( field.isAnnotationPresent(JcrUUID.class) ) {
				// make sure this is a String field
				if ( field.getType() != String.class ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrUUID must be of type java.lang.String, but is of type: " + field.getType().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrParentNode.class) ) {
				// make sure that the parent node type implements JcrEntity
				if ( !implementsJcrEntity(field.getType()) ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrParentNode is of type that does not implement JcrEntity: " + field.getType().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrChildNode.class) ) {
				// make sure that the child node type implements JcrEntity
				if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
					if ( !ReflectionUtils.isFieldParameterizedWithClass(field, JcrEntity.class) ) {
						throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is a List annotated as @JcrChildNode is not parameterized with a JcrEntity implementation.");
					}
					validateInternal(ReflectionUtils.getParameterizedClass(field), validClasses);
				} else { 
					if ( !implementsJcrEntity(field.getType()) ) {
						throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrChildNode is of type that does not implement JcrEntity: " + field.getType().getName());
					}
					validateInternal((Class)field.getType(), validClasses);
				}
				
			} else if ( field.isAnnotationPresent(JcrFileNode.class) ) {
				// make sure that the file node type is a JcrFile
				if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
					if ( !ReflectionUtils.extendsClass(ReflectionUtils.getParameterizedClass(field), JcrFile.class) ) {
						throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is a List annotated as @JcrFileNode is not parameterized with a JcrFile implementation.");
					}
				} else {
					if ( !ReflectionUtils.extendsClass(field.getType(), JcrFile.class) ) {
						throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrFileNode is of type that does not extend JcrFile: " + field.getType().getName());
					}
				}
			}
		}
	}
}
