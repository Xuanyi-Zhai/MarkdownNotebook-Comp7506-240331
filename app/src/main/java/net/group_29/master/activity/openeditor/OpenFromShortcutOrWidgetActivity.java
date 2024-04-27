package net.group_29.master.activity.openeditor;

import android.content.Intent;
import android.os.Bundle;

import net.group_29.master.activity.DocumentActivity;
import net.group_29.master.activity.MainActivity;
import net.group_29.master.activity.MarkorBaseActivity;
import net.group_29.master.util.MarkorContextUtils;

import java.io.File;
public class OpenFromShortcutOrWidgetActivity extends MarkorBaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchActivityAndFinish(getIntent());
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        launchActivityAndFinish(intent);
    }

    private void launchActivityAndFinish(Intent intent) {
        final Intent newIntent = new Intent(intent);
        final File intentFile = MarkorContextUtils.getIntentFile(intent, null);
        if (intentFile != null && intentFile.isDirectory()) {
            newIntent.setClass(this, MainActivity.class);
            startActivity(newIntent);
        } else {
            newIntent.setClass(this, DocumentActivity.class);
            DocumentActivity.launch(this, null, null, newIntent, null);
        }
        finish();
    }
}