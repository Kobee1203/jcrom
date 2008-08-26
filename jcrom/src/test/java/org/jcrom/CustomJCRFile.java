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

import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;

/**
 * Thanks to Danilo Barboza for contributing this test class.
 */
@JcrNode(nodeType = "nt:unstructured")
public class CustomJCRFile extends JcrFile {

	@JcrProperty
	private String metadata;
    
    public CustomJCRFile() {
        super();
    }

	/**
	 * @return the metadata
	 */
	public String getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	} 
}
