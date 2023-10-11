/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.ls.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4jakarta.commons.JavaCursorContextResult;
import org.eclipse.lsp4jakarta.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4jakarta.commons.utils.StringUtils;
import org.eclipse.lsp4jakarta.ls.commons.BadLocationException;
import org.eclipse.lsp4jakarta.ls.commons.snippets.ISnippetContext;
import org.eclipse.lsp4jakarta.ls.commons.snippets.Snippet;
import org.eclipse.lsp4jakarta.ls.commons.snippets.TextDocumentSnippetRegistry;
import org.eclipse.lsp4jakarta.ls.java.JakartaTextDocuments.JakartaTextDocument;
import org.eclipse.lsp4jakarta.snippets.LanguageId;
import org.eclipse.lsp4jakarta.snippets.SnippetContextForJava;
import org.eclipse.lsp4jakarta.utils.Messages;

/**
 * Java snippet registry. When a snippet is registered it replaces for the first
 * line only the content 'package ${1:packagename};' to '${packagename}' in
 * order to manage the case if packagename is empty (don't generate package) or
 * not.
 *
 * Based on:
 * https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/ls/java/JavaTextDocumentSnippetRegistry.java
 *
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentSnippetRegistry extends TextDocumentSnippetRegistry {

    private static final String PACKAGENAME_KEY = "packagename";
    private static final String EE_NAMESPACE_KEY = "ee-namespace";
    private static final String JAVAX_VALUE = "javax";
    private static final String JAKARTA_VALUE = "jakarta";
    private static final String PACKAGE_NAME = "packagename";
    private static final String CLASS_NAME = "classname";
    private static final String[] RESOLVE_VARIABLES = { PACKAGE_NAME, CLASS_NAME };

    /**
     * The type whose presence indicates that the jakarta namespace should be used.
     */
    private static final String JAKARTA_FLAG_TYPE = "jakarta.ws.rs.GET";

    private List<String> types;

    public JavaTextDocumentSnippetRegistry() {
        this(true);
    }

    public JavaTextDocumentSnippetRegistry(boolean loadDefault) {
        super(LanguageId.java.name(), loadDefault);
    }

    /**
     * Returns the all distinct types declared in context/type of each snippet.
     *
     * @return the all distinct types declared in context/type of each snippet.
     */
    public List<String> getTypes() {
        if (types != null) {
            return types;
        }
        types = collectTypes();
        return types;
    }

    private synchronized List<String> collectTypes() {
        if (types != null) {
            return types;
        }
        List<String> types = new ArrayList<>();
        types.add(JAKARTA_FLAG_TYPE);
        for (Snippet snippet : getSnippets()) {
            if (snippet.getContext() != null && snippet.getContext() instanceof SnippetContextForJava) {
                List<String> snippetTypes = ((SnippetContextForJava) snippet.getContext()).getTypes();
                if (snippetTypes != null) {
                    for (String snippetType : snippetTypes) {
                        if (!types.contains(snippetType)) {
                            types.add(snippetType);
                        }
                    }
                }
            }
        }
        return types;
    }

    @Override
    public void registerSnippet(Snippet snippet) {
        preprocessSnippetBody(snippet);
        super.registerSnippet(snippet);
    }

    /**
     * Preprocess Snippet body for managing package name.
     *
     * @param snippet
     */
    private void preprocessSnippetBody(Snippet snippet) {
        List<String> body = snippet.getBody();
        if (body.isEmpty()) {
            return;
        }
        String firstLine = body.get(0);
        if (firstLine.contains("${") && firstLine.contains(PACKAGENAME_KEY)) {
            // Transform these 3 body lines:
            // "package ${1:packagename};",
            // "",
            // "import jakarta.ws.rs.HEAD;",

            // to one line:
            // ${packagename}import jakarta.ws.rs.HEAD;

            if (body.size() >= 2 && StringUtils.isEmpty(body.get(1))) {
                // Remove the line ""
                body.remove(1);
            }
            String line = "";
            if (body.size() >= 2) {
                // Remove the line "import jakarta.ws.rs.HEAD;"
                line = body.get(1);
                body.remove(1);
            }
            // Update the line 0 to ${packagename}import
            // jakarta.ws.rs.HEAD;
            body.set(0, "${" + PACKAGENAME_KEY + "}" + line);
        }
    }

    public List<CompletionItem> getCompletionItems(JakartaTextDocument document, int completionOffset,
                                                   boolean canSupportMarkdown, boolean snippetsSupported,
                                                   BiPredicate<ISnippetContext<?>, Map<String, String>> contextFilter, ProjectLabelInfoEntry projectInfo) {
        Map<String, String> model = new HashMap<>();
        String packageStatement = "";
        String packageName = document.getPackageName();
        String lineDelimiter = System.lineSeparator();
        try {
            lineDelimiter = document.lineDelimiter(0);
        } catch (BadLocationException e) {
        }
        if (packageName == null) {
            packageStatement = new StringBuilder("package ${1:packagename};")//
                            .append(lineDelimiter) //
                            .append(lineDelimiter) //
                            .toString();
        } else {
            // fill package name to replace in the snippets
            if (packageName.length() > 0) {
                packageStatement = new StringBuilder("package ")//
                                .append(document.getPackageName()) //
                                .append(";") //
                                .append(lineDelimiter) //
                                .append(lineDelimiter) //
                                .toString();
            }
        }
        model.put(PACKAGENAME_KEY, packageStatement);
        model.put(EE_NAMESPACE_KEY,
                  projectInfo.getLabels().contains(JavaTextDocumentSnippetRegistry.JAKARTA_FLAG_TYPE) ? JavaTextDocumentSnippetRegistry.JAKARTA_VALUE : JavaTextDocumentSnippetRegistry.JAVAX_VALUE);
        return super.getCompletionItems(document, completionOffset, canSupportMarkdown, snippetsSupported,
                                        contextFilter, model);
    }

    /**
     * Returns the snippet completion items according to the context filter.
     *
     * @param replaceRange the replace range.
     * @param lineDelimiter the line delimiter.
     * @param canSupportMarkdown true if markdown is supported to generate
     *            documentation and false otherwise.
     * @param context the context filter.
     * @param prefix completion prefix.
     * @return the snippet completion items according to the context filter.
     */
    public List<CompletionItem> getCompletionItem(final Range replaceRange, final String lineDelimiter,
                                                  boolean canSupportMarkdown, List<String> context, JavaCursorContextResult cursorContext, String prefix) {
        List<Snippet> snippets = getSnippets();
        Map<String, String> values = new HashMap<String, String>();
        int size = context.size();
        if (size == snippets.size() + 2) { // the last 2 strings are package name and class name
            values.put(PACKAGE_NAME, context.get(size - 2));
            values.put(CLASS_NAME, context.get(size - 1));
        }
        String filter = (prefix != null) ? prefix.toLowerCase() : null;
        return snippets.stream()
                        // filter list based on cursor context
                        .filter(snippet -> ((SnippetContextForJava) snippet.getContext()).snippetContentAppliesToContext(cursorContext)).map(snippet -> {
                            String label = snippet.getPrefixes().get(0);
                            if (context.get(snippets.indexOf(snippet)) == null
                                // in Eclipse, the filter is not working properly, have to add additional one
                                || (filter != null && filterLabel(filter, label.toLowerCase()) != true)) {
                                return null;
                            }
                            CompletionItem item = new CompletionItem();
                            item.setLabel(label);
                            //            item.setDetail(snippet.getDescription());
                            item.setDetail(Messages.getMessage(snippet.getDescription()));
                            String insertText = getInsertText(snippet, false, lineDelimiter, values);
                            item.setKind(CompletionItemKind.Snippet);
                            item.setDocumentation(
                                                  Either.forRight(createDocumentation(snippet, canSupportMarkdown, lineDelimiter, values)));
                            item.setFilterText(label);

                            TextEdit textEdit = new TextEdit(replaceRange, insertText);
                            item.setTextEdit(Either.forLeft(textEdit));
                            item.setInsertTextFormat(InsertTextFormat.Snippet);
                            return item;
                        }).filter(completionItems -> completionItems != null).collect(Collectors.toList());
    }

    private static MarkupContent createDocumentation(Snippet snippet, boolean canSupportMarkdown,
                                                     String lineDelimiter, Map<String, String> values) {
        StringBuilder doc = new StringBuilder();
        if (canSupportMarkdown) {
            doc.append(System.lineSeparator());
            doc.append("```");
            String scope = snippet.getScope();
            if (scope != null) {
                doc.append(scope);
            }
            doc.append(System.lineSeparator());
        }
        String insertText = getInsertText(snippet, true, lineDelimiter, values);
        doc.append(insertText);
        if (canSupportMarkdown) {
            doc.append(System.lineSeparator());
            doc.append("```");
            doc.append(System.lineSeparator());
        }
        return new MarkupContent(canSupportMarkdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, doc.toString());
    }

    private static String getInsertText(Snippet snippet, boolean replace, String lineDelimiter,
                                        Map<String, String> values) {
        StringBuilder text = new StringBuilder();
        int i = 0;
        List<String> body = snippet.getBody();
        if (body != null) {
            Map<String, Set<String>> foundVars = new HashMap<String, Set<String>>();
            for (String bodyLine : body) {
                // resolve specific variables by values
                if (values != null && values.size() > 0) {
                    foundVars.clear();
                    // search for specific variables
                    getMatchedVariables(bodyLine, 0, RESOLVE_VARIABLES, foundVars);
                    if (foundVars.size() > 0) { // resolve specific variables by values
                        for (String key : foundVars.keySet()) {
                            String replacement = values.get(key);
                            if (replacement != null) {
                                Set<String> vars = foundVars.get(key);
                                for (Iterator<String> it = vars.iterator(); it.hasNext();) {
                                    bodyLine = bodyLine.replace(it.next(), replacement);
                                }
                            }
                        }
                    }
                }
                if (i > 0) {
                    text.append(lineDelimiter);
                }
                if (replace) {
                    bodyLine = replace(bodyLine);
                }
                text.append(bodyLine);
                i++;
            }
        }
        return text.toString();
    }

    /**
     * Replace place holders (ex : ${name}) from the given <code>line</code> by
     * using the given context <code>model</code>.
     *
     * @param line the line which can have some place holders.
     * @param offset the start offset where the replace must be occured.
     * @param model the context model.
     * @param keepDollarVariable true if place holder (ex : ${name}) must be kept
     *            (ex : ${name}) or not (ex : name)
     * @param newLine the replace line buffer result.
     */
    private static void replacePlaceholders(String line, int offset, Map<String, String> model,
                                            boolean keepDollarVariable, StringBuilder newLine) {
        int dollarIndex = line.indexOf("$", offset);
        if (dollarIndex == -1 || dollarIndex == line.length() - 1) {
            newLine.append(line.substring(offset, line.length()));
            return;
        }
        char next = line.charAt(dollarIndex + 1);
        if (Character.isDigit(next)) {
            // ex: line = @RegistryType(type=$1)
            if (!keepDollarVariable) {
                newLine.append(line.substring(offset, dollarIndex));
            }
            int lastDigitOffset = dollarIndex + 1;
            while (line.length() < lastDigitOffset && Character.isDigit(line.charAt(lastDigitOffset))) {
                lastDigitOffset++;
            }
            if (keepDollarVariable) {
                newLine.append(line.substring(offset, lastDigitOffset));
            }
            replacePlaceholders(line, lastDigitOffset, model, keepDollarVariable, newLine);
        } else if (next == '{') {
            int startExpr = dollarIndex;
            int endExpr = line.indexOf("}", startExpr);
            if (endExpr == -1) {
                // Should never occur
                return;
            }
            newLine.append(line.substring(offset, startExpr));
            // Parameter
            int startParam = startExpr + 2;
            int endParam = endExpr;
            boolean onlyNumber = true;
            for (int i = startParam; i < endParam; i++) {
                char ch = line.charAt(i);
                if (!Character.isDigit(ch)) {
                    onlyNumber = false;
                    if (ch == ':') {
                        startParam = i + 1;
                        break;
                    } else if (ch == '|') {
                        startParam = i + 1;
                        int index = line.indexOf(',', startExpr);
                        if (index != -1) {
                            endParam = index;
                        }
                        break;
                    } else {
                        break;
                    }
                }
            }
            String paramName = line.substring(startParam, endParam);
            if (model.containsKey(paramName)) {
                paramName = model.get(paramName);
            } else if (keepDollarVariable) {
                paramName = line.substring(startExpr, endExpr + 1);
            }
            if (!(!keepDollarVariable && onlyNumber)) {
                newLine.append(paramName);
            }
            replacePlaceholders(line, endExpr + 1, model, keepDollarVariable, newLine);
        }
    }

    /**
     * Get all matched variables from given string line.
     *
     * @param line - given string to search
     * @param start - position/index to start the search from
     * @param vars - searching variables
     * @param matched - found variables
     */
    private static void getMatchedVariables(String line, int start, String[] vars,
                                            Map<String, Set<String>> matched) {
        int idxS = line.indexOf("${", start);
        if (idxS != -1) {
            int idxE = line.indexOf('}', idxS);
            if (idxE - 1 > idxS + 2) {
                String varStr = line.substring(idxS + 2, idxE).trim().toLowerCase();
                Arrays.stream(vars).forEach(var -> {
                    if (varStr.endsWith(var) == true) {
                        if (matched.containsKey(var) != true) {
                            matched.put(var, new HashSet<String>());
                        }
                        matched.get(var).add(line.substring(idxS, idxE + 1));
                    }
                });
                getMatchedVariables(line, idxE, vars, matched);
            }
        }
    }

    private static String replace(String line) {
        return replace(line, 0, null);
    }

    private static String replace(String line, int offset, StringBuilder newLine) {
        int startExpr = line.indexOf("${", offset);
        if (startExpr == -1) {
            if (newLine == null) {
                return line;
            }
            newLine.append(line.substring(offset, line.length()));
            return newLine.toString();
        }
        int endExpr = line.indexOf("}", startExpr);
        if (endExpr == -1) {
            // Should never occur
            return line;
        }
        if (newLine == null) {
            newLine = new StringBuilder();
        }
        newLine.append(line.substring(offset, startExpr));
        // Parameter
        int startParam = startExpr + 2;
        int endParam = endExpr;
        boolean startsWithNumber = true;
        for (int i = startParam; i < endParam; i++) {
            char ch = line.charAt(i);
            if (Character.isDigit(ch)) {
                startsWithNumber = true;
            } else if (ch == ':') {
                if (startsWithNumber) {
                    startParam = i + 1;
                }
                break;
            } else if (ch == '|') {
                if (startsWithNumber) {
                    startParam = i + 1;
                    int index = line.indexOf(',', startExpr);
                    if (index != -1) {
                        endParam = index;
                    }
                }
                break;
            } else {
                break;
            }
        }
        newLine.append(line.substring(startParam, endParam));
        return replace(line, endExpr + 1, newLine);
    }

    private boolean filterLabel(String filter, String label) {
        boolean pass = true;
        if (label.contains(filter) != true) {
            char[] chars = filter.toCharArray();
            int start = 0;
            for (char ch : chars) {
                start = label.indexOf(ch, start);
                if (start == -1) {
                    pass = false;
                    break;
                }
                start++;
            }
        }
        return pass;
    }

}
