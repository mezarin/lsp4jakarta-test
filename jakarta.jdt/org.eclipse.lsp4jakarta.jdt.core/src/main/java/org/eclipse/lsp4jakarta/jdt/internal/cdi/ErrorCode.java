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

import org.eclipse.lsp4jdt.core.java.diagnostics.IJavaErrorCode;

/**
 * Contexts and Dependency Injection (CDI) error code.
 */
public enum ErrorCode implements IJavaErrorCode {
    InvalidNumberOfScopedAnnotationsByManagedBean,
    InvalidManagedBeanWithNonStaticPublicField,
    InvalidManagedBeanWithNonStaticPublicFieldInvalidScope,
    InvalidNumberOfScopeAnnotationsByProducerField,
    InvalidFieldWithProducesAndInjectAnnotations,
    InvalidNumberOfScopeAnnotationsByProducerMethod,
    InvalidMethodWithProducesAndInjectAnnotations,
    InvalidManagedBeanWithInvalidConstructor,
    InvalidGenericManagedBeanClassWithNoDependentScope,
    InvalidDisposesAnnotationOnMultipleMethodParams,
    InvalidDisposerMethodParamAnnotation,
    InvalidProducerMethodParamAnnotation,
    InvalidInjectAnnotatedMethodParamAnnotation;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return name();
    }
}
