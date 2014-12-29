/**
 * This file is part of the JCROM project.
 * Copyright (C) 2008-2014 - All rights reserved.
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
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Nicolas Dos Santos
 */
public class ColorListConverter implements Converter<List<Color>, List<String>> {

    private final ColorConverter colorConverter = new ColorConverter();

    /**
     * Convert list of Color objects to a list of Strings with format red:green:blue:alpha
     */
    @Override
    public List<String> convertToJcrProperty(List<Color> colors) {
        List<String> list = new ArrayList<String>();
        for (Color color : colors) {
            list.add(colorConverter.convertToJcrProperty(color));
        }
        return list;
    }

    /**
     * Convert a list of Strings with format red:green:blue:alpha to a list of Color objects
     */
    @Override
    public List<Color> convertToEntityAttribute(List<String> colorStrings) {
        List<Color> list = new ArrayList<Color>();
        for (String colorString : colorStrings) {
            list.add(colorConverter.convertToEntityAttribute(colorString));
        }
        return list;
    }

}
