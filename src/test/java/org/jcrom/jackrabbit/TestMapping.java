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
package org.jcrom.jackrabbit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import junit.framework.Assert;

import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.jcrom.JcrMappingException;
import org.jcrom.Jcrom;
import org.jcrom.dao.ChildDAO;
import org.jcrom.dao.ChildDAO2;
import org.jcrom.dao.ChildDAO3;
import org.jcrom.dao.ChildDAO4;
import org.jcrom.dao.CustomJCRFileDAO;
import org.jcrom.dao.EntityWithMapChildrenDAO;
import org.jcrom.dao.ParentDAO2;
import org.jcrom.dao.ParentDAO3;
import org.jcrom.dao.ParentDAO4;
import org.jcrom.entities.A;
import org.jcrom.entities.AImpl;
import org.jcrom.entities.Address;
import org.jcrom.entities.B;
import org.jcrom.entities.BImpl;
import org.jcrom.entities.BadNode;
import org.jcrom.entities.C;
import org.jcrom.entities.CImpl;
import org.jcrom.entities.Child;
import org.jcrom.entities.Child2;
import org.jcrom.entities.Child3;
import org.jcrom.entities.Child4;
import org.jcrom.entities.Circle;
import org.jcrom.entities.CustomJCRFile;
import org.jcrom.entities.CustomJCRFileParentNode;
import org.jcrom.entities.Document;
import org.jcrom.entities.EntityChild;
import org.jcrom.entities.EntityModifiedMapFieldAdded;
import org.jcrom.entities.EntityParent;
import org.jcrom.entities.EntityToBeModified;
import org.jcrom.entities.EntityWithMapChildren;
import org.jcrom.entities.EntityWithSerializedProperties;
import org.jcrom.entities.EnumEntity;
import org.jcrom.entities.FinalEntity;
import org.jcrom.entities.First;
import org.jcrom.entities.GrandChild;
import org.jcrom.entities.GrandParent;
import org.jcrom.entities.Parent;
import org.jcrom.entities.Parent2;
import org.jcrom.entities.Parent3;
import org.jcrom.entities.Parent4;
import org.jcrom.entities.Person;
import org.jcrom.entities.Photo;
import org.jcrom.entities.ProtectedPropertyNode;
import org.jcrom.entities.Rectangle;
import org.jcrom.entities.ReferenceContainer;
import org.jcrom.entities.ReferencedEntity;
import org.jcrom.entities.Second;
import org.jcrom.entities.Shape;
import org.jcrom.entities.ShapeParent;
import org.jcrom.entities.Square;
import org.jcrom.entities.Triangle;
import org.jcrom.entities.UserProfile;
import org.jcrom.entities.WithParentInterface;
import org.jcrom.invalidobject.InvalidEntity;
import org.jcrom.util.JcrUtils;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.PathUtils;
import org.junit.Test;

/**
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class TestMapping extends TestAbstract {

    private Parent createParent(String name) {
        Parent parent = new Parent();
        parent.setTitle(name);
        parent.setBirthDay(new Date());
        parent.setDrivingLicense(true);
        parent.setFingers(10);
        parent.setHairs(0L);
        parent.setHeight(1.80);
        parent.setWeight(83.54);
        parent.setNickName("Daddy");

        parent.addTag("father");
        parent.addTag("parent");
        parent.addTag("male");

        return parent;
    }

    private Parent2 createParent2(String name) {
        Parent2 parent2 = new Parent2();
        parent2.setTitle(name);

        return parent2;
    }

    private Parent3 createParent3(String name) {
        Parent3 parent3 = new Parent3();
        parent3.setTitle(name);

        return parent3;
    }

    private Parent4 createParent4(String name) {
        Parent4 parent4 = new Parent4();
        parent4.setTitle(name);

        return parent4;
    }

    private Child createChild(String name) {
        Child child = new Child();
        child.setTitle(name);
        child.setBirthDay(new Date());
        child.setDrivingLicense(false);
        child.setFingers(11);
        child.setHairs(130000L);
        child.setHeight(1.93);
        child.setWeight(90.45);
        child.setNickName("Baby");
        return child;
    }

    private Child2 createChild2(String name) {
        Child2 child2 = new Child2();
        child2.setTitle(name);
        return child2;
    }

    private Child3 createChild3(String name) {
        Child3 child3 = new Child3();
        child3.setTitle(name);
        return child3;
    }

    private Child4 createChild4(String name) {
        Child4 child4 = new Child4();
        child4.setTitle(name);
        return child4;
    }

    private GrandChild createGrandChild(String name) {
        GrandChild grandChild = new GrandChild();
        grandChild.setTitle(name);
        grandChild.setBirthDay(new Date());
        grandChild.setDrivingLicense(false);
        grandChild.setFingers(12);
        grandChild.setHairs(130000L);
        grandChild.setHeight(1.37);
        grandChild.setWeight(42.17);
        grandChild.setNickName("Zima");
        return grandChild;
    }

    static JcrFile createFile(String name) throws Exception {
        return createFile(name, false);
    }

    static JcrFile createFile(String name, boolean stream) throws Exception {
        JcrFile jcrFile = new JcrFile();
        jcrFile.setName(name);
        jcrFile.setMimeType("image/jpeg");
        jcrFile.setEncoding("UTF-8");

        File imageFile = new File("src/test/resources/ogg.jpg");

        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(imageFile.lastModified());
        jcrFile.setLastModified(lastModified);

        if (stream) {
            jcrFile.setDataProvider(new JcrDataProviderImpl(new FileInputStream(imageFile)));
        } else {
            jcrFile.setDataProvider(new JcrDataProviderImpl(imageFile));
        }

        return jcrFile;
    }

    private Photo createPhoto(String name) throws Exception {
        Photo jcrFile = new Photo();
        jcrFile.setName(name);
        jcrFile.setMimeType("image/jpeg");
        jcrFile.setOriginalFilename(name);

        File imageFile = new File("src/test/resources/ogg.jpg");

        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(imageFile.lastModified());
        jcrFile.setLastModified(lastModified);

        jcrFile.setFileSize(imageFile.length());
        jcrFile.setPhotographer("Testino");
        jcrFile.setChild(createParent("Kate"));

        jcrFile.setDataProvider(new JcrDataProviderImpl(imageFile));

        // also read the file to byte array and store it that way
        jcrFile.setFileBytes(readBytes(new FileInputStream(imageFile)));

        return jcrFile;
    }

    private byte[] readBytes(InputStream in) throws Exception {
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

    static void printNode(Node node, String indentation) throws Exception {
        System.out.println();
        System.out.println(indentation + "------- NODE -------");
        System.out.println(indentation + "Path: " + node.getPath());

        System.out.println(indentation + "------- Properties: ");
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property p = propertyIterator.nextProperty();
            if (!p.getName().equals("jcr:data") && !p.getName().equals("jcr:mixinTypes") && !p.getName().equals("fileBytes")) {
                System.out.print(indentation + p.getName() + ": ");
                if (p.getDefinition().getRequiredType() == PropertyType.BINARY) {
                    System.out.print("binary, (length:" + p.getLength() + ") ");
                } else if (!p.getDefinition().isMultiple()) {
                    System.out.print(p.getString());
                } else {
                    for (Value v : p.getValues()) {
                        System.out.print(v.getString() + ", ");
                    }
                }
                System.out.println();
            }

            if (p.getName().equals("jcr:childVersionHistory")) {
                System.out.println(indentation + "------- CHILD VERSION HISTORY -------");
                printNode(node.getSession().getNodeByIdentifier(p.getString()), indentation + "\t");
                System.out.println(indentation + "------- CHILD VERSION ENDS -------");
            }
        }

        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            printNode(nodeIterator.nextNode(), indentation + "\t");
        }
    }

    @Test
    public void testGetNodesMethod() throws Exception {
        Node jcrRootNode = session.getRootNode();
        Node rootNode = jcrRootNode.addNode("mapSuperclassTest");
        Node newNode = rootNode.addNode("newNode");
        NodeIterator nodeIterator = rootNode.getNodes("myMap"); // Gets all child nodes that match 'myMap'
        assertFalse(nodeIterator.hasNext()); // It's not correct ! there should be an empty iterator !
        session.save();
        nodeIterator = rootNode.getNodes("myMap");
        assertFalse(nodeIterator.hasNext()); // It's correct after saving
    }

    @Test
    public void testEnums() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(EnumEntity.class);

        EnumEntity.Suit[] suitArray = new EnumEntity.Suit[2];
        suitArray[0] = EnumEntity.Suit.HEARTS;
        suitArray[1] = EnumEntity.Suit.CLUBS;

        EnumEntity enumEntity = new EnumEntity();
        enumEntity.setName("MySuit");
        enumEntity.setSuit(EnumEntity.Suit.DIAMONDS);
        enumEntity.setSuitAsArray(suitArray);
        enumEntity.addSuitToList(EnumEntity.Suit.SPADES);

        Node rootNode = session.getRootNode().addNode("enumTest");
        Node newNode = jcrom.addNode(rootNode, enumEntity);
        session.save();

        EnumEntity fromNode = jcrom.fromNode(EnumEntity.class, newNode);

        assertEquals(fromNode.getSuit(), enumEntity.getSuit());
        assertTrue(fromNode.getSuitAsArray().length == enumEntity.getSuitAsArray().length);
        assertTrue(fromNode.getSuitAsArray()[0].equals(enumEntity.getSuitAsArray()[0]));
        assertTrue(fromNode.getSuitAsList().size() == enumEntity.getSuitAsList().size());
        assertTrue(fromNode.getSuitAsList().get(0).equals(enumEntity.getSuitAsList().get(0)));
    }

    @Test(expected = JcrMappingException.class)
    public void mapInvalidObject() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(InvalidEntity.class);
    }

    @Test
    public void serializedProperties() throws Exception {

        Jcrom jcrom = new Jcrom();
        jcrom.map(EntityWithSerializedProperties.class);

        EntityWithSerializedProperties entity = new EntityWithSerializedProperties();
        entity.setName("withSerializedProperties");

        Parent parent = createParent("John");
        entity.setParent(parent);

        Node rootNode = session.getRootNode().addNode("mapChildTest");
        Node newNode = jcrom.addNode(rootNode, entity);
        session.save();

        EntityWithSerializedProperties entityFromJcr = jcrom.fromNode(EntityWithSerializedProperties.class, newNode);

        assertTrue(entityFromJcr.getParent().getName().equals(entity.getParent().getName()));
        assertTrue(entityFromJcr.getParent().getBirthDay().equals(entity.getParent().getBirthDay()));
        assertTrue(entityFromJcr.getParent().getHeight() == entity.getParent().getHeight());
    }

    @Test
    public void mapsAsChildren() throws Exception {

        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(EntityWithMapChildren.class);
        jcrom.map(Child.class);

        Integer[] myIntArr1 = { 1, 2, 3 };
        Integer[] myIntArr2 = { 4, 5, 6 };
        int[] myIntArr3 = { 7, 8, 9 };

        int myInt1 = 1;
        int myInt2 = 2;

        String[] myStringArr1 = { "a", "b", "c" };
        String[] myStringArr2 = { "d", "e", "f" };
        String[] myStringArr3 = { "h", "i", "j" };

        String myString1 = "string1";
        String myString2 = "string2";

        Locale locale = Locale.ITALIAN;
        Locale[] locales = { Locale.FRENCH, Locale.GERMAN };

        EntityWithMapChildren entity = createEntityWithMapChildren(myIntArr1, myIntArr2, myIntArr3, myInt1, myInt2, myStringArr1, myStringArr2, myStringArr3, myString1, myString2, locale, locales);

        Node rootNode = session.getRootNode().addNode("mapChildTest");
        Node newNode = jcrom.addNode(rootNode, entity);
        session.save();

        EntityWithMapChildren entityFromJcr = jcrom.fromNode(EntityWithMapChildren.class, newNode);

        assertTrue(entityFromJcr.getIntegers().equals(entity.getIntegers()));
        assertTrue(entityFromJcr.getStrings().equals(entity.getStrings()));
        assertTrue(entityFromJcr.getMultiInt().length == entity.getMultiInt().length);
        assertTrue(entityFromJcr.getMultiInt()[1] == myIntArr3[1]);
        assertTrue(entityFromJcr.getMultiString().length == entity.getMultiString().length);
        assertTrue(entityFromJcr.getIntegerArrays().size() == entity.getIntegerArrays().size());
        assertTrue(entityFromJcr.getIntegerArrays().get("myIntArr1").length == myIntArr1.length);
        assertTrue(entityFromJcr.getIntegerArrays().get("myIntArr2").length == myIntArr2.length);
        assertTrue(entityFromJcr.getIntegerArrays().get("myIntArr1")[1] == myIntArr1[1]);
        assertTrue(entityFromJcr.getStringArrays().size() == entity.getStringArrays().size());
        assertTrue(entityFromJcr.getStringArrays().get("myStringArr1").length == myStringArr1.length);
        assertTrue(entityFromJcr.getStringArrays().get("myStringArr2").length == myStringArr2.length);
        assertTrue(entityFromJcr.getStringArrays().get("myStringArr1")[1].equals(myStringArr1[1]));
        assertTrue(entityFromJcr.getObjects().size() == entity.getObjects().size());
        assertTrue(entityFromJcr.getObjects().get("myObject1") instanceof Child);
        // The node name has been replaced by the key
        assertFalse("childName1".equals(((Child) entityFromJcr.getObjects().get("myObject1")).getName()));
        assertEquals("myObject1", ((Child) entityFromJcr.getObjects().get("myObject1")).getName());
        assertTrue(entityFromJcr.getObjects().get("myObject2") instanceof Child);
        // The node name has been replaced by the key
        assertFalse("childName2".equals(((Child) entityFromJcr.getObjects().get("myObject2")).getName()));
        assertEquals("myObject2", ((Child) entityFromJcr.getObjects().get("myObject2")).getName());

        assertTrue(entityFromJcr.getLocale().equals(locale));
        assertTrue(entityFromJcr.getMultiLocale().length == entity.getMultiLocale().length);
        assertTrue(entityFromJcr.getMultiLocale()[1].equals(locales[1]));
    }

    @Test
    public void testPersistMapWithDAO() throws Exception {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(EntityWithMapChildren.class);
        jcrom.map(Child.class);

        EntityWithMapChildrenDAO dao = new EntityWithMapChildrenDAO(session, jcrom, new String[] { NodeType.MIX_VERSIONABLE });

        Integer[] myIntArr1 = { 1, 2, 3 };
        Integer[] myIntArr2 = { 4, 5, 6 };
        int[] myIntArr3 = { 7, 8, 9 };

        int myInt1 = 1;
        int myInt2 = 2;

        String[] myStringArr1 = { "a", "b", "c" };
        String[] myStringArr2 = { "d", "e", "f" };
        String[] myStringArr3 = { "h", "i", "j" };

        String myString1 = "string1";
        String myString2 = "string2";

        Locale locale = Locale.ITALIAN;
        Locale[] locales = { Locale.FRENCH, Locale.GERMAN };

        EntityWithMapChildren entity = createEntityWithMapChildren(myIntArr1, myIntArr2, myIntArr3, myInt1, myInt2, myStringArr1, myStringArr2, myStringArr3, myString1, myString2, locale, locales);

        dao.create(entity);

        EntityWithMapChildren createEntity = dao.get(entity.getPath());

        assertTrue(createEntity.getIntegers().equals(entity.getIntegers()));
        assertTrue(createEntity.getStrings().equals(entity.getStrings()));
        assertTrue(createEntity.getMultiInt().length == entity.getMultiInt().length);
        assertTrue(createEntity.getMultiInt()[1] == myIntArr3[1]);
        assertTrue(createEntity.getMultiString().length == entity.getMultiString().length);
        assertTrue(createEntity.getIntegerArrays().size() == entity.getIntegerArrays().size());
        assertTrue(createEntity.getIntegerArrays().get("myIntArr1").length == myIntArr1.length);
        assertTrue(createEntity.getIntegerArrays().get("myIntArr2").length == myIntArr2.length);
        assertTrue(createEntity.getIntegerArrays().get("myIntArr1")[1] == myIntArr1[1]);
        assertTrue(createEntity.getStringArrays().size() == entity.getStringArrays().size());
        assertTrue(createEntity.getStringArrays().get("myStringArr1").length == myStringArr1.length);
        assertTrue(createEntity.getStringArrays().get("myStringArr2").length == myStringArr2.length);
        assertTrue(createEntity.getStringArrays().get("myStringArr1")[1].equals(myStringArr1[1]));
        assertTrue(createEntity.getObjects().size() == entity.getObjects().size());
        assertTrue(createEntity.getObjects().get("myObject1") instanceof Child);
        // The node name has been replaced by the key
        assertFalse("childName1".equals(((Child) createEntity.getObjects().get("myObject1")).getName()));
        assertEquals("myObject1", ((Child) createEntity.getObjects().get("myObject1")).getName());
        assertTrue(createEntity.getObjects().get("myObject2") instanceof Child);
        // The node name has been replaced by the key
        assertFalse("childName2".equals(((Child) createEntity.getObjects().get("myObject2")).getName()));
        assertEquals("myObject2", ((Child) createEntity.getObjects().get("myObject2")).getName());

        assertTrue(createEntity.getLocale().equals(locale));
        assertTrue(createEntity.getMultiLocale().length == entity.getMultiLocale().length);
        assertTrue(createEntity.getMultiLocale()[1].equals(locales[1]));

        // UPDATE

        Integer[] updateMyIntArr1 = { 9, 8, 7 };
        Integer[] updateMyIntArr2 = { 6, 5, 4 };
        int[] updateMyIntArr3 = { 3, 2, 1 };

        int updateMyInt1 = 3;
        int updateMyInt2 = 4;

        String[] updateMyStringArr1 = { "k", "l", "m" };
        String[] updateMyStringArr2 = { "n", "o", "p" };
        String[] updateMyStringArr3 = { "q", "r", "s" };

        String updateMyString1 = "string3";
        String updateMyString2 = "string4";

        Locale updateLocale = Locale.FRENCH;
        Locale[] updateLocales = { Locale.ENGLISH, Locale.GERMAN };

        EntityWithMapChildren entity2 = createEntityWithMapChildren(updateMyIntArr1, updateMyIntArr2, updateMyIntArr3, updateMyInt1, updateMyInt2, updateMyStringArr1, updateMyStringArr2, updateMyStringArr3, updateMyString1, updateMyString2, updateLocale, updateLocales);

        entity2.setName(createEntity.getName());
        entity2.setPath(createEntity.getPath());
        entity2.getObjects().clear();
        entity2.addObject("updateChildName1", createChildWithName("updateChildName1"));
        entity2.addObject("updateChildName2", createChildWithName("updateChildName2"));

        dao.update(entity2);

        EntityWithMapChildren updateEntity = dao.get(entity2.getPath());

        assertTrue(updateEntity.getIntegers().equals(entity2.getIntegers()));
        assertTrue(updateEntity.getStrings().equals(entity2.getStrings()));
        assertTrue(updateEntity.getMultiInt().length == entity2.getMultiInt().length);
        assertTrue(updateEntity.getMultiInt()[1] == updateMyIntArr3[1]);
        assertTrue(updateEntity.getMultiString().length == entity2.getMultiString().length);
        assertTrue(updateEntity.getIntegerArrays().size() == entity2.getIntegerArrays().size());
        assertTrue(updateEntity.getIntegerArrays().get("myIntArr1").length == updateMyIntArr1.length);
        assertTrue(updateEntity.getIntegerArrays().get("myIntArr2").length == updateMyIntArr2.length);
        assertTrue(updateEntity.getIntegerArrays().get("myIntArr1")[1] == updateMyIntArr1[1]);
        assertTrue(updateEntity.getStringArrays().size() == entity2.getStringArrays().size());
        assertTrue(updateEntity.getStringArrays().get("myStringArr1").length == updateMyStringArr1.length);
        assertTrue(updateEntity.getStringArrays().get("myStringArr2").length == updateMyStringArr2.length);
        assertTrue(updateEntity.getStringArrays().get("myStringArr1")[1].equals(updateMyStringArr1[1]));
        assertTrue(updateEntity.getObjects().size() == entity2.getObjects().size());
        assertTrue(updateEntity.getObjects().get("updateChildName1") instanceof Child);
        assertEquals("updateChildName1", ((Child) updateEntity.getObjects().get("updateChildName1")).getName());
        assertTrue(updateEntity.getObjects().get("updateChildName2") instanceof Child);
        assertEquals("updateChildName2", ((Child) updateEntity.getObjects().get("updateChildName2")).getName());

        assertTrue(updateEntity.getLocale().equals(updateLocale));
        assertTrue(updateEntity.getMultiLocale().length == entity2.getMultiLocale().length);
        assertTrue(updateEntity.getMultiLocale()[1].equals(updateLocales[1]));

        EntityWithMapChildren versionedEntity = dao.getVersion(entity.getPath(), "1.0");

        assertTrue(versionedEntity.getIntegers().equals(entity.getIntegers()));
        assertTrue(versionedEntity.getStrings().equals(entity.getStrings()));
        assertTrue(versionedEntity.getMultiInt().length == entity.getMultiInt().length);
        assertTrue(versionedEntity.getMultiInt()[1] == myIntArr3[1]);
        assertTrue(versionedEntity.getMultiString().length == entity.getMultiString().length);
        assertTrue(versionedEntity.getIntegerArrays().size() == entity.getIntegerArrays().size());
        assertTrue(versionedEntity.getIntegerArrays().get("myIntArr1").length == myIntArr1.length);
        assertTrue(versionedEntity.getIntegerArrays().get("myIntArr2").length == myIntArr2.length);
        assertTrue(versionedEntity.getIntegerArrays().get("myIntArr1")[1] == myIntArr1[1]);
        assertTrue(versionedEntity.getStringArrays().size() == entity.getStringArrays().size());
        assertTrue(versionedEntity.getStringArrays().get("myStringArr1").length == myStringArr1.length);
        assertTrue(versionedEntity.getStringArrays().get("myStringArr2").length == myStringArr2.length);
        assertTrue(versionedEntity.getStringArrays().get("myStringArr1")[1].equals(myStringArr1[1]));
        assertTrue(versionedEntity.getObjects().size() == entity.getObjects().size());
        assertTrue(versionedEntity.getObjects().get("myObject1") instanceof Child);
        // The node name has been replaced by the key
        assertFalse("childName1".equals(((Child) versionedEntity.getObjects().get("myObject1")).getName()));
        assertEquals("myObject1", ((Child) versionedEntity.getObjects().get("myObject1")).getName());
        assertTrue(versionedEntity.getObjects().get("myObject2") instanceof Child);
        // The node name has been replaced by the key
        assertFalse("childName2".equals(((Child) versionedEntity.getObjects().get("myObject2")).getName()));
        assertEquals("myObject2", ((Child) versionedEntity.getObjects().get("myObject2")).getName());

        assertTrue(versionedEntity.getLocale().equals(locale));
        assertTrue(versionedEntity.getMultiLocale().length == entity.getMultiLocale().length);
        assertTrue(versionedEntity.getMultiLocale()[1].equals(locales[1]));
    }

    private EntityWithMapChildren createEntityWithMapChildren(Integer[] myIntArr1, Integer[] myIntArr2, int[] myIntArr3, int myInt1, int myInt2, String[] myStringArr1, String[] myStringArr2, String[] myStringArr3, String myString1, String myString2, Locale locale, Locale[] locales) {
        EntityWithMapChildren entity = new EntityWithMapChildren();
        entity.setName("mapEntity");
        entity.addIntegerArray("myIntArr1", myIntArr1);
        entity.addIntegerArray("myIntArr2", myIntArr2);
        entity.setMultiInt(myIntArr3);
        entity.addStringArray("myStringArr1", myStringArr1);
        entity.addStringArray("myStringArr2", myStringArr2);
        entity.setMultiString(myStringArr3);
        entity.addString("myString1", myString1);
        entity.addString("myString2", myString2);
        entity.addInteger("myInt1", myInt1);
        entity.addInteger("myInt2", myInt2);
        entity.addObject("myObject1", createChildWithName("childName1"));
        entity.addObject("myObject2", createChildWithName("childName2"));
        entity.setLocale(locale);
        entity.setMultiLocale(locales);

        return entity;
    }

    private Child createChildWithName(String name) {
        Child child = new Child();
        child.setName(name);
        return child;
    }

    @Test
    public void mapsInSuperclass() throws Exception {

        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(EntityParent.class);
        jcrom.map(EntityChild.class);

        // create person
        Person person = new Person();
        person.setName("nico");
        person.setAge(30);
        person.setPhones(Arrays.asList(new String[] { "0123456789" }));

        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("key1", "value1");
        myMap.put("key2", "value2");

        EntityChild entity = new EntityChild();
        entity.setName("myChild");
        entity.setMyMap(myMap);
        entity.setPerson(person);

        Node jcrRootNode = session.getRootNode();
        Node rootNode = jcrRootNode.addNode("mapSuperclassTest");
        Node newNode = jcrom.addNode(rootNode, entity);

        session.save();

        // Export to check created nodes
        OutputStream out = new FileOutputStream(new File("target/jcr-export.xml"));
        session.exportSystemView(jcrRootNode.getPath(), out, false, false);

        EntityChild entityFromJcr = jcrom.fromNode(EntityChild.class, newNode);
        assertNotNull(entityFromJcr);
        assertNotNull(entityFromJcr.getMyMap());
        assertEquals(2, entityFromJcr.getMyMap().size());
        assertNotNull(entityFromJcr.getPerson());
        assertEquals(30, entityFromJcr.getPerson().getAge());
        assertEquals("nico", entityFromJcr.getPerson().getName());
    }

    @Test
    public void dynamicInstantiation() throws Exception {

        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(Circle.class).map(Rectangle.class).map(ShapeParent.class).map(Square.class);

        Shape circle = new Circle(5);
        circle.setName("circle");

        Shape rectangle = new Rectangle(5, 5);
        rectangle.setName("rectangle");

        Node rootNode = session.getRootNode().addNode("dynamicInstTest");
        Node circleNode = jcrom.addNode(rootNode, circle);
        Node rectangleNode = jcrom.addNode(rootNode, rectangle);
        session.save();

        Shape circleFromNode = jcrom.fromNode(Shape.class, circleNode);
        Shape rectangleFromNode = jcrom.fromNode(Shape.class, rectangleNode);

        assertTrue(circleFromNode.getArea() == circle.getArea());
        assertTrue(rectangleFromNode.getArea() == rectangle.getArea());

        // now try it with a parent with interface based child nodes
        Shape circle1 = new Circle(1);
        circle1.setName("circle1");
        Shape circle2 = new Circle(2);
        circle2.setName("circle2");
        Shape rectangle1 = new Rectangle(2, 2);
        rectangle1.setName("rectangle");

        // and a subclass:
        Shape square = new Square(3, 3);
        square.setName("square");

        ShapeParent shapeParent = new ShapeParent();
        shapeParent.setName("ShapeParent");
        shapeParent.addShape(circle1);
        shapeParent.addShape(rectangle1);
        shapeParent.addShape(square);
        shapeParent.setMainShape(circle2);

        Node shapeParentNode = jcrom.addNode(rootNode, shapeParent);
        session.save();

        ShapeParent fromNode = jcrom.fromNode(ShapeParent.class, shapeParentNode);

        assertTrue(fromNode.getMainShape().getArea() == shapeParent.getMainShape().getArea());
        assertTrue(fromNode.getShapes().size() == shapeParent.getShapes().size());
        assertTrue(fromNode.getShapes().get(0).getArea() == shapeParent.getShapes().get(0).getArea());
        assertTrue(fromNode.getShapes().get(1).getArea() == shapeParent.getShapes().get(1).getArea());
        assertTrue(fromNode.getShapes().get(2).getArea() == shapeParent.getShapes().get(2).getArea());
    }

    @Test
    public void references() throws Exception {

        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(ReferenceContainer.class);
        jcrom.map(Rectangle.class);

        // create the entity we will reference
        ReferencedEntity reference = new ReferencedEntity();
        reference.setName("myReference");
        reference.setBody("myBody");

        ReferencedEntity reference2 = new ReferencedEntity();
        reference2.setName("Reference2");
        reference2.setBody("Body of ref 2.");

        Rectangle rectangle = new Rectangle(2, 3);
        rectangle.setName("rectangle");

        Node rootNode = session.getRootNode().addNode("referenceTest");
        jcrom.addNode(rootNode, reference);
        jcrom.addNode(rootNode, reference2);
        jcrom.addNode(rootNode, rectangle);
        session.save();

        // note that the ReferenceContainer and ReferencedEntity classes both have
        // mixin types defined in their @JcrNode annotation

        ReferenceContainer refContainer = new ReferenceContainer();
        refContainer.setName("refContainer");
        refContainer.setReference(reference);
        refContainer.addReference(reference);
        refContainer.addReference(reference2);

        refContainer.setReferenceByPath(reference);
        refContainer.addReferenceByPath(reference);
        refContainer.addReferenceByPath(reference2);

        refContainer.setShape(rectangle);

        Node refNode = jcrom.addNode(rootNode, refContainer);

        ReferenceContainer fromNode = jcrom.fromNode(ReferenceContainer.class, refNode);

        assertTrue(fromNode.getReference() != null);
        assertTrue(fromNode.getReference().getName().equals(reference.getName()));
        assertTrue(fromNode.getReference().getBody().equals(reference.getBody()));

        assertTrue(fromNode.getReferences().size() == 2);
        assertTrue(fromNode.getReferences().get(1).getName().equals(reference2.getName()));
        assertTrue(fromNode.getReferences().get(1).getBody().equals(reference2.getBody()));

        assertTrue(fromNode.getReferencesByPath().size() == 2);
        assertTrue(fromNode.getReferencesByPath().get(1).getName().equals(reference2.getName()));
        assertTrue(fromNode.getReferencesByPath().get(1).getBody().equals(reference2.getBody()));

        assertTrue(fromNode.getReferenceByPath() != null);
        assertTrue(fromNode.getReferenceByPath().getName().equals(reference.getName()));
        assertTrue(fromNode.getReferenceByPath().getBody().equals(reference.getBody()));

        assertTrue(fromNode.getShape().getArea() == rectangle.getArea());
    }

    @Test
    public void versioningDAO() throws Exception {

        Jcrom jcrom = new Jcrom();
        jcrom.map(VersionedEntity.class);

        // create the root
        Node rootNode = session.getRootNode().addNode("content").addNode("versionedEntities");
        VersionedDAO versionedDao = new VersionedDAO(session, jcrom);

        VersionedEntity entity = new VersionedEntity();
        entity.setTitle("MyEntity");
        entity.setBody("First");
        entity.setPath(rootNode.getPath());

        VersionedEntity child1 = new VersionedEntity();
        child1.setName("child1");
        child1.setBody("child1Body");

        VersionedEntity child2 = new VersionedEntity();
        child2.setName("child2");
        child2.setBody("child2Body");

        entity.addVersionedChild(child1);
        entity.addVersionedChild(child2);

        Child child3 = createChild("John");
        entity.addUnversionedChild(child3);

        VersionedEntity test = new VersionedEntity();
        test.setName("testChild");
        test.setBody("testBody");
        entity.versionedChild1 = test;

        VersionedEntity test2 = new VersionedEntity();
        test2.setName("testChild2");
        test2.setBody("testBody2");
        entity.versionedChild2 = test2;

        versionedDao.create(entity);

        // change it
        entity.setBody("Second");
        entity.getUnversionedChildren().get(0).setNickName("Kalli");
        entity.getVersionedChildren().get(0).setBody("zimbob");
        entity.versionedChild1.setBody("mybody");
        entity.versionedChild2.setBody("mybody2");
        versionedDao.update(entity);

        // change it again
        entity.setBody("SecondSecond");
        versionedDao.update(entity);

        assertEquals(3, versionedDao.getVersionSize(entity.getPath()));

        // load entity
        VersionedEntity loadedEntity = versionedDao.get(entity.getPath());

        assertEquals("1.2", loadedEntity.getBaseVersionName());
        assertEquals("1.2", loadedEntity.getVersionName());
        assertEquals("Kalli", loadedEntity.getUnversionedChildren().get(0).getNickName());
        assertEquals("zimbob", loadedEntity.getVersionedChildren().get(0).getBody());
        assertEquals("mybody", loadedEntity.versionedChild1.getBody());
        assertEquals("mybody2", loadedEntity.versionedChild2.getBody());
        assertTrue(loadedEntity.getUnversionedChildren().size() == entity.getUnversionedChildren().size());
        assertTrue(versionedDao.getVersionList(entity.getPath()).size() == versionedDao.getVersionSize(entity.getPath()));

        VersionedEntity middleVersion = versionedDao.getVersion(entity.getPath(), "1.2");
        assertNotNull(middleVersion);

        // restore
        versionedDao.restoreVersion(entity.getPath(), "1.0");

        loadedEntity = versionedDao.get(entity.getPath());
        assertTrue(loadedEntity.getBaseVersionName().equals("1.0"));
        Child loadedChild3 = loadedEntity.getUnversionedChildren().get(0);
        assertEquals("Baby", loadedChild3.getNickName());

        loadedEntity.setBody("Third");
        versionedDao.update(loadedEntity);

        VersionedEntity oldEntity = versionedDao.getVersion(entity.getPath(), "1.0");
        assertTrue(oldEntity != null);
        assertTrue(oldEntity.getBody().equals("First"));

        List<VersionedEntity> versions = versionedDao.getVersionList(entity.getPath());
        for (VersionedEntity version : versions) {
            System.out.println("Version [" + version.getVersionName() + "] [" + version.getBody() + "], base [" + version.getBaseVersionName() + "] [" + version.getBaseVersionCreated() + "]");
        }

        // move
        VersionedEntity anotherEntity = new VersionedEntity();
        anotherEntity.setName("anotherEntity");
        anotherEntity.setBody("anotherBody");

        versionedDao.create(rootNode.getPath(), anotherEntity);

        VersionedEntity childEntity = loadedEntity.getVersionedChildren().get(0);

        versionedDao.move(childEntity, anotherEntity.getPath() + "/versionedChildren");

        assertTrue(versionedDao.exists(anotherEntity.getPath() + "/versionedChildren/" + childEntity.getName()));

        // remove child
        versionedDao.remove(loadedEntity.getVersionedChildren().get(1).getPath());

        assertFalse(versionedDao.exists(loadedEntity.getVersionedChildren().get(1).getPath()));

        VersionedEntity addVersionedEntity = new VersionedEntity();
        addVersionedEntity.setName("newAddedEntity");
        addVersionedEntity.setBody("newAddedEntity");
        loadedEntity.getVersionedChildren().add(addVersionedEntity);
        versionedDao.update(loadedEntity);

        versionedDao.remove(loadedEntity.getPath());
    }

    /**
     * Thanks to Andrius Kurtinaitis for identifying this problem and
     * contributing this test case.
     * @throws Exception
     */
    @Test
    public void versioningDAOChild1() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(VersionedEntity.class);

        // create the root
        Node rootNode = session.getRootNode().addNode("content").addNode("versionedEntities");
        VersionedDAO versionedDao = new VersionedDAO(session, jcrom);

        VersionedEntity entity = new VersionedEntity();
        entity.setName("MyEntity");
        entity.setBody("First");
        entity.setPath(rootNode.getPath());

        VersionedEntity child = new VersionedEntity();
        child.setName("child1");
        child.setBody("child1Body");

        entity.versionedChild1 = child;
        versionedDao.create(entity);

        VersionedEntity fromNode = versionedDao.getVersion(entity.getPath(), "1.0");
        assertEquals(child.getBody(), fromNode.versionedChild1.getBody());
    }

    /**
     * Thanks to Andrius Kurtinaitis for identifying this problem and
     * contributing this test case.
     * @throws Exception
     */
    @Test
    public void versioningDAOChild2() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(VersionedEntity.class);

        // create the root
        Node rootNode = session.getRootNode().addNode("content").addNode("versionedEntities");
        VersionedDAO versionedDao = new VersionedDAO(session, jcrom);

        VersionedEntity entity = new VersionedEntity();
        entity.setTitle("MyEntity");
        entity.setBody("First");
        entity.setPath(rootNode.getPath());

        VersionedEntity child = new VersionedEntity();
        child.setName("child1");
        child.setBody("child1Body");

        entity.versionedChild2 = child;
        versionedDao.create(entity);
        assertEquals(child.getBody(), versionedDao.getVersion(entity.getPath(), "1.0").versionedChild2.getBody());
    }

    /**
     * Thanks to Andrius Kurtinaitis for identifying this problem and
     * contributing this test case.
     * @throws Exception
     */
    @Test
    public void versioningDAOChild3() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(VersionedEntity.class);

        // create the root
        Node rootNode = session.getRootNode().addNode("content").addNode("versionedEntities");
        VersionedDAO versionedDao = new VersionedDAO(session, jcrom);

        VersionedEntity entity = new VersionedEntity();
        entity.setTitle("MyEntity");
        entity.setBody("First");
        entity.setPath(rootNode.getPath());

        VersionedEntity child = new VersionedEntity();
        child.setName("child1");
        child.setBody("child1Body");

        entity.versionedChild3 = child;
        versionedDao.create(entity);
        assertEquals(child.getBody(), versionedDao.getVersion(entity.getPath(), "1.0").versionedChild3.getBody());
    }

    /**
     * Thanks to Andrius Kurtinaitis for identifying this problem and
     * contributing this test case.
     * @throws Exception
     */
    @Test
    public void versioningDAOChild4() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(VersionedEntity.class);

        // create the root
        Node rootNode = session.getRootNode().addNode("content").addNode("versionedEntities");
        VersionedDAO versionedDao = new VersionedDAO(session, jcrom);

        VersionedEntity entity = new VersionedEntity();
        entity.setTitle("MyEntity");
        entity.setBody("First");
        entity.setPath(rootNode.getPath());

        VersionedEntity child = new VersionedEntity();
        child.setName("child1");
        child.setBody("child1Body");

        entity.versionedChild4 = child;
        versionedDao.create(entity);
        assertEquals(child.getBody(), versionedDao.getVersion(entity.getPath(), "1.0").versionedChild4.getBody());
    }

    @Test
    public void versioningDAOChild5() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(VersionedEntity.class);

        // create the root
        Node rootNode = session.getRootNode().addNode("content").addNode("versionedEntities");
        VersionedDAO versionedDao = new VersionedDAO(session, jcrom);

        VersionedEntity entity = new VersionedEntity();
        entity.setTitle("MyEntity");
        entity.setBody("First");
        entity.setPath(rootNode.getPath());

        VersionedEntity child = new VersionedEntity();
        child.setName("child1");
        child.setBody("child1Body");

        entity.versionedChild4 = child;
        versionedDao.create(entity);
        assertEquals(child.getBody(), versionedDao.getVersion(entity.getPath(), "1.0").versionedChild4.getBody());

        child.setTitle("child1 with new title");
        child = versionedDao.update(child);
        assertEquals(child.getTitle(), versionedDao.get(child.getPath()).getTitle());
    }

    @Test
    public void versioningDAOChild6() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(VersionedEntity.class);

        // create the root
        Node rootNode = session.getRootNode().addNode("content").addNode("versionedEntities");
        VersionedDAO versionedDao = new VersionedDAO(session, jcrom);

        VersionedEntity entity = new VersionedEntity();
        entity.setTitle("MyEntity");
        entity.setBody("First");
        entity.setPath(rootNode.getPath());

        versionedDao.create(entity);

        VersionedEntity child = new VersionedEntity();
        child.setName("child1");
        child.setBody("child1Body");
        child.setPath(entity.getPath());

        versionedDao.create(child);
        assertEquals(child.getBody(), versionedDao.getVersion(entity.getPath() + "/" + child.getName(), "1.0").getBody());
    }

    @Test
    public void testDAOs() throws Exception {

        Jcrom jcrom = new Jcrom();
        jcrom.map(Parent.class);

        // create the root
        Node rootNode = session.getRootNode().addNode("content").addNode("parents");
        ParentDAO parentDao = new ParentDAO(session, jcrom);

        Parent dad = createParent("John Bobs");
        dad.setDrivingLicense(false);
        dad.setPath(rootNode.getPath());

        dad.setAdoptedChild(createChild("AdoptedChild1"));
        dad.addChild(createChild("Child1"));
        dad.addChild(createChild("Child2"));

        Parent mom = createParent("Jane");
        mom.setPath(rootNode.getPath()); // mom is created directly under root
        assertFalse(parentDao.exists(dad.getPath() + "/" + dad.getName()));

        parentDao.create(dad);
        parentDao.create(mom);

        session.save();

        assertTrue(parentDao.exists(dad.getPath()));

        Parent loadedParent = parentDao.get(dad.getPath());
        assertTrue(loadedParent.getNickName().equals(dad.getNickName()));

        Parent uuidParent = parentDao.loadByUUID(loadedParent.getUuid());
        assertTrue(uuidParent.getNickName().equals(dad.getNickName()));
        Parent idParent = parentDao.loadById(loadedParent.getId());
        assertTrue(idParent.getNickName().equals(dad.getNickName()));

        Parent loadedMom = parentDao.get(mom.getPath());
        assertTrue(loadedMom.getNickName().equals(mom.getNickName()));

        // test second level update
        loadedParent.getAdoptedChild().setNickName("testing");
        loadedParent.getChildren().get(0).setNickName("hello");
        parentDao.update(loadedParent, NodeFilter.INCLUDE_ALL, 2);

        Parent updatedParent = parentDao.get(dad.getPath());
        assertEquals(loadedParent.getAdoptedChild().getNickName(), updatedParent.getAdoptedChild().getNickName());
        assertEquals(loadedParent.getChildren().get(0).getNickName(), updatedParent.getChildren().get(0).getNickName());

        List<Parent> parents = parentDao.findAll(rootNode.getPath());
        assertTrue(parents.size() == 2);

        List<Parent> parentsWithLicense = parentDao.findByLicense();
        assertTrue(parentsWithLicense.size() == 1);

        parentDao.remove(dad.getPath());
        assertFalse(parentDao.exists(dad.getPath()));

        parentDao.remove(loadedMom.getPath());
        assertFalse(parentDao.exists(mom.getPath()));

        // test a root level node
        Parent rootDad = createParent("John Smith");
        parentDao.create("/", rootDad);

        assertTrue(rootDad.getPath().equals("/" + rootDad.getName()));

        // rename a root level node
        rootDad.setName("John Smythe");
        parentDao.update(rootDad);

    }

    @Test
    public void testDAOCreateNode() throws Exception {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(Parent.class);
        jcrom.map(Parent2.class);
        jcrom.map(Parent3.class);
        jcrom.map(Parent4.class);
        jcrom.map(Child4.class);

        ParentDAO parentDao = new ParentDAO(session, jcrom);
        ParentDAO2 parentDao2 = new ParentDAO2(session, jcrom);
        ParentDAO3 parentDao3 = new ParentDAO3(session, jcrom);
        ParentDAO4 parentDao4 = new ParentDAO4(session, jcrom);
        ChildDAO childDao = new ChildDAO(session, jcrom);
        ChildDAO2 childDao2 = new ChildDAO2(session, jcrom);
        ChildDAO3 childDao3 = new ChildDAO3(session, jcrom);
        ChildDAO4 childDao4 = new ChildDAO4(session, jcrom);

        // create the root
        Node rootNode = session.getRootNode().addNode("content").addNode("parents");

        Parent2 p2 = createParent2("John Bobs 2"); // Single child
        assertFalse(parentDao.exists(rootNode.getPath() + "/" + p2.getName()));
        Parent3 p3 = createParent3("John Bobs 3"); // Multiple Child
        assertFalse(parentDao.exists(rootNode.getPath() + "/" + p3.getName()));
        Parent4 p4 = createParent4("John Bobs 4"); // Map of Child
        assertFalse(parentDao.exists(rootNode.getPath() + "/" + p4.getName()));

        parentDao2.create(p2);
        parentDao3.create(p3);
        parentDao4.create(p4);

        session.save();

        assertTrue(parentDao2.exists(p2.getPath()));
        assertTrue(parentDao3.exists(p3.getPath()));
        assertTrue(parentDao4.exists(p4.getPath()));

        Child2 child21 = createChild2("Child21");
        child21.setParent2(p2);
        Child3 child31 = createChild3("Child31");
        child31.setParent3(p3);
        Child3 child32 = createChild3("Child32");
        child32.setParent3(p3);
        Child4 child41 = createChild4("Child41");
        child41.setParent4(p4);
        Child4 child42 = createChild4("Child42");
        child42.setParent4(p4);

        child21 = childDao2.create(child21);
        child31 = childDao3.create(child31);
        child32 = childDao3.create(child32);
        child41 = childDao4.create(child41);
        child42 = childDao4.create(child42);

        p2 = parentDao2.get(p2.getPath());
        p3 = parentDao3.get(p3.getPath());
        p4 = parentDao4.get(p4.getPath());

        assertNotNull(p2.getChild());
        assertEquals(child21.getPath(), p2.getChild().getPath());
        assertNotNull(p3.getChildren());
        assertEquals(2, p3.getChildren().size());
        assertEquals(child31.getPath(), p3.getChildren().get(0).getPath());
        assertEquals(child32.getPath(), p3.getChildren().get(1).getPath());
        assertNotNull(p4.getMap());
        assertEquals(2, p4.getMap().size());
        Child4 c41 = (Child4) p4.getMap().get(child41.getName());
        assertEquals(child41.getPath(), c41.getPath());
        Child4 c42 = (Child4) p4.getMap().get(child42.getName());
        assertEquals(child42.getPath(), c42.getPath());

        /******************************************/

        Parent dad = createParent("John Bobs");
        dad.setAdoptedChild(createChild("AdoptedChild1"));
        dad.addChild(createChild("Child1"));
        dad.addChild(createChild("Child2"));

        assertFalse(parentDao.exists(rootNode.getPath() + "/" + dad.getName()));

        Parent mom = createParent("Jane");
        assertFalse(parentDao.exists(rootNode.getPath() + "/" + mom.getName()));

        parentDao.create(dad);
        parentDao.create(mom);

        session.save();

        assertTrue(parentDao.exists(dad.getPath()));
        assertTrue(parentDao.exists(mom.getPath()));

        Child c = createChild("Child3");
        c.setParent(mom);
        try {
            c = childDao.create(c); // Exception, multiple child fields found
        } catch (JcrMappingException e) {
            return;
        }
        Assert.fail("The unit test must not pass here.");
    }

    /*
    @Test
    public void versioning() throws Exception {

    Node node = session.getRootNode().addNode("versionTest");
    node.addMixin("mix:versionable");
    node.setProperty("title", "version 1");
    session.save();
    node.checkin();

    node.checkout();
    node.setProperty("title", "version 2");
    session.save();
    node.checkin();

    node.checkout();
    node.setProperty("title", "version 3");
    session.save();
    node.checkin();

    // print version history
    VersionHistory history = session.getRootNode().getNode("versionTest").getVersionHistory();
    VersionIterator iterator = history.getAllVersions();
    iterator.skip(1);
    while ( iterator.hasNext() ) {
    Version version = iterator.nextVersion();
    System.out.println("Version [" + version.getName() + "], [" + version.getPrimaryNodeType().getName() + "], created " + version.getCreated().getTime());

    NodeIterator nodeIterator = version.getNodes();
    while ( nodeIterator.hasNext() ) {
    Node versionNode = nodeIterator.nextNode();
    System.out.println("\tTitle: " + versionNode.getProperty("title").getString());

    if ( versionNode.getParent().isNodeType("nt:version") ) {
    Version parentVersion = (Version) versionNode.getParent();
    System.out.println("\tVersion name: " + parentVersion.getName() + " " + parentVersion.getCreated().getTime());
    }
    }
    }

    // print base version
    System.out.println("Base version name: " + session.getRootNode().getNode("versionTest").getBaseVersion().getName());

    // restore
    node.checkout();
    node.restore("1.0", true);

    System.out.println("Base version name (after restore): " + session.getRootNode().getNode("versionTest").getBaseVersion().getName());
    }
     */
    /**
     * Thanks to Nguyen Ngoc Trung for reporting the issues tested here. 
     * @throws java.lang.Exception
     */
    @Test
    public void testMultiValuedProperties() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(Parent.class);
        Node rootNode = session.getRootNode().addNode("root");

        Parent parent = createParent("John Mugabe");

        // test only one value in multi-valued property
        parent.getTags().clear();
        parent.getTags().add("first");

        Node newNode = jcrom.addNode(rootNode, parent);

        assertTrue(newNode.getProperty("tags").getDefinition().isMultiple());
        assertEquals(1, newNode.getProperty("tags").getValues().length);

        Parent fromNode = jcrom.fromNode(Parent.class, newNode);

        assertEquals(1, fromNode.getTags().size());
        assertEquals("first", fromNode.getTags().get(0));

        // test changing multiple values to one

        Parent parent2 = createParent("MultiParent");

        Node newNode2 = jcrom.addNode(rootNode, parent2);

        assertTrue(newNode2.getProperty("tags").getDefinition().isMultiple());
        assertEquals(3, newNode2.getProperty("tags").getValues().length);

        Parent fromNode2 = jcrom.fromNode(Parent.class, newNode2);

        fromNode2.getTags().clear();
        fromNode2.getTags().add("second");

        jcrom.updateNode(newNode2, fromNode2);

        Parent fromNodeAgain = jcrom.fromNode(Parent.class, newNode2);
        assertNotNull(fromNodeAgain);
        assertTrue(newNode2.getProperty("tags").getDefinition().isMultiple());
        assertEquals(1, newNode2.getProperty("tags").getValues().length);
    }

    @Test
    public void mapObjectsToNodesAndBack() throws Exception {

        Parent parent = createParent("John Mugabe");

        // adopt a child
        Child adoptedChild = createChild("Mubi");
        parent.setAdoptedChild(adoptedChild);

        // add children
        parent.addChild(createChild("Jane"));
        parent.addChild(createChild("Julie"));

        Child child = createChild("Robert");
        parent.addChild(child);

        // add grand children
        child.setAdoptedGrandChild(createGrandChild("Jim"));
        child.addGrandChild(createGrandChild("Adam"));

        // add a passport photo
        Photo photo = createPhoto("jcr_passport.jpg");
        parent.setPassportPhoto(photo);

        // multiple files
        for (int i = 0; i < 3; i++) {
            parent.addFile(createFile("jcr_image" + i + ".jpg"));
        }

        // map to node
        Jcrom jcrom = new Jcrom();
        jcrom.map(Parent.class); // this should automatically map the other classes, since they are referenced

        Node rootNode = session.getRootNode().addNode("root");

        // map the object to node
        String[] mixinTypes = { "mix:referenceable" };
        Node newNode = jcrom.addNode(rootNode, parent, mixinTypes);

        String uuid = newNode.getUUID();
        assertTrue(newNode.getUUID() != null && newNode.getUUID().length() > 0);
        String id = newNode.getIdentifier();
        assertTrue(newNode.getIdentifier() != null && newNode.getIdentifier().length() > 0);
        assertEquals(uuid, id);
        assertTrue(newNode.getProperty("birthDay").getDate().getTime().equals(parent.getBirthDay()));
        assertTrue(newNode.getProperty("weight").getDouble() == parent.getWeight());
        assertTrue((int) newNode.getProperty("fingers").getDouble() == parent.getFingers());
        assertTrue(new Boolean(newNode.getProperty("drivingLicense").getBoolean()).equals(new Boolean(parent.isDrivingLicense())));
        assertTrue(newNode.getProperty("hairs").getLong() == parent.getHairs());
        assertTrue(newNode.getNode("children").getNodes().nextNode().getName().equals("Jane"));
        assertTrue(newNode.getProperty("tags").getValues().length == 3);
        assertTrue(newNode.getProperty("tags").getValues()[0].getString().equals("father"));

        // map back to object
        Parent parentFromNode = jcrom.fromNode(Parent.class, newNode);

        // check the file
        File imageFileFromNode = new File("target/ogg_copy.jpg");
        parentFromNode.getPassportPhoto().getDataProvider().writeToFile(imageFileFromNode);

        assertTrue(parentFromNode.getPassportPhoto().getFileBytes().length == photo.getFileBytes().length);

        // check the list of files
        assertTrue(parentFromNode.getFiles().size() == 3);

        // validate properties
        assertTrue(parentFromNode.getUuid().equals(uuid));
        assertTrue(parentFromNode.getNickName().equals(parent.getNickName()));
        assertTrue(parentFromNode.getBirthDay().equals(parent.getBirthDay()));
        assertTrue(parentFromNode.getWeight() == parent.getWeight());
        assertTrue(parentFromNode.getFingers() == parent.getFingers());
        assertTrue(new Boolean(parentFromNode.isDrivingLicense()).equals(new Boolean(parent.isDrivingLicense())));
        assertTrue(parentFromNode.getHairs() == parent.getHairs());
        assertTrue(parentFromNode.getTags().size() == 3);

        // validate children
        assertTrue(parentFromNode.getAdoptedChild() != null && parentFromNode.getAdoptedChild().getName().equals(adoptedChild.getName()));
        assertTrue(parentFromNode.getChildren().size() == 3);
        assertTrue(parentFromNode.getChildren().get(2).getTitle().equals(child.getTitle()));
        assertTrue(parentFromNode.getChildren().get(2).getAdoptedGrandChild().getTitle().equals("Jim"));
        assertTrue(parentFromNode.getChildren().get(2).getGrandChildren().size() == 1);
        assertTrue(parentFromNode.getChildren().get(2).getGrandChildren().get(0).getTitle().equals("Adam"));

        // make sure that the JcrParentNode has been properly mapped
        assertTrue(parentFromNode.getAdoptedChild().getParent().getTitle().equals(parent.getTitle()));
        assertTrue(parentFromNode.getChildren().get(0).getParent().getTitle().equals(parent.getTitle()));
        assertTrue(parentFromNode.getChildren().get(2).getAdoptedGrandChild().getParent().getTitle().equals(child.getTitle()));
        assertTrue(parentFromNode.getChildren().get(2).getGrandChildren().get(0).getParent().getTitle().equals(child.getTitle()));

        // update the object
        parent.setNickName("Father");
        parent.setBirthDay(new Date());
        parent.setWeight(87.5);
        parent.setFingers(9);
        parent.setDrivingLicense(false);
        parent.setHairs(2);

        parent.getTags().remove(0);
        parent.addTag("test1");
        parent.addTag("test2");

        parent.getAdoptedChild().setFingers(10);

        // update photo metadata
        parent.getPassportPhoto().setPhotographer("Johnny Bob");
        parent.getPassportPhoto().setDataProvider(null);

        // update the node
        jcrom.updateNode(newNode, parent);

        //printNode(newNode, "");
        parentFromNode = jcrom.fromNode(Parent.class, newNode);
        System.out.println("Updated photographer: " + parentFromNode.getPassportPhoto().getPhotographer());
        System.out.println("InputStream is null: " + (parentFromNode.getPassportPhoto().getDataProvider().getInputStream() == null));

        // validate that the node is updated
        assertTrue(newNode.getProperty("birthDay").getDate().getTime().equals(parent.getBirthDay()));
        assertTrue(newNode.getProperty("weight").getDouble() == parent.getWeight());
        assertTrue((int) newNode.getProperty("fingers").getDouble() == parent.getFingers());
        assertTrue(new Boolean(newNode.getProperty("drivingLicense").getBoolean()).equals(new Boolean(parent.isDrivingLicense())));
        assertTrue(newNode.getProperty("hairs").getLong() == parent.getHairs());
        assertTrue((int) newNode.getNode("adoptedChild").getNodes().nextNode().getProperty("fingers").getDouble() == parent.getAdoptedChild().getFingers());
        assertTrue(parentFromNode.getTags().equals(parent.getTags()));

        parentFromNode.getFiles().remove(1);
        parentFromNode.getFiles().add(1, createFile("jcr_image5.jpg"));
        parentFromNode.addFile(createFile("jcr_image6.jpg"));
        jcrom.updateNode(newNode, parentFromNode);

        Parent updatedParent = jcrom.fromNode(Parent.class, newNode);
        assertTrue(updatedParent.getFiles().size() == parentFromNode.getFiles().size());

        // load with filter
        NodeFilter nodeFilter = new NodeFilter("children", NodeFilter.DEPTH_INFINITE, 1);
        Parent filteredParent = jcrom.fromNode(Parent.class, newNode, nodeFilter);

        assertNull(filteredParent.getAdoptedChild());
        assertEquals(updatedParent.getChildren().size(), filteredParent.getChildren().size());
        assertEquals(updatedParent.getChildren().get(0).getNickName(), filteredParent.getChildren().get(0).getNickName());

        // load with filter
        nodeFilter = new NodeFilter(NodeFilter.PROPERTY_PREFIX + "title, " + NodeFilter.PROPERTY_PREFIX + "birthDay", NodeFilter.DEPTH_INFINITE, 1);
        filteredParent = jcrom.fromNode(Parent.class, newNode, nodeFilter);
        assertNotNull(filteredParent.getTitle());
        assertNotNull(filteredParent.getBirthDay());
        assertNull(filteredParent.getAdoptedChild());
        assertTrue(filteredParent.getChildren().isEmpty());

        // move the node
        parent.setTitle("Mohammed");
        parent.setNickName("Momo");
        jcrom.updateNode(newNode, parent);

        assertFalse(rootNode.hasNode("John_Mugabe"));
        assertTrue(rootNode.hasNode("Mohammed"));
        assertTrue(rootNode.getNode("Mohammed").getProperty("nickName").getString().equals("Momo"));

        // update a child node directly
        Node adoptedChildNode = rootNode.getNode(parent.getName()).getNode("adoptedChild").getNodes().nextNode();
        adoptedChild.setNickName("Mubal");
        jcrom.updateNode(adoptedChildNode, adoptedChild);

        assertTrue(rootNode.getNode(parent.getName()).getNode("adoptedChild").getNodes().nextNode().getProperty("nickName").getString().equals(adoptedChild.getNickName()));
    }

    @Test
    public void testNodeFilter() throws Exception {
        Parent parent = createParent("John Mugabe");

        // adopt a child
        Child adoptedChild = createChild("Mubi");
        parent.setAdoptedChild(adoptedChild);

        // add children
        parent.addChild(createChild("Jane"));
        parent.addChild(createChild("Julie"));

        Child child = createChild("Robert");
        parent.addChild(child);

        // add grand children
        child.setAdoptedGrandChild(createGrandChild("Jim"));
        child.addGrandChild(createGrandChild("Adam"));

        // add a passport photo
        Photo photo = createPhoto("jcr_passport.jpg");
        parent.setPassportPhoto(photo);

        // multiple files
        for (int i = 0; i < 3; i++) {
            parent.addFile(createFile("jcr_image" + i + ".jpg"));
        }

        // map to node
        Jcrom jcrom = new Jcrom();
        jcrom.map(Parent.class); // this should automatically map the other classes, since they are referenced

        Node rootNode = session.getRootNode().addNode("root");

        // map the object to node
        String[] mixinTypes = { "mix:referenceable" };
        Node newNode = jcrom.addNode(rootNode, parent, mixinTypes);

        Parent parentFromNode = jcrom.fromNode(Parent.class, newNode);

        // load with filter all inclusion
        NodeFilter nodeFilter = new NodeFilter(NodeFilter.INCLUDE_ALL);
        Parent filteredParent = jcrom.fromNode(Parent.class, newNode, nodeFilter);

        assertEquals(parentFromNode.getTitle(), filteredParent.getTitle());
        assertNotNull(filteredParent.getBirthDay());
        assertEquals(parentFromNode.getBirthDay().getTime(), filteredParent.getBirthDay().getTime());
        assertEquals(0, Double.compare(parentFromNode.getHeight(), filteredParent.getHeight()));
        assertEquals(parentFromNode.getChildren().size(), filteredParent.getChildren().size());
        assertEquals(parentFromNode.getChildren().get(0).getNickName(), filteredParent.getChildren().get(0).getNickName());
        assertNotNull(filteredParent.getAdoptedChild());
        assertEquals(parentFromNode.getAdoptedChild().getNickName(), filteredParent.getAdoptedChild().getNickName());

        // load with filter all exclusion
        nodeFilter = new NodeFilter(NodeFilter.EXCLUDE_ALL);
        filteredParent = jcrom.fromNode(Parent.class, newNode, nodeFilter);

        assertNull(filteredParent.getTitle());
        assertNull(filteredParent.getBirthDay());
        assertNull(filteredParent.getNickName());
        assertNull(filteredParent.getAdoptedChild());
        assertTrue(filteredParent.getCustomList().isEmpty());
        assertTrue(filteredParent.getChildren().isEmpty());

        // load with filter inclusion
        nodeFilter = new NodeFilter("children", NodeFilter.DEPTH_INFINITE, 1);
        filteredParent = jcrom.fromNode(Parent.class, newNode, nodeFilter);

        assertNull(filteredParent.getAdoptedChild());
        assertEquals(parentFromNode.getChildren().size(), filteredParent.getChildren().size());
        assertEquals(parentFromNode.getChildren().get(0).getNickName(), filteredParent.getChildren().get(0).getNickName());

        // load with filter exclusion
        nodeFilter = new NodeFilter("-children", NodeFilter.DEPTH_INFINITE, 1);
        filteredParent = jcrom.fromNode(Parent.class, newNode, nodeFilter);

        assertTrue(filteredParent.getChildren().isEmpty());
        assertNotNull(filteredParent.getAdoptedChild());
        assertEquals(parentFromNode.getAdoptedChild().getNickName(), filteredParent.getAdoptedChild().getNickName());

        // load with filter property inclusion
        nodeFilter = new NodeFilter(NodeFilter.PROPERTY_PREFIX + "title, " + NodeFilter.PROPERTY_PREFIX + "birthDay", 0);
        filteredParent = jcrom.fromNode(Parent.class, newNode, nodeFilter);
        assertEquals(parentFromNode.getTitle(), filteredParent.getTitle());
        assertNotNull(filteredParent.getBirthDay());
        assertEquals(parentFromNode.getBirthDay().getTime(), filteredParent.getBirthDay().getTime());
        assertNull(filteredParent.getAdoptedChild());
        assertTrue(filteredParent.getChildren().isEmpty());

        // load with filter property exclusion
        nodeFilter = new NodeFilter("-" + NodeFilter.PROPERTY_PREFIX + "title, " + NodeFilter.PROPERTY_PREFIX + "birthDay", 1);
        filteredParent = jcrom.fromNode(Parent.class, newNode, nodeFilter);
        assertNull(filteredParent.getTitle());
        assertNull(filteredParent.getBirthDay());
        assertEquals(0, Double.compare(parentFromNode.getHeight(), filteredParent.getHeight()));
        assertEquals(parentFromNode.getChildren().size(), filteredParent.getChildren().size());
        assertEquals(parentFromNode.getChildren().get(0).getNickName(), filteredParent.getChildren().get(0).getNickName());
        assertNotNull(filteredParent.getAdoptedChild());
        assertEquals(parentFromNode.getAdoptedChild().getNickName(), filteredParent.getAdoptedChild().getNickName());

        // load with filter property exclusion
        nodeFilter = new NodeFilter("-" + NodeFilter.PROPERTY_PREFIX + "title, " + NodeFilter.PROPERTY_PREFIX + "birthDay", 0);
        filteredParent = jcrom.fromNode(Parent.class, newNode, nodeFilter);
        assertNull(filteredParent.getTitle());
        assertNull(filteredParent.getBirthDay());
        assertEquals(0, Double.compare(parentFromNode.getHeight(), filteredParent.getHeight()));
        assertTrue(filteredParent.getChildren().isEmpty());
        assertNull(filteredParent.getAdoptedChild());

        // -------------------------------
        // UPDATE NODE WITH FILTER
        // -------------------------------

        parent = jcrom.fromNode(Parent.class, newNode);
        String parentTitle = "Jane Mugabe";
        parent.setTitle(parentTitle);
        parent.setBirthDay(new Date());
        parent.setDrivingLicense(true);
        parent.setFingers(20);
        parent.setHairs(2L);
        parent.setHeight(2.80);
        parent.setWeight(62.22);
        parent.setNickName("Mammy");

        parent.getTags().clear();
        parent.addTag("mother");
        parent.addTag("parent");

        // adopt a child
        parent.setAdoptedChild(createChild("Mubo"));

        // add children
        parent.getChildren().clear();
        parent.addChild(createChild("John"));
        parent.addChild(createChild("Raphaël"));

        child = createChild("Robert");
        parent.addChild(child);

        // add grand children
        child.setAdoptedGrandChild(createGrandChild("Steve"));
        child.addGrandChild(createGrandChild("Eve"));

        // add a passport photo
        parent.setPassportPhoto(createPhoto("jcr_new_passport.jpg"));

        // multiple files
        parent.getFiles().clear();
        for (int i = 10; i < 13; i++) {
            parent.addFile(createFile("jcr_image" + i + ".jpg"));
        }

        // update with filter all inclusion
        nodeFilter = new NodeFilter(NodeFilter.INCLUDE_ALL);
        Node updateNode = jcrom.updateNode(newNode, parent, nodeFilter);

        Parent updateParentFromNode = jcrom.fromNode(Parent.class, updateNode);
        assertEquals(parent.getTitle(), updateParentFromNode.getTitle());
        assertEquals(parent.getBirthDay(), updateParentFromNode.getBirthDay());
        assertEquals(parent.isDrivingLicense(), updateParentFromNode.isDrivingLicense());
        assertEquals(parent.getFingers(), updateParentFromNode.getFingers());
        assertEquals(parent.getHairs(), updateParentFromNode.getHairs());
        assertEquals(0, Double.compare(parent.getHeight(), updateParentFromNode.getHeight()));
        assertEquals(0, Double.compare(parent.getWeight(), updateParentFromNode.getWeight()));
        assertEquals(parent.getNickName(), updateParentFromNode.getNickName());
        assertEquals(2, updateParentFromNode.getTags().size());
        assertNotNull(updateParentFromNode.getAdoptedChild());
        assertEquals(parent.getAdoptedChild().getTitle(), updateParentFromNode.getAdoptedChild().getTitle());
        assertEquals(parent.getChildren().size(), updateParentFromNode.getChildren().size());

        Parent noUpdateParent = jcrom.fromNode(Parent.class, updateNode);
        noUpdateParent.setTitle("Bob Nalyd");
        noUpdateParent.setBirthDay(null);
        noUpdateParent.setDrivingLicense(false);
        noUpdateParent.setFingers(-1);
        noUpdateParent.setHairs(-1L);
        noUpdateParent.setHeight(-1);
        noUpdateParent.setWeight(-1);
        noUpdateParent.setNickName("Noname");
        noUpdateParent.getTags().clear();
        // adopt a child
        noUpdateParent.setAdoptedChild(null);
        // add children
        noUpdateParent.getChildren().clear();
        // add a passport photo
        noUpdateParent.setPassportPhoto(null);
        // multiple files
        noUpdateParent.getFiles().clear();

        // update with filter all inclusion
        nodeFilter = new NodeFilter(NodeFilter.EXCLUDE_ALL);
        updateNode = jcrom.updateNode(updateNode, noUpdateParent, nodeFilter);

        updateParentFromNode = jcrom.fromNode(Parent.class, updateNode);
        assertEquals(parent.getTitle(), updateParentFromNode.getTitle());
        assertEquals(parent.getBirthDay(), updateParentFromNode.getBirthDay());
        assertEquals(parent.isDrivingLicense(), updateParentFromNode.isDrivingLicense());
        assertEquals(parent.getFingers(), updateParentFromNode.getFingers());
        assertEquals(parent.getHairs(), updateParentFromNode.getHairs());
        assertEquals(0, Double.compare(parent.getHeight(), updateParentFromNode.getHeight()));
        assertEquals(0, Double.compare(parent.getWeight(), updateParentFromNode.getWeight()));
        assertEquals(parent.getNickName(), updateParentFromNode.getNickName());
        assertEquals(parent.getTags().size(), updateParentFromNode.getTags().size());
        assertNotNull(updateParentFromNode.getAdoptedChild());
        assertEquals(parent.getAdoptedChild().getTitle(), updateParentFromNode.getAdoptedChild().getTitle());
        assertEquals(parent.getChildren().size(), updateParentFromNode.getChildren().size());

        parent = jcrom.fromNode(Parent.class, updateNode);
        parent.setTitle("title not updated");
        parent.addTag("tag not added");
        parent.addChild(createChild("Nico"));
        // update with filter inclusion
        nodeFilter = new NodeFilter("children", NodeFilter.DEPTH_INFINITE, 1);

        updateNode = jcrom.updateNode(updateNode, parent, nodeFilter);
        updateParentFromNode = jcrom.fromNode(Parent.class, updateNode);
        assertEquals(parentTitle, updateParentFromNode.getTitle());
        assertEquals(parent.getBirthDay(), updateParentFromNode.getBirthDay());
        assertEquals(parent.isDrivingLicense(), updateParentFromNode.isDrivingLicense());
        assertEquals(parent.getFingers(), updateParentFromNode.getFingers());
        assertEquals(parent.getHairs(), updateParentFromNode.getHairs());
        assertEquals(0, Double.compare(parent.getHeight(), updateParentFromNode.getHeight()));
        assertEquals(0, Double.compare(parent.getWeight(), updateParentFromNode.getWeight()));
        assertEquals(parent.getNickName(), updateParentFromNode.getNickName());
        assertEquals(parent.getTags().size() - 1, updateParentFromNode.getTags().size());
        assertNotNull(updateParentFromNode.getAdoptedChild());
        assertEquals(parent.getAdoptedChild().getTitle(), updateParentFromNode.getAdoptedChild().getTitle());
        assertEquals(parent.getChildren().size(), updateParentFromNode.getChildren().size());

        parent = jcrom.fromNode(Parent.class, updateNode);
        parent.setTitle("title updated");
        parent.addTag("tag added");
        parent.addChild(createChild("Nico"));
        // update with filter exclusion
        nodeFilter = new NodeFilter("-children", NodeFilter.DEPTH_INFINITE, 1);

        updateNode = jcrom.updateNode(updateNode, parent, nodeFilter);
        updateParentFromNode = jcrom.fromNode(Parent.class, updateNode);
        assertEquals(parent.getTitle(), updateParentFromNode.getTitle());
        assertEquals(parent.getBirthDay(), updateParentFromNode.getBirthDay());
        assertEquals(parent.isDrivingLicense(), updateParentFromNode.isDrivingLicense());
        assertEquals(parent.getFingers(), updateParentFromNode.getFingers());
        assertEquals(parent.getHairs(), updateParentFromNode.getHairs());
        assertEquals(0, Double.compare(parent.getHeight(), updateParentFromNode.getHeight()));
        assertEquals(0, Double.compare(parent.getWeight(), updateParentFromNode.getWeight()));
        assertEquals(parent.getNickName(), updateParentFromNode.getNickName());
        assertEquals(parent.getTags().size(), updateParentFromNode.getTags().size());
        assertNotNull(updateParentFromNode.getAdoptedChild());
        assertEquals(parent.getAdoptedChild().getTitle(), updateParentFromNode.getAdoptedChild().getTitle());
        assertEquals(parent.getChildren().size() - 1, updateParentFromNode.getChildren().size());

        parent = jcrom.fromNode(Parent.class, updateNode);
        String updateTitle = "new title updated";
        parent.setTitle(updateTitle);
        Calendar cal = Calendar.getInstance();
        cal.set(1981, Calendar.MARCH, 12, 0, 0, 0);
        Date updateBirthDay = cal.getTime();
        parent.setBirthDay(updateBirthDay);
        parent.addTag("new tag added");
        parent.addChild(createChild("Nico"));
        // update with filter property inclusion
        nodeFilter = new NodeFilter(NodeFilter.PROPERTY_PREFIX + "title, " + NodeFilter.PROPERTY_PREFIX + "birthDay", NodeFilter.DEPTH_INFINITE, 1);

        updateNode = jcrom.updateNode(updateNode, parent, nodeFilter);
        updateParentFromNode = jcrom.fromNode(Parent.class, updateNode);
        assertEquals(updateTitle, updateParentFromNode.getTitle());
        assertEquals(parent.getBirthDay(), updateParentFromNode.getBirthDay());
        assertEquals(parent.isDrivingLicense(), updateParentFromNode.isDrivingLicense());
        assertEquals(parent.getFingers(), updateParentFromNode.getFingers());
        assertEquals(parent.getHairs(), updateParentFromNode.getHairs());
        assertEquals(0, Double.compare(parent.getHeight(), updateParentFromNode.getHeight()));
        assertEquals(0, Double.compare(parent.getWeight(), updateParentFromNode.getWeight()));
        assertEquals(parent.getNickName(), updateParentFromNode.getNickName());
        assertEquals(parent.getTags().size() - 1, updateParentFromNode.getTags().size());
        assertNotNull(updateParentFromNode.getAdoptedChild());
        assertEquals(parent.getAdoptedChild().getTitle(), updateParentFromNode.getAdoptedChild().getTitle());
        assertEquals(parent.getChildren().size() - 1, updateParentFromNode.getChildren().size());

        parent = jcrom.fromNode(Parent.class, updateNode);
        parent.setTitle("new title not updated");
        parent.setBirthDay(new Date());
        parent.addTag("new tag added");
        parent.addChild(createChild("Nico"));
        // update with filter property exclusion
        nodeFilter = new NodeFilter("-" + NodeFilter.PROPERTY_PREFIX + "title, " + NodeFilter.PROPERTY_PREFIX + "birthDay", NodeFilter.DEPTH_INFINITE, 1);

        updateNode = jcrom.updateNode(updateNode, parent, nodeFilter);
        updateParentFromNode = jcrom.fromNode(Parent.class, updateNode);
        assertEquals(updateTitle, updateParentFromNode.getTitle());
        assertEquals(updateBirthDay, updateParentFromNode.getBirthDay());
        assertEquals(parent.isDrivingLicense(), updateParentFromNode.isDrivingLicense());
        assertEquals(parent.getFingers(), updateParentFromNode.getFingers());
        assertEquals(parent.getHairs(), updateParentFromNode.getHairs());
        assertEquals(0, Double.compare(parent.getHeight(), updateParentFromNode.getHeight()));
        assertEquals(0, Double.compare(parent.getWeight(), updateParentFromNode.getWeight()));
        assertEquals(parent.getNickName(), updateParentFromNode.getNickName());
        assertEquals(parent.getTags().size(), updateParentFromNode.getTags().size());
        assertNotNull(updateParentFromNode.getAdoptedChild());
        assertEquals(parent.getAdoptedChild().getTitle(), updateParentFromNode.getAdoptedChild().getTitle());
        assertEquals(parent.getChildren().size(), updateParentFromNode.getChildren().size());
    }

    /**
     * Thanks to Decebal Suiu for contributing this test case.
     * @throws Exception 
     */
    @Test
    public void testEmptyList() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(Person.class);

        // create person
        Person person = new Person();
        person.setName("peter");
        person.setAge(20);
        person.setPhones(Arrays.asList(new String[] { "053453553" }));

        // add person in jcr
        Node node = jcrom.addNode(session.getRootNode(), person);
        Person personFromJcr = jcrom.fromNode(Person.class, node);
        assertEquals(1, personFromJcr.getPhones().size());

        // update person in jcr
        person.setPhones(new ArrayList<String>()); // reset the phones
        jcrom.updateNode(node, person);

        // retrieve updated person from jcr
        personFromJcr = jcrom.fromNode(Person.class, node);
        assertFalse(personFromJcr.getPhones().size() == 1); // <<< FAILED
    }

    @Test
    public void testJcrFileMapping() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(JcrFile.class);

        Node root = session.getRootNode().addNode("files");

        JcrFile file = createFile("myfile", true);
        Calendar lastModified = file.getLastModified();

        Node node = jcrom.addNode(root, file);

        assertTrue(node.hasNode("jcr:content"));
        assertEquals("image/jpeg", node.getNode("jcr:content").getProperty("jcr:mimeType").getString());
        assertEquals("UTF-8", node.getNode("jcr:content").getProperty("jcr:encoding").getString());

        JcrFile fromNode = jcrom.fromNode(JcrFile.class, node);

        assertEquals("image/jpeg", fromNode.getMimeType());
        assertEquals("UTF-8", fromNode.getEncoding());
        assertEquals(lastModified.getTimeInMillis(), fromNode.getLastModified().getTimeInMillis());
        assertTrue(fromNode.getDataProvider().getContentLength() > 0);
    }

    /**
     * Thanks to Danilo Barboza and postmaster@doubleegg.com for contributing this test case.
     * @throws Exception 
     */
    @Test
    public void testCustomJcrFile() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(CustomJCRFile.class);

        session.getRootNode().addNode("customs");
        CustomJCRFileDAO dao = new CustomJCRFileDAO(session, jcrom);

        File imageFile = new File("src/test/resources/ogg.jpg");

        CustomJCRFile custom = new CustomJCRFile();
        custom.setPath("customs");
        custom.setMetadata("My Metadata!");
        custom.setEncoding("UTF-8");
        custom.setMimeType("image/jpg");
        custom.setLastModified(Calendar.getInstance());
        custom.setDataProvider(new JcrDataProviderImpl(imageFile));
        custom.setName(imageFile.getName());

        dao.create(custom);

        CustomJCRFile customFromJcr = dao.get(custom.getPath());

        assertEquals(custom.getName(), customFromJcr.getName());
        assertEquals(custom.getEncoding(), customFromJcr.getEncoding());
        assertEquals(custom.getMimeType(), customFromJcr.getMimeType());
        assertEquals(custom.getMetadata(), customFromJcr.getMetadata());

        // Test filtering of jcr:data
        CustomJCRFile filteredFromJcr = dao.get(custom.getPath(), "-jcr:data", NodeFilter.DEPTH_INFINITE);
        assertNull(filteredFromJcr.getDataProvider());

        customFromJcr.setEncoding("ISO-8859-1");
        customFromJcr.setMimeType("image/gif");
        customFromJcr.setMetadata("Updated metadata");
        customFromJcr.setDataProvider(null); // not going to update the file

        dao.update(customFromJcr);

        CustomJCRFile updatedFromJcr = dao.get(customFromJcr.getPath());

        assertEquals(customFromJcr.getEncoding(), updatedFromJcr.getEncoding());
        assertEquals(customFromJcr.getMimeType(), updatedFromJcr.getMimeType());
        assertEquals(customFromJcr.getMetadata(), updatedFromJcr.getMetadata());
    }

    /**
     * Thanks to eltabo for contributing this test case (Issue 95).
     * @throws Exception 
     */
    @Test
    public void testJcrFileNodeFromInputStream() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(Document.class);
        jcrom.map(Parent.class);
        jcrom.map(JcrFile.class);

        Node rootNode = session.getRootNode().addNode("root");

        Parent parent = createParent("Bob");
        parent.setJcrFile(createFile("jcr_image.jpg", true));
        Node newNode = jcrom.addNode(rootNode, parent);
        assertNotNull(newNode);
        assertTrue(newNode.hasNode("jcrFile/jcr_image.jpg/jcr:content"));

        Node fileNode = newNode.getNode("jcrFile/jcr_image.jpg");
        assertEquals("image/jpeg", fileNode.getNode("jcr:content").getProperty("jcr:mimeType").getString());
        assertEquals("UTF-8", fileNode.getNode("jcr:content").getProperty("jcr:encoding").getString());

        JcrFile fromNode = jcrom.fromNode(JcrFile.class, fileNode);

        assertEquals("image/jpeg", fromNode.getMimeType());
        assertEquals("UTF-8", fromNode.getEncoding());
        assertTrue(fromNode.getDataProvider().getContentLength() > 0);
    }

    @Test
    public void testNoChildContainerNode() throws Exception {
        Jcrom jcrom = new Jcrom();
        jcrom.map(UserProfile.class);

        Node rootNode = session.getRootNode().addNode("noChildTest");

        UserProfile userProfile = new UserProfile();
        userProfile.setName("john");

        Address address = new Address();
        address.setStreet("Some street");
        address.setPostCode("101");
        userProfile.setAddress(address);

        Node userProfileNode = jcrom.addNode(rootNode, userProfile);

        UserProfile fromJcr = jcrom.fromNode(UserProfile.class, userProfileNode);

        assertEquals(address.getName(), "address");
        assertEquals(address.getStreet(), fromJcr.getAddress().getStreet());
        assertEquals(address.getPostCode(), fromJcr.getAddress().getPostCode());

        fromJcr.getAddress().setStreet("Another street");

        jcrom.updateNode(userProfileNode, fromJcr);

        UserProfile updatedFromJcr = jcrom.fromNode(UserProfile.class, userProfileNode);

        assertEquals(fromJcr.getAddress().getStreet(), updatedFromJcr.getAddress().getStreet());
        assertEquals(fromJcr.getAddress().getPostCode(), updatedFromJcr.getAddress().getPostCode());
    }

    @Test
    public void testSecondLevelFileUpdate() throws Exception {

        Jcrom jcrom = new Jcrom();
        jcrom.map(GrandParent.class).map(Photo.class);

        GrandParent grandParent = new GrandParent();
        grandParent.setName("Charles");

        Parent parent = createParent("William");

        Photo photo = createPhoto("jcr_passport.jpg");
        parent.setPassportPhoto(photo);

        parent.setJcrFile(createFile("jcr_image.jpg"));

        grandParent.setChild(parent);

        Node rootNode = session.getRootNode().addNode("root");

        Node newNode = jcrom.addNode(rootNode, grandParent);

        GrandParent fromNode = jcrom.fromNode(GrandParent.class, newNode);
        fromNode.getChild().getPassportPhoto().setName("bobby.xml");
        fromNode.getChild().getPassportPhoto().setPhotographer("Bobbs");

        fromNode.getChild().setName("test");
        fromNode.getChild().setTitle("Something");
        fromNode.getChild().getJcrFile().setName("bob.xml");

        jcrom.updateNode(newNode, photo);
    }

    /**
     * Thanks to Bouiaw and Andrius Kurtinaitis for identifying this problem and
     * contributing this test case.
     * @throws Exception
     */
    @Test
    public void testReferenceCycles() throws Exception {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(BadNode.class);

        BadNode node1 = new BadNode();
        node1.body = "body1";

        BadNode node2 = new BadNode();
        node2.body = "body2";

        Node rootNode = session.getRootNode();
        Node n1 = jcrom.addNode(rootNode, node1);
        Node n2 = jcrom.addNode(rootNode, node2);

        node1.reference = node2;
        node2.reference = node1;

        jcrom.updateNode(n1, node1);
        jcrom.updateNode(n2, node2);

        BadNode fromNode1 = jcrom.fromNode(BadNode.class, n1);
        BadNode fromNode2 = jcrom.fromNode(BadNode.class, n2);

        assertEquals(node1.body, fromNode2.reference.body);
        assertEquals(node2.body, fromNode1.reference.body);
    }

    @Test
    public void testNestedInterfaces() throws Exception {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(AImpl.class).map(BImpl.class).map(CImpl.class);

        A a = new AImpl("a");
        B b = new BImpl("b");
        C c = new CImpl("c");

        c.setA(a);
        b.setC(c);
        a.setB(b);

        Node rootNode = session.getRootNode();
        Node nodeA = jcrom.addNode(rootNode, a);

        A fromNodeA = jcrom.fromNode(A.class, nodeA);
        assertNotNull(fromNodeA);
    }

    /**
     * Thanks to Leander for identifying this problem and
     * contributing this test case.
     * @throws Exception
     */
    @Test
    public final void testUpdateNodeNodeObject() throws Exception {
        // initialize JCrom
        final Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(Shape.class).map(Triangle.class).map(ShapeParent.class);

        // setup test data
        Triangle mainShape = new Triangle(1, 1);
        mainShape.setName("mainShape");

        Triangle childShape = new Triangle(2, 2);
        childShape.setName("childShape");

        ShapeParent shapeParent = new ShapeParent();
        shapeParent.setPath("/");
        shapeParent.setName("shapeParent");
        shapeParent.setMainShape(mainShape);
        shapeParent.addShape(childShape);

        // create node and children
        final Node rootNode = this.session.getRootNode();
        jcrom.addNode(rootNode, shapeParent);
        this.session.save();

        // get parent object from created node
        Node node = rootNode.getNode("shapeParent");
        shapeParent = jcrom.fromNode(ShapeParent.class, node);

        // make some changes
        mainShape = (Triangle) shapeParent.getMainShape();
        assertEquals("Base is wrong", 1, mainShape.getBase(), 0);
        assertEquals("Height is wrong", 1, mainShape.getHeight(), 0);
        mainShape.setBase(2);
        mainShape.setHeight(2);

        childShape = (Triangle) shapeParent.getShapes().get(0);
        assertEquals("Base is wrong", 2, mainShape.getBase(), 0);
        assertEquals("Height is wrong", 2, mainShape.getHeight(), 0);
        childShape.setBase(3);
        childShape.setHeight(3);

        // and update
        jcrom.updateNode(node, shapeParent);
        this.session.save();

        // now reread the node and check the children
        node = rootNode.getNode("shapeParent");
        shapeParent = jcrom.fromNode(ShapeParent.class, node);

        mainShape = (Triangle) shapeParent.getMainShape();

        // this will fail, still has old value
        assertEquals("Base is wrong", 2, mainShape.getBase(), 0);
        assertEquals("Height is wrong", 2, mainShape.getHeight(), 0);

        childShape = (Triangle) shapeParent.getShapes().get(0);

        // this will fail, still has old value
        assertEquals("Base is wrong", 3, childShape.getBase(), 0);
        assertEquals("Height is wrong", 3, childShape.getHeight(), 0);
    }

    /**
     * Thanks to Leander for contributing this test case.
     * @throws Exception
     */
    @Test
    public final void testAddCustomJCRFileParentNode() throws Exception {
        final Jcrom jcrom = new Jcrom();
        jcrom.map(CustomJCRFile.class);
        jcrom.map(CustomJCRFileParentNode.class);

        // create root node
        final Node customs = this.session.getRootNode().addNode("customs");

        // initialize the file
        final CustomJCRFile custom = new CustomJCRFile();
        custom.setPath("customs");
        custom.setMetadata("My Metadata!");
        custom.setEncoding("UTF-8");
        custom.setMimeType("image/jpg");
        custom.setLastModified(Calendar.getInstance());
        final File imageFile = new File("src/test/resources/ogg.jpg");
        custom.setDataProvider(new JcrDataProviderImpl(imageFile));
        custom.setName(imageFile.getName());

        final CustomJCRFileParentNode parent = new CustomJCRFileParentNode();
        parent.setName("parent");
        parent.setFile(custom);

        final Node parentNode = jcrom.addNode(customs, parent);
        final Node customNode = parentNode.getNode("file/ogg.jpg");

        // create custom directly, check the node types
        final NodeType[] mixins = customNode.getMixinNodeTypes();
        assertEquals("Mixin size is wrong.", 1, mixins.length);
        assertEquals("mix:referenceable", mixins[0].getName());

        final CustomJCRFileParentNode parentFromJcr = jcrom.fromNode(CustomJCRFileParentNode.class, parentNode);
        assertNotNull("UUID is null.", parentFromJcr.getFile().getUuid());
    }

    /**
     * Thanks to Leander for contributing this test case.
     * @throws Exception
     */
    @Test
    public final void testAddCustomJCRFile() throws Exception {
        final Jcrom jcrom = new Jcrom();
        jcrom.map(CustomJCRFile.class);
        jcrom.map(CustomJCRFileParentNode.class);

        // create root node
        final Node customs = this.session.getRootNode().addNode("customs");

        // initialize the file
        final CustomJCRFile custom = new CustomJCRFile();
        custom.setPath("customs");
        custom.setMetadata("My Metadata!");
        custom.setEncoding("UTF-8");
        custom.setMimeType("image/jpg");
        custom.setLastModified(Calendar.getInstance());
        final File imageFile = new File("src/test/resources/ogg.jpg");
        custom.setDataProvider(new JcrDataProviderImpl(imageFile));
        custom.setName(imageFile.getName());

        // create custom directly, check the node types
        final Node customNode = jcrom.addNode(customs, custom);
        final NodeType[] mixins = customNode.getMixinNodeTypes();

        assertEquals("Mixin size is wrong.", 1, mixins.length);
        assertEquals("mix:referenceable", mixins[0].getName());

        final CustomJCRFile customFromJcr = jcrom.fromNode(CustomJCRFile.class, customNode);
        assertNotNull("UUID is null.", customFromJcr.getUuid());
    }

    @Test
    public void testProtectedProperties() throws Exception {
        final Jcrom jcrom = new Jcrom();
        jcrom.map(ProtectedPropertyNode.class);

        ProtectedPropertyNode protectedNode = new ProtectedPropertyNode();
        protectedNode.setName("protected_node");
        protectedNode.setCreatedBy("John Doe");

        final Node rootNode = this.session.getRootNode();
        Node createdProtectedNode = jcrom.addNode(rootNode, protectedNode);
        this.session.save();

        JcrUtils.lock(createdProtectedNode, true, false, -1, "the_locker");

        ProtectedPropertyNode fromNode = jcrom.fromNode(ProtectedPropertyNode.class, createdProtectedNode);
        assertNotNull(fromNode);
        String createdBy = fromNode.getCreatedBy();
        assertNotNull(createdBy);
        Date created = fromNode.getCreated();
        assertNotNull(created);
        long createdTime = created.getTime();
        String identifier = fromNode.getIdentifier();
        assertNotNull(identifier);
        String id = fromNode.getId();
        assertNotNull(id);
        assertEquals(identifier, id);
        String lockOwner = fromNode.getLockOwner();
        assertNotNull(lockOwner);
        assertEquals("the_locker", lockOwner);
        boolean lockIsDeep = fromNode.isLockIsDeep();
        assertEquals(true, lockIsDeep);

        Thread.sleep(1000);

        // Properties not saved
        fromNode.setCreated(new Date());
        fromNode.setCreatedBy("Jane Doe");

        JcrUtils.unlock(createdProtectedNode);

        Node updatedProtectedNode = jcrom.updateNode(createdProtectedNode, fromNode);
        ProtectedPropertyNode fromUpdatedNode = jcrom.fromNode(ProtectedPropertyNode.class, updatedProtectedNode);
        Date updatedCreated = fromUpdatedNode.getCreated();
        assertNotNull(updatedCreated);
        assertEquals(createdBy, fromUpdatedNode.getCreatedBy());
        assertEquals(createdTime, updatedCreated.getTime());
        assertEquals(identifier, fromUpdatedNode.getIdentifier());
        assertEquals(id, fromUpdatedNode.getId());
        assertEquals(fromUpdatedNode.getIdentifier(), fromUpdatedNode.getId());
        assertNull(fromUpdatedNode.getLockOwner());
        assertFalse(fromUpdatedNode.isLockIsDeep());
    }

    /**
     * Thanks to postmaster@doubleegg.com for contributing this test case.
     * Tests adding a Map field to a mapped class at a later date, 
     * fails with PathNotFoundException in JCROM 2.0
     */
    @Test
    public void testAddMapToModel() throws Exception {
        final Jcrom jcrom = new Jcrom();
        jcrom.map(EntityToBeModified.class);
        jcrom.map(EntityModifiedMapFieldAdded.class);

        EntityToBeModified entity = new EntityToBeModified();
        entity.setName("original");
        entity.setPath("original");

        Node originalNode = jcrom.addNode(this.session.getRootNode(), entity);

        try {
            jcrom.fromNode(EntityModifiedMapFieldAdded.class, originalNode);
        } catch (JcrMappingException e) {
            if (e.getCause() instanceof PathNotFoundException) {
                fail("PathNotFoundException thrown.");
            } else {
                throw e;
            }
        }
    }

    @Test
    public void finalFields() throws Exception {
        final Jcrom jcrom = new Jcrom();
        jcrom.map(FinalEntity.class);

        FinalEntity entity = new FinalEntity("This cannot be changed");
        entity.setName("myentity");

        final Node parentNode = this.session.getRootNode().addNode("mynode");

        jcrom.addNode(parentNode, entity);

        assertTrue(parentNode.getNode("myentity").hasProperty("immutableString"));
        assertEquals(entity.getImmutableString(), parentNode.getNode("myentity").getProperty("immutableString").getString());
    }

    @Test
    public void mapPackage() throws Exception {
        final Jcrom jcrom = new Jcrom(true, true);
        jcrom.mapPackage("org.jcrom.entities");

        final Node parentNode = this.session.getRootNode().addNode("mynode");

        First first = new First();
        first.setName("first");
        first.setFirstString("nr1");

        Second second = new Second();
        second.setName("second");
        second.setSecondString("nr2");

        jcrom.addNode(parentNode, first);
        jcrom.addNode(parentNode, second);

        assertTrue(parentNode.getNode("first").hasProperty("firstString"));
        assertTrue(parentNode.getNode("second").hasProperty("secondString"));
    }

    @Test
    public void parentInterface() throws Exception {
        final Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(Square.class).map(WithParentInterface.class).map(Parent.class).map(Child.class);

        final Node parentNode = this.session.getRootNode().addNode("mynode");

        WithParentInterface child = new WithParentInterface();
        child.setName("child");

        Square square = new Square(3, 3);
        square.setName("square");
        square.setChild(child);

        Node newNode = jcrom.addNode(parentNode, square);

        Square fromNode = jcrom.fromNode(Square.class, newNode);

        assertTrue(square.getArea() == fromNode.getChild().getParent().getArea());

        // add unit test to check if we retrieve the parent node after calling method fromNode
        WithParentInterface child2 = new WithParentInterface();
        child2.setName("child2");
        Node newNode2 = jcrom.addNode(newNode, child2);
        WithParentInterface fromNode2 = jcrom.fromNode(WithParentInterface.class, newNode2);
        assertEquals(0, Double.compare(square.getArea(), fromNode2.getParent().getArea()));

        Parent parent3 = createParent("daddy");
        Child child3 = createChild("child");
        parent3.addChild(child3);
        Node parentNode3 = jcrom.addNode(parentNode, parent3);
        Node childNode3 = parentNode3.getNode("children/" + PathUtils.createValidName(child3.getName()));
        Child fromNode3 = jcrom.fromNode(Child.class, childNode3);
        assertTrue(parent3.getHeight() == fromNode3.getParent().getHeight());
        assertTrue(parent3.getTitle() == fromNode3.getParent().getTitle());
    }

    @Test
    public void customChildContainers() throws Exception {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(Parent.class);

        Node rootNode = session.getRootNode().addNode("root");

        Parent parent = createParent("Daddy");

        Child listChild1 = createChild("Sonny1");
        Child listChild2 = createChild("Sonny2");

        Child mapChild1 = createChild("Jane1");
        Child mapChild2 = createChild("Jane2");

        parent.getCustomList().add(listChild1);
        parent.getCustomList().add(listChild2);

        parent.getCustomMap().put(mapChild1.getName(), mapChild1);
        parent.getCustomMap().put(mapChild2.getName(), mapChild2);

        Node newNode = jcrom.addNode(rootNode, parent);

        Parent fromNode = jcrom.fromNode(Parent.class, newNode);

        assertTrue(fromNode.getCustomList() instanceof LinkedList);
        assertTrue(fromNode.getCustomMap() instanceof TreeMap);
    }

    @Test(expected = PathNotFoundException.class)
    public void testNodeClassChange() throws Exception {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(Rectangle.class).map(Triangle.class);

        Node rootNode = session.getRootNode().addNode("root");

        // create the node
        Triangle triangle = new Triangle(1, 1);
        triangle.setName("test");
        Node newNode = jcrom.addNode(rootNode, triangle);

        // now switch to another class for the node
        Rectangle rectangle = new Rectangle(2.5, 3.3);
        rectangle.setName("test");
        jcrom.updateNode(newNode, rectangle);

        // finally make sure that the old properties have been removed,
        // this should throw an exception:
        newNode.getProperty("base");
    }
}
