package net.group_29.master.util;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.FragmentManager;

import net.group_29.master.ApplicationObject;
import net.group_29.master.R;
import net.group_29.master.format.ActionButtonBase;
import net.group_29.master.frontend.filebrowser.MarkorFileBrowserFactory;
import net.group_29.opoc.frontend.filebrowser.GsFileBrowserOptions;
import net.group_29.opoc.util.GsBackupUtils;

import java.io.File;
import java.util.List;

public class BackupUtils extends GsBackupUtils {

    public static void showBackupSelectFromDialog(final Context context, final FragmentManager manager) {
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;

            MarkorFileBrowserFactory.showFileDialog(
                    new GsFileBrowserOptions.SelectionListenerAdapter() {
                        @Override
                        public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
                            dopt.rootFolder = ApplicationObject.settings().getNotebookDirectory();
                            dopt.titleText = R.string.select;
                        }

                        @Override
                        public void onFsViewerSelected(String request, File file, final Integer lineNumber) {
                            loadBackup(context, file);
                        }
                    }, manager, activity,
                    (c, file) -> file != null && file.exists() && file.toString().trim().toLowerCase().endsWith(".json")
            );
        }
    }

    public static void showBackupWriteToDialog(final Context context, final FragmentManager manager) {
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;

            MarkorFileBrowserFactory.showFolderDialog(
                    new GsFileBrowserOptions.SelectionListenerAdapter() {
                        @Override
                        public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
                            dopt.rootFolder = ApplicationObject.settings().getNotebookDirectory();
                            dopt.titleText = R.string.select_folder;
                        }

                        @Override
                        public void onFsViewerSelected(String request, File dir, final Integer lineNumber) {
                            makeBackup(context, getPrefNamesToBackup(), generateBackupFilepath(context, dir));
                        }
                    }, manager, activity
            );
        }
    }

    public static List<String> getPrefNamesToBackup() {
        List<String> prefs = GsBackupUtils.getPrefNamesToBackup();
        prefs.add(ActionButtonBase.ACTION_ORDER_PREF_NAME);
        return prefs;
    }
}
