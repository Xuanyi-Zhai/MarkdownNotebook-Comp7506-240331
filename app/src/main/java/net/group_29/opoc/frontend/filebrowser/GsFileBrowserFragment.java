package net.group_29.opoc.frontend.filebrowser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.group_29.master.ApplicationObject;
import net.group_29.master.R;
import net.group_29.master.format.FormatRegistry;
import net.group_29.master.frontend.FileInfoDialog;
import net.group_29.master.frontend.MarkorDialogFactory;
import net.group_29.master.frontend.filebrowser.MarkorFileBrowserFactory;
import net.group_29.master.frontend.filesearch.FileSearchEngine;
import net.group_29.master.model.AppSettings;
import net.group_29.master.util.MarkorContextUtils;
import net.group_29.opoc.frontend.base.GsFragmentBase;
import net.group_29.opoc.model.GsSharedPreferencesPropertyBackend;
import net.group_29.opoc.util.GsContextUtils;
import net.group_29.opoc.util.GsFileUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import other.writeily.model.WrMarkorSingleton;
import other.writeily.ui.WrConfirmDialog;
import other.writeily.ui.WrRenameDialog;

public class GsFileBrowserFragment extends GsFragmentBase<GsSharedPreferencesPropertyBackend, GsContextUtils> implements GsFileBrowserOptions.SelectionListener {
    //########################
    //## Static
    //########################
    public static final String FRAGMENT_TAG = "FilesystemViewerFragment";

    public static GsFileBrowserFragment newInstance() {
        return new GsFileBrowserFragment();
    }

    //########################
    //## Member
    //########################

    private RecyclerView _recyclerList;
    private SwipeRefreshLayout _swipe;
    private TextView _emptyHint;
    private TextView _topNavSubTitle;
    private ImageView _topNavLeftActionButton;

    private GsFileBrowserListAdapter _filesystemViewerAdapter;
    private GsFileBrowserOptions.Options _dopt;
    private GsFileBrowserOptions.SelectionListener _callback;
    private boolean firstResume = true;
    private AppSettings _appSettings;
    private Menu _fragmentMenu;
    private MarkorContextUtils _cu;

    //########################
    //## Methods
    //########################

    public interface FilesystemFragmentOptionsListener {
        GsFileBrowserOptions.Options getFilesystemFragmentOptions(GsFileBrowserOptions.Options existingOptions);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        Context context = getContext();
        _recyclerList = root.findViewById(R.id.ui__filesystem_dialog__list);
        _swipe = root.findViewById(R.id.pull_to_refresh);
        _emptyHint = root.findViewById(R.id.empty_hint);
        _topNavSubTitle = getActivity().findViewById(R.id.top_nav_sub_title);
        _topNavLeftActionButton = getActivity().findViewById(R.id.top_nav_cancel);

        _appSettings = ApplicationObject.settings();
        _cu = new MarkorContextUtils(root.getContext());

        if (!(getActivity() instanceof FilesystemFragmentOptionsListener)) {
            throw new RuntimeException("Error: " + getActivity().getClass().getName() + " doesn't implement FilesystemFragmentOptionsListener");
        }
        setDialogOptions(((FilesystemFragmentOptionsListener) getActivity()).getFilesystemFragmentOptions(_dopt));

        LinearLayoutManager lam = (LinearLayoutManager) _recyclerList.getLayoutManager();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), lam.getOrientation());
        _recyclerList.addItemDecoration(dividerItemDecoration);
        _previousNotebookDirectory = _appSettings.getNotebookDirectory();

        _filesystemViewerAdapter = new GsFileBrowserListAdapter(_dopt, context);
        _recyclerList.setAdapter(_filesystemViewerAdapter);
        _filesystemViewerAdapter.getFilter().filter("");
        onFsViewerDoUiUpdate(_filesystemViewerAdapter);

        _swipe.setOnRefreshListener(() -> {
            _filesystemViewerAdapter.unselectAll();
            _filesystemViewerAdapter.reloadCurrentFolder();
            _swipe.setRefreshing(false);
        });

        _topNavLeftActionButton.setOnClickListener(v -> _filesystemViewerAdapter.unselectAll());

        if (FileSearchEngine.isSearchExecuting.get()) {
            FileSearchEngine.activity.set(new WeakReference<>(getActivity()));
        }
    }

    @Override
    public String getFragmentTag() {
        return "FilesystemViewerFragment";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.opoc_filesystem_fragment;
    }

    private void setDialogOptions(GsFileBrowserOptions.Options options) {
        _dopt = options;
        _callback = _dopt.listener;
        if (_callback != null) {
            _callback.onFsViewerConfig(_dopt); // Configure every time
        }
        _dopt.listener = this;
        checkOptions();
    }

    public void onClicked(View view) {
        switch (view.getId()) {
            case R.id.ui__filesystem_dialog__button_ok:
            case R.id.ui__filesystem_dialog__home: {
                _filesystemViewerAdapter.onClick(view);
                break;
            }
            case R.id.ui__filesystem_dialog__button_cancel: {
                onFsViewerCancel(_dopt.requestId);
                break;
            }

        }
    }

    private void checkOptions() {
        if (_dopt.doSelectFile && !_dopt.doSelectMultiple) {
            _dopt.okButtonEnable = false;
        }
    }


    @Override
    public void onFsViewerSelected(String request, File file, final Integer lineNumber) {
        if (_callback != null) {
            _callback.onFsViewerSelected(_dopt.requestId, file, lineNumber);
        }
    }

    @Override
    public void onFsViewerMultiSelected(String request, File... files) {
        if (_callback != null) {
            _callback.onFsViewerMultiSelected(_dopt.requestId, files);
        }
    }

    @Override
    public void onFsViewerCancel(String request) {
        if (_callback != null) {
            _callback.onFsViewerCancel(_dopt.requestId);
        }
    }

    @Override
    public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
        if (_callback != null) {
            _callback.onFsViewerConfig(dopt);
        }
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onFsViewerDoUiUpdate(GsFileBrowserListAdapter adapter) {
        if (_callback != null) {
            _callback.onFsViewerDoUiUpdate(adapter);
        }

        // Count selected files
        final int amountSelectedFiles = _filesystemViewerAdapter.getCurrentSelection().size();
        final boolean hasSelection = amountSelectedFiles > 0;
        _topNavSubTitle.setText(hasSelection ? (getString(R.string.selected) + " " + amountSelectedFiles) : "");
        _topNavSubTitle.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
        _topNavLeftActionButton.setVisibility(hasSelection ? View.VISIBLE : View.GONE);

        updateMenuItems();
        _emptyHint.postDelayed(() -> _emptyHint.setVisibility(adapter.isCurrentFolderEmpty() ? View.VISIBLE : View.GONE), 200);
        _recyclerList.postDelayed(this::updateMenuItems, 1000);
    }

    private void updateMenuItems() {
        final String curFilepath = (getCurrentFolder() != null ? getCurrentFolder() : new File("/")).getAbsolutePath();
        final Set<File> selFiles = _filesystemViewerAdapter.getCurrentSelection();
        final int selCount = selFiles.size();
        final boolean selMulti1 = _dopt.doSelectMultiple && selCount == 1;
        final boolean selMultiMore = _dopt.doSelectMultiple && selCount > 1;
        final boolean selMultiAny = selMultiMore || selMulti1;
        final boolean selFilesOnly = _filesystemViewerAdapter.isFilesOnlySelected();

        // Check if is a favourite
        boolean selTextFilesOnly = true;
        boolean selDirectoriesOnly = true;
        boolean selWritable = (!curFilepath.equals("/storage") && !curFilepath.equals("/storage/emulated"));
        boolean allSelectedFav = true;
        final Collection<File> favFiles = _dopt.favouriteFiles != null ? _dopt.favouriteFiles : Collections.emptySet();
        for (final File f : selFiles) {
            selTextFilesOnly &= FormatRegistry.isFileSupported(f, true);
            selWritable &= f.canWrite();
            selDirectoriesOnly &= f.isDirectory();
            allSelectedFav &= favFiles.contains(f);
        }

        if (_fragmentMenu != null && _fragmentMenu.findItem(R.id.action_delete_selected_items) != null) {
            _fragmentMenu.findItem(R.id.action_search).setVisible(selFiles.isEmpty() && !_filesystemViewerAdapter.isCurrentFolderVirtual());
            _fragmentMenu.findItem(R.id.action_delete_selected_items).setVisible((selMulti1 || selMultiMore) && selWritable);
            _fragmentMenu.findItem(R.id.action_rename_selected_item).setVisible(selMulti1 && selWritable);
            _fragmentMenu.findItem(R.id.action_info_selected_item).setVisible(selMulti1);
            _fragmentMenu.findItem(R.id.action_move_selected_items).setVisible((selMulti1 || selMultiMore) && selWritable && !_cu.isUnderStorageAccessFolder(getContext(), getCurrentFolder(), true));
            _fragmentMenu.findItem(R.id.action_copy_selected_items).setVisible((selMulti1 || selMultiMore) && selWritable && !_cu.isUnderStorageAccessFolder(getContext(), getCurrentFolder(), true));
            _fragmentMenu.findItem(R.id.action_share_files).setVisible(selFilesOnly && (selMulti1 || selMultiMore) && !_cu.isUnderStorageAccessFolder(getContext(), getCurrentFolder(), true));
            _fragmentMenu.findItem(R.id.action_go_to).setVisible(!_filesystemViewerAdapter.areItemsSelected());
            _fragmentMenu.findItem(R.id.action_sort).setVisible(!_filesystemViewerAdapter.areItemsSelected());
            _fragmentMenu.findItem(R.id.action_settings).setVisible(!_filesystemViewerAdapter.areItemsSelected());
            _fragmentMenu.findItem(R.id.action_favourite).setVisible(selMultiAny && !allSelectedFav);
            _fragmentMenu.findItem(R.id.action_favourite_remove).setVisible(selMultiAny && allSelectedFav);
        }
    }

    @Override
    public void onFsViewerItemLongPressed(File file, boolean doSelectMultiple) {
        if (_callback != null) {
            _callback.onFsViewerItemLongPressed(file, doSelectMultiple);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (_filesystemViewerAdapter != null && _filesystemViewerAdapter.canGoUp() && !_filesystemViewerAdapter.isCurrentFolderHome()) {
            _filesystemViewerAdapter.goUp();
            return true;
        }
        return super.onBackPressed();
    }

    public void reloadCurrentFolder() {
        _filesystemViewerAdapter.reloadCurrentFolder();
        onFsViewerDoUiUpdate(_filesystemViewerAdapter);
    }

    public File getCurrentFolder() {
        return _filesystemViewerAdapter != null ? _filesystemViewerAdapter.getCurrentFolder() : null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        _filesystemViewerAdapter.saveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        _filesystemViewerAdapter.restoreSavedInstanceState(savedInstanceState);
    }

    private static File _previousNotebookDirectory;

    @Override
    public void onResume() {
        super.onResume();
        if (!_appSettings.getNotebookDirectory().equals(_previousNotebookDirectory)) {
            _dopt.rootFolder = _appSettings.getNotebookDirectory();
            _filesystemViewerAdapter.setCurrentFolder(_dopt.rootFolder);
        }

        if (!firstResume) {
            if (_filesystemViewerAdapter.getCurrentFolder() != null) {
                _filesystemViewerAdapter.reloadCurrentFolder();
            }
        }

        onFsViewerDoUiUpdate(_filesystemViewerAdapter);
        firstResume = false;

        final File folder = getCurrentFolder();
        final Activity activity = getActivity();
        if (isVisible() && folder != null && activity != null) {
            activity.setTitle(folder.getName());
            reloadCurrentFolder();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filesystem__menu, menu);
        _cu.tintMenuItems(menu, true, Color.WHITE);
        _cu.setSubMenuIconsVisibility(menu, true);

        MenuItem item;

        if ((item = menu.findItem(R.id.action_sort_by_name)) != null && GsFileUtils.SORT_BY_NAME.equals(_dopt.sortByType)) {
            item.setChecked(true);
        } else if ((item = menu.findItem(R.id.action_sort_by_date)) != null && GsFileUtils.SORT_BY_MTIME.equals(_dopt.sortByType)) {
            item.setChecked(true);
        } else if ((item = menu.findItem(R.id.action_sort_by_filesize)) != null && GsFileUtils.SORT_BY_FILESIZE.equals(_dopt.sortByType)) {
            item.setChecked(true);
        } else if ((item = menu.findItem(R.id.action_sort_by_mimetype)) != null && GsFileUtils.SORT_BY_MIMETYPE.equals(_dopt.sortByType)) {
            item.setChecked(true);
        }

        _fragmentMenu = menu;
        updateMenuItems();
    }

    public GsFileBrowserListAdapter getAdapter() {
        return _filesystemViewerAdapter;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int _id = item.getItemId();
        final Set<File> currentSelection = _filesystemViewerAdapter.getCurrentSelection();

        switch (_id) {
            case R.id.action_sort_by_name: {
                item.setChecked(true);
                _dopt.sortByType = _appSettings.setFileBrowserSortByType(GsFileUtils.SORT_BY_NAME);
                sortAdapter();
                return true;
            }
            case R.id.action_sort_by_date: {
                item.setChecked(true);
                _dopt.sortByType = _appSettings.setFileBrowserSortByType(GsFileUtils.SORT_BY_MTIME);
                sortAdapter();
                return true;
            }
            case R.id.action_sort_by_filesize: {
                item.setChecked(true);
                _dopt.sortByType = _appSettings.setFileBrowserSortByType(GsFileUtils.SORT_BY_FILESIZE);
                sortAdapter();
                return true;
            }
            case R.id.action_sort_by_mimetype: {
                item.setChecked(true);
                _dopt.sortByType = _appSettings.setFileBrowserSortByType(GsFileUtils.SORT_BY_MIMETYPE);
                sortAdapter();
                return true;
            }
            case R.id.action_search: {
                executeSearchAction();
                return true;
            }
            case R.id.action_go_to_home:
            case R.id.action_go_to_popular_files:
            case R.id.action_go_to_recent_files:
            case R.id.action_go_to_favourite_files:
            case R.id.action_go_to_storage:{
                final File folder = _appSettings.getFolderToLoadByMenuId(_id);
                _filesystemViewerAdapter.setCurrentFolder(folder);
                Toast.makeText(getContext(), folder.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.action_favourite: {
                if (_filesystemViewerAdapter.areItemsSelected()) {
                    final Set<File> favs = _appSettings.getFavouriteFiles();
                    favs.addAll(currentSelection);
                    _appSettings.setFavouriteFiles(favs);
                    _dopt.favouriteFiles = favs;
                    updateMenuItems();
                }
                return true;
            }
            case R.id.action_favourite_remove: {
                if (_filesystemViewerAdapter.areItemsSelected()) {
                    final Set<File> favs = _appSettings.getFavouriteFiles();
                    favs.removeAll(currentSelection);
                    _appSettings.setFavouriteFiles(favs);
                    _dopt.favouriteFiles = favs;
                    updateMenuItems();
                }
                return true;
            }
            case R.id.action_delete_selected_items: {
                askForDeletingFilesRecursive((confirmed, data) -> {
                    if (confirmed) {
                        Runnable deleter = () -> {
                            WrMarkorSingleton.getInstance().deleteSelectedItems(currentSelection, getContext());
                            _recyclerList.post(() -> {
                                _filesystemViewerAdapter.reloadCurrentFolder();
                            });
                        };
                        new Thread(deleter).start();
                    }
                });
                return true;
            }

            case R.id.action_move_selected_items:
            case R.id.action_copy_selected_items: {
                askForMoveOrCopy(_id == R.id.action_move_selected_items);
                return true;
            }

            case R.id.action_check_all: {
                _filesystemViewerAdapter.selectAll();
                return true;
            }

            case R.id.action_share_files: {
                MarkorContextUtils s = new MarkorContextUtils(getContext());
                s.shareStreamMultiple(getContext(), currentSelection, "*/*");
                _filesystemViewerAdapter.reloadCurrentFolder();
                return true;
            }

            case R.id.action_info_selected_item: {
                if (_filesystemViewerAdapter.areItemsSelected()) {
                    File file = new ArrayList<>(currentSelection).get(0);
                    FileInfoDialog.show(file, getChildFragmentManager());
                }
                return true;
            }

            case R.id.action_rename_selected_item: {
                if (_filesystemViewerAdapter.areItemsSelected()) {
                    final File file = currentSelection.iterator().next();
                    final WrRenameDialog renameDialog = WrRenameDialog.newInstance(file, renamedFile -> reloadCurrentFolder());
                    renameDialog.show(getChildFragmentManager(), WrRenameDialog.FRAGMENT_TAG);
                }
                return true;
            }

        }

        return false;
    }

    private void executeSearchAction() {
        final File currentFolder = getCurrentFolder();
        MarkorDialogFactory.showSearchFilesDialog(getActivity(), currentFolder, (relPath, lineNumber, isLong) -> {
            final File load = new File(currentFolder, relPath);
            if (!isLong) {
                if (load.isDirectory()) {
                    _filesystemViewerAdapter.setCurrentFolder(load);
                } else {
                    onFsViewerSelected("", load, lineNumber);
                }
            } else {
                _filesystemViewerAdapter.showFile(load);
            }
        });
    }

    public void sortAdapter() {
        reloadCurrentFolder();
    }

    public void clearSelection() {
        if (_filesystemViewerAdapter != null) { // Happens when restoring after rotation etc
            _filesystemViewerAdapter.unselectAll();
        }
    }


    ///////////////
    public void askForDeletingFilesRecursive(WrConfirmDialog.ConfirmDialogCallback confirmCallback) {
        final ArrayList<File> itemsToDelete = new ArrayList<>(_filesystemViewerAdapter.getCurrentSelection());
        StringBuilder message = new StringBuilder(String.format(getString(R.string.do_you_really_want_to_delete_this_witharg), getResources().getQuantityString(R.plurals.documents, itemsToDelete.size())) + "\n\n");

        for (File f : itemsToDelete) {
            message.append("\n").append(f.getAbsolutePath());
        }

        WrConfirmDialog confirmDialog = WrConfirmDialog.newInstance(getString(R.string.confirm_delete), message.toString(), itemsToDelete, confirmCallback);
        confirmDialog.show(getChildFragmentManager(), WrConfirmDialog.FRAGMENT_TAG);
    }

    private void askForMoveOrCopy(final boolean isMove) {
        final List<File> files = new ArrayList<>(_filesystemViewerAdapter.getCurrentSelection());
        MarkorFileBrowserFactory.showFolderDialog(new GsFileBrowserOptions.SelectionListenerAdapter() {
            private GsFileBrowserOptions.Options _doptMoC;

            @Override
            public void onFsViewerSelected(String request, File file, Integer lineNumber) {
                super.onFsViewerSelected(request, file, null);
                WrMarkorSingleton.getInstance().moveOrCopySelected(files, file, getActivity(), isMove);
                _filesystemViewerAdapter.unselectAll();
                _filesystemViewerAdapter.reloadCurrentFolder();
            }

            @Override
            public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
                _doptMoC = dopt;
                _doptMoC.titleText = isMove ? R.string.move : R.string.copy;
                _doptMoC.rootFolder = _appSettings.getNotebookDirectory();
                _doptMoC.startFolder = getCurrentFolder();
                // Directories cannot be moved into themselves. Don't give users the option
                final Set<String> selSet = new HashSet<>();
                for (final File f : files) {
                    selSet.add(f.getAbsolutePath());
                }
                _doptMoC.fileOverallFilter = (context, test) -> !selSet.contains(test.getAbsolutePath());
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFsViewerDoUiUpdate(GsFileBrowserListAdapter adapter) {
                if (_doptMoC.listener instanceof GsFileBrowserDialog) {
                    final TextView titleView = ((GsFileBrowserDialog) _doptMoC.listener)._dialogTitle;
                    if (titleView != null && adapter.getCurrentFolder() != null) {
                        titleView.setText(String.format("%s → %s", titleView.getContext().getString(isMove ? R.string.move : R.string.copy), adapter.getCurrentFolder().getName()));
                    }
                }
            }

            @Override
            public void onFsViewerCancel(final String request) {
                super.onFsViewerCancel(request);
                _filesystemViewerAdapter.reloadCurrentFolder(); // May be new folders
            }
        }, getChildFragmentManager(), getActivity());
    }

    private void showImportDialog() {
        MarkorFileBrowserFactory.showFileDialog(new GsFileBrowserOptions.SelectionListenerAdapter() {
            @Override
            public void onFsViewerSelected(String request, File file, final Integer lineNumber) {
                importFile(file);
                reloadCurrentFolder();
            }

            @Override
            public void onFsViewerMultiSelected(String request, File... files) {
                for (File file : files) {
                    importFile(file);
                }
                reloadCurrentFolder();
            }

            @Override
            public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
//                dopt.titleText = R.string.import_from_device;
                dopt.doSelectMultiple = true;
                dopt.doSelectFile = true;
                dopt.doSelectFolder = true;
            }
        }, getChildFragmentManager(), getActivity(), null);
    }

    private void importFile(final File file) {
        if (new File(getCurrentFolder().getAbsolutePath(), file.getName()).exists()) {
            String message = getString(R.string.file_already_exists_overwerite) + "\n[" + file.getName() + "]";
            // Ask if overwriting is okay
            WrConfirmDialog d = WrConfirmDialog.newInstance(
                    getString(R.string.confirm_overwrite), message, file, (confirmed, data) -> {
                        if (confirmed) {
                            importFileToCurrentDirectory(getActivity(), file);
                        }
                    });
            d.show(getChildFragmentManager(), WrConfirmDialog.FRAGMENT_TAG);
        } else {
            // Import
            importFileToCurrentDirectory(getActivity(), file);
        }
    }

    private void importFileToCurrentDirectory(Context context, File sourceFile) {
        GsFileUtils.copyFile(sourceFile, new File(getCurrentFolder().getAbsolutePath(), sourceFile.getName()));
        Toast.makeText(context, getString(R.string.import_) + ": " + sourceFile.getName(), Toast.LENGTH_LONG).show();
    }

    public void setCurrentFolder(final File folder) {
        if (folder != null && folder.canRead() && _filesystemViewerAdapter != null) {
            _filesystemViewerAdapter.setCurrentFolder(folder);
        }
    }

    public GsFileBrowserOptions.Options getOptions() {
        return _dopt;
    }
}
