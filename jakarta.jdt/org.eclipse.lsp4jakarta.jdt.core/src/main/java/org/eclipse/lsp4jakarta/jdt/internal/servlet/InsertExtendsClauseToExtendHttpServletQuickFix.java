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
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ExtendClassProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lspcommon.commons.codeaction.CodeActionResolveData;
import org.eclipse.lspcommon.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lspcommon.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lspcommon.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lspcommon.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lspcommon.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;

/**
 * Inserts the extends clause for the active class to extend the HTTPServlet
 * class.
 */
public class InsertExtendsClauseToExtendHttpServletQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(InsertExtendsClauseToExtendHttpServletQuickFix.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertExtendsClauseToExtendHttpServletQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        ASTNode node = context.getCoveredNode();
        ITypeBinding parentType = Bindings.getBindingOfParentType(node);
        List<CodeAction> codeActions = new ArrayList<>();
        if (parentType != null) {
            ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel(Constants.HTTP_SERVLET, parentType.getName()));
            codeAction.setRelevance(0);
            codeAction.setKind(CodeActionKind.QuickFix);
            codeAction.setDiagnostics(Arrays.asList(diagnostic));
            codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), JakartaCodeActionId.ServletExtendClass));
            codeActions.add(codeAction);
        }

        return codeActions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        ASTNode node = context.getCoveredNode();
        ITypeBinding parentType = Bindings.getBindingOfParentType(node);
        String label = getLabel(Constants.HTTP_SERVLET, parentType.getName());
        ChangeCorrectionProposal proposal = new ExtendClassProposal(label, context.getCompilationUnit(), parentType, context.getASTRoot(), "jakarta.servlet.http.HttpServlet", 0);
        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve code action edit to insert the extends clause to a classs.",
                       e);
        }

        return toResolve;
    }

    /**
     * Returns the code action label.
     *
     * @param interfaceName The interface name.
     * @param classTypeName The class type element name.
     *
     * @return The code action label.
     */
    @SuppressWarnings("restriction")
    private String getLabel(String interfaceName, String classTypeName) {
        return Messages.getMessage("LetClassExtend",
                                   org.eclipse.jdt.internal.core.manipulation.util.BasicElementLabels.getJavaElementName(classTypeName),
                                   org.eclipse.jdt.internal.core.manipulation.util.BasicElementLabels.getJavaElementName(interfaceName));
    }
}