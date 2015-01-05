/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2015 - All rights reserved.
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
package org.jcrom.modeshape;

import java.util.List;

import javax.jcr.Session;

import org.jcrom.Jcrom;
import org.jcrom.dao.AbstractJcrDAO;
import org.jcrom.entities.Parent;
import org.jcrom.util.NodeFilter;

/**
 *
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
public class ParentDAO extends AbstractJcrDAO<Parent> {

    private static final String[] MIXIN_TYPES = { "mix:referenceable" };

    public ParentDAO(Session session, Jcrom jcrom) {
        super(Parent.class, session, jcrom, MIXIN_TYPES);
    }

    public List<Parent> findByLicense() throws Exception {
        return super.findByXPath("/jcr:root/content/parents/*[@drivingLicense = xs:boolean('true')]", new NodeFilter(NodeFilter.INCLUDE_ALL, NodeFilter.DEPTH_INFINITE));
    }
}
