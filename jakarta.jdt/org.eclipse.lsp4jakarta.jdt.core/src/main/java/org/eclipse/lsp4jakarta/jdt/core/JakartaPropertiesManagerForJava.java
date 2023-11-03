package org.eclipse.lsp4jakarta.jdt.core;

import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionHandler;
import org.eclipse.lspcommon.jdt.core.AbstractPropertiesManagerForJava;
import org.eclipse.lspcommon.jdt.core.operations.java.codeaction.AbstractCodeActionHandler;

public class JakartaPropertiesManagerForJava extends AbstractPropertiesManagerForJava {

    public static final JakartaPropertiesManagerForJava INSTANCE = new JakartaPropertiesManagerForJava();
    JakartaCodeActionHandler codeActionHandler;

    public static JakartaPropertiesManagerForJava getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginId() {
        return JakartaCorePlugin.PLUGIN_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractCodeActionHandler getCodeActionHandler() {
        return JakartaCodeActionHandler.getInstance();
    }
}