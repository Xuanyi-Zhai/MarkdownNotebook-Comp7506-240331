package net.group_29.master.format.plaintext;

import net.group_29.master.frontend.textview.SyntaxHighlighterBase;
import net.group_29.master.model.AppSettings;

public class PlaintextSyntaxHighlighter extends SyntaxHighlighterBase {

    public PlaintextSyntaxHighlighter(AppSettings as) {
        super(as);
    }

    @Override
    protected void generateSpans() {
        createTabSpans(_tabSize);
        createUnderlineHexColorsSpans();
        createSmallBlueLinkSpans();
    }

}

