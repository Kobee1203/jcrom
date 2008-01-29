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
 *
 * @author Olafur Gauti Gudmundsson
 */
public class JcrDataProviderImpl implements JcrDataProvider {

	protected int type;
	protected byte[] bytes;
	protected File file;
	protected InputStream inputStream;
	
	public JcrDataProviderImpl( int type ) {
		this.type = type;
	}
	
	public void setBytes( byte[] bytes ) {
		this.bytes = bytes;
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	
	public void setFile( File file ) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}
	
	public void setInputStream( InputStream inputStream ) {
		this.inputStream = inputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
	
	public void setType( int type ) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
