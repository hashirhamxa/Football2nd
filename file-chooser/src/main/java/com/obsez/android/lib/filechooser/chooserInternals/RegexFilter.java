package com.obsez.android.lib.filechooser.chooserInternals;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Created by coco on 6/7/15.
 */
public class RegexFilter implements FileFilter {
    boolean allowHidden;
    boolean onlyDirectory;
    Pattern pattern;

    public RegexFilter() {
        this(null);
    }

    public RegexFilter(Pattern ptn) {
        this(false, false, ptn);
    }

    public RegexFilter(boolean dirOnly, boolean hidden, String ptn) {
        allowHidden = hidden;
        onlyDirectory = dirOnly;
        pattern = Pattern.compile(ptn, Pattern.CASE_INSENSITIVE);
    }

    public RegexFilter(boolean dirOnly, boolean hidden, String ptn, int flags) {
        allowHidden = hidden;
        onlyDirectory = dirOnly;
        pattern = Pattern.compile(ptn, flags);
    }

    public RegexFilter(boolean dirOnly, boolean hidden, Pattern ptn) {
        allowHidden = hidden;
        onlyDirectory = dirOnly;
        pattern = ptn;
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

        if (pattern == null) {
            return true;
        }

        if (pathname.isDirectory()) {
            return true;
        }

        String name = pathname.getName();
        if (pattern.matcher(name).matches()) {
            return true;
        }
        return false;
    }

}
