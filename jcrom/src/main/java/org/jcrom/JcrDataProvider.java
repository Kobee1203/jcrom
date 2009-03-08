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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * An interface for providing access to file content within a JcrFile instance.
 * 
 * <p>Thanks to Robin Wyles for adding content length.</p>
 * 
 * @author Olafur Gauti Gudmundsson
 */
public interface JcrDataProvider {

	public enum TYPE {FILE, BYTES, STREAM}
	
	/**
	 * Get the type of content this provider offers.
	 * 
	 * @return the type of content this provider offers
	 */
	public TYPE getType();
	
	/**
	 * Return a file.
	 * 
	 * @return file, or null if type is not JcrDataProvider.TYPE.FILE
	 */
	public File getFile();

	/**
	 * Return a byte array.
	 * 
	 * @return byte array, or null if type is not JcrDataProvider.TYPE.BYTES
	 */
	public byte[] getBytes();
	
	/**
	 * Return an input stream.
	 * 
	 * @return input stream, or null if type is not JcrDataProvider.TYPE.STREAM
	 */
	public InputStream getInputStream();

    /**
     * Return the input stream as a String.
     * NOTE: only applies when the type is JcrDataProvider.TYPE.STREAM.
     *
     * @param charset the character set to use when reading from the input stream
     * @return the String represented by the data read from the input stream
     * @throws java.io.IOException
     */
    public String getAsString( String charset ) throws IOException;
    
    /**
     * Write the content out to the File supplied.
     * 
     * @param file the file in question
     */
    public void writeToFile( File file ) throws IOException;
    
    /**
     * Returns the length of the byte array / file / input stream
     * 
     * @return
     */
    public long getContentLength();
}
