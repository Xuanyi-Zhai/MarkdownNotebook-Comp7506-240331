package net.group_29.master.format.keyvalue;

import android.graphics.Typeface;

import net.group_29.master.format.markdown.MarkdownSyntaxHighlighter;
import net.group_29.master.frontend.textview.SyntaxHighlighterBase;
import net.group_29.master.model.AppSettings;

import java.util.regex.Pattern;

public class KeyValueSyntaxHighlighter extends SyntaxHighlighterBase {
    public final static Pattern PATTERN_KEY_VALUE = Pattern.compile("(?im)^([a-z_0-9]+)[-:=]");
    public final static Pattern PATTERN_KEY_VALUE_QUOTED = Pattern.compile("(?i)([\"'][a-z_0-9\\- ]+[\"']\\s*[-:=])");
    public final static Pattern PATTERN_VCARD_KEY = Pattern.compile("(?im)^(?<FIELD>[^\\s:;]+)(;(?<PARAM>[^=:;]+)=\"?(?<VALUE>[^:;]+)\"?)*:");
    public final static Pattern PATTERN_INI_HEADER = Pattern.compile("(?im)^(\\[.*\\])$");
    public final static Pattern PATTERN_INI_KEY = Pattern.compile("(?im)^([a-z_0-9]+)\\s*[=]");
    public final static Pattern PATTERN_INI_COMMENT = Pattern.compile("(?im)^(;.*)$");
    public final static Pattern PATTERN_COMMENT = Pattern.compile("(?im)^((#|//)\\s+.*)$");
    public final static Pattern PATTERN_CSV = Pattern.compile("[,;:]");

    public KeyValueSyntaxHighlighter(AppSettings as) {
        super(as);
    }

    @Override
    protected void generateSpans() {

        createTabSpans(_tabSize);
        createUnderlineHexColorsSpans();
        createSmallBlueLinkSpans();

        createStyleSpanForMatches(PATTERN_KEY_VALUE, Typeface.BOLD);
        createStyleSpanForMatches(PATTERN_KEY_VALUE_QUOTED, Typeface.BOLD);
        createColorSpanForMatches(MarkdownSyntaxHighlighter.LIST_UNORDERED, 0xffef6D00);
        createStyleSpanForMatches(PATTERN_VCARD_KEY, Typeface.BOLD);
        createStyleSpanForMatches(PATTERN_INI_KEY, Typeface.BOLD);
        createRelativeSizeSpanForMatches(PATTERN_INI_HEADER, 1.25f);
        createColorSpanForMatches(PATTERN_INI_HEADER, 0xffef6D00);
        createColorSpanForMatches(PATTERN_INI_COMMENT, 0xff88b04b);
        createColorSpanForMatches(PATTERN_COMMENT, 0xff88b04b);

        /*
        // Too expensive
        if (getFilepath().toLowerCase().endsWith(".csv")) {
            _profiler.restart("KeyValue: csv");
            createStyleSpanForMatches(spannable, KeyValueHighlighterPattern.PATTERN_CSV.getPattern(), Typeface.BOLD);
        }
        */
    }
}

