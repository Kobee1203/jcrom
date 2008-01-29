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
public interface JcrDataProvider {

	public static final int FILE = 1;
	public static final int BYTES = 2;
	public static final int STREAM = 3;
	
	public int getType();
	
	public File getFile();
	
	public byte[] getBytes();
	
	public InputStream getInputStream();
}
