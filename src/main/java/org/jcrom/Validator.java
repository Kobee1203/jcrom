/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2015 - All rights reserved.
 * Authors: Olafur Gauti Gudmundsson, Nicolas Dos Santos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcrom.annotations.JcrBaseVersionCreated;
import org.jcrom.annotations.JcrBaseVersionName;
import org.jcrom.annotations.JcrCheckedout;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrCreated;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrIdentifier;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrParentNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrProtectedProperty;
import org.jcrom.annotations.JcrReference;
import org.jcrom.annotations.JcrSerializedProperty;
import org.jcrom.annotations.JcrUUID;
import org.jcrom.annotations.JcrVersionCreated;
import org.jcrom.annotations.JcrVersionName;
import org.jcrom.converter.Converter;
import org.jcrom.converter.DefaultConverter;
import org.jcrom.type.TypeHandler;
import org.jcrom.util.ReflectionUtils;

/**
 * This class is used by Jcrom to validate that the classes being mapped are correctly annotated as valid JCR classes.
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
class Validator {

    private static final Logger logger = Logger.getLogger(Validator.class.getName());

    private final TypeHandler typeHandler;

    private final Jcrom jcrom;

    public Validator(TypeHandler typeHandler, Jcrom jcrom) {
        this.typeHandler = typeHandler;
        this.jcrom = jcrom;
    }

    /**
     * Takes a class, validates it, and adds it to a Set which is then returned. All annotated classes referenced from
     * this class (e.g. child nodes) are also validated and added to the set. Throws a JcrMappingException if invalid
     * mapping is found.
     * 
     * @param c the Class to be validated
     * @param dynamicInstantiation when dynamic instantiation is on, we allow interfaces
     * @return a Set of the input class and referenced classes, validated and ready for mapping
     */
    Set<Class<?>> validate(Class<?> c, boolean dynamicInstantiation) {
        Set<Class<?>> validClasses = new HashSet<Class<?>>();
        validateInternal(c, validClasses, dynamicInstantiation);
        return validClasses;
    }

    private void validateInternal(Class<?> c, Set<Class<?>> validClasses, boolean dynamicInstantiation) {
        if (!validClasses.contains(c)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.finer("Processing class: " + c.getName());
            }

            validClasses.add(c);

            // when dynamic instantiation is turned on, we ignore interfaces
            if (!(c.isInterface() && dynamicInstantiation)) {
                validateFields(c, ReflectionUtils.getDeclaredAndInheritedFields(c, true), validClasses, dynamicInstantiation);
            }
        }
    }

    private void validateFields(Class<?> c, Field[] fields, Set<Class<?>> validClasses, boolean dynamicInstantiation) {
        boolean foundNameField = false;
        boolean foundPathField = false;
        for (Field field : fields) {
            field.setAccessible(true);
            if (logger.isLoggable(Level.FINE)) {
                logger.finer("In [" + c.getName() + "]: Processing field: " + field.getName());
            }

            Type genericType = field.getGenericType();
            Class<?> type = typeHandler.getType(field.getType(), genericType, null);

            if (jcrom.getAnnotationReader().isAnnotationPresent(field, JcrProperty.class)) {
                // Check whether there is a Converter defined (DefaultConverter.class means no Converter defined)
                Class<? extends Converter<?, ?>> converterClass = jcrom.getAnnotationReader().getAnnotation(field, JcrProperty.class).converter();
                if (!DefaultConverter.class.equals(converterClass)) {
                    type = ReflectionUtils.getParameterizedClass(converterClass.getGenericInterfaces()[0], 1);
                    genericType = ReflectionUtils.getConverterGenericType(converterClass, 1);
                }

                // make sure that the property type is supported
                if (typeHandler.isList(type)) {
                    if (!ReflectionUtils.isTypeParameterizedWithPropertyType(genericType, typeHandler)) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is a List annotated as @JcrProperty is not parameterized with a property type.");
                    }

                } else if (typeHandler.isMap(type)) {
                    // special case, mapping a Map of properties, so we must make sure that it is properly parameterized:
                    // first parameter must be a String
                    Class<?> keyParamClass = ReflectionUtils.getParameterizedClass(genericType, 0);
                    if (keyParamClass == null || keyParamClass != String.class) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrProperty is a java.util.Map that is not parameterised with a java.lang.String key type.");
                    }
                    // the value class must be a valid property type, or an array of valid property types
                    Class<?> valueParamClass = ReflectionUtils.getParameterizedClass(genericType, 1);
                    if (valueParamClass == null || !typeHandler.isValidMapValueType(valueParamClass)) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrProperty is a java.util.Map that is not parameterised with a valid value property type.");
                    }

                } else if (!typeHandler.isPropertyType(type)) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrProperty is not a valid JCR property (type is " + type.getName() + ").");
                }
            } else if (field.isAnnotationPresent(JcrProtectedProperty.class)) {
                if (!typeHandler.isPropertyType(type)) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrProtectedProperty is not a valid JCR property (type is " + type.getName() + ").");
                }
            } else if (field.isAnnotationPresent(JcrSerializedProperty.class)) {
                // make sure field is Serializable
                if (!Serializable.class.isAssignableFrom(type)) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrSerializedProperty does not implement java.io.Serializable (type is " + type.getName() + ").");
                }
            } else if (field.isAnnotationPresent(JcrName.class)) {
                // make sure this is a String field
                if (!typeHandler.isString(type)) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrName must be of type java.lang.String, but is of type: " + type.getName());
                }
                foundNameField = true;
            } else if (field.isAnnotationPresent(JcrUUID.class)) {
                // make sure this is a String field
                if (type != String.class) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrUUID must be of type java.lang.String, but is of type: " + type.getName());
                }
            } else if (field.isAnnotationPresent(JcrIdentifier.class)) {
                // make sure this is a String field
                if (type != String.class) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrIdentifier must be of type java.lang.String, but is of type: " + type.getName());
                }
            } else if (field.isAnnotationPresent(JcrPath.class)) {
                // make sure this is a String field
                if (!typeHandler.isString(type)) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrPath must be of type java.lang.String, but is of type: " + type.getName());
                }
                foundPathField = true;
            } else if (field.isAnnotationPresent(JcrBaseVersionName.class)) {
                // make sure this is a String field
                if (type != String.class) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrBaseVersionName must be of type java.lang.String, but is of type: " + type.getName());
                }
            } else if (field.isAnnotationPresent(JcrBaseVersionCreated.class)) {
                // make sure this is a Date/Calendar/Timestamp field
                if (!typeHandler.isDateType(type)) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrBaseVersionCreated must be of type java.util.Date / java.util.Calendar / java.sql.Timestamp, but is of type: " + type.getName());
                }
            } else if (field.isAnnotationPresent(JcrVersionName.class)) {
                // make sure this is a String field
                if (type != String.class) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrVersionName must be of type java.lang.String, but is of type: " + type.getName());
                }
            } else if (field.isAnnotationPresent(JcrVersionCreated.class)) {
                // make sure this is a Date/Calendar/Timestamp field
                if (!typeHandler.isDateType(type)) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrVersionCreated must be of type java.util.Date / java.util.Calendar / java.sql.Timestamp, but is of type: " + type.getName());
                }
            } else if (field.isAnnotationPresent(JcrCheckedout.class)) {
                // make sure this i a boolean field
                if (type != boolean.class && type != Boolean.class) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrCheckedout must be of type boolean, but is of type: " + type.getName());
                }
            } else if (field.isAnnotationPresent(JcrCreated.class)) {
                // make sure this is a Date/Calendar/Timestamp field
                if (!typeHandler.isDateType(type)) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrCreated must be of type java.util.Date / java.util.Calendar / java.sql.Timestamp, but is of type: " + type.getName());
                }
            } else if (field.isAnnotationPresent(JcrParentNode.class)) {
                // make sure that the parent node type is a valid JCR class
                validateInternal(type, validClasses, dynamicInstantiation);
            } else if (field.isAnnotationPresent(JcrChildNode.class)) {
                // make sure that the child node type are valid JCR classes
                if (typeHandler.isList(type)) {
                    // map a List of child nodes, here we must make sure that the List is parameterized
                    Class<?> paramClass = ReflectionUtils.getParameterizedClass(genericType);
                    if (paramClass != null) {
                        validateInternal(paramClass, validClasses, dynamicInstantiation);
                    } else {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrChildNode is a java.util.List that is not parameterised with a valid class type.");
                    }
                } else if (typeHandler.isMap(type)) {
                    // special case, mapping a Map of child nodes, so we must make sure that it is properly parameterized:
                    // first parameter must be a String
                    Class<?> keyParamClass = ReflectionUtils.getParameterizedClass(genericType, 0);
                    if (keyParamClass == null || keyParamClass != String.class) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrChildNode is a java.util.Map that is not parameterised with a java.lang.String key type.");
                    }
                    // the value class must be Object, or List of Objects
                    Class<?> valueParamClass = ReflectionUtils.getParameterizedClass(genericType, 1);
                    Class<?> valueParamParamClass = ReflectionUtils.getTypeArgumentOfParameterizedClass(genericType, 1, 0);
                    if (valueParamClass == null || (valueParamClass != Object.class && !(List.class.isAssignableFrom(valueParamClass) && (valueParamParamClass != null && valueParamParamClass == Object.class)))) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrChildNode is a java.util.Map that is not parameterised with a valid value type (Object or List<Object>).");
                    }
                } else {
                    validateInternal(type, validClasses, dynamicInstantiation);
                }

            } else if (field.isAnnotationPresent(JcrFileNode.class)) {
                // make sure that the file node type is a JcrFile
                if (typeHandler.isList(type)) {
                    if (!JcrFile.class.isAssignableFrom(ReflectionUtils.getParameterizedClass(genericType))) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is a List annotated as @JcrFileNode is not parameterized with a JcrFile implementation.");
                    }
                } else if (typeHandler.isMap(type)) {
                    // special case, mapping a Map of file nodes, so we must make sure that it is properly parameterized:
                    // first parameter must be a String
                    Class<?> keyParamClass = ReflectionUtils.getParameterizedClass(genericType, 0);
                    if (keyParamClass == null || keyParamClass != String.class) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrFileNode is a java.util.Map that is not parameterised with a java.lang.String key type.");
                    }
                    // the value class must be JcrFile extension, or List of JcrFile extensions
                    Class<?> valueParamClass = ReflectionUtils.getParameterizedClass(genericType, 1);
                    Class<?> valueParamParamClass = ReflectionUtils.getTypeArgumentOfParameterizedClass(genericType, 1, 0);
                    if (valueParamClass == null || (!JcrFile.class.isAssignableFrom(valueParamClass) && !(List.class.isAssignableFrom(valueParamClass) && (valueParamParamClass != null && JcrFile.class.isAssignableFrom(valueParamParamClass))))) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrFileNode is a java.util.Map that is not parameterised with a valid value type (JcrFile or List<JcrFile>).");
                    }
                } else if (!JcrFile.class.isAssignableFrom(type)) {
                    throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrFileNode is of type that does not extend JcrFile: " + type.getName());
                }
            } else if (field.isAnnotationPresent(JcrReference.class)) {
                Class<?> fieldType;
                if (typeHandler.isList(type)) {
                    fieldType = ReflectionUtils.getParameterizedClass(genericType);

                } else if (typeHandler.isMap(type)) {
                    // special case, mapping a Map of references, so we must make sure that it is properly parameterized:
                    // first parameter must be a String
                    Class<?> keyParamClass = ReflectionUtils.getParameterizedClass(genericType, 0);
                    if (keyParamClass == null || keyParamClass != String.class) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrReference is a java.util.Map that is not parameterised with a java.lang.String key type.");
                    }
                    // the value class must be Object, or List of Objects
                    Class<?> valueParamClass = ReflectionUtils.getParameterizedClass(genericType, 1);
                    Class<?> valueParamParamClass = ReflectionUtils.getTypeArgumentOfParameterizedClass(genericType, 1, 0);
                    if (valueParamClass == null || (!Object.class.isAssignableFrom(valueParamClass) && !(List.class.isAssignableFrom(valueParamClass) && (valueParamParamClass != null && Object.class.isAssignableFrom(valueParamParamClass))))) {
                        throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrReference is a java.util.Map that is not parameterised with a valid value type (Object or List<Object>).");
                    }

                    if(List.class.isAssignableFrom(valueParamClass) && valueParamParamClass != Object.class) {
                    	fieldType = valueParamParamClass;	
                    } else if(valueParamClass != Object.class) {
                    	fieldType = valueParamClass;
                    } else {
                    	fieldType = null;	
                    }                    
                } else {
                    fieldType = type;
                }

                if (fieldType != null) {
                    JcrReference jcrReference = jcrom.getAnnotationReader().getAnnotation(field, JcrReference.class);
                    // when dynamic instantiation is turned on, we ignore interfaces
                    if (!jcrReference.byPath() && !(fieldType.isInterface() && dynamicInstantiation)) {
                        // make sure the class has a @JcrUUID
                        boolean foundUUID = false;
                        boolean foundId = false;
                        for (Field refField : ReflectionUtils.getDeclaredAndInheritedFields(fieldType, true)) {
                            if (refField.isAnnotationPresent(JcrUUID.class)) {
                                foundUUID = true;
                            }
                            if (refField.isAnnotationPresent(JcrIdentifier.class)) {
                                foundId = true;
                            }
                        }
                        if (!foundUUID && !foundId) {
                            throw new JcrMappingException("In [" + c.getName() + "]: Field [" + field.getName() + "] which is annotated as @JcrReference is of type that has no @JcrUUID or @JcrIdentifier: " + type.getName());
                        }
                    }
                    // validate the class
                    validateInternal(fieldType, validClasses, dynamicInstantiation);
                }
            }
        }
        if (!foundNameField) {
            throw new JcrMappingException("In [" + c.getName() + "]: No field is annotated with @JcrName.");
        }
        if (!foundPathField) {
            throw new JcrMappingException("In [" + c.getName() + "]: No field is annotated with @JcrPath.");
        }
    }
}
