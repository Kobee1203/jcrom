/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2014 - All rights reserved.
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
package org.jcrom.util;

import java.util.Locale;
import java.util.StringTokenizer;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

import org.jcrom.JcrMappingException;

/**
 * Contains utilities used for JCR nodes
 * 
 * @author Nicolas Dos Santos
 */
public final class JcrUtils {

    private JcrUtils() {
    }

    /**
     * Checks whether the given mixin node type is in effect for the given node.
     * 
     * @param node the node
     * @param mixinType the mixin node type
     * @return <code>true</code> when the misin node type is present, <code>false</code> instead.
     * @throws RepositoryException
     */
    public static boolean hasMixinType(Node node, String mixinType) throws RepositoryException {
        for (NodeType nodeType : node.getMixinNodeTypes()) {
            if (nodeType.getName().equals(mixinType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the LockManager object from the given session.
     * @param session {@link Session}
     * @return a {@link LockManager} object
     * @throws RepositoryException
     */
    public static LockManager getLockManager(Session session) throws RepositoryException {
        LockManager lockMgr = session.getWorkspace().getLockManager();
        return lockMgr;
    }

    /**
     * Returns the VersionManager object from the given session.
     * 
     * @param session {@link Session}
     * @return a {@link VersionManager} object
     * @throws RepositoryException
     */
    public static VersionManager getVersionManager(Session session) throws RepositoryException {
        VersionManager versionMgr = session.getWorkspace().getVersionManager();
        return versionMgr;
    }

    /**
     * Sets the given node to checked-out status.
     * @param node node to check-out
     * @throws RepositoryException
     */
    public static void checkout(Node node) throws RepositoryException {
        getVersionManager(node.getSession()).checkout(node.getPath());
    }

    /**
     * Creates for the given node a new version and returns that version. 
     * Put the node into the checked-in state.
     * 
     * @param node node to checkin
     * @return the created version
     * @throws RepositoryException
     */
    public static Version checkin(Node node) throws RepositoryException {
        return getVersionManager(node.getSession()).checkin(node.getPath());
    }

    public static void checkinRecursively(Node node) {
        try {
            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                checkinRecursively(it.nextNode());
            }
            if (node.isCheckedOut() && node.isNodeType(NodeType.MIX_VERSIONABLE)) {
                //node.checkin();
                checkin(node);
            }

        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not perform check-in", e);
        }
    }

    public static void checkoutRecursively(Node node) {
        try {
            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                checkoutRecursively(it.nextNode());
            }
            if (!node.isCheckedOut() && node.isNodeType(NodeType.MIX_VERSIONABLE)) {
                //node.checkout();
                checkout(node);
            }

        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not perform check-out", e);
        }
    }

    /**
     * Places a lock on the given node.
     * 
     * @param node the node to be locked
     * @param isDeep if <code>true</code> this lock will apply to this node and all its descendants; if <code>false</code>, it applies only to this node.
     * @param isSessionScoped if <code>true</code>, this lock expires with the current session; if <code>false</code> it expires when explicitly or automatically unlocked for some other reason.
     * @param timeoutHint desired lock timeout in seconds (servers are free to ignore this value); specify Long.MAX_VALUE for no timeout.
     * @param ownerInfo a string containing owner information supplied by the client; servers are free to ignore this value.
     * @see javax.jcr.lock.LockManager#lock(String, boolean, boolean, long, String)
     */
    public static void lock(Node node, boolean isDeep, boolean isSessionScoped, long timeoutHint, String ownerInfo) {
        try {
            getLockManager(node.getSession()).lock(node.getPath(), isDeep, isSessionScoped, timeoutHint, ownerInfo);
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not perform lock", e);
        }
    }

    /**
     * Removes the lock on the given node.
     * 
     * @param node the node unlock
     * @see javax.jcr.lock.LockManager#unlock(String)
     */
    public static void unlock(Node node) {
        try {
            getLockManager(node.getSession()).unlock(node.getPath());
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not perform unlock", e);
        }
    }

    /**
     * Returns a {@link Value} object from the given class and the specified value.
     * 
     * @param c {@link Class} used to create the {@link Value} object with the correct {@link PropertyType}
     * @param value the value
     * @param valueFactory {@link ValueFactory} used to invoke the methods to create {@link Value}
     * @return a {@link Value} object
     * @throws RepositoryException
     */
    /*
    public static Value createValue(Class<?> c, Object value, ValueFactory valueFactory) throws RepositoryException {
        if (Property.class.isAssignableFrom(c)) {
            Object wrappedValue = ((Property) value).getValue();
            if (wrappedValue == null) {
                return null;
            } else {
                return createValue(wrappedValue.getClass(), wrappedValue, valueFactory);
            }
        } else if (c == String.class) {
            return valueFactory.createValue((String) value);
        } else if (c == Date.class) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) value);
            return valueFactory.createValue(cal);
        } else if (c == Timestamp.class) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(((Timestamp) value).getTime());
            return valueFactory.createValue(cal);
        } else if (c == Calendar.class) {
            return valueFactory.createValue((Calendar) value);
        } else if (c == InputStream.class) {
            //return valueFactory.createValue((InputStream) fieldValue);
            Binary binary = valueFactory.createBinary((InputStream) value);
            return valueFactory.createValue(binary);
        } else if (c.isArray() && c.getComponentType() == byte.class) {
            //return valueFactory.createValue(new ByteArrayInputStream((byte[]) fieldValue));
            Binary binary = valueFactory.createBinary(new ByteArrayInputStream((byte[]) value));
            return valueFactory.createValue(binary);
        } else if (c == Integer.class || c == int.class) {
            return valueFactory.createValue((Integer) value);
        } else if (c == Long.class || c == long.class) {
            return valueFactory.createValue((Long) value);
        } else if (c == Double.class || c == double.class) {
            return valueFactory.createValue((Double) value);
        } else if (c == Boolean.class || c == boolean.class) {
            return valueFactory.createValue((Boolean) value);
        } else if (c == Locale.class) {
            return valueFactory.createValue(((Locale) value).toString());
        } else if (c.isEnum()) {
            return valueFactory.createValue(((Enum<?>) value).name());
        }
        return null;
    }

    public static Object getValue(Class<?> type, Type genericType, Value[] values) throws RepositoryException, IOException {
        if (ReflectionUtils.implementsInterface(type, List.class)) {
            // multi-value property (List)
            List<Object> properties = new ArrayList<Object>();
            Class<?> paramClass = ReflectionUtils.getParameterizedClass(genericType);
            for (Value v : p.getValues()) {
                properties.add(JcrUtils.getValue(paramClass, v));
            }
            fieldValue = properties;
        } else if (ListProperty.class.isAssignableFrom(type)) {
            ListProperty<Object> list = (ListProperty) field.get(obj);
            List<Object> properties = new ArrayList<Object>();
            Class<?> paramClass = ReflectionUtils.getParameterizedClass(genericType);
            for (Value v : p.getValues()) {
                properties.add(JcrUtils.getValue(paramClass, genericType, v));
            }
            list.setAll(properties);
            fieldValue = list;
        } else if (type.isArray()) {
            // multi-value property (array)
            Value[] values = p.getValues();
            if (type.getComponentType() == byte.class) {
                // byte array...we need to read from the stream
                //fieldValue = Mapper.readBytes(value.getStream());
                fieldValue = IOUtils.toByteArray(value.getBinary().getStream());
            } else if (type.getComponentType() == int.class) {
                int[] arr = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = (int) values[i].getDouble();
                }
                fieldValue = arr;
            } else if (type.getComponentType() == long.class) {
                long[] arr = new long[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = values[i].getLong();
                }
                fieldValue = arr;
            } else if (type.getComponentType() == double.class) {
                double[] arr = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = values[i].getDouble();
                }
                fieldValue = arr;
            } else if (type.getComponentType() == boolean.class) {
                boolean[] arr = new boolean[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = values[i].getBoolean();
                }
                fieldValue = arr;
            } else if (type.getComponentType() == Locale.class) {
                Locale[] arr = new Locale[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = JcrUtils.parseLocale(values[i].getString());
                }
                fieldValue = arr;
            } else {
                Object[] arr = valuesToArray(type.getComponentType(), values);
                fieldValue = arr;
            }
        } else {

        }
    }
    */

    /**
     * Returns a representation of the given {@link Value} by using the given {@link Class}.
     * 
     * @param type {@link Class} used to return the representation with correct type
     * @param value {@link Value} object
     * @return a representation of {@link Value}.
     * @throws RepositoryException
     * @throws IOException
     */
    /*
    public static Object getValue(Class<?> type, Type genericType, Value value) throws RepositoryException, IOException {
        Object fieldValue = null;

        if (ReflectionUtils.implementsInterface(type, List.class)) {
            // multi-value property (List)
            List<Object> properties = new ArrayList<Object>();
            Class<?> paramClass = ReflectionUtils.getParameterizedClass(genericType);
            for (Value v : p.getValues()) {
                properties.add(JcrUtils.getValue(paramClass, v));
            }
            fieldValue = properties;
        } else if (ListProperty.class.isAssignableFrom(type)) {
            ListProperty<Object> list = (ListProperty) field.get(obj);
            List<Object> properties = new ArrayList<Object>();
            Class<?> paramClass = ReflectionUtils.getParameterizedClass(genericType);
            for (Value v : p.getValues()) {
                properties.add(JcrUtils.getValue(paramClass, v));
            }
            list.setAll(properties);
            fieldValue = list;
        } else if (ObjectProperty.class.isAssignableFrom(type)) {
            fieldValue = JcrUtils.getValue(ReflectionUtils.getObjectPropertyGeneric(obj, type, genericType), value);
        } else if (type == String.class || StringProperty.class.isAssignableFrom(type)) {
            fieldValue = value.getString();
        } else if (type == Date.class) {
            fieldValue = value.getDate().getTime();
        } else if (type == Timestamp.class) {
            fieldValue = new Timestamp(value.getDate().getTimeInMillis());
        } else if (type == Calendar.class) {
            fieldValue = value.getDate();
        } else if (type == InputStream.class) {
            //fieldValue = value.getStream();
            fieldValue = value.getBinary().getStream();
        } else if (c.isArray() && c.getComponentType() == byte.class) {
            // byte array...we need to read from the stream
            //fieldValue = Mapper.readBytes(value.getStream());
            fieldValue = IOUtils.toByteArray(value.getBinary().getStream());
        } else if (type.isArray()) {
            // multi-value property (array)
            Value[] values = p.getValues();
            if (type.getComponentType() == int.class) {
                int[] arr = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = (int) values[i].getDouble();
                }
                fieldValue = arr;
            } else if (type.getComponentType() == long.class) {
                long[] arr = new long[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = values[i].getLong();
                }
                fieldValue = arr;
            } else if (type.getComponentType() == double.class) {
                double[] arr = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = values[i].getDouble();
                }
                fieldValue = arr;
            } else if (type.getComponentType() == boolean.class) {
                boolean[] arr = new boolean[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = values[i].getBoolean();
                }
                fieldValue = arr;
            } else if (type.getComponentType() == Locale.class) {
                Locale[] arr = new Locale[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = JcrUtils.parseLocale(values[i].getString());
                }
                fieldValue = arr;
            } else {
                Object[] arr = valuesToArray(type.getComponentType(), values);
                fieldValue = arr;
            }
        } else if (type == Integer.class || type == int.class || IntegerProperty.class.isAssignableFrom(type)) {
            fieldValue = (int) value.getDouble();
        } else if (type == Long.class || type == long.class || LongProperty.class.isAssignableFrom(type)) {
            fieldValue = value.getLong();
        } else if (type == Double.class || type == double.class || DoubleProperty.class.isAssignableFrom(type)) {
            fieldValue = value.getDouble();
        } else if (type == Boolean.class || type == boolean.class || BooleanProperty.class.isAssignableFrom(type)) {
            fieldValue = value.getBoolean();
        } else if (type == Locale.class) {
            fieldValue = parseLocale(value.getString());
        } else if (type.isEnum()) {
            fieldValue = Enum.valueOf((Class<? extends Enum>) type, value.getString());
        }
        return fieldValue;
    }
    */

    /**
     * Parses given locale string to Locale object. If the string is empty or null then the we return null.
     * 
     * @param localeString a string containing locale in <code>language_country_variant</code> format.
     * @return Locale
     */
    public static Locale parseLocale(String localeString) {
        if (localeString != null && localeString.length() > 0) {
            StringTokenizer st = new StringTokenizer(localeString, "_");
            String language = st.hasMoreElements() ? st.nextToken() : Locale.getDefault().getLanguage();
            String country = st.hasMoreElements() ? st.nextToken() : "";
            String variant = st.hasMoreElements() ? st.nextToken() : "";
            return new Locale(language, country, variant);
        }
        return null;
    }
}
