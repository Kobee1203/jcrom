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
import java.io.InputStream;

/**
 * A simple implementation of the JcrDataProvider interface.
 * Developers can implement their own data provider if advanced or custom
 * functionality is needed.
 * 
 * @author Olafur Gauti Gudmundsson
 */
public class JcrDataProviderImpl implements JcrDataProvider {

	protected final TYPE type;
	protected final byte[] bytes;
	protected final File file;
	protected final InputStream inputStream;
	
	public JcrDataProviderImpl( TYPE type, byte[] bytes ) {
		this.type = type;
		this.bytes = bytes;
		this.file = null;
		this.inputStream = null;
	}
	
	public JcrDataProviderImpl( TYPE type, File file ) {
		this.type = type;
		this.file = file;
		this.bytes = null;
		this.inputStream = null;
	}
	
	public JcrDataProviderImpl( TYPE type, InputStream inputStream ) {
		this.type = type;
		this.inputStream = inputStream;
		this.bytes = null;
		this.file = null;
	}
	
	public byte[] getBytes() {
		return bytes;
	}

	public File getFile() {
		return file;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public TYPE getType() {
		return type;
	}

}
