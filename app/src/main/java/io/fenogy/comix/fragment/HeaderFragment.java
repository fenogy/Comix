package io.fenogy.comix.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import io.fenogy.comix.MainActivity;
import io.fenogy.comix.managers.LocalCoverHandler;
import io.fenogy.comix.managers.Utils;
import io.fenogy.comix.model.Comic;
import io.fenogy.comix.model.Storage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Random;

import io.fenogy.comix.MainActivity_old;
import io.fenogy.comix.R;


public class HeaderFragment extends Fragment
        implements View.OnLayoutChangeListener, Target {

    private ImageView mIconImageView;
    private ImageView mCoverImageView;
    private Picasso mPicasso;
    private Drawable mDrawable;
    private boolean mIsRunning = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_header, container, false);

        mPicasso = ((MainActivity) getActivity()).getPicasso();
        mIconImageView = (ImageView) view.findViewById(R.id.navbar_icon);
        mCoverImageView = (ImageView) view.findViewById(R.id.navbar_cover);
        if (savedInstanceState == null)
            mCoverImageView.addOnLayoutChangeListener(this);
        else
            if (mDrawable != null) showDrawable(mDrawable);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v.getWidth() > 0 && v.getHeight() > 0 && !mIsRunning) {
            createBitmap();
        }
    }

    private void createBitmap() {
        if (!Utils.isJellyBeanMR1orLater())
            return;

        mIsRunning = true;

        ArrayList<Comic> comics = Storage.getStorage(getActivity()).listComics();
        if (comics.size() > 0) {
            Comic c = comics.get(new Random().nextInt(comics.size()));
            mPicasso.load(LocalCoverHandler.getComicCoverUri(c)).into(this);
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        HalftonerTask task = new HalftonerTask(bitmap);
        task.execute();
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {}

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {}

    private void showDrawable(Drawable drawable) {
        mCoverImageView.setImageDrawable(drawable);

        mIconImageView.animate().alpha(0).setDuration(500).setListener(null);
        mCoverImageView.animate().alpha(1).setDuration(500).setListener(null);
    }

    private class HalftonerTask extends AsyncTask<Void, Void, Bitmap> {
        private Bitmap mBitmap;

        public HalftonerTask(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            double bw = mBitmap.getWidth();
            double bh = mBitmap.getHeight();
            double vw = mCoverImageView.getWidth();
            double vh = mCoverImageView.getHeight();

            int nbw, nbh, bx, by;
            if (bh/bw > vh/vw) {
                nbw = (int)vw;
                nbh = (int)(bh * (vw / bw));
                bx = 0;
                by = (int)((double)nbh / 2 - vh / 2);
            }
            else {
                nbw = (int)(bw * (vh / bh));
                nbh = (int)vh;
                bx = (int)((double)nbw / 2 - vw / 2);
                by = 0;
            }

            Bitmap scaled = Bitmap.createScaledBitmap(mBitmap, nbw, nbh, false);
            Bitmap mutable = scaled.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap bitmap = Bitmap.createBitmap(mutable, bx, by, (int)vw, (int)vh);

            double s = Math.PI/6;
            int a, r, g, b, l, t, f, p;
            int primary = getResources().getColor(R.color.primary);
            for (int y = 0; y < bitmap.getHeight(); y++) {
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    p = bitmap.getPixel(x, y);
                    a = Color.alpha(p);
                    r = Color.red(p);
                    g = Color.green(p);
                    b = Color.blue(p);
                    l = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                    t = (int)((Math.cos(s*(x+0.5))*Math.cos(s*(y+0.5))+1)*127);
                    f = (l > t) ? primary : Color.argb(a, 0, 0, 0);
                    bitmap.setPixel(x, y, f);
                }
            }

            RenderScript rs = RenderScript.create(getActivity());
            Allocation input = Allocation.createFromBitmap(rs, bitmap);
            Allocation output = Allocation.createTyped(rs, input.getType());
            ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(1);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            try {
                mDrawable = new BitmapDrawable(getActivity().getResources(), bitmap);
                showDrawable(mDrawable);
            }
            catch (Exception e) {}
        }
    }
}
