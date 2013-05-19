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

import javax.jcr.Session;
import org.jcrom.dao.AbstractJcrDAO;

/**
 * Thanks to Danilo Barboza for contributing this test class.
 */
public class CustomJCRFileDAO extends AbstractJcrDAO<CustomJCRFile> {

	/**
	 * @param session
	 * @param jcrom
	 */
	public CustomJCRFileDAO(Session session, Jcrom jcrom) {
		super(CustomJCRFile.class, session, jcrom);
	}
}
