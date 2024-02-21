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
package org.eclipse.lsp4jakarta.jdt.core;

import org.eclipse.lsp4jakarta.jdt.internal.core.java.codeaction.JakartaCodeActionHandler;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.diagnostics.JakartaDiagnosticsHandler;
import org.eclipse.lsp4jdt.core.AbstractPropertiesManagerForJava;
import org.eclipse.lsp4jdt.participants.core.java.codeaction.AbstractCodeActionHandler;
import org.eclipse.lsp4jdt.participants.core.java.diagnostics.AbstractDiagnosticsHandler;

public class JakartaPropertiesManagerForJava extends AbstractPropertiesManagerForJava {

    /** Singleton JakartaDiagnosticsHandler instance. */
    private static final JakartaPropertiesManagerForJava INSTANCE = new JakartaPropertiesManagerForJava();

    JakartaPropertiesManagerForJava() {
        super();
    }

    /**
     * Returns an instance of JakartaPropertiesManagerForJava.
     *
     * @return An instance of JakartaPropertiesManagerForJava.
     */
    public static JakartaPropertiesManagerForJava getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractCodeActionHandler getCodeActionHandler() {
        return JakartaCodeActionHandler.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractDiagnosticsHandler getDiagnosticsHandler() {
        return JakartaDiagnosticsHandler.getInstance();
    }

    /**
     * Returns the Jakarta plugin id.
     *
     * @return The Jakarta plugin id.
     */
    public String getPluginId() {
        return JakartaCorePlugin.PLUGIN_ID;
    }
}
