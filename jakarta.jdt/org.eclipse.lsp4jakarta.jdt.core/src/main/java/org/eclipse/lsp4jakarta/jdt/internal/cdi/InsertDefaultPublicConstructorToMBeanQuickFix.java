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
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.InsertDefaultConstructorToClassQuickFix;
import org.eclipse.lspcommon.commons.codeaction.ICodeActionId;

/**
 * Inserts a public default constructor the active managed bean class.
 */
public class InsertDefaultPublicConstructorToMBeanQuickFix extends InsertDefaultConstructorToClassQuickFix {

    /**
     * Constructor.
     */
    public InsertDefaultPublicConstructorToMBeanQuickFix() {
        super("public");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertDefaultPublicConstructorToMBeanQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIInsertPublicCtrtToClass;
    }
}
