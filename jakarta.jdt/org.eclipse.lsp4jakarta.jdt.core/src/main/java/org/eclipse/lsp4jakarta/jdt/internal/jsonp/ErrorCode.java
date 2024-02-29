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
package org.eclipse.lsp4jakarta.jdt.internal.jsonp;

import org.eclipse.lsp4jdt.core.java.diagnostics.IJavaErrorCode;

/**
 * JSON Processing (JSON-P) error code.
 */
public enum ErrorCode implements IJavaErrorCode {
    InvalidJsonCreatePointerTarget;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return name();
    }
}
