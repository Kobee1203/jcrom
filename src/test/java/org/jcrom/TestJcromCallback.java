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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Date;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.core.NodeImpl;
import org.jcrom.annotations.JcrNode;
import org.jcrom.callback.DefaultJcromCallback;
import org.jcrom.util.JcrUtils;
import org.jcrom.util.PathUtils;
import org.junit.Test;

/**
 * @author Nicolas Dos Santos
 *
 */
public class TestJcromCallback extends TestAbstract {

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

    @Test
    public void testJcromCallback() throws RepositoryException {
        Jcrom jcrom = new Jcrom();
        jcrom.map(Parent.class);

        Node rootNode = session.getRootNode().addNode("root");

        String name = "John Doe";
        String uuid = UUID.randomUUID().toString();

        // --------------------------------
        // Create a node without callback
        // --------------------------------
        Parent parent = createParent(name);
        parent.setUuid(uuid);

        Node newNode = jcrom.addNode(rootNode, parent);
        assertNotNull(newNode);

        Parent fromNode = jcrom.fromNode(Parent.class, newNode);
        assertNotNull(fromNode);
        assertEquals(PathUtils.createValidName(name), fromNode.getName());
        assertNotSame(uuid, fromNode.getUuid()); // UUID not same

        // --------------------------------
        // Create a node with callback
        // --------------------------------
        parent = createParent(name);
        parent.setUuid(uuid);

        newNode = jcrom.addNode(rootNode, parent, null, new DefaultJcromCallback(jcrom) {
            @Override
            public Node doAddNode(Node parentNode, String nodeName, JcrNode jcrNode, Object entity) throws RepositoryException {
                if (!(parentNode instanceof NodeImpl) && !(entity instanceof Parent)) {
                    return super.doAddNode(parentNode, nodeName, jcrNode, entity);
                }
                System.out.println("add node in callback");
                NodeImpl parentNodeImpl = (NodeImpl) parentNode;
                Parent parentEntity = (Parent) entity;
                return parentNodeImpl.addNodeWithUuid(nodeName, parentEntity.getUuid());
            }
        });
        assertNotNull(newNode);

        fromNode = jcrom.fromNode(Parent.class, newNode);
        assertNotNull(fromNode);
        assertEquals(PathUtils.createValidName(name), fromNode.getName());
        assertEquals(uuid, fromNode.getUuid()); // UUID equals

        Node updateNode = jcrom.updateNode(newNode, fromNode, null, new DefaultJcromCallback(jcrom) {
            @Override
            public void doComplete(Object entity, Node node) throws JcrMappingException, RepositoryException {
                if (!(entity instanceof Parent)) {
                    super.doComplete(entity, node);
                }
                System.out.println("complete entity in callback");
                Parent parent = (Parent) entity;
                parent.setFingers(5);
                Value value = JcrUtils.createValue(int.class, 5, node.getSession().getValueFactory());
                node.setProperty("fingers", value);
            }
        });
        assertNotNull(updateNode);

        fromNode = jcrom.fromNode(Parent.class, updateNode);
        assertNotNull(fromNode);
        assertEquals(PathUtils.createValidName(name), fromNode.getName());
        assertEquals(uuid, fromNode.getUuid()); // UUID equals
        assertEquals(5, fromNode.getFingers()); // update in callback: 5 instead of the defauolt value 10
    }
}
