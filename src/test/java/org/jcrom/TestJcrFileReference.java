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

import java.io.File;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * @author Robin Wyles
 *
 */
public class TestJcrFileReference  extends TestAbstract {

	@Test
    public void testFileReference() throws JcrMappingException, RepositoryException {
        System.out.println("JcrFileNode with reference");

        // initialise jcrom
        Jcrom jcrom = new Jcrom();
        jcrom.map(JcrFileReferenceParentNode.class);
        jcrom.map(ReferenceableJCRFile.class);

        // initialise mappable objects
        ReferenceableJCRFile fileRef = new ReferenceableJCRFile();
        fileRef.setId("12345");
        fileRef.setName("fileRef");
        fileRef.setMimeType("image/jpeg");
        fileRef.setEncoding("UTF-8");

        File imageFile = new File("src/test/resources/ogg.jpg");

        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(imageFile.lastModified());
        fileRef.setLastModified(lastModified);

        fileRef.setDataProvider(new JcrDataProviderImpl(imageFile));

        JcrFileReferenceParentNode parent = new JcrFileReferenceParentNode();
        parent.setName("parent");
        parent.setbRef(fileRef);

        jcrom.addNode(session.getRootNode(), fileRef, new String[] { "mix:referenceable" });
        String instanceBID = session.getRootNode().getNode("fileRef").getIdentifier();
        jcrom.addNode(session.getRootNode(), parent);
        
        assertEquals(session.getRootNode().getNode("parent").getProperty("bRef").getType(), PropertyType.REFERENCE);
        assertEquals(session.getRootNode().getNode("parent").getProperty("bRef").getString(), instanceBID);
        
        JcrFileReferenceParentNode entityFromJcr = jcrom.fromNode(JcrFileReferenceParentNode.class, session.getRootNode().getNode("parent"));
        assertEquals("image/jpeg", entityFromJcr.getbRef().getMimeType());
        assertEquals(imageFile.length(),entityFromJcr.getbRef().getDataProvider().getContentLength());
        ///session.save();
    }
	
	@Test
    public void testFileReferenceByPath() throws JcrMappingException, RepositoryException {
        System.out.println("JcrFileNode referenced by path");

        // initialise jcrom
        Jcrom jcrom = new Jcrom();
        jcrom.map(JcrFileReferenceByPathParentNode.class);
        jcrom.map(ReferenceableJCRFile.class);

        // initialise mappable objects
        ReferenceableJCRFile fileRef = new ReferenceableJCRFile();
        fileRef.setId("12345");
        fileRef.setName("fileRef");
        fileRef.setMimeType("image/jpeg");
        fileRef.setEncoding("UTF-8");

        File imageFile = new File("src/test/resources/ogg.jpg");

        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(imageFile.lastModified());
        fileRef.setLastModified(lastModified);

        fileRef.setDataProvider(new JcrDataProviderImpl(imageFile));

        JcrFileReferenceByPathParentNode parent = new JcrFileReferenceByPathParentNode();
        parent.setName("parent");
        parent.setbRef(fileRef);

        jcrom.addNode(session.getRootNode(), fileRef, new String[] { "mix:referenceable" });
        String instancePath = session.getRootNode().getNode("fileRef").getPath();
        jcrom.addNode(session.getRootNode(), parent);
        
        assertEquals(session.getRootNode().getNode("parent").getProperty("bRef").getType(), PropertyType.STRING);
        assertEquals(session.getRootNode().getNode("parent").getProperty("bRef").getString(), instancePath);
        
        JcrFileReferenceByPathParentNode entityFromJcr = jcrom.fromNode(JcrFileReferenceByPathParentNode.class, session.getRootNode().getNode("parent"));
        assertEquals("image/jpeg", entityFromJcr.getbRef().getMimeType());
        assertEquals(imageFile.length(),entityFromJcr.getbRef().getDataProvider().getContentLength());
        ///session.save();
    }
	
}
