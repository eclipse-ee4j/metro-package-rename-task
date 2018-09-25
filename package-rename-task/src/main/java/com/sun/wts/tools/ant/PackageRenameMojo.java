/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.wts.tools.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

/**
 * Creates renamed source files under the specified directory
 * (defaults to <tt>target/generated-sources/renamed-sources</tt>)
 *
 * @goal rename
 * @phase generate-sources
 * @requiresProject
 * @requiresDependencyResolution runtime
 *
 * @author Kohsuke Kawaguchi
 * @author Lukas Jungmann
 */
public class PackageRenameMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The root directory to put the renamed source files.
     *
     * @parameter expression="${renamedSourcesDir}" default-value="target/generated-sources/renamed-sources"
     */
    private File rootDir;

    /**
     * The root directory to put the renamed source files.
     *
     * @parameter expression="${srcSourcesDir}" default-value="${project.build.directory}/dependency"
     */
    private File srcDir;

    /**
     * Rename patterns as a map.
     *
     * If pattern value (rename to value) contains "/" character it will be used as separator for target directory
     * of the specified pattern. For example:
     *
     * {@code <com.sun.xml>java.xml.bind/com.sun.xml.internal</com.sun.xml>}
     *
     * Will be renamed to com.sun.xml.internal and put into java.xml.bind directory.
     *
     * @parameter
     */
    private Map patterns;

    /**
     * Exclude patterns when renaming (pattern to keep)
     *
     * @parameter
     */
    private String excludes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(patterns==null)
            throw new MojoExecutionException("No replacement patterns given");

        List<File> sources = new ArrayList<File>();
        if (srcDir != null && srcDir.exists() && srcDir.isDirectory()) {
            sources.add(srcDir);
        } else {
            for (String p: (List<String>) project.getCompileSourceRoots()) {
                sources.add(new File(p));
            }
        }
        for (File dir : sources) {
            PackageRenameTask task = new PackageRenameTask();
            task.setProject(createAntProject());
            task.setDestdir(rootDir);
            task.setSrcDir(dir);
            for (Map.Entry<String,String> e : (Collection<Entry<String,String>>)patterns.entrySet()) {
                RenamePattern pattern = new RenamePattern(e.getKey(), e.getValue());
                if (excludes != null) {
                    pattern.setExcludes(excludes);
                }
                task.addConfiguredPattern(pattern);
            }
            task.execute();
        }
            if (project != null) {
            project.addCompileSourceRoot(rootDir.getAbsolutePath());
        }
    }

    private Project createAntProject() {
        Project p = new Project();

        DefaultLogger antLogger = new DefaultLogger();
        antLogger.setOutputPrintStream( System.out );
        antLogger.setErrorPrintStream( System.err );
        antLogger.setMessageOutputLevel( getLog().isDebugEnabled() ? Project.MSG_DEBUG : Project.MSG_INFO );
        p.addBuildListener(antLogger);
        return p;
    }
}
