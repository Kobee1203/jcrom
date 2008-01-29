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

import java.io.Serializable;

/**
 * This is the main interface which all objects that are to be mapped
 * to/from JCR must implement.
 * 
 * @author Olafur Gauti Gudmundsson
 */
public interface JcrEntity extends Serializable {
	
	public String getName();
	public void setName( String name );
	
	public String getPath();
	public void setPath( String path );
}
