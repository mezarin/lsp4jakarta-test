package org.eclipse.lsp4jakarta.jdt.core;

import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionHandler;
import org.eclipse.lspcommon.jdt.core.PropertiesManagerForJava;
import org.eclipse.lspcommon.jdt.core.operations.java.codeaction.CodeActionHandler;

public class JakartaPropertiesManagerForJava extends PropertiesManagerForJava {

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
    public CodeActionHandler getCodeActionHandler() {
        return JakartaCodeActionHandler.getInstance();
    }
}
