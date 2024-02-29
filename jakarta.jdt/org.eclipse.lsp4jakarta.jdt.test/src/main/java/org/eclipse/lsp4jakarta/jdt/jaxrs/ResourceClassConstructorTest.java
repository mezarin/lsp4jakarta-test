/*******************************************************************************
 * Copyright (c) 2021, 2024 IBM Corporation, Matthew Shocrylas and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation, Matthew Shocrylas - initial API and implementation
 *******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.jaxrs;

import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.ca;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.d;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4jakarta.jdt.core.BaseJakartaTest;
import org.eclipse.lsp4jdt.commons.JavaCodeActionParams;
import org.eclipse.lsp4jdt.commons.JavaDiagnosticsParams;
import org.eclipse.lsp4jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jdt.participants.core.ls.JDTUtilsLSImpl;
import org.junit.Test;

public class ResourceClassConstructorTest extends BaseJakartaTest {
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void MultipleConstructorsWithEqualParams() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/jaxrs/RootResourceClassConstructorsEqualLen.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JavaDiagnosticsParams diagnosticsParams = new JavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test expected diagnostics
        Diagnostic d1 = d(7, 8, 45,
                          "Multiple constructors have the same number of parameters, it might be ambiguous which constructor is used.",
                          DiagnosticSeverity.Warning, "jakarta-jaxrs", "AmbiguousConstructors");

        Diagnostic d2 = d(11, 8, 45,
                          "Multiple constructors have the same number of parameters, it might be ambiguous which constructor is used.",
                          DiagnosticSeverity.Warning, "jakarta-jaxrs", "AmbiguousConstructors");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

    }

    @Test
    public void MultipleConstructorsWithDifferentLength() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/jaxrs/RootResourceClassConstructorsDiffLen.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JavaDiagnosticsParams diagnosticsParams = new JavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test expected diagnostics
        Diagnostic d = d(7, 8, 44,
                         "This constructor is unused, as root resource classes will only use the constructor with the most parameters.",
                         DiagnosticSeverity.Warning, "jakarta-jaxrs", "UnusedConstructor");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }

    @Test
    public void NoPublicConstructor() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/jaxrs/NoPublicConstructorClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JavaDiagnosticsParams diagnosticsParams = new JavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostics.
        Diagnostic d1 = d(7, 12, 36,
                          "Root resource classes are instantiated by the JAX-RS runtime and MUST have a public constructor.",
                          DiagnosticSeverity.Error, "jakarta-jaxrs", "NoPublicConstructors");

        Diagnostic d2 = d(11, 14, 38,
                          "Root resource classes are instantiated by the JAX-RS runtime and MUST have a public constructor.",
                          DiagnosticSeverity.Error, "jakarta-jaxrs", "NoPublicConstructors");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

        // Test expected quick fixes for diagnostic 1 (private constructor).
        String newText1 = "public NoPublicConstructorClass() {\n	}\n\n	";
        String newText2 = "public";
        JavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1 = te(7, 4, 7, 4, newText1);
        TextEdit te2 = te(7, 4, 7, 11, newText2);
        CodeAction ca1 = ca(uri, "Add a default 'public' constructor to this class", d1, te1);
        CodeAction ca2 = ca(uri, "Make constructor public", d1, te2);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1, ca2);

        // Test expected quick fixes for diagnostic 2 (protected constructor).
        JavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te3 = te(7, 4, 7, 4, newText1);
        TextEdit te4 = te(11, 4, 11, 13, newText2);
        CodeAction ca3 = ca(uri, "Add a default 'public' constructor to this class", d2, te3);
        CodeAction ca4 = ca(uri, "Make constructor public", d2, te4);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3, ca4);
    }

    @Test
    public void NoPublicConstructorProviderClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/jaxrs/NoPublicConstructorProviderClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JavaDiagnosticsParams diagnosticsParams = new JavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostics.
        Diagnostic d1 = d(19, 12, 44,
                          "Provider classes are instantiated by the JAX-RS runtime and MUST have a public constructor.",
                          DiagnosticSeverity.Error, "jakarta-jaxrs", "NoPublicConstructors");

        Diagnostic d2 = d(23, 14, 46,
                          "Provider classes are instantiated by the JAX-RS runtime and MUST have a public constructor.",
                          DiagnosticSeverity.Error, "jakarta-jaxrs", "NoPublicConstructors");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

        // Test expected quick fixes for diagnostic 1 (private constructor).
        String newText1 = "public NoPublicConstructorProviderClass() {\n	}\n\n	";
        String newText2 = "public";

        JavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit te1 = te(19, 4, 19, 4, newText1);
        TextEdit te2 = te(19, 4, 19, 11, newText2);
        CodeAction ca1 = ca(uri, "Add a default 'public' constructor to this class", d1, te1);
        CodeAction ca2 = ca(uri, "Make constructor public", d1, te2);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca1, ca2);

        // Test expected quick fixes for diagnostic 2 (protected constructor).
        JavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te3 = te(19, 4, 19, 4, newText1);
        TextEdit te4 = te(23, 4, 23, 13, newText2);
        CodeAction ca3 = ca(uri, "Add a default 'public' constructor to this class", d2, te3);
        CodeAction ca4 = ca(uri, "Make constructor public", d2, te4);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3, ca4);

    }
}
