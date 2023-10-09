/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.di;

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
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.jdt.core.BaseJakartaTest;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.junit.Test;

public class DependencyInjectionTest extends BaseJakartaTest {
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void DependencyInjectionDiagnostics() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/GreetingServlet.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Create expected diagnostics.
        Diagnostic d1 = d(17, 27, 35, "The @Inject annotation must not define a final field.",
                          DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnFinalField");
        // d1.setData(IType.FIELD);

        Diagnostic d2 = d(33, 25, 39, "The @Inject annotation must not define an abstract method.",
                          DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnAbstractMethod");
        // d2.setData(IType.METHOD);

        Diagnostic d3 = d(26, 22, 33, "The @Inject annotation must not define a final method.",
                          DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnFinalMethod");
        // d3.setData(IType.METHOD);

        Diagnostic d4 = d(43, 23, 36, "The @Inject annotation must not define a generic method.",
                          DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnGenericMethod");
        // d4.setData(IType.METHOD);

        Diagnostic d5 = d(37, 23, 35, "The @Inject annotation must not define a static method.",
                          DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnStaticMethod");
        // d5.setData(IType.METHOD);

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5);

        // Create expected quick fixes.
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit te = te(16, 4, 17, 4, "");
        CodeAction ca = ca(uri, "Remove @Inject", d1, te);
        TextEdit te1 = te(17, 11, 17, 17, "");
        CodeAction ca1 = ca(uri, "Remove the 'final' modifier", d1, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca, ca1);

        codeActionParams = createCodeActionParams(uri, d2);
        te = te(32, 4, 33, 4, "");
        ca = ca(uri, "Remove @Inject", d2, te);
        te1 = te(33, 10, 33, 19, "");
        ca1 = ca(uri, "Remove the 'abstract' modifier", d2, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca, ca1);

        codeActionParams = createCodeActionParams(uri, d3);
        te = te(25, 4, 26, 4, "");
        ca = ca(uri, "Remove @Inject", d3, te);
        te1 = te(26, 10, 26, 16, "");
        ca1 = ca(uri, "Remove the 'final' modifier", d3, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca, ca1);

        codeActionParams = createCodeActionParams(uri, d4);
        te = te(42, 4, 43, 4, "");
        ca = ca(uri, "Remove @Inject", d4, te);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca);

        codeActionParams = createCodeActionParams(uri, d5);
        te = te(36, 4, 37, 4, "");
        ca = ca(uri, "Remove @Inject", d5, te);
        te1 = te(37, 10, 37, 17, "");
        ca1 = ca(uri, "Remove the 'static' modifier", d5, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca, ca1);
    }
}
