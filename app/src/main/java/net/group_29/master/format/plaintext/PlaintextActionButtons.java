package net.group_29.master.format.plaintext;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import net.group_29.master.R;
import net.group_29.master.format.ActionButtonBase;
import net.group_29.master.format.markdown.MarkdownReplacePatternGenerator;
import net.group_29.master.frontend.textview.AutoTextFormatter;
import net.group_29.master.model.Document;

import java.util.Arrays;
import java.util.List;

public class PlaintextActionButtons extends ActionButtonBase {

    public PlaintextActionButtons(@NonNull Context context, Document document) {
        super(context, document);
    }

    @Override
    public List<ActionItem> getFormatActionList() {
        return Arrays.asList(
                new ActionItem(R.string.abid_common_checkbox_list, R.drawable.ic_check_box_black_24dp, R.string.check_list),
                new ActionItem(R.string.abid_common_unordered_list_char, R.drawable.ic_list_black_24dp, R.string.unordered_list),
                new ActionItem(R.string.abid_common_ordered_list_number, R.drawable.ic_format_list_numbered_black_24dp, R.string.ordered_list),
                new ActionItem(R.string.abid_common_indent, R.drawable.ic_format_indent_increase_black_24dp, R.string.indent),
                new ActionItem(R.string.abid_common_deindent, R.drawable.ic_format_indent_decrease_black_24dp, R.string.deindent)
        );
    }

    @Override
    protected @StringRes
    int getFormatActionsKey() {
        return R.string.pref_key__plaintext__action_keys;
    }

    @Override
    protected void renumberOrderedList() {
        // Use markdown format for plain text too
        AutoTextFormatter.renumberOrderedList(_hlEditor.getText(), MarkdownReplacePatternGenerator.formatPatterns);
    }
}
