package com.obsez.android.lib.filechooser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.obsez.android.lib.filechooser.chooserInternals.FileUtility;
import com.obsez.android.lib.filechooser.chooserInternals.UIUtility;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
import static androidx.appcompat.widget.ListPopupWindow.MATCH_PARENT;
import static androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT;
import static androidx.core.view.GravityCompat.END;
import static androidx.core.view.GravityCompat.START;
import static com.obsez.android.lib.filechooser.ChooserDialog.CHOOSE_MODE_DELETE;
import static com.obsez.android.lib.filechooser.ChooserDialog.CHOOSE_MODE_NORMAL;
import static com.obsez.android.lib.filechooser.ChooserDialog.CHOOSE_MODE_SELECT_MULTIPLE;
import static com.obsez.android.lib.filechooser.chooserInternals.UIUtility.getListYScroll;

class ChooserShowListener implements DialogInterface.OnShowListener {
    private WeakReference<ChooserDialog> chooserDialogWeakReference;

    ChooserShowListener(ChooserDialog c) {
        this.chooserDialogWeakReference = new WeakReference<>(c);
    }

    @Override
    public void onShow(final DialogInterface dialog) {
        // ensure that the buttons have the right order
        chooserDialogWeakReference.get().neutralBtn = chooserDialogWeakReference.get().alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        chooserDialogWeakReference.get().negativeBtn = chooserDialogWeakReference.get().alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        chooserDialogWeakReference.get().positiveBtn = chooserDialogWeakReference.get().alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        ViewGroup buttonBar = (ViewGroup) chooserDialogWeakReference.get().positiveBtn.getParent();
        ViewGroup.LayoutParams btnParams = buttonBar.getLayoutParams();
        btnParams.width = MATCH_PARENT;
        buttonBar.setLayoutParams(btnParams);
        buttonBar.removeAllViews();
        btnParams = chooserDialogWeakReference.get().neutralBtn.getLayoutParams();
        if (buttonBar instanceof LinearLayout) {
            ((LinearLayout.LayoutParams) btnParams).weight = 1;
            ((LinearLayout.LayoutParams) btnParams).width = 0;
        }
        if (chooserDialogWeakReference.get().enableOptions) {
            buttonBar.addView(chooserDialogWeakReference.get().neutralBtn, 0, btnParams);
        } else {
            buttonBar.addView(new Space(chooserDialogWeakReference.get().context), 0, btnParams);
        }
        buttonBar.addView(chooserDialogWeakReference.get().negativeBtn, 1);
        buttonBar.addView(chooserDialogWeakReference.get().positiveBtn, 2);

        if (chooserDialogWeakReference.get().enableMultiple) {
            chooserDialogWeakReference.get().positiveBtn.setVisibility(View.INVISIBLE);
        }

        if (chooserDialogWeakReference.get().enableOptions) {
            final int buttonColor = chooserDialogWeakReference.get().neutralBtn.getCurrentTextColor();
            final PorterDuffColorFilter filter = new PorterDuffColorFilter(buttonColor,
                PorterDuff.Mode.SRC_IN);

            chooserDialogWeakReference.get().neutralBtn.setText("");
            chooserDialogWeakReference.get().neutralBtn.setVisibility(View.VISIBLE);
            Drawable dots;
            if (chooserDialogWeakReference.get().optionsIconRes != -1) {
                dots = ContextCompat.getDrawable(chooserDialogWeakReference.get().context, chooserDialogWeakReference.get().optionsIconRes);
            } else if (chooserDialogWeakReference.get().optionsIcon != null) {
                dots = chooserDialogWeakReference.get().optionsIcon;
            } else {
                dots = ContextCompat.getDrawable(chooserDialogWeakReference.get().context, R.drawable.ic_menu_24dp);
            }
            if (dots != null) {
                dots.setColorFilter(filter);
                chooserDialogWeakReference.get().neutralBtn.setCompoundDrawablesWithIntrinsicBounds(dots, null, null, null);
            }

            final class Integer {
                int Int = 0;
            }
            final Integer scroll = new Integer();

            chooserDialogWeakReference.get().list.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    int oldHeight = oldBottom - oldTop;
                    if (v.getHeight() != oldHeight) {
                        int offset = oldHeight - v.getHeight();
                        int newScroll = getListYScroll(chooserDialogWeakReference.get().list);
                        if (scroll.Int != newScroll) offset += scroll.Int - newScroll;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            chooserDialogWeakReference.get().list.scrollListBy(offset);
                        } else {
                            chooserDialogWeakReference.get().list.scrollBy(0, offset);
                        }
                    }
                });

            final Runnable showOptions = new Runnable() {
                @Override
                public void run() {
                    if (chooserDialogWeakReference.get().options.getHeight() == 0) {
                        ViewTreeObserver viewTreeObserver = chooserDialogWeakReference.get().options.getViewTreeObserver();
                        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                if (chooserDialogWeakReference.get().options.getHeight() <= 0) {
                                    return false;
                                }
                                viewTreeObserver.removeOnPreDrawListener(this);
                                scroll.Int = getListYScroll(chooserDialogWeakReference.get().list);
                                if (chooserDialogWeakReference.get().options.getParent() instanceof FrameLayout) {
                                    final ViewGroup.MarginLayoutParams params =
                                        (ViewGroup.MarginLayoutParams) chooserDialogWeakReference.get().list.getLayoutParams();
                                    params.bottomMargin = chooserDialogWeakReference.get().options.getHeight();
                                    chooserDialogWeakReference.get().list.setLayoutParams(params);
                                }
                                chooserDialogWeakReference.get().options.setVisibility(View.VISIBLE);
                                chooserDialogWeakReference.get().options.requestFocus();
                                return true;
                            }
                        });
                    } else {
                        scroll.Int = getListYScroll(chooserDialogWeakReference.get().list);
                        chooserDialogWeakReference.get().options.setVisibility(View.VISIBLE);
                        chooserDialogWeakReference.get().options.requestFocus();
                        if (chooserDialogWeakReference.get().options.getParent() instanceof FrameLayout) {
                            final ViewGroup.MarginLayoutParams params =
                                (ViewGroup.MarginLayoutParams) chooserDialogWeakReference.get().list.getLayoutParams();
                            params.bottomMargin = chooserDialogWeakReference.get().options.getHeight();
                            chooserDialogWeakReference.get().list.setLayoutParams(params);
                        }
                    }
                }
            };
            final Runnable hideOptions = () -> {
                scroll.Int = getListYScroll(chooserDialogWeakReference.get().list);
                chooserDialogWeakReference.get().options.setVisibility(View.GONE);
                if (chooserDialogWeakReference.get().options.getParent() instanceof FrameLayout) {
                    ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) chooserDialogWeakReference.get().list.getLayoutParams();
                    params.bottomMargin = 0;
                    chooserDialogWeakReference.get().list.setLayoutParams(params);
                }
            };

            chooserDialogWeakReference.get().neutralBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (chooserDialogWeakReference.get().newFolderView != null
                        && chooserDialogWeakReference.get().newFolderView.getVisibility() == View.VISIBLE) {
                        return;
                    }

                    if (chooserDialogWeakReference.get().options == null) {
                        // region Draw options view. (this only happens the first time one clicks on options)
                        // Root view (FrameLayout) of the ListView in the AlertDialog.
                        int rootId = chooserDialogWeakReference.get().context.getResources().getIdentifier("contentPanel", "id", chooserDialogWeakReference.get().context.getPackageName());
                        ViewGroup tmpRoot = ((AlertDialog) dialog).findViewById(rootId);
                        // In case the root id was changed or not found.
                        if (tmpRoot == null) {
                            rootId = chooserDialogWeakReference.get().context.getResources().getIdentifier("contentPanel", "id", "android");
                            tmpRoot = ((AlertDialog) dialog).findViewById(rootId);
                            if (tmpRoot == null) return;
                        }
                        final ViewGroup root = tmpRoot;

                        // Create options view.
                        final FrameLayout options = new FrameLayout(chooserDialogWeakReference.get().context);
                        ViewGroup.MarginLayoutParams params;
                        if (root instanceof LinearLayout) {
                            params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                            LinearLayout.LayoutParams param =
                                ((LinearLayout.LayoutParams) chooserDialogWeakReference.get().list.getLayoutParams());
                            param.weight = 1;
                            chooserDialogWeakReference.get().list.setLayoutParams(param);
                        } else {
                            params = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, BOTTOM);
                        }
                        root.addView(options, params);
                        options.setFocusable(false);

                        if (root instanceof FrameLayout) {
                            chooserDialogWeakReference.get().list.bringToFront();
                        }

                        TypedArray ta = chooserDialogWeakReference.get().context.obtainStyledAttributes(R.styleable.FileChooser);
                        final int style = ta.getResourceId(R.styleable.FileChooser_fileChooserDialogStyle,
                            R.style.FileChooserDialogStyle);
                        ta.recycle();
                        final Context buttonContext = new ContextThemeWrapper(chooserDialogWeakReference.get().context, style);

                        // Create a button for the option to create a new directory/folder.
                        final Button createDir = new Button(buttonContext, null,
                            android.R.attr.buttonBarButtonStyle);
                        if (chooserDialogWeakReference.get().createDirRes != -1) {
                            createDir.setText(chooserDialogWeakReference.get().createDirRes);
                        } else if (chooserDialogWeakReference.get().createDir != null) {
                            createDir.setText(chooserDialogWeakReference.get().createDir);
                        } else {
                            createDir.setText(R.string.option_create_folder);
                        }
                        createDir.setTextColor(buttonColor);
                        // Drawable for the button.
                        final Drawable plus;
                        if (chooserDialogWeakReference.get().createDirIconRes != -1) {
                            plus = ContextCompat.getDrawable(chooserDialogWeakReference.get().context, chooserDialogWeakReference.get().createDirIconRes);
                        } else if (chooserDialogWeakReference.get().createDirIcon != null) {
                            plus = chooserDialogWeakReference.get().createDirIcon;
                        } else {
                            plus = ContextCompat.getDrawable(chooserDialogWeakReference.get().context, R.drawable.ic_add_24dp);
                        }
                        if (plus != null) {
                            plus.setColorFilter(filter);
                            createDir.setCompoundDrawablesWithIntrinsicBounds(plus, null, null, null);
                        }
                        params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT,
                            START | CENTER_VERTICAL);
                        params.leftMargin = UIUtility.dip2px(10);
                        options.addView(createDir, params);

                        // Create a button for the option to delete a file.
                        final Button delete = new Button(buttonContext, null,
                            android.R.attr.buttonBarButtonStyle);
                        if (chooserDialogWeakReference.get().deleteRes != -1) {
                            delete.setText(chooserDialogWeakReference.get().deleteRes);
                        } else if (chooserDialogWeakReference.get().delete != null) {
                            delete.setText(chooserDialogWeakReference.get().delete);
                        } else {
                            delete.setText(R.string.options_delete);
                        }
                        delete.setTextColor(buttonColor);
                        final Drawable bin;
                        if (chooserDialogWeakReference.get().deleteIconRes != -1) {
                            bin = ContextCompat.getDrawable(chooserDialogWeakReference.get().context, chooserDialogWeakReference.get().deleteIconRes);
                        } else if (chooserDialogWeakReference.get().deleteIcon != null) {
                            bin = chooserDialogWeakReference.get().deleteIcon;
                        } else {
                            bin = ContextCompat.getDrawable(chooserDialogWeakReference.get().context, R.drawable.ic_delete_24dp);
                        }
                        if (bin != null) {
                            bin.setColorFilter(filter);
                            delete.setCompoundDrawablesWithIntrinsicBounds(bin, null, null, null);
                        }
                        params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT,
                            END | CENTER_VERTICAL);
                        params.rightMargin = UIUtility.dip2px(10);
                        options.addView(delete, params);

                        chooserDialogWeakReference.get().options = options;
                        showOptions.run();

                        // Event Listeners.
                        createDir.setOnClickListener(new View.OnClickListener() {
                            private EditText input = null;

                            @Override
                            public void onClick(final View view) {
                                //Toast.makeText(getBaseContext(), "new folder clicked", Toast
                                // .LENGTH_SHORT).show();
                                hideOptions.run();
                                File newFolder = new File(chooserDialogWeakReference.get().currentDir, "New folder");
                                for (int i = 1; newFolder.exists(); i++) {
                                    newFolder = new File(chooserDialogWeakReference.get().currentDir, "New folder (" + i + ')');
                                }
                                if (this.input != null) {
                                    this.input.setText(newFolder.getName());
                                }

                                if (chooserDialogWeakReference.get().newFolderView == null) {
                                    // region Draw a view with input to create new folder. (this only
                                    // happens the first time one clicks on New folder)
                                    TypedArray ta = chooserDialogWeakReference.get().context.obtainStyledAttributes(
                                        R.styleable.FileChooser);
                                    final int style = ta.getResourceId(
                                        R.styleable.FileChooser_fileChooserNewFolderStyle,
                                        R.style.FileChooserNewFolderStyle);
                                    final Context context = new ContextThemeWrapper(chooserDialogWeakReference.get().context, style);
                                    ta.recycle();
                                    ta = context.obtainStyledAttributes(R.styleable.FileChooser);

                                    try {
                                        //noinspection ConstantConditions
                                        ((AlertDialog) dialog).getWindow().clearFlags(
                                            FLAG_NOT_FOCUSABLE | FLAG_ALT_FOCUSABLE_IM);
                                        //noinspection ConstantConditions
                                        ((AlertDialog) dialog).getWindow().setSoftInputMode(
                                            SOFT_INPUT_STATE_VISIBLE |
                                                ta.getInt(
                                                    R.styleable.FileChooser_fileChooserNewFolderSoftInputMode,
                                                    0x30));
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                    }

                                    // A semitransparent background overlay.
                                    final FrameLayout overlay = new FrameLayout(chooserDialogWeakReference.get().context);
                                    overlay.setBackgroundColor(
                                        ta.getColor(R.styleable.FileChooser_fileChooserNewFolderOverlayColor,
                                            0x60ffffff));
                                    overlay.setScrollContainer(true);
                                    ViewGroup.MarginLayoutParams params;
                                    if (root instanceof FrameLayout) {
                                        params = new FrameLayout.LayoutParams(
                                            MATCH_PARENT, MATCH_PARENT, CENTER);
                                    } else {
                                        params = new LinearLayout.LayoutParams(
                                            MATCH_PARENT, MATCH_PARENT);
                                    }
                                    root.addView(overlay, params);

                                    overlay.setOnClickListener(null);
                                    overlay.setVisibility(View.INVISIBLE);
                                    chooserDialogWeakReference.get().newFolderView = overlay;

                                    // A LinearLayout and a pair of Space to center views.
                                    LinearLayout linearLayout = new LinearLayout(chooserDialogWeakReference.get().context);
                                    params = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT,
                                        CENTER);
                                    overlay.addView(linearLayout, params);
                                    overlay.setFocusable(false);

                                    float widthWeight = ta.getFloat(
                                        R.styleable.FileChooser_fileChooserNewFolderWidthWeight, 0.56f);
                                    if (widthWeight <= 0) widthWeight = 0.56f;
                                    if (widthWeight > 1f) widthWeight = 1f;

                                    Space leftSpace = new Space(chooserDialogWeakReference.get().context);
                                    params = new LinearLayout.LayoutParams(0, WRAP_CONTENT,
                                        (1f - widthWeight) / 2);
                                    linearLayout.addView(leftSpace, params);
                                    leftSpace.setFocusable(false);

                                    // A solid holder view for the EditText and Buttons.
                                    final LinearLayout holder = new LinearLayout(chooserDialogWeakReference.get().context);
                                    holder.setOrientation(LinearLayout.VERTICAL);
                                    holder.setBackgroundColor(
                                        ta.getColor(R.styleable.FileChooser_fileChooserNewFolderBackgroundColor,
                                            0xffffffff));
                                    final int elevation = ta.getInt(
                                        R.styleable.FileChooser_fileChooserNewFolderElevation, 25);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        holder.setElevation(elevation);
                                    } else {
                                        ViewCompat.setElevation(holder, elevation);
                                    }
                                    params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, widthWeight);
                                    linearLayout.addView(holder, params);
                                    holder.setFocusable(false);

                                    Space rightSpace = new Space(chooserDialogWeakReference.get().context);
                                    params = new LinearLayout.LayoutParams(0, WRAP_CONTENT,
                                        (1f - widthWeight) / 2);
                                    linearLayout.addView(rightSpace, params);
                                    rightSpace.setFocusable(false);

                                    final EditText input = new EditText(chooserDialogWeakReference.get().context);
                                    final int color = ta.getColor(
                                        R.styleable.FileChooser_fileChooserNewFolderTextColor, buttonColor);
                                    input.setTextColor(color);
                                    input.getBackground().mutate().setColorFilter(color,
                                        PorterDuff.Mode.SRC_ATOP);
                                    input.setText(newFolder.getName());
                                    input.setSelectAllOnFocus(true);
                                    input.setSingleLine(true);
                                    // There should be no suggestions, but...
                                    input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                        | InputType.TYPE_TEXT_VARIATION_FILTER
                                        | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                                    input.setFilters(new InputFilter[]{
                                        chooserDialogWeakReference.get().newFolderFilter != null ? chooserDialogWeakReference.get().newFolderFilter
                                            : new FileUtility.NewFolderFilter()});
                                    input.setGravity(CENTER_HORIZONTAL);
                                    input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                    params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                                    params.setMargins(3, 2, 3, 0);
                                    holder.addView(input, params);

                                    this.input = input;

                                    // A horizontal LinearLayout to hold buttons
                                    final FrameLayout buttons = new FrameLayout(chooserDialogWeakReference.get().context);
                                    params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                                    holder.addView(buttons, params);

                                    // The Cancel button.
                                    final Button cancel = new Button(buttonContext, null,
                                        android.R.attr.buttonBarButtonStyle);
                                    if (chooserDialogWeakReference.get().newFolderCancelRes != -1) {
                                        cancel.setText(chooserDialogWeakReference.get().newFolderCancelRes);
                                    } else if (chooserDialogWeakReference.get().newFolderCancel != null) {
                                        cancel.setText(chooserDialogWeakReference.get().newFolderCancel);
                                    } else {
                                        cancel.setText(R.string.new_folder_cancel);
                                    }
                                    cancel.setTextColor(buttonColor);
                                    params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT,
                                        START);
                                    buttons.addView(cancel, params);

                                    // The OK button.
                                    final Button ok = new Button(buttonContext, null,
                                        android.R.attr.buttonBarButtonStyle);
                                    if (chooserDialogWeakReference.get().newFolderOkRes != -1) {
                                        ok.setText(chooserDialogWeakReference.get().newFolderOkRes);
                                    } else if (chooserDialogWeakReference.get().newFolderOk != null) {
                                        ok.setText(chooserDialogWeakReference.get().newFolderOk);
                                    } else {
                                        ok.setText(R.string.new_folder_ok);
                                    }
                                    ok.setTextColor(buttonColor);
                                    params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT,
                                        END);
                                    buttons.addView(ok, params);

                                    final int id = cancel.hashCode();
                                    cancel.setId(id);
                                    ok.setNextFocusLeftId(id);
                                    input.setNextFocusLeftId(id);

                                    // Event Listeners.
                                    input.setOnEditorActionListener(
                                        (v, actionId, event) -> {
                                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                                UIUtility.hideKeyboardFrom(chooserDialogWeakReference.get().context, input);
                                                chooserDialogWeakReference.get().createNewDirectory(
                                                    input.getText().toString());
                                                overlay.setVisibility(View.GONE);
                                                overlay.clearFocus();
                                                if (chooserDialogWeakReference.get().enableDpad) {
                                                    Button b = chooserDialogWeakReference.get().neutralBtn;
                                                    b.setFocusable(true);
                                                    b.requestFocus();
                                                    chooserDialogWeakReference.get().list.setFocusable(true);
                                                }
                                                return true;
                                            }
                                            return false;
                                        });
                                    cancel.setOnClickListener(v -> {
                                        UIUtility.hideKeyboardFrom(chooserDialogWeakReference.get().context, input);
                                        overlay.setVisibility(View.GONE);
                                        overlay.clearFocus();
                                        if (chooserDialogWeakReference.get().enableDpad) {
                                            Button b = chooserDialogWeakReference.get().neutralBtn;
                                            b.setFocusable(true);
                                            b.requestFocus();
                                            chooserDialogWeakReference.get().list.setFocusable(true);
                                        }
                                    });
                                    ok.setOnClickListener(v -> {
                                        UIUtility.hideKeyboardFrom(chooserDialogWeakReference.get().context, input);
                                        chooserDialogWeakReference.get().createNewDirectory(
                                            input.getText().toString());
                                        UIUtility.hideKeyboardFrom(chooserDialogWeakReference.get().context, input);
                                        overlay.setVisibility(View.GONE);
                                        overlay.clearFocus();
                                        if (chooserDialogWeakReference.get().enableDpad) {
                                            Button b = chooserDialogWeakReference.get().neutralBtn;
                                            b.setFocusable(true);
                                            b.requestFocus();
                                            chooserDialogWeakReference.get().list.setFocusable(true);
                                        }
                                    });
                                    ta.recycle();
                                    // endregion
                                }

                                if (chooserDialogWeakReference.get().newFolderView.getVisibility() != View.VISIBLE) {
                                    chooserDialogWeakReference.get().newFolderView.setVisibility(View.VISIBLE);
                                    if (chooserDialogWeakReference.get().enableDpad) {
                                        chooserDialogWeakReference.get().newFolderView.requestFocus();
                                        chooserDialogWeakReference.get().neutralBtn.setFocusable(false);
                                        chooserDialogWeakReference.get().list.setFocusable(false);
                                    }
                                    if (chooserDialogWeakReference.get().pathView != null &&
                                        chooserDialogWeakReference.get().pathView.getVisibility() == View.VISIBLE) {
                                        chooserDialogWeakReference.get().newFolderView.setPadding(0, UIUtility.dip2px(32),
                                            0, UIUtility.dip2px(12));
                                    } else {
                                        chooserDialogWeakReference.get().newFolderView.setPadding(0, UIUtility.dip2px(12),
                                            0, UIUtility.dip2px(12));
                                    }
                                } else {
                                    chooserDialogWeakReference.get().newFolderView.setVisibility(View.GONE);
                                    if (chooserDialogWeakReference.get().enableDpad) {
                                        chooserDialogWeakReference.get().newFolderView.clearFocus();
                                        chooserDialogWeakReference.get().neutralBtn.setFocusable(true);
                                        chooserDialogWeakReference.get().list.setFocusable(true);
                                    }
                                }
                            }
                        });
                        delete.setOnClickListener(v1 -> {
                            //Toast.makeText(_c.get()._context, "delete clicked", Toast.LENGTH_SHORT).show();
                            hideOptions.run();

                            if (chooserDialogWeakReference.get()._chooseMode == CHOOSE_MODE_SELECT_MULTIPLE) {
                                boolean success = true;
                                for (File file : chooserDialogWeakReference.get().adapter.getSelected()) {
                                    chooserDialogWeakReference.get().result.onChoosePath(file.getAbsolutePath(), file);
                                    if (success) {
                                        try {
                                            FileUtility.deleteFileRecursively(file);
                                        } catch (IOException e) {
                                            Toast.makeText(chooserDialogWeakReference.get().context, e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                            success = false;
                                        }
                                    }
                                }
                                chooserDialogWeakReference.get().adapter.clearSelected();
                                chooserDialogWeakReference.get().positiveBtn.setVisibility(
                                    View.INVISIBLE);
                                chooserDialogWeakReference.get()._chooseMode = CHOOSE_MODE_NORMAL;
                                chooserDialogWeakReference.get().refreshDirs();
                                return;
                            }

                            chooserDialogWeakReference.get()._chooseMode =
                                chooserDialogWeakReference.get()._chooseMode != CHOOSE_MODE_DELETE ? CHOOSE_MODE_DELETE
                                    : CHOOSE_MODE_NORMAL;
                            if (chooserDialogWeakReference.get()._deleteModeIndicator == null) {
                                chooserDialogWeakReference.get()._deleteModeIndicator = () -> {
                                    if (chooserDialogWeakReference.get()._chooseMode == CHOOSE_MODE_DELETE) {
                                        final int color1 = 0x80ff0000;
                                        final PorterDuffColorFilter red =
                                            new PorterDuffColorFilter(color1,
                                                PorterDuff.Mode.SRC_IN);
                                        chooserDialogWeakReference.get().neutralBtn.getCompoundDrawables()
                                            [0].setColorFilter(
                                            red);
                                        chooserDialogWeakReference.get().neutralBtn.setTextColor(color1);
                                        delete.getCompoundDrawables()[0].setColorFilter(red);
                                        delete.setTextColor(color1);
                                    } else {
                                        chooserDialogWeakReference.get().neutralBtn.getCompoundDrawables()
                                            [0].clearColorFilter();
                                        chooserDialogWeakReference.get().neutralBtn.setTextColor(buttonColor);
                                        delete.getCompoundDrawables()[0].clearColorFilter();
                                        delete.setTextColor(buttonColor);
                                    }
                                };
                            }
                            chooserDialogWeakReference.get()._deleteModeIndicator.run();
                        });
                        // endregion
                    } else if (chooserDialogWeakReference.get().options.getVisibility() == View.VISIBLE) {
                        hideOptions.run();
                    } else {
                        showOptions.run();
                    }
                }
            });
        }
    }
}
