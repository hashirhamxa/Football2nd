package com.obsez.android.lib.filechooser.chooserTool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.core.content.ContextCompat;

import com.obsez.android.lib.filechooser.R;
import com.obsez.android.lib.filechooser.chooserInternals.FileUtility;
import com.obsez.android.lib.filechooser.chooserInternals.UIUtility;
import com.obsez.android.lib.filechooser.chooserInternals.CustomDrawable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

/**
 * Created by coco on 6/7/15.
 */
public class DirectoryAdapter extends ArrayAdapter<File> {

    public DirectoryAdapter(Context cxt, String dateFormat) {
        super(cxt, R.layout.li_row_textview, R.id.text, new ArrayList<>());
        this.init(dateFormat);
    }

    public DirectoryAdapter(Context cxt, List<File> entries, int resId, String dateFormat) {
        super(cxt, resId, R.id.text, entries);
        this.init(dateFormat);
    }

    @SuppressLint("SimpleDateFormat")
    private void init(String dateFormat) {
        formatter = new SimpleDateFormat(
                dateFormat != null && !"".equals(dateFormat.trim()) ? dateFormat.trim() : "yyyy/MM/dd HH:mm:ss");
        if (defaultFolderIcon == null)
            defaultFolderIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_folder);
        if (defaultFileIcon == null)
            defaultFileIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_file);

        TypedArray ta = getContext().obtainStyledAttributes(R.styleable.FileChooser);
        int colorFilter = ta.getColor(R.styleable.FileChooser_fileListItemSelectedTint,
                getContext().getResources().getColor(R.color.li_row_background_tint));
        ta.recycle();
        this.colorFilter = new PorterDuffColorFilter(colorFilter, PorterDuff.Mode.MULTIPLY);
    }


    @FunctionalInterface
    public interface GetView {
        /**
         * @param file        file that should me displayed
         * @param isSelected  whether file is selected when _enableMultiple is set to true
         * @param isFocused   @deprecated! use fileListItemFocusedDrawable attribute instead
         * @param convertView see {@link ArrayAdapter#getView(int, View, ViewGroup)}
         * @param parent      see {@link ArrayAdapter#getView(int, View, ViewGroup)}
         * @param inflater    a layout inflater with the FileChooser theme wrapped context
         * @return your custom row item view
         */
        @NonNull
        View getView(@NonNull File file, boolean isSelected, @Deprecated boolean isFocused, View convertView,
                     @NonNull ViewGroup parent, @NonNull LayoutInflater inflater);
    }

    public void overrideGetView(GetView getView) {
        this.getView = getView;
    }

    // This function is called to show each view item
    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final File file = super.getItem(position);
        if (file == null) return super.getView(position, convertView, parent);
        final boolean isSelected = selected.get(file.hashCode(), null) != null;
        if (getView != null) {
            return getView.getView(file, isSelected, false, convertView, parent,
                    LayoutInflater.from(getContext()));
        }

        ViewGroup view = (ViewGroup) super.getView(position, convertView, parent);

        TextView tvName = view.findViewById(R.id.text);
        TextView tvSize = view.findViewById(R.id.txt_size);
        TextView tvDate = view.findViewById(R.id.txt_date);
        //ImageView ivIcon = (ImageView) view.findViewById(R.id.icon);

        tvDate.setVisibility(View.VISIBLE);
        tvName.setText(file.getName());
        Drawable icon;
        if (file.isDirectory()) {
            icon = defaultFolderIcon.getConstantState().newDrawable();
            tvSize.setText("");
            if (file.lastModified() != 0L) {
                tvDate.setText(formatter.format(new Date(file.lastModified())));
            } else {
                tvDate.setVisibility(View.GONE);
            }
        } else {
            Drawable d = null;
            if (resolveFileType) {
                d = UIUtility.resolveFileTypeIcon(getContext(), Uri.fromFile(file));
                if (d != null) {
                    d = new CustomDrawable(d, 24, 24);
                }
            }
            if (d == null) {
                d = defaultFileIcon;
            }
            icon = d.getConstantState().newDrawable();
            tvSize.setText(FileUtility.getReadableFileSize(file.length()));
            tvDate.setText(formatter.format(new Date(file.lastModified())));
        }
        if (file.isHidden()) {
            final PorterDuffColorFilter filter = new PorterDuffColorFilter(0x80ffffff,
                    PorterDuff.Mode.SRC_ATOP);
            icon.mutate().setColorFilter(filter);
        }
        tvName.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

        View root = view.findViewById(R.id.root);
        if (root.getBackground() == null) {
            root.setBackgroundResource(R.color.li_row_background);
        }
        if (!isSelected) {
            root.getBackground().clearColorFilter();
        } else {
            root.getBackground().setColorFilter(colorFilter);
        }

        return view;
    }

    public Drawable getDefaultFolderIcon() {
        return defaultFolderIcon;
    }

    public void setDefaultFolderIcon(Drawable defaultFolderIcon) {
        this.defaultFolderIcon = defaultFolderIcon;
    }

    public Drawable getDefaultFileIcon() {
        return defaultFileIcon;
    }

    public void setDefaultFileIcon(Drawable defaultFileIcon) {
        this.defaultFileIcon = defaultFileIcon;
    }

    public boolean isResolveFileType() {
        return resolveFileType;
    }

    public void setResolveFileType(boolean resolveFileType) {
        this.resolveFileType = resolveFileType;
    }

    public void setEntries(List<File> entries) {
        setNotifyOnChange(false);
        super.clear();
        setNotifyOnChange(true);
        super.addAll(entries);
        //_hoveredIndex = -1;
    }

    @Override
    public long getItemId(int position) {
        try {
            //noinspection ConstantConditions
            return getItem(position).hashCode();
        } catch (IndexOutOfBoundsException e) {
            try {
                //noinspection ConstantConditions
                return getItem(0).hashCode();
            } catch (IndexOutOfBoundsException ex) {
                return 0;
            }
        }
    }

    public void selectItem(int position) {
        int id = (int) getItemId(position);
        if (selected.get(id, null) == null) {
            selected.append(id, getItem(position));
        } else {
            selected.delete(id);
        }
        notifyDataSetChanged();
    }

    public boolean isSelected(int position) {
        return isSelectedById((int) getItemId(position));
    }

    public boolean isSelectedById(int id) {
        return selected.get(id, null) != null;
    }

    public boolean isAnySelected() {
        return selected.size() > 0;
    }

    public boolean isOneSelected() {
        return selected.size() == 1;
    }

    public List<File> getSelected() {
        ArrayList<File> list = new ArrayList<File>();
        for (int i = 0; i < selected.size(); i++) {
            list.add(selected.valueAt(i));
        }
        return list;
    }

    public void clearSelected() {
        try {
            selected.clear();
        } catch (Resources.NotFoundException e) {
            selected = new SparseArrayCompat<>();
        }
    }

    public boolean isEmpty() {
        return getCount() == 0 || (getCount() == 1 && (getItem(0) instanceof RootFile));
    }

    public Stack<Integer> getIndexStack() {
        return indexStack;
    }

    private SimpleDateFormat formatter;
    private Drawable defaultFolderIcon = null;
    private Drawable defaultFileIcon = null;
    private boolean resolveFileType = false;
    private PorterDuffColorFilter colorFilter;
    private SparseArrayCompat<File> selected = new SparseArrayCompat<File>();
    private GetView getView = null;
    private final Stack<Integer> indexStack = new Stack<>();
}

