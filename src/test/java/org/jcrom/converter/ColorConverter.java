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
package org.jcrom.converter;

import java.awt.Color;


/**
 * 
 * @author Nicolas Dos Santos
 */
public class ColorConverter implements Converter<Color, String> {

    private static final String SEPARATOR = ":";

    /**
     * Convert Color object to a String with format red:green:blue:alpha
     */
    @Override
    public String convertToJcrProperty(Color color) {
        StringBuilder sb = new StringBuilder();
        sb.append(color.getRed());
        sb.append(SEPARATOR);
        sb.append(color.getGreen());
        sb.append(SEPARATOR);
        sb.append(color.getBlue());
        sb.append(SEPARATOR);
        sb.append(color.getAlpha());
        return sb.toString();
    }

    /**
     * Convert a String with format red:green:blue:alpha to a Color object
     */
    @Override
    public Color convertToEntityAttribute(String colorString) {
        String[] rgb = colorString.split(SEPARATOR);
        return new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]), Integer.parseInt(rgb[3]));
    }

}
