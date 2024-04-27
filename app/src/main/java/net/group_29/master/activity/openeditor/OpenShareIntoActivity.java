package net.group_29.master.activity.openeditor;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class OpenShareIntoActivity extends OpenEditorActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent openShare = new Intent(this, OpenFromShortcutOrWidgetActivity.class)
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, "");

        _cu.animateToActivity(this, openShare, true, 1);
    }
}
