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

import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 17/10/2014
 * Time: 16:20
 */
public class JavaFXEntity {

    @JcrPath
    protected String path;

    @JcrName
    protected String name;

    public StringProperty stringFX = new SimpleStringProperty();

    public String string;

    public ListProperty<String> listFX = new SimpleListProperty(FXCollections.observableArrayList());

    public List<String> list = new LinkedList<String>();

    public Map<String, Double> map = new HashMap<String, Double>();

    public MapProperty<String, Double> mapFX = new SimpleMapProperty(FXCollections.observableHashMap());

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
}
