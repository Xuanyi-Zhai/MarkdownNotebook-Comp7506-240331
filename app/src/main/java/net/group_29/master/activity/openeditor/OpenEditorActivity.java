package net.group_29.master.activity.openeditor;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import net.group_29.master.activity.MarkorBaseActivity;
import net.group_29.master.activity.StoragePermissionActivity;
import net.group_29.master.model.Document;

import java.io.File;

public class OpenEditorActivity extends MarkorBaseActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StoragePermissionActivity.requestPermissions(this);
    }

    protected void openEditorForFile(final File file, final Integer line) {
        final Intent openIntent = new Intent(getApplicationContext(), OpenFromShortcutOrWidgetActivity.class)
                .setAction(Intent.ACTION_EDIT)
                .putExtra(Document.EXTRA_FILE, file);

        if (line != null) {
            openIntent.putExtra(Document.EXTRA_FILE_LINE_NUMBER, line);
        }

        _cu.animateToActivity(this, openIntent, true, 1);
    }
}
