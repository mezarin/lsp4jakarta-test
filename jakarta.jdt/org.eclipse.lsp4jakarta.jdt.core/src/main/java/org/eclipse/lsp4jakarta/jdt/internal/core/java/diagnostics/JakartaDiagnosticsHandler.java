/*******************************************************************************
* Copyright (c) 2024 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.internal.core.java.diagnostics;

import org.eclipse.lsp4jdt.participants.core.java.diagnostics.AbstractDiagnosticsHandler;

/**
 * Jakarta customization of the LSP4JDT's diagnostic handler.
 */
public class JakartaDiagnosticsHandler extends AbstractDiagnosticsHandler {

    /** Singleton JakartaDiagnosticsHandler instance. */
    public static final JakartaDiagnosticsHandler INSTANCE = new JakartaDiagnosticsHandler();

    /**
     * Returns an instance of JakartaDiagnosticsHandler.
     *
     * @return An instance of JakartaDiagnosticsHandler.
     */
    public static JakartaDiagnosticsHandler getInstance() {
        return INSTANCE;
    }
}
