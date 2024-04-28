package net.group_29.master.format;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

import net.group_29.master.ApplicationObject;
import net.group_29.master.R;
import net.group_29.master.format.binary.EmbedBinaryTextConverter;
import net.group_29.master.format.keyvalue.KeyValueSyntaxHighlighter;
import net.group_29.master.format.keyvalue.KeyValueTextConverter;
import net.group_29.master.format.markdown.MarkdownActionButtons;
import net.group_29.master.format.markdown.MarkdownReplacePatternGenerator;
import net.group_29.master.format.markdown.MarkdownSyntaxHighlighter;
import net.group_29.master.format.markdown.MarkdownTextConverter;
import net.group_29.master.format.plaintext.PlaintextActionButtons;
import net.group_29.master.format.plaintext.PlaintextSyntaxHighlighter;
import net.group_29.master.format.plaintext.PlaintextTextConverter;
import net.group_29.master.format.todotxt.TodoTxtActionButtons;
import net.group_29.master.format.todotxt.TodoTxtAutoTextFormatter;
import net.group_29.master.format.todotxt.TodoTxtSyntaxHighlighter;
import net.group_29.master.format.todotxt.TodoTxtTextConverter;
import net.group_29.master.frontend.textview.AutoTextFormatter;
import net.group_29.master.frontend.textview.ListHandler;
import net.group_29.master.frontend.textview.SyntaxHighlighterBase;
import net.group_29.master.model.AppSettings;
import net.group_29.master.model.Document;

import java.io.File;
import java.util.Locale;

public class FormatRegistry {
    public static final int FORMAT_UNKNOWN = 0;
    public static final int FORMAT_WIKITEXT = R.string.action_format_wikitext;
    public static final int FORMAT_MARKDOWN = R.string.action_format_markdown;
    public static final int FORMAT_CSV = R.string.action_format_csv;
    public static final int FORMAT_PLAIN = R.string.action_format_plaintext;
    public static final int FORMAT_ASCIIDOC = R.string.action_format_asciidoc;
    public static final int FORMAT_TODOTXT = R.string.action_format_todotxt;
    public static final int FORMAT_KEYVALUE = R.string.action_format_keyvalue;
    public static final int FORMAT_EMBEDBINARY = R.string.action_format_embedbinary;
    public static final int FORMAT_ORGMODE = R.string.action_format_orgmode;


    public final static MarkdownTextConverter CONVERTER_MARKDOWN = new MarkdownTextConverter();

    public final static TodoTxtTextConverter CONVERTER_TODOTXT = new TodoTxtTextConverter();
    public final static KeyValueTextConverter CONVERTER_KEYVALUE = new KeyValueTextConverter();
    public final static PlaintextTextConverter CONVERTER_PLAINTEXT = new PlaintextTextConverter();
    public final static EmbedBinaryTextConverter CONVERTER_EMBEDBINARY = new EmbedBinaryTextConverter();


    // Order here is used to **determine** format by it's file extension and/or content heading
    private final static TextConverterBase[] CONVERTERS = new TextConverterBase[]{
            CONVERTER_MARKDOWN,
            CONVERTER_TODOTXT,
            CONVERTER_KEYVALUE,
            CONVERTER_PLAINTEXT,
    };

    public static boolean isFileSupported(final File file, final boolean... textOnly) {
        final boolean textonly = textOnly != null && textOnly.length > 0 && textOnly[0];
        if (file != null) {
            final String filepath = file.getAbsolutePath().toLowerCase(Locale.ROOT);
            for (TextConverterBase converter : CONVERTERS) {
                if (textonly && converter instanceof EmbedBinaryTextConverter) {
                    continue;
                }
                if (converter.isFileOutOfThisFormat(filepath)) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface TextFormatApplier {
        void applyTextFormat(int textFormatId);
    }

    public static FormatRegistry getFormat(int formatId, @NonNull final Context context, final Document document) {
        final FormatRegistry format = new FormatRegistry();
        final AppSettings appSettings = ApplicationObject.settings();

        switch (formatId) {
            case FORMAT_CSV: {
                format._textActions = new PlaintextActionButtons(context, document);
                format._autoFormatInputFilter = new AutoTextFormatter(MarkdownReplacePatternGenerator.formatPatterns);
                format._autoFormatTextWatcher = new ListHandler(MarkdownReplacePatternGenerator.formatPatterns);
                break;
            }
            case FORMAT_PLAIN: {
                format._converter = CONVERTER_PLAINTEXT;
                format._highlighter = new PlaintextSyntaxHighlighter(appSettings);
                format._textActions = new PlaintextActionButtons(context, document);
                format._autoFormatInputFilter = new AutoTextFormatter(MarkdownReplacePatternGenerator.formatPatterns);
                format._autoFormatTextWatcher = new ListHandler(MarkdownReplacePatternGenerator.formatPatterns);
                break;
            }
            case FORMAT_TODOTXT: {
                format._converter = CONVERTER_TODOTXT;
                format._highlighter = new TodoTxtSyntaxHighlighter(appSettings);
                format._textActions = new TodoTxtActionButtons(context, document);
                format._autoFormatInputFilter = new TodoTxtAutoTextFormatter();
                break;
            }
            case FORMAT_KEYVALUE: {
                format._converter = CONVERTER_KEYVALUE;
                format._highlighter = new KeyValueSyntaxHighlighter(appSettings);
                format._textActions = new PlaintextActionButtons(context, document);
                break;
            }
            case FORMAT_EMBEDBINARY: {
                format._converter = CONVERTER_EMBEDBINARY;
                format._highlighter = new PlaintextSyntaxHighlighter(appSettings);
                format._textActions = new PlaintextActionButtons(context, document);
                break;
            }
            default:
            case FORMAT_MARKDOWN: {
                formatId = FORMAT_MARKDOWN;
                format._converter = CONVERTER_MARKDOWN;
                format._highlighter = new MarkdownSyntaxHighlighter(appSettings);
                format._textActions = new MarkdownActionButtons(context, document);
                format._autoFormatInputFilter = new AutoTextFormatter(MarkdownReplacePatternGenerator.formatPatterns);
                format._autoFormatTextWatcher = new ListHandler(MarkdownReplacePatternGenerator.formatPatterns);
                break;
            }
        }
        format._formatId = formatId;
        return format;
    }

    private ActionButtonBase _textActions;
    private SyntaxHighlighterBase _highlighter;
    private TextConverterBase _converter;
    private InputFilter _autoFormatInputFilter;
    private TextWatcher _autoFormatTextWatcher;
    private int _formatId;

    public ActionButtonBase getActions() {
        return _textActions;
    }

    public TextWatcher getAutoFormatTextWatcher() {
        return _autoFormatTextWatcher;
    }

    public InputFilter getAutoFormatInputFilter() {
        return _autoFormatInputFilter;
    }

    public SyntaxHighlighterBase getHighlighter() {
        return _highlighter;
    }

    public TextConverterBase getConverter() {
        return _converter;
    }

    public int getFormatId() {
        return _formatId;
    }
}
