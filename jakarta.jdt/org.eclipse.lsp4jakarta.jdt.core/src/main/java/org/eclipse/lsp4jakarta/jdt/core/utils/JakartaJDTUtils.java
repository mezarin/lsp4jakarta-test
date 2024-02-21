package org.eclipse.lsp4jakarta.jdt.core.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class JakartaJDTUtils {
    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(JakartaJDTUtils.class.getName());

    /** Jakarta project indicator. */
    public static final String JAKARTA_RS_GET = "jakarta.ws.rs.GET";

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
}
