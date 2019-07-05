package io.fenogy.comix.parsers;


import io.fenogy.comix.managers.NaturalOrderComparator;
import io.fenogy.comix.managers.Utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TarParser implements Parser {
    private List<TarEntry> mEntries;

    private class TarEntry {
        final TarArchiveEntry entry;
        final byte[] bytes;

        public TarEntry(TarArchiveEntry entry, byte[] bytes) {
            this.entry = entry;
            this.bytes = bytes;
        }
    }

    @Override
    public void parse(File file) throws IOException {
        mEntries = new ArrayList<>();

        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
        TarArchiveInputStream is = new TarArchiveInputStream(fis);
        TarArchiveEntry entry = is.getNextTarEntry();
        while (entry != null) {
            if (entry.isDirectory()) {
                continue;
            }
            if (Utils.isImage(entry.getName())) {
                mEntries.add(new TarEntry(entry, Utils.toByteArray(is)));
            }
            entry = is.getNextTarEntry();
        }

        Collections.sort(mEntries, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return ((TarEntry) o).entry.getName();
            }
        });
    }

    @Override
    public int numPages() {
        return mEntries.size();
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        return new ByteArrayInputStream(mEntries.get(num).bytes);
    }

    @Override
    public String getType() {
        return "tar";
    }

    @Override
    public void destroy() throws IOException {

    }
}
