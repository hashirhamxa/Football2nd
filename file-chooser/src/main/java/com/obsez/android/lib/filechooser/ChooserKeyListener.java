package com.obsez.android.lib.filechooser;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;

import java.lang.ref.WeakReference;

class ChooserKeyListener implements DialogInterface.OnKeyListener {
    private WeakReference<ChooserDialog> chooserReference;

    ChooserKeyListener(ChooserDialog c) {
        this.chooserReference = new WeakReference<>(c);
    }

    /**
     * Called when a key is dispatched to a dialog. This allows listeners to
     * get a chance to respond before the dialog.
     *
     * @param dialog  the dialog the key has been dispatched to
     * @param keyCode the code for the physical key that was pressed
     * @param event   the KeyEvent object containing full information about
     *                the event
     * @return {@code true} if the listener has consumed the event,
     * {@code false} otherwise
     */
    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            if (chooserReference.get().newFolderView != null && chooserReference.get().newFolderView.getVisibility() == VISIBLE) {
                chooserReference.get().newFolderView.setVisibility(GONE);
                return true;
            }
            chooserReference.get()._onBackPressed.onBackPressed(chooserReference.get().alertDialog);
            return true;
        }

        if (!chooserReference.get().enableDpad) return true;

        if (!chooserReference.get().list.hasFocus()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (chooserReference.get().neutralBtn == null) {
                        return false;
                    }
                    if (chooserReference.get().neutralBtn.hasFocus() || chooserReference.get().negativeBtn.hasFocus()
                        || chooserReference.get().positiveBtn.hasFocus()) {
                        if (chooserReference.get().options != null && chooserReference.get().options.getVisibility() == VISIBLE) {
                            chooserReference.get().options.requestFocus(
                                chooserReference.get().neutralBtn.hasFocus() ? View.FOCUS_RIGHT : View.FOCUS_LEFT);
                            return true;
                        } else if (chooserReference.get().newFolderView != null
                            && chooserReference.get().newFolderView.getVisibility() == VISIBLE) {
                            chooserReference.get().newFolderView.requestFocus(View.FOCUS_LEFT);
                            return true;
                        } else {
                            chooserReference.get().list.requestFocus();
                            chooserReference.get().lastSelected = true;
                            return true;
                        }
                    }
                    if (chooserReference.get().options != null && chooserReference.get().options.hasFocus()) {
                        chooserReference.get().list.requestFocus();
                        chooserReference.get().lastSelected = true;
                        return true;
                    }
                    break;
                default:
                    return false;
            }
        }

        if (chooserReference.get().list.hasFocus()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    chooserReference.get()._onBackPressed.onBackPressed(chooserReference.get().alertDialog);
                    chooserReference.get().lastSelected = false;
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    chooserReference.get().list.performItemClick(chooserReference.get().list, chooserReference.get().list.getSelectedItemPosition(),
                        chooserReference.get().list.getSelectedItemId());
                    chooserReference.get().lastSelected = false;
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (chooserReference.get().lastSelected) {
                        chooserReference.get().lastSelected = false;
                        if (chooserReference.get().options != null && chooserReference.get().options.getVisibility() == VISIBLE) {
                            chooserReference.get().options.requestFocus();
                        } else {
                            if (chooserReference.get().neutralBtn.getVisibility() == VISIBLE) {
                                chooserReference.get().neutralBtn.requestFocus();
                            } else {
                                chooserReference.get().negativeBtn.requestFocus();
                            }
                        }
                        return true;
                    }
                    break;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        this.chooserReference.clear();
        this.chooserReference = null;
        super.finalize();
    }
}
