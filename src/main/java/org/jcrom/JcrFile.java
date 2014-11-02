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

import java.io.Serializable;
import java.util.Calendar;

import javax.jcr.nodetype.NodeType;

import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;

/**
 * This class represents a JCR file node. It has the properties specified
 * in the "nt:file" > "jcr:content" node. The JcrDataProvider then provides
 * access to the actual file content.
 * <br/><br/>
 * 
 * Note that this class has an @JcrNode annotation that sets the node type
 * to "nt:file" ( {@link javax.jcr.nodetype.NodeType#NT_FILE} ). Extending classes may override this with a custom node type
 * as required. This is useful if you want to have custom metadata fields
 * stored on the file node.
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
@JcrNode(nodeType = NodeType.NT_FILE)
public class JcrFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @JcrPath
    protected String path;
    @JcrName
    protected String name;

    protected String mimeType;
    protected Calendar lastModified;
    protected String encoding;
    protected JcrDataProvider dataProvider;

    public JcrFile() {
    }

    public JcrDataProvider getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(JcrDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
