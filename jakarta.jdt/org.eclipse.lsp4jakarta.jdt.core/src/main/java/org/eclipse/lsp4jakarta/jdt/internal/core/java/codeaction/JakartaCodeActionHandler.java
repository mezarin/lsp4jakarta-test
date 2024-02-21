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
package org.eclipse.lsp4jakarta.jdt.internal.core.java.codeaction;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4jdt.participants.core.java.codeaction.AbstractCodeActionHandler;

/**
 * Jakarta customization of the LSP4JDT's code action handler.
 */
public class JakartaCodeActionHandler extends AbstractCodeActionHandler {

    /** Singleton JakartaCodeActionHandler instance. */
    public static final JakartaCodeActionHandler INSTANCE = new JakartaCodeActionHandler();

    /**
     * Returns an instance of JakartaCodeActionHandler.
     *
     * @return An instance of JakartaCodeActionHandler.
     */
    public static JakartaCodeActionHandler getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getDefaultSupportedKinds() {
        return Arrays.asList(CodeActionKind.QuickFix);
    }

}