/**
 * Copyright (C) 2008-2013 by JCROM Authors (Olafur Gauti Gudmundsson, Nicolas Dos Santos) - All rights reserved.
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

import static org.junit.Assert.assertEquals;

import javax.jcr.PropertyType;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;

import org.jcrom.annotations.JcrIdentifier;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrReference;
import org.junit.Test;

/**
 * @author Nicolas Dos Santos
 *
 */
public class TestJcrReference extends TestAbstract {

    @Test
    public void testCreateWeakReference() throws JcrMappingException, RepositoryException {
        System.out.println("assert creation of weak reference");

        // initialise jcrom
        Jcrom jcrom = new Jcrom();
        jcrom.map(A1.class);
        jcrom.map(A2.class);
        jcrom.map(A3.class);
        jcrom.map(B.class);

        // initialise mappable objects
        B instanceB = new B();
        instanceB.setId("12345");
        instanceB.setName("instanceB");

        A1 instanceA1 = new A1();
        instanceA1.setName("instanceA1");
        instanceA1.setbRef(instanceB);

        A2 instanceA2 = new A2();
        instanceA2.setName("instanceA2");
        instanceA2.setbRef(instanceB);

        A3 instanceA3 = new A3();
        instanceA3.setName("instanceA3");
        instanceA3.setbRef(instanceB);

        jcrom.addNode(session.getRootNode(), instanceB, new String[] { "mix:referenceable" });
        jcrom.addNode(session.getRootNode(), instanceA1);
        jcrom.addNode(session.getRootNode(), instanceA2);
        jcrom.addNode(session.getRootNode(), instanceA3);

        String instanceBID = session.getRootNode().getNode("instanceB").getIdentifier();
        session.getRootNode().getNode("instanceB").remove();

        // A1 holds a weak reference
        System.out.println(session.getRootNode().getNode("instanceA1").getProperty("bRef").getString());
        assertEquals(session.getRootNode().getNode("instanceA1").getProperty("bRef").getType(), PropertyType.WEAKREFERENCE);
        assertEquals(session.getRootNode().getNode("instanceA1").getProperty("bRef").getString(), instanceBID);

        // A2 holds a reference
        System.out.println(session.getRootNode().getNode("instanceA2").getProperty("bRef").getString());
        assertEquals(session.getRootNode().getNode("instanceA2").getProperty("bRef").getType(), PropertyType.REFERENCE);
        assertEquals(session.getRootNode().getNode("instanceA2").getProperty("bRef").getString(), instanceBID);

        // A3 holds a reference
        System.out.println(session.getRootNode().getNode("instanceA3").getProperty("bRef").getString());
        assertEquals(session.getRootNode().getNode("instanceA3").getProperty("bRef").getType(), PropertyType.REFERENCE);
        assertEquals(session.getRootNode().getNode("instanceA3").getProperty("bRef").getString(), instanceBID);
    }

    @Test
    public void testReferentialIntegrity() throws JcrMappingException, RepositoryException {
        System.out.println("assert referential integrity using weak reference");

        // initialise jcrom
        Jcrom jcrom = new Jcrom();
        jcrom.map(A1.class);
        jcrom.map(B.class);

        // initialise mappable objects
        B instanceB = new B();
        instanceB.setId("12345");
        instanceB.setName("instanceB");

        A1 instanceA1 = new A1();
        instanceA1.setName("instanceA1");
        instanceA1.setbRef(instanceB);

        jcrom.addNode(session.getRootNode(), instanceB, new String[] { "mix:referenceable" });
        String instanceBID = session.getRootNode().getNode("instanceB").getIdentifier();
        jcrom.addNode(session.getRootNode(), instanceA1);
        session.getRootNode().getNode("instanceB").remove();
        session.save();

        assertEquals(session.getRootNode().getNode("instanceA1").getProperty("bRef").getType(), PropertyType.WEAKREFERENCE);
        assertEquals(session.getRootNode().getNode("instanceA1").getProperty("bRef").getString(), instanceBID);
    }

    @Test(expected = ReferentialIntegrityException.class)
    public void testNoReferentialIntegrity() throws JcrMappingException, RepositoryException {
        System.out.println("no referential integrity using default reference");

        // initialise jcrom
        Jcrom jcrom = new Jcrom();
        jcrom.map(A3.class);
        jcrom.map(B.class);

        // initialise mappable objects
        B instanceB = new B();
        instanceB.setId("12345");
        instanceB.setName("instanceB");

        A3 instanceA3 = new A3();
        instanceA3.setName("instanceA3");
        instanceA3.setbRef(instanceB);

        jcrom.addNode(session.getRootNode(), instanceB, new String[] { "mix:referenceable" });
        jcrom.addNode(session.getRootNode(), instanceA3);
        session.getRootNode().getNode("instanceB").remove();
        session.save();
    }

    private class A1 {
        @JcrName
        private String name;

        @JcrPath
        private String path;

        @JcrReference(weak = true)
        private B bRef;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public B getbRef() {
            return bRef;
        }

        public void setbRef(B bRef) {
            this.bRef = bRef;
        }

    }

    private class A2 {
        @JcrName
        private String name;

        @JcrPath
        private String path;

        @JcrReference(weak = false)
        B bRef;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public B getbRef() {
            return bRef;
        }

        public void setbRef(B bRef) {
            this.bRef = bRef;
        }

    }

    private class A3 {
        @JcrName
        private String name;

        @JcrPath
        private String path;

        @JcrReference
        private B bRef;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public B getbRef() {
            return bRef;
        }

        public void setbRef(B bRef) {
            this.bRef = bRef;
        }

    }

    private class B {
        @JcrIdentifier
        String id;

        @JcrName
        String name;

        @JcrPath
        String path;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}
