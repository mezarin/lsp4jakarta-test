/*******************************************************************************
* Copyright (c) 2019, 2023 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.lsp4jakarta.commons.ClasspathKind;

/**
 * JDT Jakarta utilities.
 * 
 * Based on:
 * https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/core/utils/JDTMicroProfileUtils.java
 *
 * @author Angelo ZERR
 */
public class JDTJakartaUtils {
	/** Logger object to record events for this class. */
	private static final Logger LOGGER = Logger.getLogger(JDTJakartaUtils.class.getName());

	/** Jakarta project indicator. */
	public static final String JAKARTA_RS_GET = "jakarta.ws.rs.GET";

	/**
	 * Returns the project URI of the given project.
	 *
	 * @param project the java project
	 * @return the project URI of the given project.
	 */
	public static String getProjectURI(IJavaProject project) {
		return getProjectURI(project.getProject());
	}

	/**
	 * returns the project URI of the given project.
	 *
	 * @param project the project
	 * @return the project URI of the given project.
	 */
	public static String getProjectURI(IProject project) {
		return project.getLocation().toOSString();
	}

	/**
	 * Returns true if the given resource <code>resource</code> is on the 'test'
	 * classpath of the given java project <code>javaProject</code> and false
	 * otherwise.
	 *
	 * @param resource    the resource
	 * @param javaProject the project.
	 * @return true if the given resource <code>resource</code> is on the 'test'
	 *         classpath of the given java project <code>javaProject</code> and
	 *         false otherwise.
	 */
	public static ClasspathKind getClasspathKind(IResource resource, IJavaProject javaProject) {
		IPath exactPath = resource.getFullPath();
		IPath path = exactPath;

		// ensure that folders are only excluded if all of their children are excluded
		int resourceType = resource.getType();
		boolean isFolderPath = resourceType == IResource.FOLDER || resourceType == IResource.PROJECT;

		IClasspathEntry[] classpath;
		try {
			classpath = ((JavaProject) javaProject).getResolvedClasspath();
		} catch (JavaModelException e) {
			return ClasspathKind.NONE; // not a Java project
		}
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			IPath entryPath = entry.getPath();
			if (entryPath.equals(exactPath)) { // package fragment roots must match exactly entry pathes (no exclusion
												// there)
				return getClasspathKind(entry);
			}
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=276373
			// When a classpath entry is absolute, convert the resource's relative path to a
			// file system path and compare
			// e.g - /P/lib/variableLib.jar and /home/P/lib/variableLib.jar when compared
			// should return true
			if (entryPath.isAbsolute()
					&& entryPath.equals(ResourcesPlugin.getWorkspace().getRoot().getLocation().append(exactPath))) {
				return getClasspathKind(entry);
			}
			if (entryPath.isPrefixOf(path)) {
				// && !Util.isExcluded(path, ((ClasspathEntry)
				// entry).fullInclusionPatternChars(),
				// ((ClasspathEntry) entry).fullExclusionPatternChars(), isFolderPath)) {
				return getClasspathKind(entry);
			}
		}
		return ClasspathKind.NONE;

	}

	/**
	 * Returns true if the given <code>project</code> has a nature specified by
	 * <code>natureId</code> and false otherwise.
	 *
	 * @param project  the project
	 * @param natureId the nature id
	 * @return true if the given <code>project</code> has a nature specified by
	 *         <code>natureId</code> and false otherwise.
	 */
	public static boolean hasNature(IProject project, String natureId) {
		try {
			return project != null && project.hasNature(natureId);
		} catch (CoreException e) {
			return false;
		}
	}

	private static ClasspathKind getClasspathKind(IClasspathEntry entry) {
		return entry.isTest() ? ClasspathKind.TEST : ClasspathKind.SRC;
	}

	/**
	 * Returns true if <code>javaProject</code> is a Jakarta project. Returns
	 * false otherwise.
	 *
	 * @param javaProject the Java project to check
	 * @return true only if <code>javaProject</code> is a Jakarta project.
	 */
	public static boolean isJakartaProject(IJavaProject javaProject) {

		// Here we make a determination if we are in a jakarta project - we look for a
		// well known
		// jakarta class on the classpath - which it will find if the project has the
		// jakarta EE dependency in it pom
		try {
			return javaProject.findType(JAKARTA_RS_GET) != null;
		} catch (JavaModelException e) {
			LOGGER.log(Level.INFO, "Current Java project is not a Jakarta project", e);
			return false;
		}

	}

	/**
	 * Returns an array of all the java projects that are currently loaded into the
	 * JDT
	 * workspace.
	 *
	 * @return an array of all the projects that are currently loaded into the JDT
	 *         workspace
	 */
	public static IJavaProject[] getJavaProjects() {
		return Stream.of(getAllProjects()) //
				.filter(JDTJakartaUtils::isJavaProject) //
				.map(p -> JavaCore.create(p)) //
				.filter(p -> p != null) //
				.toArray(IJavaProject[]::new);
	}

	/**
	 * Returns an array of all the projects that are currently loaded into the JDT
	 * workspace.
	 *
	 * @return an array of all the projects that are currently loaded into the JDT
	 *         workspace
	 */
	private static IProject[] getAllProjects() {
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}

	private static boolean isJavaProject(IProject project) {
		if (project == null) {
			return false;
		}
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			return false;
		}
	}
}
