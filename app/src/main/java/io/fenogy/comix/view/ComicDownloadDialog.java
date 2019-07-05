package io.fenogy.comix.view;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.fenogy.comix.R;
import io.fenogy.comix.fragment.LibraryBrowserFragment;
import io.fenogy.comix.managers.ComicCatalogueHandler;
import io.fenogy.comix.model.Api;
import io.fenogy.comix.model.CatalogueItem;
import io.fenogy.comix.parsers.Parser;
import io.fenogy.comix.parsers.ParserFactory;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.ContentValues.TAG;



public class ComicDownloadDialog extends AppCompatDialog
        implements View.OnClickListener {

    private Button mDownloadButton;
    private Button mCancelButton;
    private Button mOpenButton;
    private TextView mTitleTextView;
    private TextView mProgressTextView;
    private TextView mNameTextView;
    private TextView mSizeTextView;
    private TextView mDescriptionTextView;
    private TextView mAuthorTextView;

    private ProgressBar mProgressBar;
    private int progressStatus = 0;
    private Uri uri;
    private Picasso mPicasso;


    private String mThumbsFilename;
    private Parser mParser;
    private ComicCatalogueHandler mCatalogueHandler;
    private int mCurrentThumb;
    private CoverImageView mThumbImageView;
    private ImageView mSep;
    private CatalogueItem mCatalogueItem;
    private Context mContext;
    private boolean isFinishedDownload;
    private boolean isDownloadInProgress = false;




    public ComicDownloadDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_comic_download);
        mDownloadButton = (Button) findViewById(R.id.button_download_comic);
        mCancelButton = (Button) findViewById(R.id.button_cancel);
        mOpenButton = (Button) findViewById(R.id.button_read);

        mTitleTextView = (TextView) findViewById(R.id.tv_comic_download_title);
        mProgressTextView = (TextView) findViewById(R.id.tv_progress);
        mNameTextView = (TextView) findViewById(R.id.tv_comic_name);
        mSizeTextView = (TextView) findViewById(R.id.tv_comic_size);
        mDescriptionTextView = (TextView) findViewById(R.id.tv_comic_description);
        mAuthorTextView = (TextView) findViewById(R.id.tv_comic_author);
        //mSep = (ImageView)findViewById(R.id.imageView5);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mContext = context;
        mDownloadButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mOpenButton.setOnClickListener(this);

        mNameTextView.setText("නම : " );
        mSizeTextView.setText("ප්\u200Dරමාණය : ");
        mDescriptionTextView.setText("විස්තරය : ");
        mAuthorTextView.setText("කතෘ : ");
        isFinishedDownload = false;



    }

    public ComicDownloadDialog(Context context, CatalogueItem catalogueItem) {
        super(context);
        setContentView(R.layout.dialog_comic_download);
        mDownloadButton = (Button) findViewById(R.id.button_download_comic);
        mCancelButton = (Button) findViewById(R.id.button_cancel);
        mOpenButton = (Button) findViewById(R.id.button_read);

        mTitleTextView = (TextView) findViewById(R.id.tv_comic_download_title);
        mProgressTextView = (TextView) findViewById(R.id.tv_progress);
        mNameTextView = (TextView) findViewById(R.id.tv_comic_name);
        mSizeTextView = (TextView) findViewById(R.id.tv_comic_size);
        mDescriptionTextView = (TextView) findViewById(R.id.tv_comic_description);
        mAuthorTextView = (TextView) findViewById(R.id.tv_comic_author);
        mThumbImageView = (CoverImageView) findViewById(R.id.cover_image_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mContext = context;

        mDownloadButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mOpenButton.setOnClickListener(this);
        mCatalogueItem = catalogueItem;
        mTitleTextView.setText(catalogueItem.getName().substring(0,catalogueItem.getName().indexOf('.')));
        mNameTextView.setText("නම : " +catalogueItem.getName().substring(0,catalogueItem.getName().indexOf('.')));
        mSizeTextView.setText("ප්\u200Dරමාණය : "+catalogueItem.getSize());
        mDescriptionTextView.setText("විස්තරය : "+catalogueItem.getDescription());
        mAuthorTextView.setText("කතෘ : "+catalogueItem.getAuthor());

        File thumbsFile = new File((context.getExternalFilesDir(null)), "thumbs.zip" );
        mParser = ParserFactory.create(thumbsFile);
        mThumbsFilename = thumbsFile.getName();

        mCurrentThumb = catalogueItem.getId()-1;

        mCatalogueHandler = new ComicCatalogueHandler(mParser);
        mPicasso = new Picasso.Builder(context)
                .addRequestHandler(mCatalogueHandler)
                .build();

        uri = mCatalogueHandler.getThumbUri(mCurrentThumb);
//            mPicasso.load(uri)
//                    .into(mCoverView);

//////////////////////////////////////////////////////////////////////

        try{

            mPicasso.load(uri)
                    .into(mThumbImageView);
        }catch(NullPointerException e){

            mPicasso.load(Uri.parse("android.resource://io.fenogy.comix/" + R.drawable.placeholder))
                    .into(mThumbImageView);
        }

    }





    @Override
    public void onClick(View v) {
        if (v == mDownloadButton) {

//            if(!isFinishedDownload) {
//                mProgressBar.setVisibility(View.VISIBLE);
//                mProgressTextView.setVisibility(View.VISIBLE);
//                if(mSep != null)
//                mSep.setVisibility(View.VISIBLE);
//                LibraryBrowserFragment.RequireLibraryUpdate = true;
//                downloadComicFile();
//            }else{
//
//                //Toast.makeText(mContext,"Download will continue in background..",Toast.LENGTH_SHORT).show();
//                dismiss();
//            }
            if(!isDownloadInProgress) {

                if (!isFinishedDownload) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressTextView.setVisibility(View.VISIBLE);
                    if (mSep != null)
                        mSep.setVisibility(View.VISIBLE);
                    LibraryBrowserFragment.RequireLibraryUpdate = true;
                    downloadComicFile();
                } else {

                    //Toast.makeText(mContext,"Download will continue in background..",Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }else{

                Toast.makeText(mContext,"Download already started..",Toast.LENGTH_SHORT).show();
            }


        }else if(v == mCancelButton){

            if(isDownloadInProgress) {
                Toast.makeText(mContext, "Download will continue in background..", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        }

    }

    public void downloadComicFile(){

        int cacheSize = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(mContext.getCacheDir(), cacheSize);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .build();

        //Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://fenogy.file.core.windows.net/").client(okHttpClient);
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://onedrive.live.com/").client(okHttpClient);
        //https://onedrive.live.com/download?cid=94749B4F47280D97&resid=94749B4F47280D97%21401&authkey=AHsLrWlBu8wLWow

        Retrofit retrofit = builder.build();
        Toast.makeText(mContext,"Keep dialog open till download finished..",Toast.LENGTH_SHORT).show();

        Api downloadService = retrofit.create(Api.class);

//        String url = "new/media/" + mCatalogueItem.getName()
//                + "?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D";
        //url = "100MB.bin";
        //String url =mCatalogueItem.getName();
        String url =mCatalogueItem.getUri();
        //String url = "download?cid=94749B4F47280D97&resid=94749B4F47280D97%21401&authkey=AHsLrWlBu8wLWow";
        Call<ResponseBody> call2 = downloadService.downloadFileStream(url);
        mProgressTextView.setText("Requested.. ");
        isDownloadInProgress = true;
        call2.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {


                if (response.isSuccessful()) {
                    //Log.d(TAG, "server contacted and has file");
                    mProgressTextView.setText("Waiting.. ");
                    //now the download is started and not cancelable
                    isDownloadInProgress = true;
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {

                            boolean writtenToDisk;

                            try {


                                // todo change the file location/name according to your needs
                                File directory = new File(mContext.getExternalFilesDir(null)+File.separator+"media");
                                if(!directory.exists()) {
                                    directory.mkdirs();
                                }
                                File futureStudioIconFile = new File(mContext.getExternalFilesDir(null) + File.separator +"media" + File.separator + mCatalogueItem.getName());

                                InputStream inputStream = null;
                                OutputStream outputStream = null;


                                try {
                                    byte[] fileReader = new byte[4096];

                                    long fileSize = response.body().contentLength();
                                    long fileSizeDownloaded = 0;

                                    inputStream = response.body().byteStream();
                                    outputStream = new FileOutputStream(futureStudioIconFile);

                                    while (true) {
                                        int read = inputStream.read(fileReader);

                                        if (read == -1) {
                                            break;
                                        }

                                        outputStream.write(fileReader, 0, read);

                                        fileSizeDownloaded += read;

                                        //Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                                        x= fileSizeDownloaded;
                                        y= fileSize;
                                        publishProgress();

                                    }

                                    outputStream.flush();
                                    isFinishedDownload = true;
                                    isDownloadInProgress = false;
                                    writtenToDisk= true;
                                } catch (IOException e) {
                                    writtenToDisk= false;
                                } finally {
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }

                                    if (outputStream != null) {
                                        outputStream.close();
                                    }
                                }
                            } catch (IOException e) {
                                writtenToDisk= false;
                            }

                            //Log.d(TAG, "file download was a success? " + writtenToDisk);
                            return null;
                        }

                        @Override
                        protected void onProgressUpdate(Void... voids) {
                            updateProgress2();
                            super.onProgressUpdate();
                        }
                    }.execute();

                } else {
                    //Log.d(TAG, "server contact failed");
                    isDownloadInProgress = false;
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.e(TAG, "error");
                //Toast.makeText(,"failed",Toast.LENGTH_SHORT).show();
                mProgressTextView.setText("Failed..check connectivity or update catalogue..");
                isDownloadInProgress = false;
            }
        });

    }
    public long x,y;

    public void updateProgress2(){

        double xx,yy;
        String measureStr = "KB";
        //if larger than 1MB
        if(y > 1048576){
            //show in MB
            xx = round((x/(1024*1024)),2);
            yy = round((y/(1024*1024)),2);
            measureStr = "MB";
        }else{
            //show in KB
            xx = round((x/(1024)),0);
            yy = round((y/(1024)),0);
        }

        String a = String.valueOf(xx);
        String b = String.valueOf(yy);
        String c = a + measureStr+ " of " + b + measureStr;
        double z = 0;
        if(y > 0.0) {

            z = (double) x*100 / (double) y;
            z = round(z,2);
        }
        String d = String.valueOf(z);


        mProgressTextView.setText("Downloading.. "+ d  +"% "+c  );
        mProgressBar.setProgress((int)z);

        //when download is finished
        if(x > 0 && x == y){

            isFinishedDownload = true;
            isDownloadInProgress = false;
            mDownloadButton.setText("Close");
            mDownloadButton.setTextColor(Color.YELLOW);
//            try{
//                Thread.sleep(1200);
//            }catch (Exception e){
//
//            }
//            dismiss();
        }
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }



}
