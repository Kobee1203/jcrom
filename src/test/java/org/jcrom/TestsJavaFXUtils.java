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
import org.jcrom.util.JavaFXUtils;
import org.junit.Test;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 17/10/2014
 * Time: 16:19
 */
public class TestsJavaFXUtils {

    @Test
    public void testGetObject() throws NoSuchFieldException, IllegalAccessException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        javaFXEntity.setStringFX("testFX");
        javaFXEntity.setString("test");
        Field fieldFX = JavaFXEntity.class.getField("stringFX");
        Field field = JavaFXEntity.class.getField("string");
        assertEquals("testFX", JavaFXUtils.getObject(fieldFX, javaFXEntity));
        assertEquals("test", JavaFXUtils.getObject(field, javaFXEntity));
    }

    @Test
    public void testSetObject() throws NoSuchFieldException, IllegalAccessException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        javaFXEntity.setStringFX("");
        javaFXEntity.setString("");
        Field fieldFX = JavaFXEntity.class.getField("stringFX");
        Field field = JavaFXEntity.class.getField("string");
        JavaFXUtils.setObject(fieldFX, javaFXEntity, "testFX");
        JavaFXUtils.setObject(field, javaFXEntity, "test");
        assertEquals("testFX", javaFXEntity.getStringFX());
        assertEquals("test", javaFXEntity.getString());
    }

    @Test
    public void testSetObjectList() throws NoSuchFieldException, IllegalAccessException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldFX = JavaFXEntity.class.getField("listFX");
        List<String> content = Arrays.asList("a", "b", "c");
        JavaFXUtils.setObject(fieldFX, javaFXEntity, content);
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
        JavaFXUtils.setObject(fieldFX, javaFXEntity, content);
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
        assertTrue(JavaFXUtils.isList(listFX));
        assertTrue(JavaFXUtils.isList(list));
        assertFalse(JavaFXUtils.isList(fieldStringFX));
    }

    @Test
    public void testIsMap() throws NoSuchFieldException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldStringFX = JavaFXEntity.class.getField("stringFX");
        Field mapFX = JavaFXEntity.class.getField("mapFX");
        Field map = JavaFXEntity.class.getField("map");
        assertTrue(JavaFXUtils.isMap(mapFX));
        assertTrue(JavaFXUtils.isMap(map));
        assertFalse(JavaFXUtils.isMap(fieldStringFX));
    }

    @Test
    public void testIsNotString() throws NoSuchFieldException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldStringFX = JavaFXEntity.class.getField("stringFX");
        Field fieldString = JavaFXEntity.class.getField("string");
        Field listFX = JavaFXEntity.class.getField("listFX");
        assertTrue(JavaFXUtils.isNotString(listFX));
        assertFalse(JavaFXUtils.isNotString(fieldString));
        assertFalse(JavaFXUtils.isNotString(fieldStringFX));
    }

    @Test
    public void testGetType() throws NoSuchFieldException {
        JavaFXEntity javaFXEntity = new JavaFXEntity();
        Field fieldStringFX = JavaFXEntity.class.getField("stringFX");
        Field fieldString = JavaFXEntity.class.getField("string");
        Field fieldObjectProperty = JavaFXEntity.class.getField("objectProperty");
        assertEquals(String.class, JavaFXUtils.getType(fieldString, javaFXEntity));
        assertEquals(StringProperty.class, JavaFXUtils.getType(fieldStringFX, javaFXEntity));
        assertEquals(JavaFXEntity.class, JavaFXUtils.getType(fieldObjectProperty, javaFXEntity));
    }

}
