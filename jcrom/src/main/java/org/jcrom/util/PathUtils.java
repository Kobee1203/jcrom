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
package org.jcrom.util;

/**
 * Contains utilities used for JCR node names and paths.
 * 
 * @author Olafur Gauti Gudmundsson
 */
public class PathUtils {
	
	/**
     * Replaces occurences of non-alphanumeric characters with a
     * supplied char. A non-alphanumeric character at the beginning or end
	 * is replaced with ''.
     */
    public static String replaceNonAlphanumeric(String str, char subst) {
        StringBuffer ret = new StringBuffer(str.length());
        char[] testChars = str.toCharArray();
		char lastChar = 'A';
        for (int i = 0; i < testChars.length; i++) {
            if (Character.isLetterOrDigit(testChars[i])) {
                ret.append(testChars[i]);
				lastChar = testChars[i];
            } else if ( i > 0 && (i+1) != testChars.length && lastChar != subst ) {
                ret.append( subst );
				lastChar = subst;
            }
        }
		return ret.toString();
    }
	
	/**
	 * Creates a valid JCR node name from the String supplied, by
	 * replacing all non-alphanumeric chars.
	 * 
	 * @param str the input String
	 * @return a valid JCR node name for the String
	 */
	public static String createValidName( String str ) {
		return replaceNonAlphanumeric(str, '_');
	}
}
