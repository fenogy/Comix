package io.fenogy.comix.view;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
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


public class AboutDialog extends AppCompatDialog
        implements View.OnClickListener {


    private Button mCancelButton;
    private Context mContext;





    public AboutDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_about_discliamer);

        mCancelButton = (Button) findViewById(R.id.button_ok_dad);

        mContext = context;

        mCancelButton.setOnClickListener(this);


    }

    public AboutDialog(Context context, CatalogueItem catalogueItem) {
        super(context);
        setContentView(R.layout.dialog_about_discliamer);

        mCancelButton = (Button) findViewById(R.id.button_cancel);



        mCancelButton.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
      if(v == mCancelButton){

            dismiss();
        }

    }



}
