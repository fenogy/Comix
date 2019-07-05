package io.fenogy.comix.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import io.fenogy.comix.Constants;
import io.fenogy.comix.model.Comic;
import io.fenogy.comix.parsers.Parser;
import io.fenogy.comix.parsers.ParserFactory;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class LocalCoverHandler extends RequestHandler {

    private final static String HANDLER_URI = "localcover";
    private Context mContext;

    public LocalCoverHandler(Context context) {
        mContext = context;
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return HANDLER_URI.equals(data.uri.getScheme());
    }

    @Override
    public Result load(Request data, int networkPolicy) throws IOException {
        String path = getCoverPath(data.uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return new Result(BitmapFactory.decodeFile(path, options), Picasso.LoadedFrom.DISK);
    }

    private String getCoverPath(Uri comicUri) throws IOException {

        File coverFile = Utils.getCacheFile(mContext, comicUri.getPath());

        if (!coverFile.isFile()) {
            Parser parser = ParserFactory.create(comicUri.getPath());
            InputStream stream = parser.getPage(0);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);
            options.inSampleSize = Utils.calculateInSampleSize(options,
                    Constants.COVER_THUMBNAIL_WIDTH, Constants.COVER_THUMBNAIL_HEIGHT);
            options.inJustDecodeBounds = false;
            stream.close();
            stream = parser.getPage(0);
            Bitmap result = BitmapFactory.decodeStream(stream, null, options);

            FileOutputStream outputStream = new FileOutputStream(coverFile);
            result.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.close();
        }

        return coverFile.getAbsolutePath();
    }


    public static Uri getComicCoverUri(Comic comic) {
        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .path(comic.getFile().getAbsolutePath())
                .fragment(comic.getType())
                .build();
    }
}
