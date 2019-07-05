package io.fenogy.comix.managers;

import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

import io.fenogy.comix.parsers.Parser;


public class ComicCatalogueHandler extends RequestHandler {
    private final static String HANDLER_URI = "cataloguecomic";
    private Parser mParser;

    public ComicCatalogueHandler(Parser parser) {
        mParser = parser;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        int pageNum = Integer.parseInt(request.uri.getFragment());
        InputStream stream = mParser.getPage(pageNum);
        return new Result(stream, Picasso.LoadedFrom.DISK);
    }

    public Uri getThumbUri(int thumbId) {

        if(thumbId < 0)
            thumbId = 0;
        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .authority("")
                .fragment(Integer.toString(thumbId))
                .build();
    }

}
