/*******************************************************************************
* Copyright (c) 2020, 2023 IBM Corporation, Ankush Sharma and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Ankush Sharma - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;

/**
 * @author ankushsharma
 * @brief Diagnostics implementation for Jakarta Persistence 3.0
 */

// Imports
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lspcommon.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lspcommon.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lspcommon.jdt.core.utils.IJDTUtils;
import org.eclipse.lspcommon.jdt.core.utils.PositionUtils;
import org.eclipse.lspcommon.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Persistence diagnostic participant that manages the use of @Entity
 * annotations.
 */
public class PersistenceEntityDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] alltypes;
        IAnnotation[] allAnnotations;

        alltypes = unit.getAllTypes();
        for (IType type : alltypes) {
            allAnnotations = type.getAnnotations();

            IAnnotation EntityAnnotation = null;
            for (IAnnotation annotation : allAnnotations) {
                if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                         Constants.ENTITY)) {
                    EntityAnnotation = annotation;
                }
            }

            if (EntityAnnotation != null) {
                // Define boolean requirements for the diagnostics
                boolean hasPublicOrProtectedNoArgConstructor = false;
                boolean hasArgConstructor = false;
                boolean isEntityClassFinal = false;

                // Get the Methods of the annotated Class
                for (IMethod method : type.getMethods()) {
                    if (DiagnosticUtils.isConstructorMethod(method)) {
                        // We have found a method that is a constructor
                        if (method.getNumberOfParameters() > 0) {
                            hasArgConstructor = true;
                            continue;
                        }
                        // Don't need to perform subtractions to check flags because eclipse notifies on
                        // illegal constructor modifiers
                        if (method.getFlags() != Flags.AccPublic && method.getFlags() != Flags.AccProtected)
                            continue;
                        hasPublicOrProtectedNoArgConstructor = true;
                    }
                    // All Methods of this class should not be final
                    if (isFinal(method.getFlags())) {
                        Range range = PositionUtils.toNameRange(method, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("EntityNoFinalMethods"), range,
                                                                 Constants.DIAGNOSTIC_SOURCE, method.getElementType(),
                                                                 ErrorCode.InvalidFinalMethodInEntityAnnotatedClass, DiagnosticSeverity.Error));
                    }
                }

                // Go through the instance variables and make sure no instance vars are final
                for (IField field : type.getFields()) {
                    // If a field is static, we do not care about it, we care about all other field
                    if (isStatic(field.getFlags())) {
                        continue;
                    }
                    // If we find a non-static variable that is final, this is a problem
                    if (isFinal(field.getFlags())) {
                        Range range = PositionUtils.toNameRange(field, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("EntityNoFinalVariables"), range,
                                                                 Constants.DIAGNOSTIC_SOURCE, field.getElementType(),
                                                                 ErrorCode.InvalidPersistentFieldInEntityAnnotatedClass, DiagnosticSeverity.Error));
                    }
                }

                // Ensure that the Entity class is not given a final modifier
                if (isFinal(type.getFlags()))
                    isEntityClassFinal = true;

                // Create Diagnostics if needed
                if (!hasPublicOrProtectedNoArgConstructor && hasArgConstructor) {
                    Range range = PositionUtils.toNameRange(type, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("EntityNoArgConstructor"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidConstructorInEntityAnnotatedClass, DiagnosticSeverity.Error));

                }

                if (isEntityClassFinal) {
                    Range range = PositionUtils.toNameRange(type, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("EntityNoFinalClass"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, type.getElementType(),
                                                             ErrorCode.InvalidFinalModifierOnEntityAnnotatedClass, DiagnosticSeverity.Error));
                }
            }
        }

        return diagnostics;
    }

    /**
     * check if the modifier provided is static
     *
     * @param flag
     * @return
     * @note modifier flags are an addition of all flags combined
     */
    private boolean isStatic(int flag) {
        // If a field is static, we do not care about it, we care about all other field
        Integer isPublicStatic = flag - Flags.AccPublic;
        Integer isPrivateStatic = flag - Flags.AccPrivate;
        Integer isFinalStatic = flag - Flags.AccFinal;
        Integer isProtectedStatic = flag - Flags.AccProtected;
        Integer isStatic = flag;
        if (isPublicStatic.equals(Flags.AccStatic) || isPrivateStatic.equals(Flags.AccStatic)
            || isStatic.equals(Flags.AccStatic) || isFinalStatic.equals(Flags.AccStatic)
            || isProtectedStatic.equals(Flags.AccStatic)) {
            return true;
        }
        return false;
    }

    /**
     * check if the modifier provided is final
     *
     * @param flag
     * @return
     * @note modifier flags are an addition of all flags combined
     */
    private boolean isFinal(int flag) {
        Integer isPublicFinal = flag - Flags.AccPublic;
        Integer isPrivateFinal = flag - Flags.AccPrivate;
        Integer isProtectedFinal = flag - Flags.AccProtected;
        Integer isFinal = flag;
        if (isPublicFinal.equals(Flags.AccFinal) || isPrivateFinal.equals(Flags.AccFinal)
            || isProtectedFinal.equals(Flags.AccFinal) || isFinal.equals(Flags.AccFinal)) {
            return true;
        }
        return false;
    }

}
