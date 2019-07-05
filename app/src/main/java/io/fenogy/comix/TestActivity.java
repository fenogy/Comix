package io.fenogy.comix;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import io.fenogy.comix.fragment.ReaderFragment;
import io.fenogy.comix.managers.Utils;
import io.fenogy.comix.model.Api;
import io.fenogy.comix.model.CatalogueItem;
import io.fenogy.comix.model.CatalogueStorage;
import io.fenogy.comix.model.ComicCatalogueJSONModel;
import io.fenogy.comix.model.ComicJSONModel;
import io.fenogy.comix.parsers.Parser;
import io.fenogy.comix.parsers.ParserFactory;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;
import static io.fenogy.comix.model.CatalogueStorage.getCatalogueStorage;

public class TestActivity extends AppCompatActivity implements Button.OnClickListener{

    private Button downloadButton;
    private Button refreshButton;
    private TextView updateTextView;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private boolean isRefreshed = false;
    private String mThumbsFilename;
    private Parser mParser;
    private ZipFile mZipFile;
    private ArrayList<ZipEntry> mEntries;
    private ArrayList<ZipEntry> mOtherEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        downloadButton = (Button) findViewById(R.id.button2_download);
        refreshButton = (Button) findViewById(R.id.button_redownload);
        updateTextView = (TextView) findViewById(R.id.textViewu);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        downloadButton.setOnClickListener(this);
        refreshButton.setOnClickListener(this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                onStartup();
            }
        }, 100);
    }

    public void onStartup(){

        String path = getExternalFilesDir(null)+File.separator+"media";
        File directory = new File(getExternalFilesDir(null)+File.separator+"media");
        if(!directory.exists()) {
            directory.mkdirs();
        }


        List<ComicJSONModel> currentList = getListFromCatalogueFile();
        ArrayList<CatalogueItem> mCatalogueItemList;

        CatalogueStorage mCatalogueStorage = getCatalogueStorage(this);

        boolean isCatalogueEmpty = mCatalogueStorage.itemsAvailable();

        if(!isCatalogueEmpty){

            //No catalogue available
            //cold start try downloaing the catalogue
            onUpdateRequest();
            currentList = getListFromCatalogueFile();
            mCatalogueStorage = getCatalogueStorage(this);
            isCatalogueEmpty = mCatalogueStorage.itemsAvailable();
            //if no data received, the catalogue might not have downloaded
            //it can be go ahead and download later
        }else{

            //Catalogue is available.go to main activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        //We will only download the inital catalogue for the first run.
        //No refreshing of catalogue is allowed here

//        if(!mCatalogueStorage.itemsAvailable() && (currentList != null) || isRefreshed){
//
//            if(isRefreshed){
//
//                currentList = getListFromCatalogueFile();
//                mCatalogueStorage = getCatalogueStorage(this);
//                mCatalogueStorage.clearStorage();
//                isRefreshed = false;
//            }
//
//
//            for(ComicJSONModel m :currentList ){
//
//                mCatalogueStorage.addToCatalogue(
//                        m.getName(),
//                        m.getUri(),
//                        m.getPages(),
//                        m.getAuthor(),
//                        m.getAuthor_id(),
//                        m.getDescription(),
//                        m.getSize(),
//                        m.getLanguage(),
//                        m.getCategory(),
//                        "");
//            }
//
//        }else{
//
//            mCatalogueItemList = mCatalogueStorage.listFullCatalogue();
//            int c = mCatalogueItemList.size();
//        }






    }


    public void onUpdateRequest(){

        //        ComicJSONModel m1 = new ComicJSONModel(0, "සොහොන් පිළ්ළුව.cbz","example",0,
//                "1MB", "test", "romance","sin",false);
//        ComicJSONModel m2 = new ComicJSONModel(1, "hero.cbz","example",0,
//                "1MB", "test", "romance","en",false);
//
//        List<ComicJSONModel> mList = new ArrayList<ComicJSONModel>();
//        mList.add(m1);
//        mList.add(m2);
//        ComicCatalogueJSONModel xx = new ComicCatalogueJSONModel(mList);
//        ComicCatalogueJSONModel xy;
//
//        Gson gson = new Gson();
//        Type type = new TypeToken<ComicCatalogueJSONModel>() {}.getType();
//        //String json = gson.toJson(xx, type);
//        String json = "{\"catalogue\":[{\"author\":\"example\",\"category\":\"romance\",\"description\":\"test\",\"language\":\"sin\",\"name\":\"සොහොන් පිළ්ළුව.cbz\",\"size\":\"1MB\",\"author_id\":0,\"id\":0,\"update\":false},{\"author\":\"example\",\"category\":\"romance\",\"description\":\"test\",\"language\":\"en\",\"name\":\"hero.cbz\",\"size\":\"1MB\",\"author_id\":0,\"id\":1,\"update\":false}]}";
//        System.out.println(json);
//        xy = gson.fromJson(json,type);




        //Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://www.google.lk/");
        //Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://fenogy.file.core.windows.net/").addConverterFactory(GsonConverterFactory.create());
        //Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://fenogy.file.core.windows.net/");
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://onedrive.live.com/");
        //Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://speed.hetzner.de/");

        Retrofit retrofit = builder.build();
        Toast.makeText(this,"clicked",Toast.LENGTH_SHORT).show();
        //https://doc-04-80-apps-viewer.googleusercontent.com/
        Api downloadService = retrofit.create(Api.class);
        //downloadService.downloadFileWithDynamicUrlSync()
        //Api downloadService = ServiceGenerator.create(Api.class);
        ///https://drive.google.com/open?id=0B82dAg9OVx6lZUZhVURtM1M2ZHM
        //Call<ResponseBody> call = downloadService.downloadFileWithDynamicUrlSync("images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");

        //Call<ResponseBody> call = downloadService.downloadFileWithDynamicUrlSync("new/1.json?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");

        //Call<ResponseBody> call = downloadService.downloadFileStream("new/sohon-pilluwa.cbr?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");
        //Call<ComicCatalogueJSONModel> call = downloadService.getCatalogue("new/catalogue/catalogue.json?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");
        //Call<ResponseBody> call = downloadService.downloadFileStream("100MB.bin");

        //Call<ResponseBody> call = downloadService.downloadFileStream("new/catalogue/catalogue.json?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");
        Call<ResponseBody> call = downloadService.downloadFileStream("download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21115&authkey=ANfETR3Sx4cZUoU");
//        call.enqueue(new Callback<ComicCatalogueJSONModel>() {
//            @Override
//            public void onResponse(Call<ComicCatalogueJSONModel> call, Response<ComicCatalogueJSONModel> response) {
//
//                //In this point we got our hero list
//                //thats damn easy right ;)
//                //List<Hero> heroList = response.body();
//                ComicCatalogueJSONModel mCatalogueJSONModel = response.body();
//                if(mCatalogueJSONModel != null) {
//
//                    List<ComicJSONModel> catalogue = mCatalogueJSONModel.getCatalogue();
//                    int x = 3;
//                    x = x+2;
////                    if (hjm.getId() == myID) {
////                        setDebugText(String.valueOf(hjm.getId()));
////                        //setText("flakaøhla yuqúh'tayd .,mñka mj;S'");
////                        mainLoopHandler.post(mainLoopRunnable);
////                    }
//                }
//                //String heroes = new String[heroList.size()];
//
//                //now we can do whatever we want with this list
//
//            }
//
//            @Override
//            public void onFailure(Call<ComicCatalogueJSONModel> call, Throwable t) {
//                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
//                //setText("wid¾:lhs'kej; W;aidy lrkak");
//                //setText("අන්තර්ජාල සබඳතාවය ගිලිහිගොස් ඇත");
//
//                //setDebugText("no response for male");
//
//            }
//        });
//        call.enqueue(new Callback<ResponseBody>() {
//
//            @Override
//            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
//
//
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "server contacted and has file");
//
//                    new AsyncTask<Void,Void,Void>(){
//                        @Override
//                        protected Void doInBackground(Void... voids) {
//
//                            boolean writtenToDisk;
//                            //boolean writtenToDisk = writeResponseBodyToDisk(response.body());
//                            //publishProgress();
//                            //Toast.makeText(getParent(),"downloaded",Toast.LENGTH_SHORT).show();
//                            try {
//                                // todo change the file location/name according to your needs
//                                File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "catalogue.json");
//
//                                InputStream inputStream = null;
//                                OutputStream outputStream = null;
//
//
//                                try {
//                                    byte[] fileReader = new byte[4096];
//
//                                    long fileSize = response.body().contentLength();
//                                    long fileSizeDownloaded = 0;
//
//                                    inputStream = response.body().byteStream();
//                                    outputStream = new FileOutputStream(futureStudioIconFile);
//
//                                    while (true) {
//                                        int read = inputStream.read(fileReader);
//
//                                        if (read == -1) {
//                                            break;
//                                        }
//
//                                        outputStream.write(fileReader, 0, read);
//
//                                        fileSizeDownloaded += read;
//
//                                        Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
//                                        x= fileSizeDownloaded;
//                                        y= fileSize;
//                                        publishProgress();
//                                        //updateProgress(fileSizeDownloaded,fileSizeDownloaded);
//                                    }
//
//                                    outputStream.flush();
//
//                                    writtenToDisk= true;
//                                } catch (IOException e) {
//                                    writtenToDisk= false;
//                                } finally {
//                                    if (inputStream != null) {
//                                        inputStream.close();
//                                    }
//
//                                    if (outputStream != null) {
//                                        outputStream.close();
//                                    }
//                                }
//                            } catch (IOException e) {
//                                writtenToDisk= false;
//                            }
//
//                            Log.d(TAG, "file download was a success? " + writtenToDisk);
//                            return null;
//                        }
//
//                        @Override
//                        protected void onProgressUpdate(Void... voids) {
//                            updateProgress();
//                            super.onProgressUpdate();
//                        }
//                    }.execute();
//
//                } else {
//                    Log.d(TAG, "server contact failed");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e(TAG, "error");
//                //Toast.makeText(,"failed",Toast.LENGTH_SHORT).show();
//            }
//        });

        //Call<ResponseBody> call2 = downloadService.downloadFileStream("new/catalogue/thumbs.zip?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");
        Call<ResponseBody> call2 = downloadService.downloadFileStream("download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21112&authkey=AHGNnvdba8raGqc");
        call2.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {


                if (response.isSuccessful()) {
                    ////Log.d(TAG, "server contacted and has file");

                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {

                            boolean writtenToDisk;
                            //boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                            //publishProgress();
                            //Toast.makeText(getParent(),"downloaded",Toast.LENGTH_SHORT).show();
                            try {
                                // todo change the file location/name according to your needs
                                File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "thumbs.zip");

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
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.e(TAG, "error");
                //Toast.makeText(this,"failed",Toast.LENGTH_SHORT).show();
                updateTextView.setText("Failed to download the catalogue..check data connectivity..");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 1000ms
                        startMain();

                    }
                }, 3000);
            }
        });

    }

    public void startMain(){

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    public void onClick(View view) {

        String path = getExternalFilesDir(null)+File.separator+"media";
        File directory = new File(getExternalFilesDir(null)+File.separator+"media");
        if(!directory.exists()) {
            directory.mkdirs();
        }

        if(view.getId() == R.id.button_redownload ){

            onClick2(view);

            isRefreshed = true;

        }else{

            List<ComicJSONModel> currentList = getListFromCatalogueFile();
            ArrayList<CatalogueItem> mCatalogueItemList;

            CatalogueStorage mCatalogueStorage = getCatalogueStorage(this);

            boolean test = mCatalogueStorage.itemsAvailable();

            if(!test){

                onClick2(view);
                currentList = getListFromCatalogueFile();
                mCatalogueStorage = getCatalogueStorage(this);
                test = mCatalogueStorage.itemsAvailable();
            }

            if(!mCatalogueStorage.itemsAvailable() && (currentList != null) || isRefreshed){

                if(isRefreshed){

                    currentList = getListFromCatalogueFile();
                    mCatalogueStorage = getCatalogueStorage(this);
                    mCatalogueStorage.clearStorage();
                    isRefreshed = false;
                }


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

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }




    }
    //@Override
    public void onClick2(View view){


//        ComicJSONModel m1 = new ComicJSONModel(0, "සොහොන් පිළ්ළුව.cbz","example",0,
//                "1MB", "test", "romance","sin",false);
//        ComicJSONModel m2 = new ComicJSONModel(1, "hero.cbz","example",0,
//                "1MB", "test", "romance","en",false);
//
//        List<ComicJSONModel> mList = new ArrayList<ComicJSONModel>();
//        mList.add(m1);
//        mList.add(m2);
//        ComicCatalogueJSONModel xx = new ComicCatalogueJSONModel(mList);
//        ComicCatalogueJSONModel xy;
//
//        Gson gson = new Gson();
//        Type type = new TypeToken<ComicCatalogueJSONModel>() {}.getType();
//        //String json = gson.toJson(xx, type);
//        String json = "{\"catalogue\":[{\"author\":\"example\",\"category\":\"romance\",\"description\":\"test\",\"language\":\"sin\",\"name\":\"සොහොන් පිළ්ළුව.cbz\",\"size\":\"1MB\",\"author_id\":0,\"id\":0,\"update\":false},{\"author\":\"example\",\"category\":\"romance\",\"description\":\"test\",\"language\":\"en\",\"name\":\"hero.cbz\",\"size\":\"1MB\",\"author_id\":0,\"id\":1,\"update\":false}]}";
//        System.out.println(json);
//        xy = gson.fromJson(json,type);




        //Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://www.google.lk/");
        //Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://fenogy.file.core.windows.net/").addConverterFactory(GsonConverterFactory.create());
        //Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://fenogy.file.core.windows.net/");
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://onedrive.live.com/");
        //Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://speed.hetzner.de/");

        Retrofit retrofit = builder.build();
        Toast.makeText(this,"clicked",Toast.LENGTH_SHORT).show();
        //https://doc-04-80-apps-viewer.googleusercontent.com/
        Api downloadService = retrofit.create(Api.class);
        //downloadService.downloadFileWithDynamicUrlSync()
        //Api downloadService = ServiceGenerator.create(Api.class);
        ///https://drive.google.com/open?id=0B82dAg9OVx6lZUZhVURtM1M2ZHM
        //Call<ResponseBody> call = downloadService.downloadFileWithDynamicUrlSync("images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");

        //Call<ResponseBody> call = downloadService.downloadFileWithDynamicUrlSync("new/1.json?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");

        //Call<ResponseBody> call = downloadService.downloadFileStream("new/sohon-pilluwa.cbr?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");
        //Call<ComicCatalogueJSONModel> call = downloadService.getCatalogue("new/catalogue/catalogue.json?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");
        //Call<ResponseBody> call = downloadService.downloadFileStream("100MB.bin");

        //Call<ResponseBody> call = downloadService.downloadFileStream("new/catalogue/catalogue.json?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");
        Call<ResponseBody> call = downloadService.downloadFileStream("download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21115&authkey=ANfETR3Sx4cZUoU");
//        call.enqueue(new Callback<ComicCatalogueJSONModel>() {
//            @Override
//            public void onResponse(Call<ComicCatalogueJSONModel> call, Response<ComicCatalogueJSONModel> response) {
//
//                //In this point we got our hero list
//                //thats damn easy right ;)
//                //List<Hero> heroList = response.body();
//                ComicCatalogueJSONModel mCatalogueJSONModel = response.body();
//                if(mCatalogueJSONModel != null) {
//
//                    List<ComicJSONModel> catalogue = mCatalogueJSONModel.getCatalogue();
//                    int x = 3;
//                    x = x+2;
////                    if (hjm.getId() == myID) {
////                        setDebugText(String.valueOf(hjm.getId()));
////                        //setText("flakaøhla yuqúh'tayd .,mñka mj;S'");
////                        mainLoopHandler.post(mainLoopRunnable);
////                    }
//                }
//                //String heroes = new String[heroList.size()];
//
//                //now we can do whatever we want with this list
//
//            }
//
//            @Override
//            public void onFailure(Call<ComicCatalogueJSONModel> call, Throwable t) {
//                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
//                //setText("wid¾:lhs'kej; W;aidy lrkak");
//                //setText("අන්තර්ජාල සබඳතාවය ගිලිහිගොස් ඇත");
//
//                //setDebugText("no response for male");
//
//            }
//        });
//        call.enqueue(new Callback<ResponseBody>() {
//
//            @Override
//            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
//
//
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "server contacted and has file");
//
//                    new AsyncTask<Void,Void,Void>(){
//                        @Override
//                        protected Void doInBackground(Void... voids) {
//
//                            boolean writtenToDisk;
//                            //boolean writtenToDisk = writeResponseBodyToDisk(response.body());
//                            //publishProgress();
//                            //Toast.makeText(getParent(),"downloaded",Toast.LENGTH_SHORT).show();
//                            try {
//                                // todo change the file location/name according to your needs
//                                File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "catalogue.json");
//
//                                InputStream inputStream = null;
//                                OutputStream outputStream = null;
//
//
//                                try {
//                                    byte[] fileReader = new byte[4096];
//
//                                    long fileSize = response.body().contentLength();
//                                    long fileSizeDownloaded = 0;
//
//                                    inputStream = response.body().byteStream();
//                                    outputStream = new FileOutputStream(futureStudioIconFile);
//
//                                    while (true) {
//                                        int read = inputStream.read(fileReader);
//
//                                        if (read == -1) {
//                                            break;
//                                        }
//
//                                        outputStream.write(fileReader, 0, read);
//
//                                        fileSizeDownloaded += read;
//
//                                        Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
//                                        x= fileSizeDownloaded;
//                                        y= fileSize;
//                                        publishProgress();
//                                        //updateProgress(fileSizeDownloaded,fileSizeDownloaded);
//                                    }
//
//                                    outputStream.flush();
//
//                                    writtenToDisk= true;
//                                } catch (IOException e) {
//                                    writtenToDisk= false;
//                                } finally {
//                                    if (inputStream != null) {
//                                        inputStream.close();
//                                    }
//
//                                    if (outputStream != null) {
//                                        outputStream.close();
//                                    }
//                                }
//                            } catch (IOException e) {
//                                writtenToDisk= false;
//                            }
//
//                            Log.d(TAG, "file download was a success? " + writtenToDisk);
//                            return null;
//                        }
//
//                        @Override
//                        protected void onProgressUpdate(Void... voids) {
//                            updateProgress();
//                            super.onProgressUpdate();
//                        }
//                    }.execute();
//
//                } else {
//                    Log.d(TAG, "server contact failed");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e(TAG, "error");
//                //Toast.makeText(,"failed",Toast.LENGTH_SHORT).show();
//            }
//        });

        //Call<ResponseBody> call2 = downloadService.downloadFileStream("new/catalogue/thumbs.zip?sv=2018-03-28&ss=bfqt&srt=sco&sp=rla&se=2023-05-03T03:34:19Z&st=2018-12-12T19:34:19Z&spr=https,http&sig=7VcoRkFKkPGRLyQWYIjZ5vvqr%2BwPKTH2Z3B1yimPo%2BQ%3D");
        Call<ResponseBody> call2 = downloadService.downloadFileStream("download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21112&authkey=AHGNnvdba8raGqc");
        call2.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {


                if (response.isSuccessful()) {
                    //Log.d(TAG, "server contacted and has file");

                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {

                            boolean writtenToDisk;
                            //boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                            //publishProgress();
                            //Toast.makeText(getParent(),"downloaded",Toast.LENGTH_SHORT).show();
                            try {
                                // todo change the file location/name according to your needs
                                File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "thumbs.zip");

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
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.e(TAG, "error");
                //Toast.makeText(,"failed",Toast.LENGTH_SHORT).show();
            }
        });
        //mDirectorySelectDialog.show();
    }


    public long x,y;
    public void updateProgress(){

        String a = String.valueOf(x);
        String b = String.valueOf(y);
        String c = a + " of " + b;
        double z = 0;
        if(y > 0.0) {

            z = (double) x*100 / (double) y;
            z = round(z,1);
        }
        String d = String.valueOf(z);

        updateTextView.setText("Downloading catelogue..\r\n"+c + "  " + d + "%");
        progressBar.setProgress((int)z);
    }

    public void updateProgress2(){

        String a = String.valueOf(x);
        String b = String.valueOf(y);
        String c = a + " of " + b;
        double z = 0;
        if(y > 0.0) {

            z = (double) x*100 / (double) y;
            z = round(z,1);
        }
        String d = String.valueOf(z);

        updateTextView.setText("Downloading thumbnails..\r\n"+c + "  " + d + "%");
        progressBar.setProgress((int)z);
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
    private boolean writeResponseBodyToDisk(ResponseBody body,AsyncTask a) {
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "sohon-pilluwa2.cbr");

            InputStream inputStream = null;
            OutputStream outputStream = null;


            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
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
                    //publishProgress();
                    //updateProgress(fileSizeDownloaded,fileSizeDownloaded);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void createCatalogueJSONFile(){

        InputStream is;
        ZipInputStream zis;
                //Check downloaded zipfile
        File thumbsFile = new File((this.getExternalFilesDir(null)), "thumbs.zip" );
        //DefineOutput path
        String path = getExternalFilesDir(null) + File.separator + "catalogue.json";
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

                    // Need to create directories if not exists, or
                    // it will generate an Exception...
//                    if (ze.isDirectory()) {
//                        File fmd = new File(path);
//                        fmd.mkdirs();
//                        continue;
//                    }

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
//        //Check downloaded zipfile
//        File thumbsFile = new File((this.getExternalFilesDir(null)), "thumbs.zip" );
//        //DefineOutput path
//        String path = getExternalFilesDir(null) + File.separator + "catalogue1.json";
//        OutputStream outputStream = null;
//
//        try{
//
//            mZipFile = new ZipFile(thumbsFile.getAbsolutePath());
//            mOtherEntries = new ArrayList<ZipEntry>();
//
//            Enumeration<? extends ZipEntry> e = mZipFile.entries();
//            while (e.hasMoreElements()) {
//                ZipEntry ze = e.nextElement();
//                if(ze.getName().contains(".json")){
//                    mOtherEntries.add(ze);
//                }
//            }
//
//            for (ZipEntry entry:mOtherEntries) {
//
//                if(entry.getName().equals("catalogue.json")){
//
//                    //create new catalogue file in path
//                    File newCatalogue = new File(path);
//                    outputStream = new FileOutputStream(newCatalogue);
//                    byte[] fileReader = new byte[1024];
//
//                    while (true) {
//                        //read from zip file
//                        int read = mZipFile.getInputStream(entry).read(fileReader);
//
//                        if (read == -1) {
//                            break;
//                        }
//                        //write to catalogue file
//                        outputStream.write(fileReader, 0, read);
//
//                    }
//
//                    outputStream.flush();
//                    mZipFile.getInputStream(entry).close();
//
//                }
//            }
//
//
//
//        }catch(IOException e){
//
//        }


    }
    public List<ComicJSONModel> getListFromCatalogueFile(){

        ComicCatalogueJSONModel cm;
        //StringBuilder json = new StringBuilder();

        try {

            String path = getExternalFilesDir(null) + File.separator + "catalogue.json";
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
}
