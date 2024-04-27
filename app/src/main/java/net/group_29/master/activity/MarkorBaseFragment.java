package net.group_29.master.activity;

import android.content.Context;

import androidx.annotation.Nullable;

import net.group_29.master.ApplicationObject;
import net.group_29.master.model.AppSettings;
import net.group_29.master.util.MarkorContextUtils;
import net.group_29.opoc.frontend.base.GsFragmentBase;

public abstract class MarkorBaseFragment extends GsFragmentBase<AppSettings, MarkorContextUtils> {
    @Nullable
    @Override
    public AppSettings createAppSettingsInstance(Context applicationContext) {
        return ApplicationObject.settings();
    }

    @Nullable
    @Override
    public MarkorContextUtils createContextUtilsInstance(Context applicationContext) {
        return new MarkorContextUtils(applicationContext);
    }
}
