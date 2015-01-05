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
package org.jcrom.jackrabbit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.jcrom.Jcrom;
import org.jcrom.entities.JavaFXEntity;
import org.jcrom.util.PathUtils;
import org.junit.Test;

/**
 * 
 * @author Nicolas Dos Santos
 */
public class TestJavaFX extends TestAbstract {

    private static final List<String> list = Arrays.asList("simple value 1", "simple value 2", "simple value 3");

    private static final ListProperty<String> listFX = new SimpleListProperty(FXCollections.observableArrayList()) {
        {
            add("fx value 1");
            add("fx value 2");
            add("fx value 3");
        }
    };

    private static final Map<String, Double> map = new HashMap<String, Double>() {
        {
            put("simple key 1", 10d);
            put("simple key 2", 20d);
            put("simple key 3", 30d);
        }
    };

    private static final MapProperty<String, Double> mapFX = new SimpleMapProperty(FXCollections.observableHashMap()) {
        {
            put("fx key 1", 10d);
            put("fx key 2", 20d);
            put("fx key 3", 30d);
        }
    };

    private static final String string = "Simple String";

    private static final String stringFX = "Java FX String";

    private static final ObjectProperty<JavaFXEntity> objectProperty = new SimpleObjectProperty<JavaFXEntity>();

    private static final List<String> childList = Arrays.asList("simple child value 1", "simple child value 2", "simple child value 3");

    private static final ListProperty<String> childListFX = new SimpleListProperty(FXCollections.observableArrayList()) {
        {
            add("fx child value 1");
            add("fx child value 2");
            add("fx child value 3");
        }
    };

    private static final Map<String, Double> childMap = new HashMap<String, Double>() {
        {
            put("simple child key 1", 40d);
            put("simple child key 2", 50d);
            put("simple child key 3", 60d);
        }
    };

    private static final MapProperty<String, Double> childMapFX = new SimpleMapProperty(FXCollections.observableHashMap()) {
        {
            put("fx child key 1", 40d);
            put("fx child key 2", 50d);
            put("fx child key 3", 60d);
        }
    };

    private static final String childString = "Simple String";

    private static final String childStringFX = "Java FX String";

    private JavaFXEntity createJavaFXEntity(String name) {
        JavaFXEntity entity = new JavaFXEntity();
        entity.setName(name);
        entity.setList(list);
        entity.setListFX(listFX);
        entity.setMap(map);
        entity.setMapFX(mapFX);
        entity.setString(string);
        entity.setStringFX(stringFX);
        objectProperty.setValue(createJavaFXChildEntity("Child " + name));
        entity.setObjectProperty(objectProperty);
        return entity;
    }

    private JavaFXEntity createJavaFXChildEntity(String name) {
        JavaFXEntity entity = new JavaFXEntity();
        entity.setName(name);
        entity.setList(childList);
        entity.setListFX(childListFX);
        entity.setMap(childMap);
        entity.setMapFX(childMapFX);
        entity.setString(childString);
        entity.setStringFX(childStringFX);
        return entity;
    }

    @Test
    public void testJavaFX() throws RepositoryException {
        Jcrom jcrom = new Jcrom();
        jcrom.map(JavaFXEntity.class);

        Node rootNode = session.getRootNode().addNode("root");

        String name = "Java FX Entity";

        JavaFXEntity entity = createJavaFXEntity(name);

        Node newNode = jcrom.addNode(rootNode, entity);
        assertNotNull(newNode);

        JavaFXEntity fromNode = jcrom.fromNode(JavaFXEntity.class, newNode);
        assertNotNull(fromNode);
        assertEquals(PathUtils.createValidName(name), fromNode.getName());
        assertEquals(list, fromNode.getList());
        assertEquals(listFX.get(), fromNode.getListFX());
        assertEquals(map, fromNode.getMap());
        assertEquals(mapFX.get(), fromNode.getMapFX());
        assertEquals(string, fromNode.getString());
        assertEquals(stringFX, fromNode.getStringFX());
        ObjectProperty<JavaFXEntity> objectProperty = fromNode.getObjectProperty();
        JavaFXEntity childEntity = objectProperty.getValue();
        assertEquals(childList, childEntity.getList());
        assertEquals(childListFX.get(), childEntity.getListFX());
        assertEquals(childMap, childEntity.getMap());
        assertEquals(childMapFX.get(), childEntity.getMapFX());
        assertEquals(childString, childEntity.getString());
        assertEquals(childStringFX, childEntity.getStringFX());
    }
}
