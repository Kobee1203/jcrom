/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2013 - All rights reserved.
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

import javax.jcr.Node;
import javax.jcr.SimpleCredentials;

import org.jcrom.Jcrom;
import org.jcrom.entities.Document;
import org.jcrom.entities.Folder;
import org.jcrom.entities.FolderReference;
import org.jcrom.entities.HierarchyNode;
import org.jcrom.util.NodeFilter;
import org.junit.Test;

/**
 * Thanks to Leander for identifying this problem, and submitting the
 * unit test.
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class TestReferenceHistory extends TestAbstract {

    /**
     * .
     * 
     * @throws Exception
     */
    @Test
    public final void testReferenceHistoryMaxDepth() throws Exception {
        Jcrom jcrom = new Jcrom(false, true);
        jcrom.map(HierarchyNode.class);
        jcrom.map(Folder.class);
        jcrom.map(FolderReference.class);
        jcrom.map(Document.class);

        // create root node
        final Node junitNode = this.session.getRootNode().addNode("junit");

        Folder directoryA = new Folder();
        directoryA.setName("Directory_A");

        Folder directoryA1 = new Folder();
        directoryA1.setName("Directory_A_1");
        directoryA.getChildren().add(directoryA1);

        Folder directoryA2 = new Folder();
        directoryA2.setName("Directory_A_2");
        directoryA.getChildren().add(directoryA2);

        Document document1 = new Document();
        document1.setName("Document_1");
        directoryA2.getChildren().add(document1);

        Document document2 = new Document();
        document2.setName("Document_2");
        directoryA2.getChildren().add(document2);

        // create the nodes
        jcrom.addNode(junitNode, directoryA);
        session.save();

        // create a reference and add to directory A1
        FolderReference folderReference = new FolderReference();
        folderReference.setName(directoryA2.getName());
        folderReference.setReference(directoryA2);
        directoryA1.getChildren().add(folderReference);

        //Node directoryA1Node = session.getNodeByUUID(directoryA1.getUuid());
        //jcrom.updateNode(directoryA1Node, directoryA1);
        Node directoryA1Node = session.getNodeByIdentifier(directoryA1.getId());
        jcrom.updateNode(directoryA1Node, directoryA1);
        session.save();
        session.logout();

        jcrom = new Jcrom(false, true);
        jcrom.map(HierarchyNode.class);
        jcrom.map(Folder.class);
        jcrom.map(FolderReference.class);
        jcrom.map(Document.class);

        session = repo.login(new SimpleCredentials(userID, password));

        // now search the for
        //Node directoryANode = session.getNodeByUUID(directoryA.getUuid());
        Node directoryANode = session.getNodeByIdentifier(directoryA.getId());
        Folder folder = jcrom.fromNode(Folder.class, directoryANode, NodeFilter.INCLUDE_ALL, 3);

        assertEquals("Wrong child count of Directory_A.", 2, folder.getChildren().size());
        for (HierarchyNode h : folder.getChildren()) {
            if (h.getName().equals("Directory_A_1")) {
                // there should be one child here with no childs
                Folder f = (Folder) h;
                assertEquals("Wrong child count of Directory_A_1.", 1, f.getChildren().size());
                FolderReference fr = (FolderReference) f.getChildren().get(0);
                assertNotNull("Reference is NULL.", fr.getReference());
                assertEquals("Wrong child count for folder reference.", 0, fr.getChildren().size());

            } else if (h.getName().equals("Directory_A_2")) {
                // but here we should have 2 childs but we only have 0
                Folder f = (Folder) h;
                assertEquals("Wrong child count of Directory_A_2.", 2, f.getChildren().size());
            }
        }
    }

}
