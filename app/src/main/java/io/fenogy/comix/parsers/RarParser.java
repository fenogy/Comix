package io.fenogy.comix.parsers;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import io.fenogy.comix.managers.NaturalOrderComparator;
import io.fenogy.comix.managers.Utils;


public class RarParser implements Parser {
    private ArrayList<FileHeader> mHeaders = new ArrayList<FileHeader>();
    private Archive mArchive;
    private File mCacheDir;
    private boolean mSolidFileExtracted = false;

    @Override
    public void parse(File file) throws IOException {
        try {
            mArchive = new Archive(file);
            //new Archive()
        }
        catch (RarException e) {
            throw new IOException("unable to open archive");
        }

        FileHeader header = mArchive.nextFileHeader();
        while (header != null) {
            if (!header.isDirectory()) {
                String name = getName(header);
                if (Utils.isImage(name)) {
                    mHeaders.add(header);
                }
            }

            header = mArchive.nextFileHeader();
        }

        Collections.sort(mHeaders, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return getName((FileHeader) o);
            }
        });
    }

    private String getName(FileHeader header) {
        return header.isUnicode() ? header.getFileNameW() : header.getFileNameString();
    }

    @Override
    public int numPages() {
        return mHeaders.size();
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        if (mArchive.getMainHeader().isSolid()) {
            // solid archives require special treatment
            synchronized (this) {
                if (!mSolidFileExtracted) {
                    for (FileHeader h : mArchive.getFileHeaders()) {
                        if (!h.isDirectory() && Utils.isImage(getName(h))) {
                            getPageStream(h);
                        }
                    }
                    mSolidFileExtracted = true;
                }
            }
        }
        return getPageStream(mHeaders.get(num));
    }

    private InputStream getPageStream(FileHeader header) throws IOException {
        try {
            if (mCacheDir != null) {
                String name = getName(header);
                File cacheFile = new File(mCacheDir, Utils.MD5(name));

                if (cacheFile.exists()) {
                    return new FileInputStream(cacheFile);
                }

                synchronized (this) {
                    if (!cacheFile.exists()) {
                        FileOutputStream os = new FileOutputStream(cacheFile);
                        try {
                            mArchive.extractFile(header, os);
                        }
                        catch (Exception e) {
                            os.close();
                            cacheFile.delete();
                            throw e;
                        }
                        os.close();
                    }
                }
                return new FileInputStream(cacheFile);
            }
            return mArchive.getInputStream(header);
        }
        catch (RarException e) {
            throw new IOException("unable to parse rar");
        }
    }

    @Override
    public void destroy() throws IOException {
        if (mCacheDir != null) {
            for (File f : mCacheDir.listFiles()) {
                f.delete();
            }
            mCacheDir.delete();
        }
        mArchive.close();
    }

    @Override
    public String getType() {
        return "rar";
    }

    public void setCacheDirectory(File cacheDirectory) {
        mCacheDir = cacheDirectory;
        if (!mCacheDir.exists()) {
            mCacheDir.mkdir();
        }
        if (mCacheDir.listFiles() != null) {
            for (File f : mCacheDir.listFiles()) {
                f.delete();
            }
        }
    }
}
