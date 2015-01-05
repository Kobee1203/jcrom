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
package org.jcrom.entities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;

/**
 * 
 * @author Nicolas Dos Santos
 * @author Antoine Mischler
 */
public class JavaFXEntity extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrProperty
    public StringProperty stringFX = new SimpleStringProperty();

    @JcrProperty
    public String string;

    @JcrProperty
    public ListProperty<String> listFX = new SimpleListProperty(FXCollections.observableArrayList());

    @JcrProperty
    public List<String> list = new LinkedList<String>();

    @JcrProperty
    public Map<String, Double> map = new HashMap<String, Double>();

    @JcrProperty
    public MapProperty<String, Double> mapFX = new SimpleMapProperty(FXCollections.observableHashMap());

    @JcrChildNode
    public ObjectProperty<JavaFXEntity> objectProperty = new SimpleObjectProperty<JavaFXEntity>();

    public String getStringFX() {
        return stringFX.get();
    }

    public StringProperty stringFXProperty() {
        return stringFX;
    }

    public void setStringFX(String stringFX) {
        this.stringFX.set(stringFX);
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public ObservableList<String> getListFX() {
        return listFX.get();
    }

    public ListProperty<String> listFXProperty() {
        return listFX;
    }

    public void setListFX(ObservableList<String> listFX) {
        this.listFX.set(listFX);
    }

    public ObservableMap<String, Double> getMapFX() {
        return mapFX.get();
    }

    public MapProperty<String, Double> mapFXProperty() {
        return mapFX;
    }

    public void setMapFX(ObservableMap<String, Double> mapFX) {
        this.mapFX.set(mapFX);
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Map<String, Double> getMap() {
        return map;
    }

    public void setMap(Map<String, Double> map) {
        this.map = map;
    }

    public ObjectProperty<JavaFXEntity> getObjectProperty() {
        return objectProperty;
    }

    public void setObjectProperty(ObjectProperty<JavaFXEntity> objectProperty) {
        this.objectProperty = objectProperty;
    }
}
