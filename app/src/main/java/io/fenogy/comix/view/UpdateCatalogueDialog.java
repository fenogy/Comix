package io.fenogy.comix.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.fenogy.comix.Constants;
import io.fenogy.comix.R;
import io.fenogy.comix.fragment.LibraryBrowserFragment;
import io.fenogy.comix.managers.ComicCatalogueHandler;
import io.fenogy.comix.model.Api;
import io.fenogy.comix.model.CatalogueItem;
import io.fenogy.comix.model.CatalogueStorage;
import io.fenogy.comix.model.ComicCatalogueJSONModel;
import io.fenogy.comix.model.ComicJSONModel;
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
import static io.fenogy.comix.model.CatalogueStorage.getCatalogueStorage;


public class UpdateCatalogueDialog extends AppCompatDialog
        implements View.OnClickListener {

    private Button mCancelButton;
    private TextView mProgressTextView;
    private ProgressBar mProgressBar;

    private int progressStatus = 0;
    private Context mContext;




    public UpdateCatalogueDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_catalogue_update);
        mCancelButton = (Button) findViewById(R.id.button_cancel_ucd);
        mProgressTextView = (TextView) findViewById(R.id.tv_progress_ucd);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_ucd);


        mContext = context;
        mCancelButton.setOnClickListener(this);

        mProgressTextView.setText("Update requested.. ");
        onStartup();

    }


    @Override
    public void onClick(View v) {

        if(v == mCancelButton){
            dismiss();
        }

    }


    public long x,y;
    public List<ComicJSONModel> getListFromCatalogueFile(){

        ComicCatalogueJSONModel cm;
        //StringBuilder json = new StringBuilder();

        try {

            String path = mContext.getExternalFilesDir(null) + File.separator + "catalogue.json";
            //BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-16"));
            //new FileInputStream(path, "UTF-8");
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                json = json + line + '\n' ;
//                //json.append('\n');
//            }
            //json = "{\"catalogue\":[{\"author\":\"example\",\"category\":\"romance\",\"description\":\"test\",\"language\":\"sin\",\"name\":\"සොහොන් පිළ්ළුව.cbz\",\"size\":\"1MB\",\"author_id\":0,\"id\":0,\"update\":false},{\"author\":\"example\",\"category\":\"romance\",\"description\":\"test\",\"language\":\"en\",\"name\":\"hero.cbz\",\"size\":\"1MB\",\"author_id\":0,\"id\":1,\"update\":false}]}";


//            //String json = gson.toJson(xx, type);
//            String json = "{\"catalogue\":[{\"author\":\"example\",\"category\":\"romance\",\"description\":\"test\",\"language\":\"sin\",\"name\":\"සොහොන් පිළ්ළුව.cbz\",\"size\":\"1MB\",\"author_id\":0,\"id\":0,\"update\":false},{\"author\":\"example\",\"category\":\"romance\",\"description\":\"test\",\"language\":\"en\",\"name\":\"hero.cbz\",\"size\":\"1MB\",\"author_id\":0,\"id\":1,\"update\":false}]}";
//            cm = gson.fromJson(json,type);
            Gson gson = new Gson();
            Type type = new TypeToken<ComicCatalogueJSONModel>() {}.getType();
            //cm = gson.fromJson(json,type);
            cm = gson.fromJson(bufferedReader, type);
            bufferedReader.close();
            return cm.getCatalogue();

        }catch(IOException e){

            return null;

        }



    }
    public void onStartup(){

        String path = mContext.getExternalFilesDir(null)+File.separator+"media";
        File directory = new File(mContext.getExternalFilesDir(null)+File.separator+"media");
        if(!directory.exists()) {
            directory.mkdirs();
        }


        List<ComicJSONModel> currentList = getListFromCatalogueFile();
        ArrayList<CatalogueItem> mCatalogueItemList;

        CatalogueStorage mCatalogueStorage = getCatalogueStorage(mContext);

        onUpdateRequest();
        currentList = getListFromCatalogueFile();
        mCatalogueStorage = getCatalogueStorage(mContext);


        if(currentList != null){

            mCatalogueStorage = getCatalogueStorage(mContext);
            mCatalogueStorage.clearStorage();

            for(ComicJSONModel m :currentList ){

                mCatalogueStorage.addToCatalogue(
                        m.getName(),
                        m.getUri(),
                        m.getPages(),
                        m.getAuthor(),
                        m.getAuthor_id(),
                        m.getDescription(),
                        m.getSize(),
                        m.getLanguage(),
                        m.getCategory(),
                        "");
            }

        }else{

            mCatalogueItemList = mCatalogueStorage.listFullCatalogue();
            int c = mCatalogueItemList.size();
        }

        //loadCatalogue();
        //mCatalogueAdapter.notifyDataSetChanged();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);

    }

    public void generateCatalogueAfterDownload(){

        List<ComicJSONModel> currentList = getListFromCatalogueFile();
        ArrayList<CatalogueItem> mCatalogueItemList;

        CatalogueStorage mCatalogueStorage = getCatalogueStorage(mContext);

        currentList = getListFromCatalogueFile();


        if(currentList != null){

            mCatalogueStorage = getCatalogueStorage(mContext);
            mCatalogueStorage.clearStorage();

            for(ComicJSONModel m :currentList ){

                mCatalogueStorage.addToCatalogue(
                        m.getName(),
                        m.getUri(),
                        m.getPages(),
                        m.getAuthor(),
                        m.getAuthor_id(),
                        m.getDescription(),
                        m.getSize(),
                        m.getLanguage(),
                        m.getCategory(),
                        "");
            }

        }else{

            mCatalogueItemList = mCatalogueStorage.listFullCatalogue();
            int c = mCatalogueItemList.size();
        }
    }
    private void createCatalogueJSONFile(){

        InputStream is;
        ZipInputStream zis;
        //Check downloaded zipfile
        File thumbsFile = new File((mContext.getExternalFilesDir(null)), "thumbs.zip" );
        //DefineOutput path
        String path = mContext.getExternalFilesDir(null) + File.separator + "catalogue.json";
        try
        {
            String filename;
            is = new FileInputStream(thumbsFile);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {

                filename = ze.getName();

                if (filename.equals("catalogue.json")) {

                    FileOutputStream fout = new FileOutputStream(path);

                    while ((count = zis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }

                    fout.close();
                    zis.closeEntry();
                }
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();

        }

        setProgressStatus("Successful..");
        try{
            Thread.sleep(1200);
        }catch (Exception e){

        }
        dismiss();


    }
    public void onUpdateRequest(){


        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://onedrive.live.com/");
        Retrofit retrofit = builder.build();

        Api downloadService = retrofit.create(Api.class);
        //Call<ResponseBody> call = downloadService.downloadFileStream("download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21115&authkey=ANfETR3Sx4cZUoU");
        //Call<ResponseBody> call2 = downloadService.downloadFileStream("download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21112&authkey=AHGNnvdba8raGqc");

        Call<ResponseBody> call2 = downloadService.downloadFileStream(getCatalogueURL());


        call2.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {


                if (response.isSuccessful()) {
                    //Log.d(TAG, "server contacted and has file");
                    setProgressStatus("Receiving...");
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {

                            boolean writtenToDisk;

                            try {
                                // todo change the file location/name according to your needs
                                File futureStudioIconFile = new File(mContext.getExternalFilesDir(null) + File.separator + "thumbs.zip");

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
                                        //updateProgress(fileSizeDownloaded,fileSizeDownloaded);
                                    }

                                    outputStream.flush();

                                    writtenToDisk= true;
                                    createCatalogueJSONFile();
                                    //generateCatalogueAfterDownload();

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
                    //Internet is available.. try from next location
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.e(TAG, "error");
                setProgressStatus("Catalogue update failed.. Check data connectivity and retry from menu..");
                //Toast.makeText(,"failed",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void setProgressStatus(String s){

        mProgressTextView.setText(s);
    }



    public void updateProgress2(){

        double xx,yy;
        String measureStr = "KB";
        //if larger than 1MB
        if(y > 1048576){
            //show in MB
            xx = round((x/(1024*1024)),1);
            yy = round((y/(1024*1024)),1);
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
            z = round(z,1);
        }
        String d = String.valueOf(z);

        mProgressTextView.setText("Updating.. "+ d  +"% "+c  );
        mProgressBar.setProgress((int)z);
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public String getCatalogueURL(){

        SharedPreferences preferences = mContext.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(Constants.SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL,null);
    }


}
