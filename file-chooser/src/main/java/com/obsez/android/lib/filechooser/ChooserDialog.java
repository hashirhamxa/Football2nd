package com.obsez.android.lib.filechooser;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.obsez.android.lib.filechooser.chooserInternals.ExitFilter;
import com.obsez.android.lib.filechooser.chooserInternals.FileUtility;
import com.obsez.android.lib.filechooser.chooserInternals.RegexFilter;
import com.obsez.android.lib.filechooser.chooserPermissions.PermissionsUtility;
import com.obsez.android.lib.filechooser.chooserTool.DirectoryAdapter;
import com.obsez.android.lib.filechooser.chooserTool.RootFile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static androidx.appcompat.widget.ListPopupWindow.MATCH_PARENT;
import static androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT;
import static com.obsez.android.lib.filechooser.chooserInternals.FileUtility.NewFolderFilter;

/**
 * Created by coco on 6/7/15.
 */
public class ChooserDialog implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener {
    @FunctionalInterface
    public interface Result {
        void onChoosePath(String dir, File dirFile);
    }

    public ChooserDialog(Context cxt, @StyleRes int fileChooserTheme) {
        this.context = cxt;
        init(fileChooserTheme);
    }

    public ChooserDialog(Activity activity, @StyleRes int fileChooserTheme) {
        this.context = activity;
        init(fileChooserTheme);
    }

    public ChooserDialog(Fragment fragment, @StyleRes int fileChooserTheme) {
        this.context = fragment.getActivity();
        init(fileChooserTheme);
    }

    public ChooserDialog(Context cxt) {
        this.context = cxt;
        init();
    }

    public ChooserDialog(Activity activity) {
        this.context = activity;
        init();
    }

    public ChooserDialog(Fragment fragment) {
        this.context = fragment.getActivity();
        init();
    }

    private void init() {
        init(null);
    }

    private void init(@Nullable @StyleRes Integer fileChooserTheme) {
        _onBackPressed = new ChooserBackPressHandler(this);

        if (fileChooserTheme == null) {
            TypedValue typedValue = new TypedValue();
            if (!this.context.getTheme().resolveAttribute(
                    R.attr.fileChooserStyle, typedValue, true)) {
                this.context = new ContextThemeWrapper(this.context, R.style.FileChooserStyle);
            } else {
                this.context = new ContextThemeWrapper(this.context, typedValue.resourceId);
            }
        } else {
            //noinspection UnnecessaryUnboxing
            this.context = new ContextThemeWrapper(this.context, fileChooserTheme.intValue());
        }
    }

    public ChooserDialog withFilter(FileFilter ff) {
        withFilter(false, false, (String[]) null);
        this.fileFilter = ff;
        return this;
    }

    public ChooserDialog withFilter(boolean dirOnly, boolean allowHidden, FileFilter ff) {
        withFilter(dirOnly, allowHidden, (String[]) null);
        this.fileFilter = ff;
        return this;
    }

    public ChooserDialog withFilter(boolean allowHidden, String... suffixes) {
        return withFilter(false, allowHidden, suffixes);
    }

    public ChooserDialog withFilter(boolean dirOnly, final boolean allowHidden, String... suffixes) {
        this.dirOnly = dirOnly;
        if (suffixes == null || suffixes.length == 0) {
            this.fileFilter = dirOnly ?
                    file -> file.isDirectory() && (!file.isHidden() || allowHidden) : file ->
                    !file.isHidden() || allowHidden;
        } else {
            this.fileFilter = new ExitFilter(this.dirOnly, allowHidden, suffixes);
        }
        return this;
    }

    public ChooserDialog withFilterRegex(boolean dirOnly, boolean allowHidden, String pattern, int flags) {
        this.dirOnly = dirOnly;
        this.fileFilter = new RegexFilter(this.dirOnly, allowHidden, pattern, flags);
        return this;
    }

    public ChooserDialog withFilterRegex(boolean dirOnly, boolean allowHidden, String pattern) {
        this.dirOnly = dirOnly;
        this.fileFilter = new RegexFilter(this.dirOnly, allowHidden, pattern, Pattern.CASE_INSENSITIVE);
        return this;
    }

    public ChooserDialog withStartFile(String startFile) {
        if (startFile != null) {
            currentDir = new File(startFile);
        } else {
            currentDir = new File(FileUtility.getStoragePath(context, false));
        }

        if (!currentDir.isDirectory()) {
            currentDir = currentDir.getParentFile();
        }

        if (currentDir == null) {
            currentDir = new File(FileUtility.getStoragePath(context, false));
        }

        return this;
    }

    public ChooserDialog withChosenListener(Result r) {
        this.result = r;
        return this;
    }

    /**
     * called every time {@link KeyEvent#KEYCODE_BACK} is caught,
     * and current directory is not the root of Primary/SdCard storage.
     */
    public ChooserDialog withOnBackPressedListener(OnBackPressedListener listener) {
        if (this._onBackPressed instanceof ChooserBackPressHandler) {
            ((ChooserBackPressHandler) this._onBackPressed).onBackPressedListener = listener;
        }
        return this;
    }

    /**
     * called if {@link KeyEvent#KEYCODE_BACK} is caught,
     * and current directory is the root of Primary/SdCard storage.
     */
    public ChooserDialog withOnLastBackPressedListener(OnBackPressedListener listener) {
        if (this._onBackPressed instanceof ChooserBackPressHandler) {
            ((ChooserBackPressHandler) this._onBackPressed).onLastBackPressed = listener;
        }
        return this;
    }

    public ChooserDialog withResources(@StringRes int titleRes, @StringRes int okRes, @StringRes int cancelRes) {
        this.titleRes = titleRes;
        this.okRes = okRes;
        this.negativeRes = cancelRes;
        return this;
    }

    public ChooserDialog withStringResources(@Nullable String titleRes, @Nullable String okRes,
                                             @Nullable String cancelRes) {
        this.title = titleRes;
        this.ok = okRes;
        this.negative = cancelRes;
        return this;
    }

    /**
     * To enable the option pane with create/delete folder on the fly.
     * When u set it true, you may need WRITE_EXTERNAL_STORAGE declaration too.
     *
     * @param enableOptions true/false
     * @return this
     */
    public ChooserDialog enableOptions(boolean enableOptions) {
        this.enableOptions = enableOptions;
        return this;
    }

    public ChooserDialog withOptionResources(@StringRes int createDirRes, @StringRes int deleteRes,
                                             @StringRes int newFolderCancelRes, @StringRes int newFolderOkRes) {
        this.createDirRes = createDirRes;
        this.deleteRes = deleteRes;
        this.newFolderCancelRes = newFolderCancelRes;
        this.newFolderOkRes = newFolderOkRes;
        return this;
    }

    public ChooserDialog withOptionStringResources(@Nullable String createDir, @Nullable String delete,
                                                   @Nullable String newFolderCancel, @Nullable String newFolderOk) {
        this.createDir = createDir;
        this.delete = delete;
        this.newFolderCancel = newFolderCancel;
        this.newFolderOk = newFolderOk;
        return this;
    }

    public ChooserDialog withOptionIcons(@DrawableRes int optionsIconRes, @DrawableRes int createDirIconRes,
                                         @DrawableRes int deleteRes) {
        this.optionsIconRes = optionsIconRes;
        this.createDirIconRes = createDirIconRes;
        this.deleteIconRes = deleteRes;
        return this;
    }

    public ChooserDialog withOptionIcons(@Nullable Drawable optionsIcon, @Nullable Drawable createDirIcon,
                                         @Nullable Drawable deleteIcon) {
        this.optionsIcon = optionsIcon;
        this.createDirIcon = createDirIcon;
        this.deleteIcon = deleteIcon;
        return this;
    }

    public ChooserDialog withNewFolderFilter(NewFolderFilter filter) {
        this.newFolderFilter = filter;
        return this;
    }

    public ChooserDialog withIcon(@DrawableRes int iconId) {
        this.iconRes = iconId;
        return this;
    }

    public ChooserDialog withIcon(@Nullable Drawable icon) {
        this.icon = icon;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ChooserDialog withLayoutView(@LayoutRes int layoutResId) {
        this.layoutRes = layoutResId;
        return this;
    }

    public ChooserDialog withDateFormat() {
        return this.withDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    public ChooserDialog withDateFormat(String format) {
        this.dateFormat = format;
        return this;
    }

    public ChooserDialog withNegativeButton(@StringRes int cancelTitle,
                                            final DialogInterface.OnClickListener listener) {
        this.negativeRes = cancelTitle;
        this.negativeListener = listener;
        return this;
    }

    public ChooserDialog withNegativeButton(@Nullable String cancelTitle,
                                            final DialogInterface.OnClickListener listener) {
        this.negative = cancelTitle;
        if (cancelTitle != null) this.negativeRes = -1;
        this.negativeListener = listener;
        return this;
    }

    public ChooserDialog withNegativeButtonListener(final DialogInterface.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    /**
     * onCancelListener will be triggered on back pressed or clicked outside of dialog
     */
    public ChooserDialog withOnCancelListener(final DialogInterface.OnCancelListener listener) {
        this.cancelListener = listener;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public ChooserDialog withOnDismissListener(final DialogInterface.OnDismissListener listener) {
        dismissListener = listener;
        return this;
    }

    public ChooserDialog withFileIcons(final boolean tryResolveFileTypeAndIcon, final Drawable fileIcon,
                                       final Drawable folderIcon) {
        adapterSetter = adapter -> {
            if (fileIcon != null) adapter.setDefaultFileIcon(fileIcon);
            if (folderIcon != null) adapter.setDefaultFolderIcon(folderIcon);
            adapter.setResolveFileType(tryResolveFileTypeAndIcon);
        };
        return this;
    }

    public ChooserDialog withFileIconsRes(final boolean tryResolveFileTypeAndIcon, final int fileIcon,
                                          final int folderIcon) {
        adapterSetter = adapter -> {
            if (fileIcon != -1) {
                adapter.setDefaultFileIcon(ContextCompat.getDrawable(context, fileIcon));
            }
            if (folderIcon != -1) {
                adapter.setDefaultFolderIcon(
                        ContextCompat.getDrawable(context, folderIcon));
            }
            adapter.setResolveFileType(tryResolveFileTypeAndIcon);
        };
        return this;
    }

    /**
     * @param setter you can override {@link DirectoryAdapter#getView(int, View, ViewGroup)}
     *               see {@link AdapterSetter} for more information
     * @return this
     */
    public ChooserDialog withAdapterSetter(AdapterSetter setter) {
        adapterSetter = setter;
        return this;
    }

    /**
     * @param cb give a hook at navigating up to a directory
     * @return this
     */
    public ChooserDialog withNavigateUpTo(CanNavigateUp cb) {
        folderNavUpCB = cb;
        return this;
    }

    /**
     * @param cb give a hook at navigating to a child directory
     * @return this
     */
    public ChooserDialog withNavigateTo(CanNavigateTo cb) {
        folderNavToCB = cb;
        return this;
    }

    public ChooserDialog disableTitle(boolean disableTitle) {
        this.disableTitle = disableTitle;
        return this;
    }

    /**
     * allows dialog title follows the current folder name
     *
     * @param followDir dialog title will follow the changing of directory
     * @return this
     */
    public ChooserDialog titleFollowsDir(boolean followDir) {
        this.followDir = followDir;
        return this;
    }

    public ChooserDialog displayPath(boolean displayPath) {
        this.displayPath = displayPath;
        return this;
    }

    public ChooserDialog customizePathView(CustomizePathView callback) {
        customizePathView = callback;
        return this;
    }

    public ChooserDialog enableMultiple(boolean enableMultiple) {
        this.enableMultiple = enableMultiple;
        return this;
    }

    public ChooserDialog cancelOnTouchOutside(boolean cancelOnTouchOutside) {
        this.cancelOnTouchOutside = cancelOnTouchOutside;
        return this;
    }

    public ChooserDialog enableDpad(boolean enableDpad) {
        this.enableDpad = enableDpad;
        return this;
    }

    public ChooserDialog build() {
        TypedArray ta = context.obtainStyledAttributes(R.styleable.FileChooser);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context,
                ta.getResourceId(R.styleable.FileChooser_fileChooserDialogStyle, R.style.FileChooserDialogStyle));
        final int style = ta.getResourceId(R.styleable.FileChooser_fileChooserListItemStyle,
                R.style.FileChooserListItemStyle);
        ta.recycle();
        final Context context = new ContextThemeWrapper(this.context, style);
        ta = context.obtainStyledAttributes(R.styleable.FileChooser);
        final int listview_item_selector = ta.getResourceId(R.styleable.FileChooser_fileListItemFocusedDrawable,
                R.drawable.listview_item_selector);
        ta.recycle();

        adapter = new DirectoryAdapter(context, this.dateFormat);
        if (adapterSetter != null) adapterSetter.apply(adapter);

        refreshDirs();
        builder.setAdapter(adapter, this);

        if (!disableTitle) {
            if (titleRes != -1) {
                builder.setTitle(titleRes);
            } else if (title != null) {
                builder.setTitle(title);
            } else {
                builder.setTitle(R.string.choose_file);
            }
        }

        if (iconRes != -1) {
            builder.setIcon(iconRes);
        } else if (icon != null) {
            builder.setIcon(icon);
        }

        if (layoutRes != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setView(layoutRes);
            }
        }

        if (dirOnly || enableMultiple) {
            // choosing folder, or multiple files picker
            DialogInterface.OnClickListener listener = (dialog, which) -> {
                if (result != null) {
                    result.onChoosePath(currentDir.getAbsolutePath(), currentDir);
                }
            };
            if (okRes != -1) {
                builder.setPositiveButton(okRes, listener);
            } else if (ok != null) {
                builder.setPositiveButton(ok, listener);
            } else {
                builder.setPositiveButton(R.string.title_choose, listener);
            }
        }

        if (negativeRes != -1) {
            builder.setNegativeButton(negativeRes, negativeListener);
        } else if (negative != null) {
            builder.setNegativeButton(negative, negativeListener);
        } else {
            builder.setNegativeButton(R.string.dialog_cancel, negativeListener);
        }

        if (cancelListener != null) {
            builder.setOnCancelListener(cancelListener);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && dismissListener != null) {
            builder.setOnDismissListener(dismissListener);
        }

        builder.setOnKeyListener(new ChooserKeyListener(this));

        alertDialog = builder.create();

        alertDialog.setCanceledOnTouchOutside(this.cancelOnTouchOutside);
        alertDialog.setOnShowListener(new ChooserShowListener(this));

        list = alertDialog.getListView();
        list.setOnItemClickListener(this);
        if (enableMultiple) {
            list.setOnItemLongClickListener(this);
        }

        if (enableDpad) {
            list.setSelector(listview_item_selector);
            list.setDrawSelectorOnTop(true);
            list.setItemsCanFocus(true);
            list.setOnItemSelectedListener(this);
            list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        list.requestFocus();
        return this;
    }

    private void showDialog() {
        Window window = alertDialog.getWindow();
        if (window != null) {
            TypedArray ta = context.obtainStyledAttributes(R.styleable.FileChooser);
            window.setGravity(ta.getInt(R.styleable.FileChooser_fileChooserDialogGravity, Gravity.CENTER));
            ta.recycle();
        }
        alertDialog.show();
    }

    public ChooserDialog show() {
        if (alertDialog == null || list == null) {
            build();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            showDialog();
            return this;
        }

        if (permissionListener == null) {
            permissionListener = new PermissionsUtility.OnPermissionListener() {
                @Override
                public void onPermissionGranted(String[] permissions) {
                    boolean show = false;
                    for (String permission : permissions) {
                        if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                                || (Build.VERSION.SDK_INT >= 33 && permission.equals(Manifest.permission.READ_MEDIA_VIDEO))) {
                            show = true;
                            break;
                        }
                    }
                    if (!show) return;
                    if (enableOptions) {
                        show = false;
                        for (String permission : permissions) {
                            if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                show = true;
                                break;
                            }
                        }
                    }
                    if (!show) return;
                    if (adapter.isEmpty()) refreshDirs();
                    showDialog();
                }

                @Override
                public void onPermissionDenied(String[] permissions) {
                    //
                }

                @Override
                public void onShouldShowRequestPermissionRationale(final String[] permissions) {
                    Toast.makeText(context, "You denied the Read/Write permissions on SDCard.",
                            Toast.LENGTH_LONG).show();
                }
            };
        }

        String[] permissions =
                /*_enableOptions ?*/ new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}
                /*: new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}*/;

        if (Build.VERSION.SDK_INT >= 33 && context.getApplicationInfo().targetSdkVersion >= 33) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_VIDEO};
        }

        PermissionsUtility.checkPermissions(context, permissionListener, permissions);

        return this;
    }

    private boolean displayRoot;

    private void displayPath(String path) {
        if (pathView == null) {
            int rootId = context.getResources().getIdentifier("contentPanel", "id", context.getPackageName());
            ViewGroup root = ((AlertDialog) alertDialog).findViewById(rootId);
            // In case the root id was changed or not found.
            if (root == null) {
                rootId = context.getResources().getIdentifier("contentPanel", "id", "android");
                root = ((AlertDialog) alertDialog).findViewById(rootId);
                if (root == null) return;
            }

            ViewGroup.MarginLayoutParams params;
            if (root instanceof LinearLayout) {
                params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            } else {
                params = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.TOP);
            }

            TypedArray ta = context.obtainStyledAttributes(R.styleable.FileChooser);
            int style = ta.getResourceId(R.styleable.FileChooser_fileChooserPathViewStyle,
                    R.style.FileChooserPathViewStyle);
            final Context context = new ContextThemeWrapper(this.context, style);
            ta.recycle();
            ta = context.obtainStyledAttributes(R.styleable.FileChooser);

            displayRoot = ta.getBoolean(R.styleable.FileChooser_fileChooserPathViewDisplayRoot, true);

            pathView = new TextView(context);
            root.addView(pathView, 0, params);

            int elevation = ta.getInt(R.styleable.FileChooser_fileChooserPathViewElevation, 2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pathView.setElevation(elevation);
            } else {
                ViewCompat.setElevation(pathView, elevation);
            }
            ta.recycle();

            if (customizePathView != null) {
                customizePathView.customize(pathView);
            }
        }

        if (path == null) {
            pathView.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams param = ((ViewGroup.MarginLayoutParams) list.getLayoutParams());
            if (pathView.getParent() instanceof FrameLayout) {
                param.topMargin = 0;
            }
            list.setLayoutParams(param);
        } else {
            if (roots == null) {
                roots = FileUtility.getStoragePaths(context).keySet();
            }
            for (String key : roots) {
                if (path.contains(key)) {
                    path = path.substring(displayRoot ? key.lastIndexOf('/') + 1 : key.length());
                    break;
                }
            }
            pathView.setText(path);

            while (pathView.getLineCount() > 1) {
                int i = path.indexOf("/");
                i = path.indexOf("/", i + 1);
                if (i == -1) break;
                path = "..." + path.substring(i);
                pathView.setText(path);
            }

            pathView.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams param = ((ViewGroup.MarginLayoutParams) list.getLayoutParams());
            if (pathView.getHeight() == 0) {
                ViewTreeObserver viewTreeObserver = pathView.getViewTreeObserver();
                viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (pathView.getHeight() <= 0) {
                            return false;
                        }
                        viewTreeObserver.removeOnPreDrawListener(this);
                        if (pathView.getParent() instanceof FrameLayout) {
                            param.topMargin = pathView.getHeight();
                        }
                        list.setLayoutParams(param);
                        list.post(() -> list.setSelection(0));
                        return true;
                    }
                });
            } else {
                if (pathView.getParent() instanceof FrameLayout) {
                    param.topMargin = pathView.getHeight();
                }
                list.setLayoutParams(param);
            }
        }
    }

    private Set<String> roots = null;

    private void listDirs() {
        entries.clear();

        if (currentDir == null) {
            currentDir = new File(FileUtility.getStoragePath(context, false));
        }

        // Get files
        File[] files = currentDir.listFiles(fileFilter);

        // Add the ".." entry
        LinkedHashMap<String, String> storagePaths = FileUtility.getStoragePaths(context);
        Set<String> storageKeys = storagePaths.keySet();
        boolean withinVolume = false;
        for (String storageKey : storageKeys) {
            if (currentDir.getAbsolutePath().startsWith(storageKey)) {
                withinVolume = true;
                break;
            }
        }
        if (!withinVolume) {
            for (String storageKey : storageKeys) {
                entries.add(new RootFile(storageKey, storagePaths.get(storageKey))); //⇠
            }
        }
        boolean displayPath = false;
        if (entries.isEmpty() /*&& _currentDir.getParentFile() != null && _currentDir.getParentFile().canRead()*/) {
            entries.add(new RootFile(currentDir.getParentFile().getAbsolutePath(), ".."));
            displayPath = true;
        }

        if (files == null || !withinVolume) {
            if (alertDialog != null && alertDialog.isShowing() && this.displayPath) {
                displayPath(null);
            }
            return;
        }

        List<File> dirList = new LinkedList<>();
        List<File> fileList = new LinkedList<>();

        for (File f : files) {
            if (f.isDirectory()) {
                dirList.add(f);
            } else {
                fileList.add(f);
            }
        }

        sortByName(dirList);
        sortByName(fileList);
        entries.addAll(dirList);
        entries.addAll(fileList);

        // #45: setup dialog title too
        if (alertDialog != null && !disableTitle) {
            if (followDir) {
                if (displayPath) {
                    alertDialog.setTitle(currentDir.getName());
                } else {
                    if (titleRes != -1) {
                        alertDialog.setTitle(titleRes);
                    } else if (title != null) {
                        alertDialog.setTitle(title);
                    } else {
                        alertDialog.setTitle(R.string.choose_file);
                    }
                }

            }
        }

        // don't display path before alert dialog is shown
        // to avoid the exception under android M:
        //   Caused by android.util.AndroidRuntimeException: requestFeature() must be called before adding
        // content
        // issue #60
        if (alertDialog != null && alertDialog.isShowing() && this.displayPath) {
            if (displayPath) {
                displayPath(currentDir.getPath());
            } else {
                displayPath(null);
            }
        }
    }

    private void sortByName(List<File> list) {
        Collections.sort(list, (f1, f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
    }

    void createNewDirectory(String name) {
        if (FileUtility.createNewDirectory(name, currentDir)) {
            refreshDirs();
            return;
        }

        final File newDir = new File(currentDir, name);
        Toast.makeText(context,
                "Couldn't create folder " + newDir.getName() + " at " + newDir.getAbsolutePath(),
                Toast.LENGTH_LONG).show();
    }

    Runnable _deleteModeIndicator;
    private int scrollTo;

    @Override
    public void onItemClick(AdapterView<?> parent_, View list_, int position, long id_) {
        if (position < 0 || position >= entries.size()) return;

        scrollTo = 0;
        File file = entries.get(position);
        if (file instanceof RootFile) {
            if (folderNavUpCB == null) folderNavUpCB = _defaultNavUpCB;
            /*if (_folderNavUpCB.canUpTo(file))*/
            {
                currentDir = file;
                _chooseMode = _chooseMode == CHOOSE_MODE_DELETE ? CHOOSE_MODE_NORMAL : _chooseMode;
                if (_deleteModeIndicator != null) _deleteModeIndicator.run();
                lastSelected = false;
                if (!adapter.getIndexStack().empty()) {
                    scrollTo = adapter.getIndexStack().pop();
                }
            }
        } else {
            switch (_chooseMode) {
                case CHOOSE_MODE_NORMAL:
                    if (file.isDirectory()) {
                        if (folderNavToCB == null) folderNavToCB = _defaultNavToCB;
                        if (folderNavToCB.canNavigate(file)) {
                            currentDir = file;
                            scrollTo = 0;
                            adapter.getIndexStack().push(position);
                        }
                    } else if ((!dirOnly) && result != null) {
                        alertDialog.dismiss();
                        result.onChoosePath(file.getAbsolutePath(), file);
                        if (enableMultiple) {
                            result.onChoosePath(currentDir.getAbsolutePath(), currentDir);
                        }
                        return;
                    }
                    lastSelected = false;
                    break;
                case CHOOSE_MODE_SELECT_MULTIPLE:
                    if (file.isDirectory()) {
                        if (folderNavToCB == null) folderNavToCB = _defaultNavToCB;
                        if (folderNavToCB.canNavigate(file)) {
                            currentDir = file;
                            scrollTo = 0;
                            adapter.getIndexStack().push(position);
                        }
                    } else {
                        adapter.selectItem(position);
                        if (!adapter.isAnySelected()) {
                            _chooseMode = CHOOSE_MODE_NORMAL;
                            positiveBtn.setVisibility(View.INVISIBLE);
                        }
                        result.onChoosePath(file.getAbsolutePath(), file);
                        return;
                    }
                    break;
                case CHOOSE_MODE_DELETE:
                    try {
                        FileUtility.deleteFileRecursively(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    _chooseMode = CHOOSE_MODE_NORMAL;
                    if (_deleteModeIndicator != null) _deleteModeIndicator.run();
                    scrollTo = -1;
                    break;
                default:
                    // ERROR! It shouldn't get here...
                    return;
            }
        }
        refreshDirs();
        if (scrollTo != -1) {
            list.setSelection(scrollTo);
            list.post(() -> list.setSelection(scrollTo));
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View list, int position, long id) {
        File file = entries.get(position);
        if (file instanceof RootFile || file.isDirectory()) {
            return true;
        }
        if (adapter.isSelected(position)) return true;
        result.onChoosePath(file.getAbsolutePath(), file);
        adapter.selectItem(position);
        _chooseMode = CHOOSE_MODE_SELECT_MULTIPLE;
        positiveBtn.setVisibility(View.VISIBLE);
        if (_deleteModeIndicator != null) _deleteModeIndicator.run();
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        //
    }

    void refreshDirs() {
        listDirs();
        adapter.setEntries(entries);
    }

    public void dismiss() {
        alertDialog.dismiss();
    }

    boolean lastSelected = false;

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
        lastSelected = position == entries.size() - 1;
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {
        lastSelected = false;
    }

    List<File> entries = new ArrayList<>();
    DirectoryAdapter adapter;
    File currentDir;
    Context context;
    AlertDialog alertDialog;
    ListView list;
    Result result = null;
    private boolean dirOnly;
    private FileFilter fileFilter;
    private @StringRes
    int titleRes = -1, okRes = -1, negativeRes = -1;
    private @Nullable
    String title, ok, negative;
    private @DrawableRes
    int iconRes = -1;
    private @Nullable
    Drawable icon;
    private @LayoutRes
    int layoutRes = -1;
    private String dateFormat;
    private DialogInterface.OnClickListener negativeListener;
    private DialogInterface.OnCancelListener cancelListener;
    private DialogInterface.OnDismissListener dismissListener;
    private boolean disableTitle;
    boolean enableOptions;
    private boolean followDir;
    private boolean displayPath = true;
    TextView pathView;
    private CustomizePathView customizePathView;
    View options;
    @StringRes
    int createDirRes = -1, deleteRes = -1, newFolderCancelRes = -1, newFolderOkRes = -1;
    @Nullable
    String createDir, delete, newFolderCancel, newFolderOk;
    @DrawableRes
    int optionsIconRes = -1, createDirIconRes = -1, deleteIconRes = -1;
    @Nullable
    Drawable optionsIcon, createDirIcon, deleteIcon;
    @Nullable
    View newFolderView;
    boolean enableMultiple;
    private PermissionsUtility.OnPermissionListener permissionListener;
    private boolean cancelOnTouchOutside;
    boolean enableDpad = true;
    Button neutralBtn;
    Button negativeBtn;
    Button positiveBtn;


    @FunctionalInterface
    public interface AdapterSetter {
        void apply(DirectoryAdapter adapter);
    }

    private AdapterSetter adapterSetter = null;

    @FunctionalInterface
    public interface CanNavigateUp {
        boolean canUpTo(File dir);
    }

    @FunctionalInterface
    public interface CanNavigateTo {
        boolean canNavigate(File dir);
    }

    private CanNavigateUp folderNavUpCB;
    private CanNavigateTo folderNavToCB;

    private final static CanNavigateUp _defaultNavUpCB = dir -> dir != null && dir.canRead();

    private final static CanNavigateTo _defaultNavToCB = dir -> true;

    /**
     * attempts to move to the parent directory
     *
     * @return true if successful. false otherwise
     */
    public boolean goBack() {
        if (entries.size() > 0 &&
                (entries.get(0).getName().equals(".."))) {
            list.performItemClick(list, 0, 0);
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface OnBackPressedListener {
        void onBackPressed(AlertDialog dialog);
    }

    OnBackPressedListener _onBackPressed;

    private final static String sSdcardStorage = ".. SDCard Storage";
    private final static String sPrimaryStorage = ".. Primary Storage";

    static final int CHOOSE_MODE_NORMAL = 0;
    static final int CHOOSE_MODE_DELETE = 1;
    static final int CHOOSE_MODE_SELECT_MULTIPLE = 2;

    int _chooseMode = CHOOSE_MODE_NORMAL;

    NewFolderFilter newFolderFilter;

    @FunctionalInterface
    public interface CustomizePathView {
        void customize(TextView pathView);
    }
}
