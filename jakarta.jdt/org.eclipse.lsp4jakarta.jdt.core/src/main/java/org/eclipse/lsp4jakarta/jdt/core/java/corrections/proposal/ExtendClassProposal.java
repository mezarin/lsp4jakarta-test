/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copied from /org.eclipse.jdt.ui/src/org/eclipse/jdt/internal/ui/text/correction/proposals/ImplementInterfaceProposal.java
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lspcommon.jdt.core.java.corrections.proposal.ASTRewriteCorrectionProposal;

public class ExtendClassProposal extends ASTRewriteCorrectionProposal {

    private final IBinding fBinding;
    private final CompilationUnit fAstRoot;
    private final String interfaceType;

    public ExtendClassProposal(String name, ICompilationUnit targetCU, ITypeBinding binding, CompilationUnit astRoot,
                               String interfaceType, int relevance) {
        super(name, CodeActionKind.QuickFix, targetCU, null, relevance);

        Assert.isTrue(binding != null && Bindings.isDeclarationBinding(binding));

        fBinding = binding;
        fAstRoot = astRoot;
        this.interfaceType = interfaceType;
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        ASTNode boundNode = fAstRoot.findDeclaringNode(fBinding);
        ASTNode declNode = null;
        CompilationUnit newRoot = fAstRoot;
        if (boundNode != null) {
            declNode = boundNode; // is same CU
        } else {
            newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
            declNode = newRoot.findDeclaringNode(fBinding.getKey());
        }
        ImportRewrite imports = createImportRewrite(newRoot);

        if (declNode instanceof TypeDeclaration) {
            AST ast = declNode.getAST();

            ImportRewriteContext importRewriteContext = new ContextSensitiveImportRewriteContext(declNode, imports);
            String name = imports.addImport(interfaceType, importRewriteContext);
            Type newInterface = ast.newSimpleType(ast.newName(name));

            ASTRewrite rewrite = ASTRewrite.create(ast);

            rewrite.set(declNode, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, newInterface, null);

            return rewrite;
        }
        return null;
    }

}