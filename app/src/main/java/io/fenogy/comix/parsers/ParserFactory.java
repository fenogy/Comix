package io.fenogy.comix.parsers;


import java.io.File;
import java.io.IOException;

import io.fenogy.comix.managers.Utils;

public class ParserFactory {

    public static Parser create(String file) {
        return create(new File(file));
    }

    public static Parser create(File file) {
        Parser parser = null;
        String fileName = file.getAbsolutePath().toLowerCase();
        if (file.isDirectory()) {
            parser = new DirectoryParser();
        }
        if (Utils.isZip(fileName)) {
            parser = new ZipParser();
        }
        else if (Utils.isRar(fileName)) {
            parser = new RarParser();
        }
        else if (Utils.isTarball(fileName)) {
            parser = new TarParser();
        }
        else if (Utils.isSevenZ(fileName)) {
            parser = new SevenZParser();
        }
        return tryParse(parser, file);
    }

    private static Parser tryParse(Parser parser, File file) {
        if (parser == null) {
            return null;
        }
        try {
            parser.parse(file);
        }
        catch (IOException e) {
            return null;
        }

        if (parser instanceof DirectoryParser && parser.numPages() < 4)
            return null;

        return parser;
    }
}
