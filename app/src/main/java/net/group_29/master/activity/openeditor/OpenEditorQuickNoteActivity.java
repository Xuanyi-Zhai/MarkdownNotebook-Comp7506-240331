package net.group_29.master.activity.openeditor;

import android.os.Bundle;

import androidx.annotation.Nullable;

import net.group_29.master.model.Document;

public class OpenEditorQuickNoteActivity extends OpenEditorActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openEditorForFile(_appSettings.getQuickNoteFile(), Document.EXTRA_FILE_LINE_NUMBER_LAST);
    }
}
