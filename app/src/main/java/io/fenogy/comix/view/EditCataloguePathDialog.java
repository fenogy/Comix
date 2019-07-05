package io.fenogy.comix.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.fenogy.comix.Constants;
import io.fenogy.comix.R;
import io.fenogy.comix.model.CatalogueItem;


public class EditCataloguePathDialog extends AppCompatDialog
        implements View.OnClickListener {

    private Button mDefaultButton;
    private Button mCancelButton;
    private Button mUpdateButton;
    private EditText mURL;
    private Context mContext;



    public EditCataloguePathDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_edit_catalogue);

        mCancelButton = (Button) findViewById(R.id.button_cancel_ecd);
        mDefaultButton = (Button) findViewById(R.id.button_default_ecd);
        mUpdateButton = (Button) findViewById(R.id.button_update_ecd);
        mURL = (EditText) findViewById(R.id.edit_text_ecd);
        mContext = context;

        mCancelButton.setOnClickListener(this);
        //mDefaultButton.setOnClickListener(this);
        //mUpdateButton.setOnClickListener(this);


    }

    public EditCataloguePathDialog(Context context, String currentURL) {
        super(context);
        setContentView(R.layout.dialog_edit_catalogue);

        mCancelButton = (Button) findViewById(R.id.button_cancel_ecd);
        mDefaultButton = (Button) findViewById(R.id.button_default_ecd);
        mUpdateButton = (Button) findViewById(R.id.button_update_ecd);
        mURL = (EditText) findViewById(R.id.edit_text_ecd);
        mContext = context;

        mCancelButton.setOnClickListener(this);
        mDefaultButton.setOnClickListener(this);
        mUpdateButton.setOnClickListener(this);
        mURL.setText(currentURL);

    }


    @Override
    public void onClick(View v) {
      if(v == mCancelButton){

            dismiss();
        }
        if(v == mDefaultButton){

            mURL.setText(Constants.SETTINGS_CATALOGUE_ONEDRIVE_DEFAULT_SUB_URL);
            //dismiss();
        }
        if(v == mUpdateButton){

            setCatalogueURL(mURL.getText().toString());
            Toast.makeText(mContext,"Try Updating the catalogue..",Toast.LENGTH_SHORT).show();
            dismiss();
        }

    }

    public String getCatalogueURL(){

        SharedPreferences preferences = mContext.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(Constants.SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL,null);
    }

    public void setCatalogueURL(String url){

        if(url !="" || url != null) {
            SharedPreferences preferences = mContext.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL, url);
            editor.apply();
        }
    }



}
