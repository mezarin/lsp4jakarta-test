/*******************************************************************************
* Copyright (c) 2021 IBM Corporation.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Ananya Rao
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

public class MultipleConstructorInjectTest extends BaseJakartaTest {

	protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

	@Test
	public void multipleInject() throws Exception {
		IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/MultipleConstructorWithInject.java"));
		String uri = javaFile.getLocation().toFile().toURI().toString();

		JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
		diagnosticsParams.setUris(Arrays.asList(uri));

		// test expected diagnostic
		Diagnostic d1 = d(22, 11, 40,
				"The @Inject annotation must not define more than one constructor.",
				DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnMultipleConstructors");

		Diagnostic d2 = d(26, 11, 40,
				"The @Inject annotation must not define more than one constructor.",
				DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnMultipleConstructors");

		Diagnostic d3 = d(31, 14, 43,
				"The @Inject annotation must not define more than one constructor.",
				DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnMultipleConstructors");

		assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);

		// test expected quick-fix
		JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
		TextEdit te = te(21, 4, 22, 4, "");
		CodeAction ca = ca(uri, "Remove @Inject", d1, te);
		assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca);

		JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d3);
		TextEdit te2 = te(30, 4, 31, 4, "");
		CodeAction ca2 = ca(uri, "Remove @Inject", d3, te2);
		assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca2);
	}
}
