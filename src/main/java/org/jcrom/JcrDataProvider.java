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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * An interface for providing access to file content within a JcrFile instance.
 * 
 * <p>Thanks to Robin Wyles for adding content length.</p>
 * <p>This interface extends {@link java.io.Serializable} to resolve <i><a href="http://code.google.com/p/jcrom/issues/detail?id=91">Issue 91</a></i></p>
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public interface JcrDataProvider extends Serializable {

    enum TYPE {
        FILE,
        BYTES,
        STREAM
    }

    /**
     * Get the type of content this provider offers.
     * 
     * @return the type of content this provider offers
     */
    TYPE getType();

    /**
     * Check whether the current JcrDataProvider contains a file.
     * 
     * @return <code>true</code> if the JcrDataProvider contains a file, <code>false</code> instead
     */
    boolean isFile();

    /**
     * Check whether the current JcrDataProvider contains a byte array.
     * 
     * @return <code>true</code> if the JcrDataProvider contains a byte array, <code>false</code> instead
     */
    boolean isBytes();

    /**
     * Check whether the current JcrDataProvider contains a stream.
     * 
     * @return <code>true</code> if the JcrDataProvider contains a stream, <code>false</code> instead
     */
    boolean isStream();

    /**
     * Return a file.
     * 
     * @return file, or null if type is not JcrDataProvider.TYPE.FILE
     */
    File getFile();

    /**
     * Return a byte array.
     * 
     * @return byte array, or null if type is not JcrDataProvider.TYPE.BYTES
     */
    byte[] getBytes();

    /**
     * Return an input stream.
     * 
     * @return input stream, or null if type is not JcrDataProvider.TYPE.STREAM
     */
    InputStream getInputStream();

    /**
     * Write the content out to the File supplied.
     * 
     * @param file the file in question
     */
    void writeToFile(File file) throws IOException;

    /**
     * Returns the length of the byte array / file / input stream
     * 
     * @return content length in long value
     */
    long getContentLength();

    /**
     * Check whether the current JcrDataProvider contains a persisted content.
     * This is useful to know whether the JcrDataProvider should not be used to update a Node
     * 
     * @return <code>true</code> if the JcrDataProvider contains a content already persisted, <code>false</code> instead
     */
    boolean isPersisted();
}
