package net.group_29.master.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;

import net.group_29.master.ApplicationObject;
import net.group_29.master.R;
import net.group_29.master.frontend.filebrowser.MarkorFileBrowserFactory;
import net.group_29.master.model.AppSettings;
import net.group_29.opoc.format.GsSimpleMarkdownParser;
import net.group_29.opoc.frontend.base.GsPreferenceFragmentBase;
import net.group_29.opoc.frontend.filebrowser.GsFileBrowserOptions;

import java.io.File;
import java.io.IOException;

public class MoreInfoFragment extends GsPreferenceFragmentBase<AppSettings> {
    public static final String TAG = "MoreInfoFragment";

    public static MoreInfoFragment newInstance() {
        return new MoreInfoFragment();
    }

    @Override
    public int getPreferenceResourceForInflation() {
        return R.xml.prefactions__more_information;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected AppSettings getAppSettings(Context context) {
        return ApplicationObject.settings();
    }

    @Override
    public boolean isDividerVisible() {
        return true;
    }

    @Override
    @SuppressWarnings({"ConstantConditions", "ConstantIfStatement", "StatementWithEmptyBody"})
    public Boolean onPreferenceClicked(Preference preference, String key, int keyResId) {
        final FragmentManager fragManager = getActivity().getSupportFragmentManager();
        Activity activity = getActivity();
        if (isAdded() && preference.hasKey()) {
            switch (keyResId) {
                case R.string.pref_key__more_info__app: {
                    _cu.openWebpageInExternalBrowser(getContext(), "https://github.com/Xuanyi-Zhai/MarkdownNotebook-Comp7506-240331#readme");
                    return true;
                }
                case R.string.pref_key__more_info__settings: {
                    _cu.animateToActivity(activity, SettingsActivity.class, false, 124);
                    return true;
                }
                case R.string.pref_key__more_info__rate_app: {
                    _cu.showGooglePlayEntryForThisApp(getContext());
                    return true;
                }
                case R.string.pref_key__more_info__help: {
                    _cu.openWebpageInExternalBrowser(getContext(),
                            String.format("https://github.com/Xuanyi-Zhai/MarkdownNotebook-Comp7506-240331#readme", getString(R.string.app_name_real).toLowerCase()));
                    return true;
                }

                case R.string.pref_key__notebook_directory: {
                    MarkorFileBrowserFactory.showFolderDialog(new GsFileBrowserOptions.SelectionListenerAdapter() {
                        @Override
                        public void onFsViewerSelected(String request, File file, final Integer lineNumber) {
                            _appSettings.setNotebookDirectory(file);
                            _appSettings.setRecreateMainRequired(true);
                            doUpdatePreferences();
                        }

                        @Override
                        public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
                            dopt.titleText = R.string.select_storage_folder;
                            dopt.rootFolder = _appSettings.getNotebookDirectory();
                        }
                    }, fragManager, getActivity());
                    return true;
                }
            }
        }
        return null;
    }

    @Override
    protected boolean isAllowedToTint(Preference pref) {
        return !getString(R.string.pref_key__more_info__app).equals(pref.getKey());
    }

    @Override
    public void doUpdatePreferences() {
        Preference pref;
        String remove = "/storage/emulated/0/";
        updateSummary(R.string.pref_key__notebook_directory,
                _cu.htmlToSpanned("<small><small>" + _appSettings.getNotebookDirectory().getAbsolutePath().replace(remove, "") + "</small></small>")
        );
        updateSummary(R.string.pref_key__quicknote_filepath,
                _cu.htmlToSpanned("<small><small>" + _appSettings.getQuickNoteFile().getAbsolutePath().replace(remove, "") + "</small></small>")
        );
        updateSummary(R.string.pref_key__todo_filepath,
                _cu.htmlToSpanned("<small><small>" + _appSettings.getTodoFile().getAbsolutePath().replace(remove, "") + "</small></small>")
        );
        updatePreference(R.string.pref_key__is_launcher_for_special_files_enabled, null,
                ("Launcher (" + getString(R.string.special_documents) + ")"),
                getString(R.string.app_drawer_launcher_special_files_description), true
        );
        updateSummary(R.string.pref_key__exts_to_always_open_in_this_app, _appSettings.getString(R.string.pref_key__exts_to_always_open_in_this_app, ""));

        updateSummary(R.string.pref_key__snippet_directory_path, _appSettings.getSnippetsDirectory().getAbsolutePath());

        final String fileDescFormat = _appSettings.getString(R.string.pref_key__file_description_format, "");
        if (fileDescFormat.equals("")) {
            updateSummary(R.string.pref_key__file_description_format, getString(R.string.default_));
        } else {
            updateSummary(R.string.pref_key__file_description_format, fileDescFormat);
        }

        setPreferenceVisible(R.string.pref_key__is_multi_window_enabled, Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

        setPreferenceVisible(R.string.pref_key__set_encryption_password, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && _appSettings.isDefaultPasswordSet()) {
            updateSummary(R.string.pref_key__set_encryption_password, getString(R.string.hidden_password));
        }

        final int[] experimentalKeys = new int[]{
                R.string.pref_key__swipe_to_change_mode,
                R.string.pref_key__todotxt__hl_delay,
                R.string.pref_key__markdown__hl_delay_v2,
                R.string.pref_key__theming_hide_system_statusbar,
                R.string.pref_key__tab_width_v2,
                R.string.pref_key__editor_line_spacing,
        };
        for (final int keyId : experimentalKeys) {
            setPreferenceVisible(keyId, _appSettings.isExperimentalFeaturesEnabled());
        }
        if ((pref = findPreference(R.string.pref_key__more_info__help)) != null) {
            pref.setTitle(getString(R.string.help) + " / FAQ");
        }
    }
}
