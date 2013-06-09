/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2013 - All rights reserved.
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
package org.jcrom.entities;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;

/**
 * Thanks to Leander for contributing this.
 * 
 * @author Olafur Gauti Gudmundsson
 * @author Nicolas Dos Santos
 */
@JcrNode(classNameProperty = "className")
public class Triangle extends AbstractJcrEntity implements Shape {

    private static final long serialVersionUID = 1L;

    @JcrProperty
    private double height;

    @JcrProperty
    private double base;

    /**
     * TODO Insert getter method comment here.
     *
     * @return the height
     */
    public final double getHeight() {
        return this.height;
    }

    /**
     * TODO Insert setter method comment here.
     *
     * @param height the height to set
     */
    public final void setHeight(final double height) {
        this.height = height;
    }

    /**
     * TODO Insert getter method comment here.
     *
     * @return the base
     */
    public final double getBase() {
        return this.base;
    }

    /**
     * TODO Insert setter method comment here.
     *
     * @param base the base to set
     */
    public final void setBase(final double base) {
        this.base = base;
    }

    /**
     * Default Constructor.
     */
    public Triangle() {
    }

    /**
     * Default Constructor.
     */
    public Triangle(final double base, final double height) {
        this.base = base;
        this.height = height;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jcrom.entities.Shape#getArea()
     */
    @Override
    public double getArea() {
        return (0.5d * this.base) * this.height;
    }

}
