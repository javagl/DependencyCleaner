/*
 * www.javagl.de - DependencyCleaner
 *
 * Copyright (c) 2018 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.dependencycleaner;

import java.util.Objects;

/**
 * POJO summarizing an artifact
 */
class ArtifactInfo
{
    /**
     * The group ID
     */
    private final String groupId;
    
    /**
     * The artifact ID
     */
    private final String artifactId;
    
    /**
     * The version
     */
    private final String version;
    
    /**
     * Create a new instance
     * 
     * @param groupId The group ID
     * @param artifactId The artifact ID
     * @param version The version
     */
    ArtifactInfo(String groupId, String artifactId, String version)
    {
        this.groupId = Objects.requireNonNull(
            groupId, "The groupId may not be null");
        this.artifactId = Objects.requireNonNull(
            artifactId, "The artifactId may not be null");
        this.version = Objects.requireNonNull(
            version, "The version may not be null");
    }
    
    /**
     * Returns the group ID
     * 
     * @return The group ID
     */
    String getGroupId()
    {
        return groupId;
    }
    
    /**
     * Returns the artifact ID
     * 
     * @return The artifact ID
     */
    String getArtifactId()
    {
        return artifactId;
    }
    
    /**
     * Returns the version
     * 
     * @return The version
     */
    String getVersion()
    {
        return version;
    }
    
    @Override
    public String toString()
    {
        return groupId + ":" + artifactId + ":" + version;
    }
}
