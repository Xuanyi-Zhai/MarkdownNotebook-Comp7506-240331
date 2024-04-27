package net.group_29.opoc.frontend.settings;

import android.app.Activity;
import android.content.pm.PackageManager;

import net.group_29.opoc.util.GsContextUtils;

import java.io.File;

@SuppressWarnings({"unused", "WeakerAccess"})
public class GsPermissionChecker {
    protected static final int CODE_PERMISSION_EXTERNAL_STORAGE = GsContextUtils.REQUEST_STORAGE_PERMISSION_M;

    protected Activity _activity;

    public GsPermissionChecker(Activity activity) {
        _activity = activity;
    }

    public boolean doIfExtStoragePermissionGranted() {
        return doIfExtStoragePermissionGranted(null);
    }

    public boolean doIfExtStoragePermissionGranted(final String whyNeeded) {
        if (!GsContextUtils.instance.checkExternalStoragePermission(_activity)) {
            GsContextUtils.instance.requestExternalStoragePermission(_activity, whyNeeded);
            return false;
        }
        return true;
    }

    public boolean checkPermissionResult(final int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0) {
            switch (requestCode) {
                case CODE_PERMISSION_EXTERNAL_STORAGE: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean mkdirIfStoragePermissionGranted(final File dir) {
        return doIfExtStoragePermissionGranted() && (dir.exists() || dir.mkdirs());
    }
}
