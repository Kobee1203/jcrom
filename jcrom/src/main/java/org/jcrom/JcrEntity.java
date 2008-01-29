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
	
	/**
	 * Get the JCR node name for this entity. Note that JCROM will
	 * automatically transform the name to a valid path name
	 * (replacing non-alphanumerical chars with an underscore).
	 * 
	 * @return the JCR node name for this entity
	 */
	public String getName();
	
	/**
	 * This method is called from JCROM when mapping this entity
	 * from a JCR node.
	 * 
	 * @param name
	 */
	public void setName( String name );
	
	/**
	 * Get the full JCR path for this entity. This will be empty until
	 * the entity has been mapped from JCR.
	 * 
	 * @return the full JCR path for the entity
	 */
	public String getPath();
	
	/**
	 * This method is called from JCROM when mapping this entity
	 * from a JCR node.
	 * 
	 * @param path
	 */
	public void setPath( String path );
}
