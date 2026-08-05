package com.obsez.android.lib.filechooser.chooserInternals;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by coco on 6/7/15.
 */
public class ExitFilter implements FileFilter {
    boolean allowHidden;
    boolean onlyDirectory;
    String[] ext;

    public ExitFilter() {
        this(false, false);
    }

    public ExitFilter(String... ext_list) {
        this(false, false, ext_list);
    }

    public ExitFilter(boolean dirOnly, boolean hidden, String... ext_list) {
        allowHidden = hidden;
        onlyDirectory = dirOnly;
        ext = ext_list;
    }

    @Override
    public boolean accept(File pathname) {
        if (!allowHidden) {
            if (pathname.isHidden()) {
                return false;
            }
        }

        if (onlyDirectory) {
            if (!pathname.isDirectory()) {
                return false;
            }
        }

        if (ext == null) {
            return true;
        }

        if (pathname.isDirectory()) {
            return true;
        }

        String ext = FileUtility.getExtensionWithoutDot(pathname);
        for (String e : this.ext) {
            if (ext.equalsIgnoreCase(e)) {
                return true;
            }
        }
        return false;
    }

}
