package net.group_29.master.format.markdown;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import net.group_29.master.R;
import net.group_29.master.activity.DocumentActivity;
import net.group_29.master.format.ActionButtonBase;
import net.group_29.master.frontend.MarkorDialogFactory;
import net.group_29.master.frontend.textview.AutoTextFormatter;
import net.group_29.master.frontend.textview.TextViewUtils;
import net.group_29.master.model.Document;
import net.group_29.opoc.util.GsContextUtils;
import net.group_29.opoc.util.GsFileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownActionButtons extends ActionButtonBase {

    // Group 1 matches text, group 2 matches path
    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[([^\\]]*)\\]\\(([^)]+)\\)");

    private static final Pattern WEB_URL = Pattern.compile("https?://[^\\s/$.?#].[^\\s]*");

    private final Set<Integer> _disabledHeadings = new HashSet<>();

    public static final String LINE_PREFIX = "^(>\\s|#{1,6}\\s|\\s*[-*+](?:\\s\\[[ xX]\\])?\\s|\\s*\\d+[.)]\\s)?";
    public static final Pattern LINE_BOLD = Pattern.compile(LINE_PREFIX + "(\\s*)(\\*\\*)(\\S.*\\S)(\\3)(\\s*)$");
    public static final Pattern LINE_ITALIC = Pattern.compile(LINE_PREFIX + "(\\s*)(_)(\\S.*\\S)(\\3)(\\s*)$");
    public static final Pattern LINE_STRIKEOUT = Pattern.compile(LINE_PREFIX + "(\\s*)(~~)(\\S.*\\S)(\\3)(\\s*)$");
    public static final Pattern LINE_NONE = Pattern.compile(LINE_PREFIX + "(\\s*)(.*?)(\\s*)$");
    public static final Pattern CHECKED_LIST_LINE = Pattern.compile("^(\\s*)(([-*+])\\s\\[([xX ])\\]\\s)");
    public MarkdownActionButtons(@NonNull Context context, Document document) {
        super(context, document);
    }

    @Override
    protected @StringRes
    int getFormatActionsKey() {
        return R.string.pref_key__markdown__action_keys;
    }

    @Override
    public List<ActionItem> getFormatActionList() {
        return Arrays.asList(
                new ActionItem(R.string.abid_common_unordered_list_char, R.drawable.ic_list_black_24dp, R.string.unordered_list),
                new ActionItem(R.string.abid_common_ordered_list_number, R.drawable.ic_format_list_numbered_black_24dp, R.string.ordered_list),
                new ActionItem(R.string.abid_markdown_bold, R.drawable.ic_format_bold_black_24dp, R.string.bold),
                new ActionItem(R.string.abid_markdown_italic, R.drawable.ic_format_italic_black_24dp, R.string.italic),
                new ActionItem(R.string.abid_markdown_code_inline, R.drawable.ic_code_black_24dp, R.string.inline_code),
                new ActionItem(R.string.abid_markdown_quote, R.drawable.ic_format_quote_black_24dp, R.string.quote),
                new ActionItem(R.string.abid_markdown_h1, R.drawable.format_header_1, R.string.heading_1),
                new ActionItem(R.string.abid_markdown_h2, R.drawable.format_header_2, R.string.heading_2),
                new ActionItem(R.string.abid_markdown_h3, R.drawable.format_header_3, R.string.heading_3),
                new ActionItem(R.string.abid_common_indent, R.drawable.ic_format_indent_increase_black_24dp, R.string.indent),
                new ActionItem(R.string.abid_common_deindent, R.drawable.ic_format_indent_decrease_black_24dp, R.string.deindent)
        );
    }

    @Override
    public boolean onActionClick(final @StringRes int action) {
        switch (action) {
            case R.string.abid_markdown_quote: {
                runRegexReplaceAction(MarkdownReplacePatternGenerator.toggleQuote());
                return true;
            }
            case R.string.abid_markdown_h1: {
                runRegexReplaceAction(MarkdownReplacePatternGenerator.setOrUnsetHeadingWithLevel(1));
                return true;
            }
            case R.string.abid_markdown_h2: {
                runRegexReplaceAction(MarkdownReplacePatternGenerator.setOrUnsetHeadingWithLevel(2));
                return true;
            }
            case R.string.abid_markdown_h3: {
                runRegexReplaceAction(MarkdownReplacePatternGenerator.setOrUnsetHeadingWithLevel(3));
                return true;
            }
            case R.string.abid_common_unordered_list_char: {
                final String listChar = _appSettings.getUnorderedListCharacter();
                runRegexReplaceAction(MarkdownReplacePatternGenerator.replaceWithUnorderedListPrefixOrRemovePrefix(listChar));
                return true;
            }
            case R.string.abid_common_checkbox_list: {
                final String listChar = _appSettings.getUnorderedListCharacter();
                runRegexReplaceAction(MarkdownReplacePatternGenerator.toggleToCheckedOrUncheckedListPrefix(listChar));
                return true;
            }
            case R.string.abid_common_ordered_list_number: {
                runRegexReplaceAction(MarkdownReplacePatternGenerator.replaceWithOrderedListPrefixOrRemovePrefix());
                runRenumberOrderedListIfRequired();
                return true;
            }
            case R.string.abid_markdown_bold: {
                runSurroundAction("**");
                return true;
            }
            case R.string.abid_markdown_italic: {
                runSurroundAction("_");
                return true;
            }
            case R.string.abid_markdown_strikeout: {
                runSurroundAction("~~");
                return true;
            }
            case R.string.abid_markdown_code_inline: {
                runSurroundAction("`");
                return true;
            }
            case R.string.abid_markdown_horizontal_line: {
                _hlEditor.insertOrReplaceTextOnCursor("----\n");
                return true;
            }
            case R.string.abid_markdown_table_insert_columns: {
                MarkorDialogFactory.showInsertTableRowDialog(getActivity(), false, this::insertTableRow);
                return true;
            }
            case R.string.abid_common_open_link_browser: {
                if (followLinkUnderCursor()) {
                    return true;
                }
            }
            default: {
                return runCommonAction(action);
            }
        }
    }

    /**
     * Used to surround selected text with a given delimiter (and remove it if present)
     *
     * Not super intelligent about how patterns can be combined.
     * Current regexes just look for the litera delimiters.
     *
     * @param pattern - Pattern to match if delimiter is present
     * @param delim   - Delimiter to surround text with
     */
    private void runLineSurroundAction(final Pattern pattern, final String delim) {
        runRegexReplaceAction(
                new ReplacePattern(pattern, "$1$2$4$6"),
                new ReplacePattern(LINE_NONE, "$1$2" + delim + "$3" + delim + "$4")
        );
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onActionLongClick(final @StringRes int action) {
        switch (action) {
            case R.string.abid_markdown_table_insert_columns: {
                MarkorDialogFactory.showInsertTableRowDialog(getActivity(), true, this::insertTableRow);
                return true;
            }
            case R.string.abid_markdown_code_inline: {
                _hlEditor.withAutoFormatDisabled(() -> {
                    final int c = _hlEditor.setSelectionExpandWholeLines();
                    _hlEditor.getText().insert(_hlEditor.getSelectionStart(), "\n```\n");
                    _hlEditor.getText().insert(_hlEditor.getSelectionEnd(), "\n```\n");
                    _hlEditor.setSelection(c + "\n```\n".length());
                });
                return true;
            }
            case R.string.abid_markdown_bold: {
                runLineSurroundAction(LINE_BOLD, "**");
                return true;
            }
            case R.string.abid_markdown_italic: {
                runLineSurroundAction(LINE_ITALIC, "_");
                return true;
            }
            case R.string.abid_markdown_strikeout: {
                runLineSurroundAction(LINE_STRIKEOUT, "~~");
                return true;
            }
            case R.string.abid_common_checkbox_list: {
                MarkorDialogFactory.showDocumentChecklistDialog(
                        getActivity(), _hlEditor.getText(), CHECKED_LIST_LINE, 4, "xX", " ",
                        pos -> TextViewUtils.setSelectionAndShow(_hlEditor, pos));
                return true;
            }
            default: {
                return runCommonLongPressAction(action);
            }
        }
    }

    private boolean followLinkUnderCursor() {
        final int sel = TextViewUtils.getSelection(_hlEditor)[0];
        if (sel < 0) {
            return false;
        }

        final String line = TextViewUtils.getSelectedLines(_hlEditor, sel);
        final int cursor = sel - TextViewUtils.getLineStart(_hlEditor.getText(), sel);

        final Matcher m = MARKDOWN_LINK.matcher(line);
        while (m.find()) {
            final String group = m.group(2);
            if (m.start() <= cursor && m.end() > cursor && group != null) {
                if (WEB_URL.matcher(group).matches()) {
                    GsContextUtils.instance.openWebpageInExternalBrowser(getActivity(), group);
                    return true;
                } else {
                    final File f = GsFileUtils.makeAbsolute(group, _document.getFile().getParentFile());
                    if (GsFileUtils.canCreate(f)) {
                        DocumentActivity.handleFileClick(getActivity(), f, null);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void insertTableRow(int cols, boolean isHeaderEnabled) {
        StringBuilder sb = new StringBuilder();
        _hlEditor.requestFocus();

        // Append if current line empty
        final int[] sel = TextViewUtils.getLineSelection(_hlEditor);
        if (sel[0] != -1 && sel[0] == sel[1]) {
            sb.append("\n");
        }

        for (int i = 0; i < cols - 1; i++) {
            sb.append("  | ");
        }
        if (isHeaderEnabled) {
            sb.append("\n");
            for (int i = 0; i < cols; i++) {
                sb.append("---");
                if (i < cols - 1) {
                    sb.append("|");
                }
            }
        }
        _hlEditor.moveCursorToEndOfLine(0);
        _hlEditor.insertOrReplaceTextOnCursor(sb.toString());
        _hlEditor.moveCursorToBeginOfLine(0);
        if (isHeaderEnabled) {
            _hlEditor.simulateKeyPress(KeyEvent.KEYCODE_DPAD_UP);
        }
    }

    @Override
    public boolean runTitleClick() {
        final Matcher m = MarkdownReplacePatternGenerator.PREFIX_ATX_HEADING.matcher("");
        MarkorDialogFactory.showHeadlineDialog(getActivity(), _hlEditor, _disabledHeadings, (text, start, end) -> {
            if (m.reset(text.subSequence(start, end)).find()) {
                return m.end(2) - m.start(2) - 1;
            }
            return -1;
        });
        return true;
    }

    @Override
    protected void renumberOrderedList() {
        AutoTextFormatter.renumberOrderedList(_hlEditor.getText(), MarkdownReplacePatternGenerator.formatPatterns);
    }
}
