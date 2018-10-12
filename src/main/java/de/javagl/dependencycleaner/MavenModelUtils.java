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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Utility methods related to Maven models
 */
class MavenModelUtils
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(MavenModelUtils.class.getName());
    
    /**
     * Read the {@link ArtifactInfo} for the specified POM. Returns
     * <code>null</code> if an error occurs.
     * 
     * @param pom The POM
     * @return The {@link ArtifactInfo}
     */
    static ArtifactInfo readArtifactInfo(Path pom)
    {
        try (Reader reader = new FileReader(pom.toFile()))
        {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);
            String groupId = model.getGroupId();
            String artifactId = model.getArtifactId();
            String version = model.getVersion();
            if (groupId == null)
            {
                Parent parent = model.getParent();
                groupId = parent.getGroupId();
            }
            if (version == null)
            {
                Parent parent = model.getParent();
                version = parent.getVersion();
            }
            ArtifactInfo artifactInfo = new ArtifactInfo(
                groupId, artifactId, version);
            return artifactInfo;
        }
        catch (IOException e) 
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        catch (XmlPullParserException e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Read the {@link ArtifactInfo} for the dependencies of the specified 
     * POM. Returns <code>null</code> if an error occurs.
     * 
     * @param pom The POM
     * @return The {@link ArtifactInfo} objects
     */
    static List<ArtifactInfo> readDependencyArtifactInfos(Path pom)
    {
        try (Reader reader = new FileReader(pom.toFile()))
        {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);
            List<Dependency> dependencies = model.getDependencies();
            List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
            
            Properties properties = model.getProperties();
            Map<String, String> substitutions = 
                new LinkedHashMap<String, String>();
            for (Entry<Object, Object> entry : properties.entrySet())
            {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if ((key instanceof String) && (value instanceof String))
                {
                    substitutions.put(
                        String.valueOf(key), String.valueOf(value));
                }
            }
            StrSubstitutor strSubstitutor = new StrSubstitutor(substitutions);
            
            for (Dependency dependency : dependencies)
            {
                String groupId = dependency.getGroupId();
                String artifactId = dependency.getArtifactId();
                String version = dependency.getVersion();
                
                groupId = strSubstitutor.replace(groupId);
                artifactId = strSubstitutor.replace(artifactId);
                version = strSubstitutor.replace(version);
                
                ArtifactInfo artifactInfo = new ArtifactInfo(
                    groupId, artifactId, version);
                artifactInfos.add(artifactInfo);
            }
            return artifactInfos;
        }
        catch (IOException e) 
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        catch (XmlPullParserException e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }
    
    
    /**
     * Private constructor to prevent instantiation
     */
    private MavenModelUtils()
    {
        // Private constructor to prevent instantiation
    }
    
}
