package com.obsez.android.lib.filechooser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;

import java.lang.ref.WeakReference;

class ChooserBackPressHandler implements ChooserDialog.OnBackPressedListener {
    private final WeakReference<ChooserDialog> chooserReference;

    ChooserBackPressHandler(ChooserDialog e) {
        this.chooserReference = new WeakReference<>(e);
    }

    @Override
    public void onBackPressed(AlertDialog dialog) {
        if (!chooserReference.get().entries.isEmpty()
                && (chooserReference.get().entries.get(0).getName().equals(".."))) {
            if (onBackPressedListener != null) {
                onBackPressedListener.onBackPressed(dialog);
            } else {
                defaultBack.onBackPressed(dialog);
            }
        } else {
            if (onLastBackPressed != null) {
                onLastBackPressed.onBackPressed(dialog);
            } else {
                defaultLastBack.onBackPressed(dialog);
            }
        }
    }

    ChooserDialog.OnBackPressedListener onBackPressedListener;
    ChooserDialog.OnBackPressedListener onLastBackPressed;

    private static final ChooserDialog.OnBackPressedListener defaultLastBack = AppCompatDialog::cancel;
    private static final ChooserDialog.OnBackPressedListener defaultBack = AppCompatDialog::cancel;
}
