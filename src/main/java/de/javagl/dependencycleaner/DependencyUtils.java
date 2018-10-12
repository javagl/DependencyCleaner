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

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;

/**
 * Utility methods for resolving Maven artifact dependencies, mostly
 * based on https://stackoverflow.com/a/40820480/3182664. 
 */
class DependencyUtils
{
    /**
     * Returns the dependencies of the specified artifact
     * 
     * @param artifactInfo The {@link ArtifactInfo}
     * @return The DependencyResult
     */
    static DependencyResult resolveDependencies(ArtifactInfo artifactInfo) 
    {
        Artifact artifact = new DefaultArtifact(
            artifactInfo.getGroupId() 
            + ":" + artifactInfo.getArtifactId() 
            + ":" + artifactInfo.getVersion());
        try
        {
            DependencyResult dependencyResult = 
                resolveDependenciesInternal(artifact);
            //print(dependencyResult);
            return dependencyResult;
        }
        catch (DependencyResolutionException e)
        {
            throw new DependencyCleanerException(e);
        }
    }
    
    /**
     * Implementation of the dependency resolution
     * 
     * @param artifact The Artifact
     * @return The DependencyResult
     * @throws DependencyResolutionException If the dependency could not be 
     * resolved
     */
    private static DependencyResult resolveDependenciesInternal(
        Artifact artifact)
        throws DependencyResolutionException
    {
        // Based on https://stackoverflow.com/a/40820480/3182664
        DefaultServiceLocator locator =
            MavenRepositorySystemUtils.newServiceLocator();
        RepositorySystem system = newRepositorySystem(locator);
        RepositorySystemSession session =
            newLocalRepositorySystemSession(system);
        RemoteRepository central = new RemoteRepository.Builder("central",
            "default", "http://repo1.maven.org/maven2/").build();

        Dependency dependency = new Dependency(artifact, JavaScopes.COMPILE);
        CollectRequest collectRequest = 
            new CollectRequest(dependency, Arrays.asList(central));
        DependencyFilter dependencyFilter = null;
        // TODO Could offer the option to define filters here:
        //    DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
        DependencyRequest dependencyRequest =
            new DependencyRequest(collectRequest, dependencyFilter);
        DependencyResult dependencyResult =
            system.resolveDependencies(session, dependencyRequest);

        return dependencyResult;
    }

    /**
     * Create a new RepositorySystem
     * 
     * @param locator The DefaultServiceLocator
     * @return The RepositorySystem
     */
    private static RepositorySystem newRepositorySystem(
        DefaultServiceLocator locator)
    {
        // From https://stackoverflow.com/a/40820480/3182664
        locator.addService(
            RepositoryConnectorFactory.class,
            BasicRepositoryConnectorFactory.class);
        locator.addService(
            TransporterFactory.class,
            FileTransporterFactory.class);
        locator.addService(
            TransporterFactory.class,
            HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    /**
     * Create a new RepositorySystemSession
     * 
     * @param system The RepositorySystem
     * @return The RepositorySystemSession
     */
    private static RepositorySystemSession
        newLocalRepositorySystemSession(RepositorySystem system)
    {
        // Based on https://stackoverflow.com/a/40820480/3182664
        DefaultRepositorySystemSession session =
            MavenRepositorySystemUtils.newSession();
        File localRepositoryFile =
            Paths.get(System.getProperty("user.home"), 
                ".m2/repository").toFile();
        LocalRepository localRepository = 
            new LocalRepository(localRepositoryFile);
        session.setLocalRepositoryManager(
            system.newLocalRepositoryManager(session, localRepository));
        return session;
    }

    /**
     * Debug print 
     * @param dependencyResult The DependencyResult
     */
    private static void print(DependencyResult dependencyResult)
    {
        System.out.println("DependencyResult " + dependencyResult);
        List<ArtifactResult> artifactResults = 
            dependencyResult.getArtifactResults();
        for (ArtifactResult artifactResult : artifactResults)
        {
            File file = artifactResult.getArtifact().getFile();
            System.out.println("   "+file);
        }
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private DependencyUtils()
    {
        // Private constructor to prevent instantiation
    }
}
