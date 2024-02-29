/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hani Damlaj
 *     Jianing Xu
 *******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jdt.participants.core.ls.JDTUtilsLSImpl;

import com.google.gson.Gson;

/**
 * CDI diagnostics participant that manages the use of a managed bean.
 */
public class ManagedBeanDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] types = unit.getAllTypes();
        String[] scopeFQNames = Constants.SCOPE_FQ_NAMES.toArray(String[]::new);
        for (IType type : types) {
            List<String> managedBeanAnnotations = DiagnosticUtils.getMatchedJavaElementNames(type,
                                                                                             Stream.of(type.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new),
                                                                                             scopeFQNames);
            boolean isManagedBean = managedBeanAnnotations.size() > 0;

            if (managedBeanAnnotations.size() > 1) {
                // convert to simple name
                List<String> diagnosticData = managedBeanAnnotations.stream().map(annotation -> DiagnosticUtils.getSimpleName(annotation)).collect(Collectors.toList());

                Range range = PositionUtils.toNameRange(type, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("ScopeTypeAnnotationsManagedBean"), range,
                                                         Constants.DIAGNOSTIC_SOURCE, (new Gson().toJsonTree(diagnosticData)),
                                                         ErrorCode.InvalidNumberOfScopedAnnotationsByManagedBean, DiagnosticSeverity.Error));
            }

            String[] injectAnnotations = { Constants.PRODUCES_FQ_NAME, Constants.INJECT_FQ_NAME };
            IField fields[] = type.getFields();
            for (IField field : fields) {
                int fieldFlags = field.getFlags();
                String[] annotationNames = Stream.of(field.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new);
                List<String> fieldScopes = DiagnosticUtils.getMatchedJavaElementNames(type, annotationNames,
                                                                                      scopeFQNames);

                // If a managed bean has a non-static public field, it must have scope @Dependent.
                // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#managed_beans
                if (isManagedBean && Flags.isPublic(fieldFlags) && !Flags.isStatic(fieldFlags)
                    && !managedBeanAnnotations.contains(Constants.DEPENDENT_FQ_NAME)) {
                    Range range = PositionUtils.toNameRange(field, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ManagedBeanWithNonStaticPublicField"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidManagedBeanWithNonStaticPublicField, DiagnosticSeverity.Error));
                }

                // If a managed bean with a non-static public field declares any scope other than @Dependent,
                // the container automatically detects the problem and treats it as a definition error.
                // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#managed_beans
                if (isManagedBean && Flags.isPublic(fieldFlags) && !Flags.isStatic(fieldFlags)
                    && fieldScopes.size() == 1 && !fieldScopes.get(0).equals(Constants.DEPENDENT_FQ_NAME)) {
                    Range range = PositionUtils.toNameRange(field, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ManagedBeanWithNonStaticPublicFieldInvalidScope"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidManagedBeanWithNonStaticPublicFieldInvalidScope, DiagnosticSeverity.Error));
                }

                // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#declaring_bean_scope
                // A bean class or producer method or field may specify at most one scope type
                // annotation. If a bean class or producer method or field specifies multiple
                // scope type annotations, the container automatically detects the problem and
                // treats it as a definition error.
                //
                // Here we only look at the fields.
                List<String> fieldInjects = DiagnosticUtils.getMatchedJavaElementNames(type, annotationNames,
                                                                                       injectAnnotations);
                boolean isProducerField = false, isInjectField = false;
                for (String annotation : fieldInjects) {
                    if (Constants.PRODUCES_FQ_NAME.equals(annotation))
                        isProducerField = true;
                    else if (Constants.INJECT_FQ_NAME.equals(annotation))
                        isInjectField = true;
                }
                if (isProducerField && fieldScopes.size() > 1) {
                    List<String> diagnosticData = fieldScopes.stream().map(annotation -> DiagnosticUtils.getSimpleName(annotation)).collect(Collectors.toList()); // convert
                                                                                                                                                                  // to
                                                                                                                                                                  // simple
                                                                                                                                                                  // name
                    diagnosticData.add(Constants.PRODUCES);
                    Range range = PositionUtils.toNameRange(field, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ScopeTypeAnnotationsProducerField"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, (new Gson().toJsonTree(diagnosticData)),
                                                             ErrorCode.InvalidNumberOfScopeAnnotationsByProducerField, DiagnosticSeverity.Error));
                }

                if (isProducerField && isInjectField) {

                    // Produces and Inject Annotations Checks:
                    //
                    // go through each field and method to make sure @Produces and @Inject are not
                    // used together
                    //
                    // see: https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
                    // declaring_producer_field
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
                    // declaring_producer_method
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
                    // declaring_injected_field
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
                    // declaring_initializer
                    //
                    // A single field cannot have the same
                    Range range = PositionUtils.toNameRange(field, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ManagedBeanProducesAndInjectField"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidFieldWithProducesAndInjectAnnotations, DiagnosticSeverity.Error));
                }

            }

            IMethod[] methods = type.getMethods();
            List<IMethod> constructorMethods = new ArrayList<IMethod>();
            for (IMethod method : methods) {

                // Find all methods on the type that are constructors.
                if (DiagnosticUtils.isConstructorMethod(method))
                    constructorMethods.add(method);

                // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#declaring_bean_scope
                // A bean class or producer method or field may specify at most one scope type
                // annotation. If a bean class or producer method or field specifies multiple
                // scope type annotations, the container automatically detects the problem and
                // treats it as a definition error.
                //
                // Here we only look at the methods.
                String[] annotationNames = Stream.of(method.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new);
                List<String> methodScopes = DiagnosticUtils.getMatchedJavaElementNames(type, annotationNames,
                                                                                       scopeFQNames);
                List<String> methodInjects = DiagnosticUtils.getMatchedJavaElementNames(type, annotationNames,
                                                                                        injectAnnotations);
                boolean isProducerMethod = false, isInjectMethod = false;
                for (String annotation : methodInjects) {
                    if (Constants.PRODUCES_FQ_NAME.equals(annotation))
                        isProducerMethod = true;
                    else if (Constants.INJECT_FQ_NAME.equals(annotation))
                        isInjectMethod = true;
                }

                if (isProducerMethod && methodScopes.size() > 1) {
                    List<String> diagnosticData = methodScopes.stream().map(annotation -> DiagnosticUtils.getSimpleName(annotation)).collect(Collectors.toList()); // convert
                                                                                                                                                                   // to
                                                                                                                                                                   // simple
                                                                                                                                                                   // name
                    diagnosticData.add(Constants.PRODUCES);
                    Range range = PositionUtils.toNameRange(method, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ScopeTypeAnnotationsProducerMethod"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, (new Gson().toJsonTree(diagnosticData)),
                                                             ErrorCode.InvalidNumberOfScopeAnnotationsByProducerMethod, DiagnosticSeverity.Error));
                }

                if (isProducerMethod && isInjectMethod) {

                    // Produces and Inject Annotations Checks:
                    //
                    // go through each field and method to make sure @Produces and @Inject are not
                    // used together
                    //
                    // see: https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
                    // declaring_producer_field
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
                    // declaring_producer_method
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
                    // declaring_injected_field
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
                    // declaring_initializer
                    //
                    // A single method cannot have the same
                    Range range = PositionUtils.toNameRange(method, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ManagedBeanProducesAndInjectMethod"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidMethodWithProducesAndInjectAnnotations, DiagnosticSeverity.Error));
                }

            }

            if (isManagedBean && constructorMethods.size() > 0) {
                // If the managed bean does not have a constructor that takes no parameters,
                // it must have a constructor annotated @Inject. No additional special
                // annotations are required.

                // If there are no constructor methods, there is an implicit empty constructor
                // generated by the compiler.
                List<IMethod> methodsNeedingDiagnostics = new ArrayList<IMethod>();
                for (IMethod m : constructorMethods) {
                    if (m.getNumberOfParameters() == 0) {
                        methodsNeedingDiagnostics.clear();
                        break;
                    }
                    IAnnotation[] annotations = m.getAnnotations();
                    boolean hasParameterizedInjectConstructor = false;
                    // look up '@Inject' annotation
                    for (IAnnotation annotation : annotations) {
                        if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                                 Constants.INJECT_FQ_NAME)) {
                            hasParameterizedInjectConstructor = true;
                            break;
                        }
                    }
                    if (hasParameterizedInjectConstructor) {
                        methodsNeedingDiagnostics.clear();
                        break;
                    } else
                        methodsNeedingDiagnostics.add(m);
                }

                // Deliver a diagnostic on all parameterized constructors that they must add an
                // @Inject annotation
                for (IMethod m : methodsNeedingDiagnostics) {
                    Range range = PositionUtils.toNameRange(m, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ManagedBeanConstructorWithParameters"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidManagedBeanWithInvalidConstructor, DiagnosticSeverity.Error));
                }
            }

            // If a managed bean class is of generic type, it must be annotated
            // with @Dependent
            if (isManagedBean) {
                boolean isClassGeneric = type.getTypeParameters().length != 0;
                boolean isDependent = managedBeanAnnotations.stream().anyMatch(annotation -> Constants.DEPENDENT_FQ_NAME.equals(annotation));

                if (isClassGeneric && !isDependent) {
                    Range range = PositionUtils.toNameRange(type, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ManagedBeanGenericType"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidGenericManagedBeanClassWithNoDependentScope, DiagnosticSeverity.Error));
                }
            }

            // Inject and Disposes, Observes, ObservesAsync Annotations:
            //
            // go through each method to make sure @Inject
            // and @Disposes, @Observes, @ObservesAsync are not used together
            //
            // see: https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
            // declaring_bean_constructor
            // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
            // declaring_initializer

            invalidParamsCheck(context, uri, unit, diagnostics, type, Constants.INJECT_FQ_NAME);

            if (isManagedBean) {

                // Produces and Disposes, Observes, ObservesAsync Annotations:
                //
                // go through each method to make sure @Produces
                // and @Disposes, @Observes, @ObservesAsync are not used together
                //
                // see: https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#
                // declaring_producer_method
                //
                // note:
                // we need to check for bean defining annotations first to make sure the
                // managed bean is discovered.
                invalidParamsCheck(context, uri, unit, diagnostics, type, Constants.PRODUCES_FQ_NAME);

                for (IMethod method : methods) {
                    int numDisposes = 0;
                    Set<String> invalidAnnotations = new TreeSet<>();
                    ILocalVariable[] params = method.getParameters();

                    for (ILocalVariable param : params) {
                        IAnnotation[] annotations = param.getAnnotations();
                        for (IAnnotation annotation : annotations) {
                            String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                                 annotation.getElementName(),
                                                                                                 Constants.INVALID_PRODUCER_PARAMS_FQ);
                            if (Constants.DISPOSES_FQ_NAME.equals(matchedAnnotation)) {
                                numDisposes++;
                            } else if (Constants.OBSERVES_FQ_NAME.equals(matchedAnnotation)
                                       || Constants.OBSERVES_ASYNC_FQ_NAME.equals(matchedAnnotation)) {
                                invalidAnnotations.add("@" + annotation.getElementName());
                            }
                        }
                    }

                    if (numDisposes == 0)
                        continue;
                    if (numDisposes > 1) {
                        Range range = PositionUtils.toNameRange(method, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("ManagedBeanDisposeOneParameter"), range,
                                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                                 ErrorCode.InvalidDisposesAnnotationOnMultipleMethodParams, DiagnosticSeverity.Error));
                    }

                    if (!invalidAnnotations.isEmpty()) {
                        Range range = PositionUtils.toNameRange(method, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 createInvalidDisposesLabel(invalidAnnotations), range,
                                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                                 ErrorCode.InvalidDisposerMethodParamAnnotation, DiagnosticSeverity.Error));
                    }
                }
            }
        }

        return diagnostics;
    }

    private void invalidParamsCheck(JavaDiagnosticsContext context, String uri, ICompilationUnit unit,
                                    List<Diagnostic> diagnostics, IType type, String target) throws JavaModelException {
        // this method will be called to scan all methods looking for either @Produces annotations OR @Inject annotations. In either
        // scenario this method will then check for disallowed parameter annotations and add diagnostics to be displayed if detected.
        for (IMethod method : type.getMethods()) {
            IAnnotation targetAnnotation = null;

            for (IAnnotation annotation : method.getAnnotations()) {
                if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), target)) {
                    targetAnnotation = annotation;
                    break;
                }
            }

            if (targetAnnotation == null)
                continue;

            Set<String> invalidAnnotations = new TreeSet<>();
            ILocalVariable[] params = method.getParameters();

            for (ILocalVariable param : params) {
                List<String> paramScopes;
                // look at the params of any method annotated @Produces - check for invalid parameter annotations
                if (Constants.PRODUCES_FQ_NAME.equals(target)) {
                    paramScopes = DiagnosticUtils.getMatchedJavaElementNames(type,
                                                                             Stream.of(param.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new),
                                                                             Constants.INVALID_PRODUCER_PARAMS_FQ);
                } else { // look at the params of any method annotated @Inject - check for invalid parameter annotations
                    paramScopes = DiagnosticUtils.getMatchedJavaElementNames(type,
                                                                             Stream.of(param.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new),
                                                                             Constants.INVALID_INITIALIZER_PARAMS_FQ);
                }
                for (String annotation : paramScopes) {
                    invalidAnnotations.add("@" + DiagnosticUtils.getSimpleName(annotation));
                }
            }

            if (!invalidAnnotations.isEmpty()) {
                Range range = PositionUtils.toNameRange(method, context.getUtils());
                if (Constants.PRODUCES_FQ_NAME.equals(target)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             createInvalidProducesLabel(invalidAnnotations), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidProducerMethodParamAnnotation, DiagnosticSeverity.Error));
                } else {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             createInvalidInjectLabel(invalidAnnotations), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidInjectAnnotatedMethodParamAnnotation, DiagnosticSeverity.Error));
                }
            }
        }
    }

    private String createInvalidInjectLabel(Set<String> invalidAnnotations) {
        return Messages.getMessage("ManagedBeanInvalidInject", String.join(", ", invalidAnnotations));
    }

    private String createInvalidProducesLabel(Set<String> invalidAnnotations) {
        return Messages.getMessage("ManagedBeanInvalidProduces", String.join(", ", invalidAnnotations));
    }

    private String createInvalidDisposesLabel(Set<String> invalidAnnotations) {
        return Messages.getMessage("ManagedBeanInvalidDisposer", String.join(", ", invalidAnnotations));
    }

}
