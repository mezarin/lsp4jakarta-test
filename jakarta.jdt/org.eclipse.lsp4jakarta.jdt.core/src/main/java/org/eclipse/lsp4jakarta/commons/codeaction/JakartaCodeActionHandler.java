package org.eclipse.lsp4jakarta.commons.codeaction;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lspcommon.jdt.core.operations.java.codeaction.AbstractCodeActionHandler;

public class JakartaCodeActionHandler extends AbstractCodeActionHandler {

    /** Singleton JakartaCodeActionHandler instance. */
    public static final JakartaCodeActionHandler INSTANCE = new JakartaCodeActionHandler();

    /**
     * Returns an instance of JakartaCodeActionHandler.
     *
     * @return An instance of JakartaCodeActionHandler.
     */
    public static JakartaCodeActionHandler getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getDefaultSupportedKinds() {
        return Arrays.asList(CodeActionKind.QuickFix);
    }
}
