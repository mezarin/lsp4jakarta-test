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
package org.eclipse.lsp4jakarta.jdt.internal.annotations;

import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.InsertAnnotationAttributesQuickFix;
import org.eclipse.lsp4jdt.commons.codeaction.ICodeActionId;

/**
 * Inserts the type attribute to the @Resource annotation to the active element.
 */
public class InsertTypeAttributeToResourceAnnotation extends InsertAnnotationAttributesQuickFix {

    /**
     * Constructor.
     */
    public InsertTypeAttributeToResourceAnnotation() {
        super("jakarta.annotation.Resource", "type");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertTypeAttributeToResourceAnnotation.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.InsertResourceAnnotationTypeAttribute;
    }
}
