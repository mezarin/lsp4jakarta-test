package org.eclipse.lsp4jakarta.jdt.internal.core.ls;

import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jdt.participants.core.ls.AbstractJavaProjectDelegateCommandHandler;

/**
 * Jakarta command handler for information procurement.
 */
public class JakartaJavaProjectDelegateCommandHandler extends AbstractJavaProjectDelegateCommandHandler {

    private static final String PROJECT_LABELS_COMMAND_ID = "jakarta/java/projectLabels";
    private static final String WORKSPACE_LABELS_COMMAND_ID = "jakarta/java/workspaceLabels";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectLabelCommandId() {
        return PROJECT_LABELS_COMMAND_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorspacelabelCommandId() {
        return WORKSPACE_LABELS_COMMAND_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginId() {
        return JakartaCorePlugin.PLUGIN_ID;
    }
}
