/*
 * Copyright 2011 William Bernardet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.japi.checker;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public final class Utils {
    
    private Utils() { }
    
    public static String fixNull(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }
    
    public static String fixEmpty(String str) {
        if (str == null || "".equals(str)) {
            return null;
        }
        return str;
    }
    
    /**
     * Check if file denote a zip kind of archive.
     * @param file
     * @return
     */
    public static boolean isArchive(File file) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            zf.entries(); // forcing to do something with the file.
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e) {
                    // swallow the exception...
                }
            }
        }
    }

}
