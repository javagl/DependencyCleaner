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

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.resolution.DependencyResult;

/**
 * Methods to create {@link DependencyStatus} instances
 */
class DependencyStatuses
{
    /**
     * Compute the {@link DependencyStatus} for the given {@link ArtifactInfo}
     * objects
     * 
     * @param artifactInfos The {@link ArtifactInfo} objects
     * @return The {@link DependencyStatus}
     */
    static DependencyStatus compute(
        Iterable<? extends ArtifactInfo> artifactInfos) 
    {
        DependencyTreeNode root = new DependencyTreeNode("Root", null, null);
        for (ArtifactInfo artifactInfo : artifactInfos)
        {
            DependencyResult dependencyResult = 
                DependencyUtils.resolveDependencies(artifactInfo);
            DependencyNode dependencyNode = dependencyResult.getRoot();
            DependencyTreeNode dependencyTreeNode = buildTree(dependencyNode);
            root.addChild(dependencyTreeNode);
        }
        
        Set<Path> paths = computePaths(root);
        
        Set<Path> pathsToRefresh = new LinkedHashSet<Path>();
        Map<Path, Boolean> validPaths = new LinkedHashMap<Path, Boolean>();
        for (Path path : paths)
        {
            boolean valid = Utils.isValidJar(path);
            validPaths.put(path, valid);
            if (!valid)
            {
                pathsToRefresh.add(path);
            }
        }
        DependencyStatus dependencyStatus = 
            new DependencyStatus(root, validPaths, pathsToRefresh);
        return dependencyStatus;
    }
    
    /**
     * Recursively build the {@link DependencyTreeNode} tree based on the
     * given DependencyNode
     * 
     * @param node The node
     * @return The resulting node
     */
    private static DependencyTreeNode buildTree(DependencyNode node)
    {
        Artifact artifact = node.getArtifact();
        String name = artifact.toString();
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        Path path = null;
        if (artifact.getFile() != null)
        {
            path = artifact.getFile().toPath();
        }
        ArtifactInfo artifactInfo = 
            new ArtifactInfo(groupId, artifactId, version);
        DependencyTreeNode result = 
            new DependencyTreeNode(name, artifactInfo, path);
        for (DependencyNode child : node.getChildren())
        {
            DependencyTreeNode childTree = buildTree(child);
            result.addChild(childTree);
        }
        return result;
    }
    
    /**
     * Compute all paths that appear in the given {@link DependencyTreeNode}
     * 
     * @param dependencyTreeNode The {@link DependencyTreeNode}
     * @return The paths
     */
    private static Set<Path> computePaths(
        DependencyTreeNode dependencyTreeNode)
    {
        Set<Path> paths = new LinkedHashSet<Path>();
        computePaths(dependencyTreeNode, paths);
        return paths;
    }
    
    /**
     * Compute all paths that appear in the given {@link DependencyTreeNode}
     * 
     * @param dependencyTreeNode The {@link DependencyTreeNode}
     * @param paths The target set
     */
    private static void computePaths(
        DependencyTreeNode dependencyTreeNode, Set<Path> paths)
    {
        Path path = dependencyTreeNode.getPath();
        if (path != null)
        {
            paths.add(path);
        }
        for (DependencyTreeNode child : dependencyTreeNode.getChildren())
        {
            computePaths(child, paths);
        }
    }
    

    /**
     * Private constructor to prevent instantiation
     */
    private DependencyStatuses()
    {
        // Private constructor to prevent instantiation
    }
}
