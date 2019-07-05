package io.fenogy.comix.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public interface Parser {
    void parse(File file) throws IOException;
    void destroy() throws IOException;

    String getType();
    InputStream getPage(int num) throws IOException;
    int numPages();
}
