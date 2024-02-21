/*******************************************************************************
* Copyright (c) 2023 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4jdt.commons.JavaCompletionParams;
import org.eclipse.lsp4jdt.commons.JavaCompletionResult;

/**
 * Jakarta completion provider.
 */
public interface JakartaJavaCompletionProvider {

    @JsonRequest("jakarta/java/completion")
    CompletableFuture<JavaCompletionResult> getJavaCompletion(JavaCompletionParams javaParams);

}
