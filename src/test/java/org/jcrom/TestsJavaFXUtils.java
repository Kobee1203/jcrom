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
package org.jcrom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.StringProperty;

import org.jcrom.entities.JavaFXEntity;
import org.jcrom.type.JavaFXTypeHandler;
import org.jcrom.type.TypeHandler;
import org.junit.Test;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 17/10/2014
 * Time: 16:19
 */
public class TestsJavaFXUtils {

    private final TypeHandler typeHandler = new JavaFXTypeHandler();

    @Test
    public void testGetObject() throws NoSuchFieldException, IllegalAccessException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        javaFXEntity.setStringFX("testFX");
        javaFXEntity.setString("test");
        Field fieldFX = JavaFXEntity.class.getField("stringFX");
        Field field = JavaFXEntity.class.getField("string");
        assertEquals("testFX", typeHandler.getObject(fieldFX, javaFXEntity));
        assertEquals("test", typeHandler.getObject(field, javaFXEntity));
    }

    @Test
    public void testSetObject() throws NoSuchFieldException, IllegalAccessException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        javaFXEntity.setStringFX("");
        javaFXEntity.setString("");
        Field fieldFX = JavaFXEntity.class.getField("stringFX");
        Field field = JavaFXEntity.class.getField("string");
        typeHandler.setObject(fieldFX, javaFXEntity, "testFX");
        typeHandler.setObject(field, javaFXEntity, "test");
        assertEquals("testFX", javaFXEntity.getStringFX());
        assertEquals("test", javaFXEntity.getString());
    }

    @Test
    public void testSetObjectList() throws NoSuchFieldException, IllegalAccessException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldFX = JavaFXEntity.class.getField("listFX");
        List<String> content = Arrays.asList("a", "b", "c");
        typeHandler.setObject(fieldFX, javaFXEntity, content);
        assertArrayEquals(new Object[] { "a", "b", "c" }, javaFXEntity.getListFX().toArray());
    }

    @Test
    public void testSetObjectMap() throws NoSuchFieldException, IllegalAccessException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldFX = JavaFXEntity.class.getField("mapFX");
        Map<String, Double> content = new HashMap<String, Double>();
        content.put("a", 1.0);
        content.put("b", 2.0);
        content.put("c", 3.0);
        typeHandler.setObject(fieldFX, javaFXEntity, content);
        assertEquals(3, javaFXEntity.getMapFX().size());
        assertTrue(javaFXEntity.getMapFX().keySet().containsAll(Arrays.asList("a", "b", "c")));
        assertTrue(javaFXEntity.getMapFX().values().containsAll(Arrays.asList(1.0, 2.0, 3.0)));
    }

    @Test
    public void testIsList() throws NoSuchFieldException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldStringFX = JavaFXEntity.class.getField("stringFX");
        Field listFX = JavaFXEntity.class.getField("listFX");
        Field list = JavaFXEntity.class.getField("list");
        assertTrue(typeHandler.isList(listFX.getType()));
        assertTrue(typeHandler.isList(list.getType()));
        assertFalse(typeHandler.isList(fieldStringFX.getType()));
    }

    @Test
    public void testIsMap() throws NoSuchFieldException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldStringFX = JavaFXEntity.class.getField("stringFX");
        Field mapFX = JavaFXEntity.class.getField("mapFX");
        Field map = JavaFXEntity.class.getField("map");
        assertTrue(typeHandler.isMap(mapFX.getType()));
        assertTrue(typeHandler.isMap(map.getType()));
        assertFalse(typeHandler.isMap(fieldStringFX.getType()));
    }

    @Test
    public void testIsNotString() throws NoSuchFieldException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldStringFX = JavaFXEntity.class.getField("stringFX");
        Field fieldString = JavaFXEntity.class.getField("string");
        Field listFX = JavaFXEntity.class.getField("listFX");
        assertFalse(typeHandler.isString(listFX.getType()));
        assertTrue(typeHandler.isString(fieldString.getType()));
        assertTrue(typeHandler.isString(fieldStringFX.getType()));
    }

    @Test
    public void testGetType() throws NoSuchFieldException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldStringFX = JavaFXEntity.class.getField("stringFX");
        Field fieldString = JavaFXEntity.class.getField("string");
        Field fieldObjectProperty = JavaFXEntity.class.getField("objectProperty");
        assertEquals(String.class, typeHandler.getType(fieldString.getType(), fieldString.getGenericType(), javaFXEntity));
        assertEquals(StringProperty.class, typeHandler.getType(fieldStringFX.getType(), fieldStringFX.getGenericType(), javaFXEntity));
        assertEquals(JavaFXEntity.class, typeHandler.getType(fieldObjectProperty.getType(), fieldObjectProperty.getGenericType(), javaFXEntity));
    }

}
