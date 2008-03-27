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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jcrom.annotations.JcrBaseVersionCreated;
import org.jcrom.annotations.JcrBaseVersionName;
import org.jcrom.annotations.JcrCheckedout;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrParentNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrReference;
import org.jcrom.annotations.JcrSerializedProperty;
import org.jcrom.annotations.JcrUUID;
import org.jcrom.annotations.JcrVersionCreated;
import org.jcrom.annotations.JcrVersionName;
import org.jcrom.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by Jcrom to validate that the classes being mapped
 * are correctly annotated as valid JCR classes.
 * 
 * @author Olafur Gauti Gudmundsson
 */
class Validator {
	
	private static final Logger logger = LoggerFactory.getLogger(Validator.class);

	/**
	 * Takes a class, validates it, and adds it to a Set
	 * which is then returned. All annotated classes referenced from
	 * this class (e.g. child nodes) are also validated and added to the set.
	 * Throws a JcrMappingException if invalid mapping is found.
	 * 
	 * @param c the Class to be validated
	 * @return a Set of the input class and referenced classes, validated
	 * and ready for mapping
	 */
	static Set validate( Class c ) {
		Set<Class> validClasses = new HashSet<Class>();
		validateInternal(c, validClasses);
		return validClasses;
	}
	
	private static void validateInternal( Class c, Set<Class> validClasses ) {
		if ( !validClasses.contains(c) ) {
			if ( logger.isDebugEnabled() ) {
				logger.debug("Processing class: " + c.getName());
			}

			validClasses.add(c);
			validateFields(c, ReflectionUtils.getDeclaredAndInheritedFields(c), validClasses);
		}
	}
	
	
	private static void validateFields( Class c, Field[] fields, Set<Class> validClasses ) {
		boolean foundNameField = false;
		boolean foundPathField = false;
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
				
			} else if ( field.isAnnotationPresent(JcrSerializedProperty.class) ) {
				// make sure field is Serializable
				if ( !ReflectionUtils.implementsInterface(field.getType(), Serializable.class) ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrSerializedProperty does not implement java.io.Serializable (type is " + field.getType().getName() + ").");
				}
				
			} else if ( field.isAnnotationPresent(JcrName.class) ) {
				// make sure this is a String field
				if ( field.getType() != String.class ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrName must be of type java.lang.String, but is of type: " + field.getType().getName());
				}
				foundNameField = true;
			
			} else if ( field.isAnnotationPresent(JcrUUID.class) ) {
				// make sure this is a String field
				if ( field.getType() != String.class ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrUUID must be of type java.lang.String, but is of type: " + field.getType().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrPath.class) ) {
				// make sure this is a String field
				if ( field.getType() != String.class ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrPath must be of type java.lang.String, but is of type: " + field.getType().getName());
				}
				foundPathField = true;
				
			} else if ( field.isAnnotationPresent(JcrBaseVersionName.class) ) {
				// make sure this is a String field
				if ( field.getType() != String.class ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrBaseVersionName must be of type java.lang.String, but is of type: " + field.getType().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrBaseVersionCreated.class) ) {
				// make sure this is a Date/Calendar/Timestamp field
				if ( !ReflectionUtils.isDateType(field.getType()) ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrBaseVersionCreated must be of type java.util.Date / java.util.Calendar / java.sql.Timestamp, but is of type: " + field.getType().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrVersionName.class) ) {
				// make sure this is a String field
				if ( field.getType() != String.class ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrVersionName must be of type java.lang.String, but is of type: " + field.getType().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrVersionCreated.class) ) {
				// make sure this is a Date/Calendar/Timestamp field
				if ( !ReflectionUtils.isDateType(field.getType()) ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrVersionCreated must be of type java.util.Date / java.util.Calendar / java.sql.Timestamp, but is of type: " + field.getType().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrCheckedout.class) ) {
				// make sure this i a boolean field
				if ( field.getType() != boolean.class && field.getType() != Boolean.class ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrCheckedout must be of type boolean, but is of type: " + field.getType().getName());
				}
				
			} else if ( field.isAnnotationPresent(JcrParentNode.class) ) {
				// make sure that the parent node type is a valid JCR class
				validateInternal(field.getType(), validClasses);
				
			} else if ( field.isAnnotationPresent(JcrChildNode.class) ) {
				// make sure that the child node type are valid JCR classes
				if ( ReflectionUtils.implementsInterface(field.getType(), List.class) ) {
					Class paramClass = ReflectionUtils.getParameterizedClass(field);
					if ( paramClass != null ) {
						validateInternal(ReflectionUtils.getParameterizedClass(field), validClasses);
					} else {
						throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrChildNode is a java.util.List that is not parameterised with a valid class type.");
					}
				} else if ( ReflectionUtils.implementsInterface(field.getType(), Map.class) ) {
					// special case, mapping a Map to a child node, so we must
					// make sure that it is properly parameterized:
					// first parameter must be a String
					Class keyParamClass = ReflectionUtils.getParameterizedClass(field, 0);
					if ( keyParamClass == null || keyParamClass != String.class ) {
						throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrChildNode is a java.util.Map that is not parameterised with a java.lang.String key type.");
					}
					// the value class must be a valid property type, or an array
					// of valid property types
					Class valueParamClass = ReflectionUtils.getParameterizedClass(field, 1);
					if ( valueParamClass == null || !ReflectionUtils.isValidMapValueType(valueParamClass) ) {
						throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrChildNode is a java.util.Map that is not parameterised with a valid value property type.");
					}
				} else {
					validateInternal(field.getType(), validClasses);
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
				
			} else if ( field.isAnnotationPresent(JcrReference.class) ) {
				// make sure the class has a @JcrUUID
				boolean foundUUID = false;
				for ( Field refField : ReflectionUtils.getDeclaredAndInheritedFields(field.getType()) ) {
					if ( refField.isAnnotationPresent(JcrUUID.class) ) {
						foundUUID = true;
					}
				}
				if ( !foundUUID ) {
					throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrReference is of type that has no @JcrUUID: " + field.getType().getName());
				}
				// validate the class
				validateInternal(field.getType(), validClasses);
			}
		}
		if ( !foundNameField ) {
			throw new JcrMappingException("In [" + c.getName() + "]: No field is annotated with @JcrName.");
		}
		if ( !foundPathField ) {
			throw new JcrMappingException("In [" + c.getName() + "]: No field is annotated with @JcrPath.");
		}
	}
}
