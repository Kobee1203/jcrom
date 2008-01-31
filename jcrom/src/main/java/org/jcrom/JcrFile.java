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
import java.util.Calendar;
import org.jcrom.annotations.JcrNode;

/**
 * This class represents a JCR file node. It has the properties specified
 * in the "nt:file" > "jcr:content" node. The JcrDataProvider then provides
 * access to the actual file content.
 * <br/><br/>
 * 
 * Note that this class has an @JcrNode annotation that sets the node type
 * to "nt:file". Extending classes may override this with a custom node type
 * as required. This is useful if you want to have custom metadata fields
 * stored on the file node.
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(nodeType = "nt:file")
public class JcrFile implements JcrEntity {

	protected String path;
	protected String name;
	
	protected String mimeType;
	protected Calendar lastModified;
	protected String encoding;

	protected JcrDataProvider dataProvider;
	
	public JcrFile() {
	}
	
	public static JcrFile fromFile( String name, File file, String mimeType ) {
		JcrFile jcrFile = new JcrFile();
		jcrFile.setName(name);
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(file.lastModified());
		
		jcrFile.setLastModified(cal);
		jcrFile.setMimeType(mimeType);
		jcrFile.setDataProvider( new JcrDataProviderImpl(JcrDataProvider.TYPE.FILE, file) );
		
		return jcrFile;
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
