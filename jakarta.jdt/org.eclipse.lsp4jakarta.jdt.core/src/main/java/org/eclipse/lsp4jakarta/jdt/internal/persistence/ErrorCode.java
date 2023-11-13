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
package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import org.eclipse.lspcommon.jdt.core.java.diagnostics.IJavaErrorCode;

/**
 * Persistence error code.
 */
public enum ErrorCode implements IJavaErrorCode {
    InvalidFinalMethodInEntityAnnotatedClass,
    InvalidPersistentFieldInEntityAnnotatedClass,
    InvalidConstructorInEntityAnnotatedClass,
    InvalidFinalModifierOnEntityAnnotatedClass,
    InvalidMapKeyAnnotationsOnSameMethod,
    InvalidMapKeyAnnotationsOnSameField,
    InvalidMethodWithMultipleMPJCAnnotations,
    InvalidFieldWithMultipleMPJCAnnotations;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return name();
    }
}
