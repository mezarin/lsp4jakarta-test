/*******************************************************************************
* Copyright (c) 2021, 2022 IBM Corporation.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Himanshu Chotwani
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.di;

/**
 * Dependency injection diagnostic constants.
 */
public class Constants {

    /* Annotation Constants */
    public static final String PRODUCES = "Produces";
    public static final String INJECT = "Inject";
    public static final String INJECT_FQ_NAME = "jakarta.inject.Inject";
    public static final String QUALIFIER = "Qualifier";
    public static final String NAMED = "Named";

    /* Diagnostics fields constants */
    public static final String DIAGNOSTIC_SOURCE = "jakarta-di";
}
