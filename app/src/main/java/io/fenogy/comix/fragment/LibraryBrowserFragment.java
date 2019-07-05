package io.fenogy.comix.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import io.fenogy.comix.Constants;
import io.fenogy.comix.MainActivity;
import io.fenogy.comix.R;
import io.fenogy.comix.ReaderActivity;
import io.fenogy.comix.managers.ComicCatalogueHandler;
import io.fenogy.comix.managers.RecentCoverHandler;
import io.fenogy.comix.managers.DirectoryListingManager;
import io.fenogy.comix.managers.LocalCoverHandler;
import io.fenogy.comix.managers.Scanner;
import io.fenogy.comix.managers.Utils;
import io.fenogy.comix.model.Api;
import io.fenogy.comix.model.CatalogueItem;
import io.fenogy.comix.model.CatalogueStorage;
import io.fenogy.comix.model.Comic;
import io.fenogy.comix.model.ComicCatalogueJSONModel;
import io.fenogy.comix.model.ComicJSONModel;
import io.fenogy.comix.model.Storage;
import io.fenogy.comix.parsers.Parser;
import io.fenogy.comix.parsers.ParserFactory;
import io.fenogy.comix.view.AboutDialog;
import io.fenogy.comix.view.ComicDownloadDialog;
import io.fenogy.comix.view.EditCataloguePathDialog;
import io.fenogy.comix.view.UpdateCatalogueDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static android.content.ContentValues.TAG;
import static io.fenogy.comix.model.CatalogueStorage.getCatalogueStorage;

public class LibraryBrowserFragment extends Fragment
        implements SearchView.OnQueryTextListener {
    public static final String PARAM_PATH = "browserCurrentPath";
    public static String MEDIA_LIBRARY_PATH;
    final int ITEM_VIEW_TYPE_COMIC = 1;
    final int ITEM_VIEW_TYPE_HEADER_RECENT = 2;
    final int ITEM_VIEW_TYPE_HEADER_ALL = 3;

    final int NUM_HEADERS = 2;

    private List<Comic> mComics = new ArrayList<>();
    private List<Comic> mAllItems = new ArrayList<>();
    private List<Comic> mRecentItems = new ArrayList<>();

    private List<CatalogueItem> mCatalogueItems = new ArrayList<>();

    private String mPath;
    private String mFilterSearch = "";
    private Picasso mPicasso,mPicasso2,mPicasso3;
    private int mFilterRead = R.id.menu_browser_filter_all;

    private String mThumbsFilename;
    private Parser mParser;
    private ComicCatalogueHandler mCatalogueHandler;
    private RecentCoverHandler mRecentCoverHandler;
    private int mCurrentThumb;
    private ComicDownloadDialog mComicDownloadDialog;
    private UpdateCatalogueDialog mUpdateCatalogueDialog;
    private View mEmptyRecentsView;
    private View mEmptyDownloadsView;
    private View mEmptyCatalogueView;
    private RecyclerView mComicListView;
    private RecyclerView mDownloadedComicListView;
    private RecyclerView mRecentComicListView;
    private RecyclerView mComicCatalogueListView;
    private CatalogueGridAdapter mCatalogueAdapter;
    public static boolean RequireLibraryUpdate;
    public static boolean RequireReadAfterDismiss;
    private DirectoryListingManager mComicsListManager;
    private boolean mIsRefreshPlanned = false;
    private CatalogueItem mLastOpenedCatalogueItem;
    public String mCataloguePath ="";

    private Handler mUpdateHandler = new UpdateHandler(this);

    private Typeface mSinhalaRegularFont,mSinhalaMediumAbhayFont;

    public static LibraryBrowserFragment create(String path) {
        LibraryBrowserFragment fragment = new LibraryBrowserFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    public LibraryBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mPath = getArguments().getString(PARAM_PATH);

        MEDIA_LIBRARY_PATH = getActivity().getExternalFilesDir(null) + File.separator +"media";
        mPath = MEDIA_LIBRARY_PATH;

        SharedPreferences preferences = this.getActivity().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.SETTINGS_LIBRARY_DIR, MEDIA_LIBRARY_PATH);
        //editor.putString(Constants.SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL, "download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21112&authkey=AHGNnvdba8raGqc");

        //editor.apply();

        ReaderActivity.LANDSCAPE_MODE=isReaderOrientaionLandscape();

        //If no previous catalogue path available get the default hardcode one
        mCataloguePath = preferences.getString(Constants.SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL,null);
        if(mCataloguePath == "" || mCataloguePath == null){

            editor = preferences.edit();
            editor.putString(Constants.SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL, "download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21112&authkey=AHGNnvdba8raGqc");
            editor.apply();
            mCataloguePath = preferences.getString(Constants.SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL,null);
        }else{
            editor.apply();
        }



        loadCatalogue();
        getComics();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_librarybrowser_11, container, false);


        final ImageButton menuButton = (ImageButton) view.findViewById(R.id.imageBtnMenu);

        final int numColumns = calculateNumColumns();
        int spacing = (int) getResources().getDimension(R.dimen.grid_margin);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        layoutManager.setSpanSizeLookup(createSpanSizeLookup());

//        mComicListView = (RecyclerView) view.findViewById(R.id.library_grid);
//        mComicListView.setHasFixedSize(true);
//        mComicListView.setLayoutManager(layoutManager);
//        mComicListView.setAdapter(new ComicGridAdapter());
//        mComicListView.addItemDecoration(new GridSpacingItemDecoration(numColumns, spacing));

        getActivity().setTitle(new File(getArguments().getString(PARAM_PATH)).getName());
        mPicasso = ((MainActivity) getActivity()).getPicasso();

        GridLayoutManager layoutManager2 = new GridLayoutManager(getActivity(), 3,GridLayoutManager.HORIZONTAL,false);
        //layoutManager2.se
        loadCatalogue();
        //getComics();

        layoutManager2.setSpanSizeLookup(createSpanSizeLookup2());
        mCatalogueAdapter = new CatalogueGridAdapter();
        mComicCatalogueListView  = (RecyclerView) view.findViewById(R.id.library_new);
        mComicCatalogueListView.setHasFixedSize(true);
        mComicCatalogueListView.setLayoutManager(layoutManager2);

        mComicCatalogueListView.setAdapter(mCatalogueAdapter);
        mEmptyCatalogueView = view.findViewById(R.id.empty_catalogue_view);
//        mEmptyCatalogueView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                catalogueUpdateRequest();
//                showEmptyCatalogueMessage(mCatalogueItems.isEmpty());
//            }
//        });
        //for downloaded items
        GridLayoutManager layoutManagerDownloaded = new GridLayoutManager(getActivity(), 1,GridLayoutManager.HORIZONTAL,false);
        //layoutManager2.se

        layoutManagerDownloaded.setSpanSizeLookup(createSpanSizeLookup2());
        mDownloadedComicListView  = (RecyclerView) view.findViewById(R.id.library_downloaded);
        mDownloadedComicListView.setHasFixedSize(true);
        mDownloadedComicListView.setLayoutManager(layoutManagerDownloaded);
        mDownloadedComicListView.setAdapter(new DownloadedComicGridAdapter());
        mEmptyDownloadsView = view.findViewById(R.id.empty_downloaded_view);
        //registerForContextMenu(mDownloadedComicListView);

        //for recent items
        GridLayoutManager layoutManagerRecents = new GridLayoutManager(getActivity(), 1,GridLayoutManager.HORIZONTAL,false);
        //layoutManager2.se

        layoutManagerDownloaded.setSpanSizeLookup(createSpanSizeLookup2());
        mRecentComicListView  = (RecyclerView) view.findViewById(R.id.library_recent);
        mRecentComicListView.setHasFixedSize(true);
        mRecentComicListView.setLayoutManager(layoutManagerRecents);
        mRecentComicListView.setAdapter(new RecentComicGridAdapter());
        mEmptyRecentsView = view.findViewById(R.id.empty_recent_view);


        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });


        AssetManager am = getActivity().getApplicationContext().getAssets();
        mSinhalaMediumAbhayFont = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "AbhayaLibre-SemiBold.otf"));

        //////////
        Scanner.getInstance().forceScanLibrary();
        showEmptyDownloadsMessage(false);
        setLoading(true);

        //getComics();
        showEmptyDownloadsMessage(mAllItems.isEmpty());
        showEmptyRecentsMessage(mRecentItems.isEmpty());
        showEmptyCatalogueMessage(mCatalogueItems.isEmpty());
        ////////////////

        //mComicCatalogueListView.addItemDecoration(new GridSpacingItemDecoration(3, 8));



        return view;
    }

//    @Override
//    public void onResume() {
//        getComics();
//        super.onResume();
//    }

    @Override
    public void onResume() {
        super.onResume();
        //getComics();
        onStartup();
        showEmptyDownloadsMessage(mAllItems.isEmpty());
        showEmptyRecentsMessage(mRecentItems.isEmpty());
        showEmptyCatalogueMessage(mCatalogueItems.isEmpty());

        if(mCatalogueItems.isEmpty()){

            catalogueUpdateRequest();

        }

        Scanner.getInstance().addUpdateHandler(mUpdateHandler);
        Scanner.getInstance().forceScanLibrary();
        if (Scanner.getInstance().isRunning()) {
            setLoading(true);
        }
    }

    @Override
    public void onPause() {
        Scanner.getInstance().removeUpdateHandler(mUpdateHandler);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.browser, menu);

        //MenuItem searchItem = menu.findItem(R.id.search);
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void showPopupMenu(View v) {

        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                //LibraryBrowserFragment.
                switch (item.getItemId()) {
                    case R.id.menu_browser_filter_all:
                    case R.id.menu_browser_filter_read:
                    case R.id.menu_browser_filter_unread:
                    case R.id.menu_browser_filter_unfinished:
                    case R.id.menu_browser_filter_reading:
                        item.setChecked(true);
                        mFilterRead = item.getItemId();
                        filterContent();
                        return true;
                    case R.id.menu_browser_update_catalogue:
                        catalogueUpdateRequest();
                        return true;

                    case R.id.menuReaderOrientation:
//                        MenuItem landItem = popup.getMenu().findItem(R.id.menu_browser_landscape);
//                        MenuItem portItem = popup.getMenu().findItem(R.id.menu_browser_landscape);
//                        if (ReaderActivity.LANDSCAPE_MODE) {
//                            landItem.setChecked(true);
//                            portItem.setChecked(false);
//                        } else {
//                            landItem.setChecked(false);
//                            portItem.setChecked(true);
//                        }

                    case R.id.menu_browser_landscape:
                        //item.setChecked(ReaderActivity.LANDSCAPE_MODE);
                        //getActivity()
                                //.getSharedPreferences(Constants.SETTINGS_PAGE_ORIENTATION, 0).set

                        ReaderActivity.LANDSCAPE_MODE = true;
                        setReaderLandscape();
                        return true;
                    case R.id.menu_browser_portrait:
                        //item.setChecked(!ReaderActivity.LANDSCAPE_MODE);
                        ReaderActivity.LANDSCAPE_MODE = false;
                        setReaderPortrait();
                        return true;
                    case R.id.menu_browser_about:
                        new AboutDialog(getActivity()).show();
                        return true;
                    case R.id.menu_browser_update_catalogue_url:
                        new EditCataloguePathDialog(getActivity(),getCatalogueURL()).show();
                        return true;

                    default:return false;
                }

                //return super.onMenuItemClick(item);
            }
        });

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.browser, popup.getMenu());
        popup.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_browser_filter_all:
            case R.id.menu_browser_filter_read:
            case R.id.menu_browser_filter_unread:
            case R.id.menu_browser_filter_unfinished:
            case R.id.menu_browser_filter_reading:
                item.setChecked(true);
                mFilterRead = item.getItemId();
                filterContent();
                return true;
            case R.id.menu_browser_update_catalogue:
                catalogueUpdateRequest();
                return true;
            case R.id.menu_browser_landscape:
                item.setChecked(true);
                //ReaderActivity.LANDSCAPE_MODE = true;
                return true;
            case R.id.menu_browser_portrait:
                item.setChecked(true);
               //ReaderActivity.LANDSCAPE_MODE = false;
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public String getCatalogueURL(){

        SharedPreferences preferences = this.getActivity().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(Constants.SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL,null);
    }

    public void setCatalogueURL(String url){

        if(url !="" || url != null) {
            SharedPreferences preferences = this.getActivity().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL, url);
            editor.apply();
        }
    }
    public boolean isReaderOrientaionLandscape(){

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        String o = preferences.getString(Constants.SETTINGS_PAGE_ORIENTATION,Constants.LANDSCAPE);

        if(o.contains(Constants.LANDSCAPE)){
            return true;
        }

        return false;

    }
    public void setReaderLandscape(){

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(Constants.SETTINGS_PAGE_ORIENTATION, Constants.LANDSCAPE);
        editor.apply();

    }

    public void setReaderPortrait(){

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(Constants.SETTINGS_PAGE_ORIENTATION, Constants.PORTRAIT);
        editor.apply();

    }

    public void showAbout(){

        new AboutDialog(getActivity()).show();
    }
    public void catalogueUpdateRequest(){

        mUpdateCatalogueDialog = new UpdateCatalogueDialog(getActivity());

        mUpdateCatalogueDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                onStartup();
                loadCatalogue();
                showEmptyCatalogueMessage(mCatalogueItems.isEmpty());
                mCatalogueAdapter.notifyDataSetChanged();

            }
        });
        mUpdateCatalogueDialog.show();
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mFilterSearch = s;
        filterContent();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    public void openComic(Comic comic) {
        if (!comic.getFile().exists()) {
            Toast.makeText(
                    getActivity(),
                    R.string.warning_missing_file,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), ReaderActivity.class);
        intent.putExtra(ReaderFragment.PARAM_HANDLER, comic.getId());
        intent.putExtra(ReaderFragment.PARAM_MODE, ReaderFragment.Mode.MODE_LIBRARY);
        startActivity(intent);
    }

    private void showEmptyRecentsMessage(boolean show) {
        mEmptyRecentsView.setVisibility(show ? View.VISIBLE : View.GONE);
        //mRefreshLayout.setEnabled(!show);
    }

    private void showEmptyDownloadsMessage(boolean show) {
        mEmptyDownloadsView.setVisibility(show ? View.VISIBLE : View.GONE);
        //mRefreshLayout.setEnabled(!show);
    }

    private void showEmptyCatalogueMessage(boolean show) {
        mEmptyCatalogueView.setVisibility(show ? View.VISIBLE : View.GONE);
        //mRefreshLayout.setEnabled(!show);
    }

    public void emptyViewCheck(){

        showEmptyDownloadsMessage(mAllItems.isEmpty());
        showEmptyRecentsMessage(mRecentItems.isEmpty());
        showEmptyCatalogueMessage(mCatalogueItems.isEmpty());
    }
    private void getComics() {

//        //added recently
//        if (!Scanner.getInstance().isRunning()) {
//            setLoading(true);
//            Scanner.getInstance().scanLibrary();
//        }
        //Scanner.getInstance().forceScanLibrary();
        //showEmptyMessage(false);
        //setLoading(true);
//        try{
//            Thread.sleep(1000);
//
//        }catch (InterruptedException e){
//
//        }
//        ///added finished
        mComics = Storage.getStorage(getActivity()).listComics(mPath);
        findRecents();
        filterContent();
    }

    private void loadCatalogue(){

        CatalogueStorage mCatalogueStorage = getCatalogueStorage(getActivity());

        if(mCatalogueStorage.itemsAvailable()){

            mCatalogueItems = mCatalogueStorage.listFullCatalogue();
            int c = mCatalogueItems.size();
        }
    }

    private void findRecents() {
        mRecentItems.clear();

        for (Comic c : mComics) {
            if (c.updatedAt > 0) {
                mRecentItems.add(c);
            }
        }

        if (mRecentItems.size() > 0) {
            Collections.sort(mRecentItems, new Comparator<Comic>() {
                @Override
                public int compare(Comic lhs, Comic rhs) {
                    return lhs.updatedAt > rhs.updatedAt ? -1 : 1;
                }
            });
        }

        if (mRecentItems.size() > Constants.MAX_RECENT_COUNT) {
            mRecentItems
                    .subList(Constants.MAX_RECENT_COUNT, mRecentItems.size())
                    .clear();
        }
    }

    private void filterContent() {
        mAllItems.clear();

        for (Comic c : mComics) {
            if (mFilterSearch.length() > 0 && !c.getFile().getName().contains(mFilterSearch))
                continue;
            if (mFilterRead != R.id.menu_browser_filter_all) {
                if (mFilterRead == R.id.menu_browser_filter_read && c.getCurrentPage() != c.getTotalPages())
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_unread && c.getCurrentPage() != 0)
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_unfinished && c.getCurrentPage() == c.getTotalPages())
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_reading &&
                        (c.getCurrentPage() == 0 || c.getCurrentPage() == c.getTotalPages()))
                    continue;
            }
            mAllItems.add(c);
        }

        if (mComicListView != null) {
            mComicListView.getAdapter().notifyDataSetChanged();
            showEmptyDownloadsMessage(mAllItems.isEmpty());
            //showEmptyRecentsMessage(mRecentItems.isEmpty());
            showEmptyCatalogueMessage(mCatalogueItems.isEmpty());
        }
    }

    private Comic getComicAtPosition(int position) {
        Comic comic;
        if (hasRecent()) {
            if (position > 0 && position < mRecentItems.size() + 1)
                comic = mRecentItems.get(position - 1);
            else
                comic = mAllItems.get(position - mRecentItems.size() - NUM_HEADERS);
        }
        else {
            comic = mAllItems.get(position);
        }
        return comic;
    }

    private Comic getDownloadedComicAtPosition(int position) {
        Comic comic;

            comic = mAllItems.get(position);

        return comic;
    }
    private boolean deleteDownloadedComicAtPosition(int position) {

        boolean result = false;

        Comic comic =  mAllItems.get(position);
        String fileName = comic.getFile().getName();
        File file = new File(getLibraryDir() +File.separator+ fileName);

        try {
            if (file.delete()) {

                result = true;
                mAllItems.remove(position);
                mDownloadedComicListView.getAdapter().notifyDataSetChanged();
                findRecents();
                mRecentComicListView.getAdapter().notifyDataSetChanged();


            }
        }catch(Exception e){

        }
        return result;
    }

    private Comic getRecentComicAtPosition(int position) {
        Comic comic;

        comic = mRecentItems.get(position);

        return comic;
    }

    private int getItemViewTypeAtPosition(int position) {
        if (hasRecent()) {
            if (position == 0)
                return ITEM_VIEW_TYPE_HEADER_RECENT;
            else if (position == mRecentItems.size() + 1)
                return ITEM_VIEW_TYPE_HEADER_ALL;
        }
        return ITEM_VIEW_TYPE_COMIC;
    }

    private boolean hasRecent() {
        return mFilterSearch.length() == 0
                && mFilterRead == R.id.menu_browser_filter_all
                && mRecentItems.size() > 0;
    }

    private int calculateNumColumns() {
        int deviceWidth = Utils.getDeviceWidth(getActivity());
        int columnWidth = getActivity().getResources().getInteger(R.integer.grid_comic_column_width);

        return Math.round((float) deviceWidth / columnWidth);
    }

    private GridLayoutManager.SpanSizeLookup createSpanSizeLookup() {
        final int numColumns = calculateNumColumns();

        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (getItemViewTypeAtPosition(position) == ITEM_VIEW_TYPE_COMIC)
                    return 1;
                return numColumns;
            }
        };
    }

    private GridLayoutManager.SpanSizeLookup createSpanSizeLookup2() {
        final int numColumns = calculateNumColumns();

        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (getItemViewTypeAtPosition(position) == ITEM_VIEW_TYPE_COMIC)
                    return 1;
                //return numColumns;
                return 1;
            }
        };
    }

    private final class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpanCount;
        private int mSpacing;

        public GridSpacingItemDecoration(int spanCount, int spacing) {
            mSpanCount = spanCount;
            mSpacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            if (hasRecent()) {
                // those are headers
                if (position == 0 || position == mRecentItems.size() + 1)
                    return;

                if (position > 0 && position < mRecentItems.size() + 1) {
                    position -= 1;
                }
                else {
                    position -= (NUM_HEADERS + mRecentItems.size());
                }
            }

            int column = position % mSpanCount;

            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            outRect.right = (column + 1) * mSpacing / mSpanCount;

            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing;
        }
    }


    private final class ComicGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {
            if (hasRecent()) {
                return mAllItems.size() + mRecentItems.size() + NUM_HEADERS;
            }
            return mAllItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return getItemViewTypeAtPosition(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

            if (i == ITEM_VIEW_TYPE_HEADER_RECENT) {
                TextView view = (TextView) LayoutInflater.from(ctx)
                        .inflate(R.layout.header_library, viewGroup, false);
                view.setText(R.string.library_header_recent);

                int spacing = (int) getResources().getDimension(R.dimen.grid_margin);
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                lp.setMargins(0, spacing, 0, 0);

                return new HeaderViewHolder(view);
            }
            else if (i == ITEM_VIEW_TYPE_HEADER_ALL) {
                TextView view = (TextView) LayoutInflater.from(ctx)
                        .inflate(R.layout.header_library, viewGroup, false);
                view.setText(R.string.library_header_all);

                return new HeaderViewHolder(view);
            }

            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_comic_modern, viewGroup, false);
            return new ComicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder.getItemViewType() == ITEM_VIEW_TYPE_COMIC) {
                Comic comic = getComicAtPosition(i);
                ComicViewHolder holder = (ComicViewHolder) viewHolder;
                holder.setupComic(comic);
            }
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }

        public void setTitle(int titleRes) {
            ((TextView) itemView).setText(titleRes);
        }
    }

    private class ComicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mCoverView;
        private TextView mTitleTextView;
        private TextView mPagesTextView;

        public ComicViewHolder(View itemView) {
            super(itemView);
            mCoverView = (ImageView) itemView.findViewById(R.id.comicImageView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.comicTitleTextView);
            mPagesTextView = (TextView) itemView.findViewById(R.id.comicPagerTextView);

//            mTitleTextView.setTypeface(mSinhalaMediumAbhayFont);
//            mPagesTextView.setTypeface(mSinhalaMediumAbhayFont);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        public void setupComic(Comic comic) {

            //mTitleTextView.setText(comic.getFile().getName()); //My changes
            String fileName = comic.getFile().getName();
            int dotIndex = fileName.lastIndexOf('.');
            String fileNameNoExtension = fileName.substring(0, dotIndex);
            mTitleTextView.setText(fileNameNoExtension);

            //replace with percentage
            //mPagesTextView.setText(Integer.toString(comic.getCurrentPage()) + '/' + Integer.toString(comic.getTotalPages()));
            int percentage = 0;
            if(comic.getTotalPages() > 0){

                percentage = (comic.getCurrentPage() *100)/comic.getTotalPages();
            }
            mPagesTextView.setText(Integer.toString(percentage) + '%');

            mPicasso.load(LocalCoverHandler.getComicCoverUri(comic))
                    .into(mCoverView);
        }

        @Override
        public void onClick(View v) {
            int i = getAdapterPosition();
            Comic comic = getComicAtPosition(i);

            openComic(comic);
        }
    }

    private class DownloadedComicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        private ImageView mCoverView;
        private TextView mTitleTextView;
        private TextView mPagesTextView;

        public DownloadedComicViewHolder(View itemView) {
            super(itemView);
            mCoverView = (ImageView) itemView.findViewById(R.id.comicImageView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.comicTitleTextView);
            //mPagesTextView = (TextView) itemView.findViewById(R.id.comicPagerTextView);

            //mTitleTextView.setTypeface(mSinhalaMediumAbhayFont);
            //mPagesTextView.setTypeface(mSinhalaMediumAbhayFont);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setupComic(Comic comic) {

            //mTitleTextView.setText(comic.getFile().getName()); //My changes
            String fileName = comic.getFile().getName();
            int dotIndex = fileName.lastIndexOf('.');
            String fileNameNoExtension = fileName.substring(0, dotIndex);

            mTitleTextView.setText(fileNameNoExtension);

            //replace with percentage
            //mPagesTextView.setText(Integer.toString(comic.getCurrentPage()) + '/' + Integer.toString(comic.getTotalPages()));
            int percentage = 0;
            if(comic.getTotalPages() > 0){

                percentage = (comic.getCurrentPage() *100)/comic.getTotalPages();
            }
            //mPagesTextView.setText(Integer.toString(percentage) + '%');

            mPicasso.load(LocalCoverHandler.getComicCoverUri(comic))
                    .into(mCoverView);
        }

        @Override
        public void onClick(View v) {
            int i = getAdapterPosition();
            Comic comic = getDownloadedComicAtPosition(i);
            openComic(comic);
        }

        @Override
        public boolean onLongClick(View view) {

            //int i = Integer.parseInt((String)view.getTag());
            final int i = getAdapterPosition();
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(),R.style.MyDialogTheme);

            Comic comic = getDownloadedComicAtPosition(i);
            String fileName = comic.getFile().getName();
            int dotIndex = fileName.lastIndexOf('.');
            String fileNameNoExtension = fileName.substring(0, dotIndex);
            alert.setTitle("Want to delete " + fileNameNoExtension + " ?");
            //.setMessage("Want to delete? " + fileNameNoExtension);
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    deleteDownloadedComicAtPosition(i);
                    Scanner.getInstance().forceScanLibrary();
                    if (Scanner.getInstance().isRunning()) {
                        setLoading(true);
                    }
                    dialog.dismiss();
                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.show();
            //mAllItems.remove(getAdapterPosition());
            //mDownloadedComicListView.getAdapter().notifyDataSetChanged();
            //this.getAdapter().notifyDataSetChanged();

            return false;
        }
    }

    private final class DownloadedComicGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {

            return mAllItems.size();
        }

//        @Override
//        public int getItemViewType(int position) {
//            return getItemViewTypeAtPosition(position);
//        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_comic_modern, viewGroup, false);
            return new DownloadedComicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

            Comic comic = getDownloadedComicAtPosition(i);
            DownloadedComicViewHolder holder = (DownloadedComicViewHolder) viewHolder;
            holder.setupComic(comic);

        }
    }


    private class RecentComicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mCoverView;
        private TextView mTitleTextView;
        private TextView mPagesTextView;

        public RecentComicViewHolder(View itemView) {
            super(itemView);
            mCoverView = (ImageView) itemView.findViewById(R.id.recentComicImageView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.comicTitleTextView);
            mPagesTextView = (TextView) itemView.findViewById(R.id.comicPagerTextView);

            //mTitleTextView.setTypeface(mSinhalaMediumAbhayFont);
            //mPagesTextView.setTypeface(mSinhalaMediumAbhayFont);
//            mTitleTextView.setTypeface(mSinhalaMediumAbhayFont);
//            mPagesTextView.setTypeface(mSinhalaMediumAbhayFont);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        public void setupComic(Comic comic) {

            //mTitleTextView.setText(comic.getFile().getName()); //My changes
            String fileName = comic.getFile().getName();
            int dotIndex = fileName.lastIndexOf('.');
            String fileNameNoExtension = fileName.substring(0, dotIndex);
            mTitleTextView.setText(fileNameNoExtension);

            //replace with percentage
            //mPagesTextView.setText(Integer.toString(comic.getCurrentPage()) + '/' + Integer.toString(comic.getTotalPages()));
            int percentage = 0;
            if(comic.getTotalPages() > 0){

                percentage = (comic.getCurrentPage() *100)/comic.getTotalPages();
            }
            mPagesTextView.setText(Integer.toString(percentage) + '%');

//            mPicasso.load(LocalCoverHandler.getComicCoverUri(comic))
//                    .into(mCoverView);
            //Original implementation..change n test for RAM usage


            mParser = ParserFactory.create(comic.getFile());

            mCatalogueHandler = new ComicCatalogueHandler(mParser);
            mPicasso3 = new Picasso.Builder(getActivity())
                    .addRequestHandler(mCatalogueHandler)
                    .build();

            Uri uri = mCatalogueHandler.getThumbUri(comic.getCurrentPage()-1);

            try{

                mPicasso3.load(uri)
                        .into(mCoverView);
            }catch(NullPointerException e){

                mPicasso3.load(Uri.parse("android.resource://io.fenogy.comix/" + R.drawable.placeholder))
                        .into(mCoverView);
            }

//            mRecentCoverHandler = new RecentCoverHandler(getActivity());
//            mPicasso.load(mRecentCoverHandler.getComicCoverUri(comic,comic.getCurrentPage()))
//                    .into(mCoverView);


        }

        @Override
        public void onClick(View v) {
            int i = getAdapterPosition();
            Comic comic = getRecentComicAtPosition(i);
            openComic(comic);
        }
    }

    private final class RecentComicGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {

            return mRecentItems.size();
        }

//        @Override
//        public int getItemViewType(int position) {
//            return getItemViewTypeAtPosition(position);
//        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_comic_recent, viewGroup, false);
            return new RecentComicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

            Comic comic = getRecentComicAtPosition(i);
            RecentComicViewHolder holder = (RecentComicViewHolder) viewHolder;
            holder.setupComic(comic);

        }
    }
    private final class CatalogueGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {

            return mCatalogueItems.size();
        }

//        @Override
//        public int getItemViewType(int position) {
//            return getItemViewTypeAtPosition(position);
//        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

//            viewGroup.setScaleX(0.33f);
//            viewGroup.setScaleY(0.33f);
            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_catalogue, viewGroup, false);

            return new CatalogueItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

                CatalogueItem catal = getCatalogueAtPosition(i);
                CatalogueItemViewHolder holder = (CatalogueItemViewHolder) viewHolder;
                holder.setupCatalogueItem(catal);

        }
    }

    private final class GridSpacingItemDecoration2 extends RecyclerView.ItemDecoration {
        private int mSpanCount;
        private int mSpacing;

        public GridSpacingItemDecoration2(int spanCount, int spacing) {
            mSpanCount = spanCount;
            mSpacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            if (hasRecent()) {
                // those are headers
                if (position == 0 || position == mRecentItems.size() + 1)
                    return;

                if (position > 0 && position < mRecentItems.size() + 1) {
                    position -= 1;
                }
                else {
                    position -= (NUM_HEADERS + mRecentItems.size());
                }
            }

            int column = position % mSpanCount;

            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            outRect.right = (column + 1) * mSpacing / mSpanCount;

            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing;
        }
    }

    private class CatalogueItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mCoverView;

        public CatalogueItemViewHolder(View itemView) {
            super(itemView);
            mCoverView = (ImageView) itemView.findViewById(R.id.catalogueImageView);
//            mCoverView.setScaleX(0.3f);
//            mCoverView.setScaleY(0.3f);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        public void setupCatalogueItem(CatalogueItem catal) {

//            String fileName = catal.getName();
//            //int dotIndex = fileName.lastIndexOf('.');
//            //Uri.Builder builder = new Uri.Builder();
//            //builder.path((getActivity().getExternalFilesDir(null) + File.separator + "0.jpg"));
//            //builder.path("localcover:/storage/emulated/0/Android/data/io.fenogy.comix/media/%E0%B6%A0%E0%B7%94%E0%B6%BD%E0%B7%8A%E0%B6%BD%20%E0%B7%83%E0%B7%99%E0%B6%A7%E0%B7%8A%E0%B6%A7%E0%B7%92.cbz#zip");
//            //Uri uri = builder.build();
            Uri uri;
//            String thumb_name = String.valueOf(catal.getId()-1) +".jpg";
//            String thumb_placeholder = "default.jpg";
//            File externalFile = new File((getActivity().getExternalFilesDir(null)), thumb_name );
//
//            if(!externalFile.exists()){
//
//                //externalFile = new File((getActivity().getExternalFilesDir(null)), thumb_placeholder );
//                uri = Uri.parse("android.resource://io.fenogy.comix/" + R.drawable.placeholder);
//            }else{
//
//                uri = Uri.fromFile(externalFile);
//
//            }
////////////////////////////////////////////////////
            File thumbsFile = new File((getActivity().getExternalFilesDir(null)), "thumbs.zip" );
            mParser = ParserFactory.create(thumbsFile);
            mThumbsFilename = thumbsFile.getName();

            mCurrentThumb = catal.getId()-1;

            mCatalogueHandler = new ComicCatalogueHandler(mParser);
            mPicasso2 = new Picasso.Builder(getActivity())
                    .addRequestHandler(mCatalogueHandler)
                    .build();

            uri = mCatalogueHandler.getThumbUri(mCurrentThumb);
//            mPicasso.load(uri)
//                    .into(mCoverView);

//////////////////////////////////////////////////////////////////////

            try{

                mPicasso2.load(uri)
                        .into(mCoverView);
            }catch(NullPointerException e){

                mPicasso2.load(Uri.parse("android.resource://io.fenogy.comix/" + R.drawable.placeholder))
                        .into(mCoverView);
            }

        }

        @Override
        public void onClick(View v) {

            mLastOpenedCatalogueItem = getCatalogueAtPosition(getAdapterPosition());
            mComicDownloadDialog = new ComicDownloadDialog(getActivity()
                    ,getCatalogueAtPosition(getAdapterPosition()));

            mComicDownloadDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                        //if(RequireLibraryUpdate){
                    mIsRefreshPlanned = true;
                    //refreshLibraryDelayed();
//                            showEmptyDownloadsMessage(false);
//                            setLoading(true);
                            //getUpdatedComicsFromDirectory();
                            //getComics();
                            //Scanner.getInstance().addUpdateHandler(mUpdateHandler);
                            Scanner.getInstance().forceScanLibrary();
                            if (Scanner.getInstance().isRunning()) {
                                setLoading(true);
                            }
                            if(mLastOpenedCatalogueItem != null && RequireReadAfterDismiss){


                            }
                            //((ComicGridAdapter)mComicListView.getAdapter()).notifyDataSetChanged();
                            //RequireLibraryUpdate = false;
                        //}
                }
            });
            mComicDownloadDialog.show();
//            int i = getAdapterPosition();
//            Comic comic = getComicAtPosition(i);
//            openComic(comic);
        }
    }

    private CatalogueItem getCatalogueAtPosition(int position) {

        return mCatalogueItems.get(position);
    }

    private void getUpdatedComicsFromDirectory() {
        List<Comic> comics = Storage.getStorage(getActivity()).listDirectoryComics();
        //issue here hardcode the directorty
        mComicsListManager = new DirectoryListingManager(comics, getLibraryDir());
    }

    private void refreshLibraryDelayed() {
        if (!mIsRefreshPlanned) {
            final Runnable updateRunnable = new Runnable() {
                @Override
                public void run() {
                    getUpdatedComicsFromDirectory();
                    getComics();
                    //((DownloadedComicGridAdapter)mComicListView.getAdapter()).notifyDataSetChanged();
                    ((DownloadedComicGridAdapter)mDownloadedComicListView.getAdapter()).notifyDataSetChanged();
                    showEmptyDownloadsMessage(mAllItems.isEmpty());
                    //showEmptyRecentsMessage(mRecentItems.isEmpty());
                    showEmptyCatalogueMessage(mCatalogueItems.isEmpty());
                    mIsRefreshPlanned = false;
                }
            };
            mIsRefreshPlanned = true;
            //mComicListView.postDelayed(updateRunnable, 100);
            mDownloadedComicListView.postDelayed(updateRunnable, 100);
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            //mRefreshLayout.setRefreshing(true);
            //mComicListView.setOnItemClickListener(null);
        }
        else {
            //mRefreshLayout.setRefreshing(false);
            showEmptyDownloadsMessage(mComicsListManager.getCount() == 0);
            //mComicListView.setOnItemClickListener(this);
        }
    }

    private String getLibraryDir() {
        return getActivity()
                .getSharedPreferences(Constants.SETTINGS_NAME, 0)
                .getString(Constants.SETTINGS_LIBRARY_DIR, null);
        //return "/storage/emulated/0/Android/data/io.fenogy.comix/files/media";
        //return getExternalFilesDir(null)+File.separator+"media";
    }


    private static class UpdateHandler extends Handler {
        private WeakReference<LibraryBrowserFragment> mOwner;

        public UpdateHandler(LibraryBrowserFragment fragment) {
            mOwner = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            LibraryBrowserFragment fragment = mOwner.get();
            if (fragment == null) {
                return;
            }

            if (msg.what == Constants.MESSAGE_MEDIA_UPDATED) {
                fragment.refreshLibraryDelayed();
            }
            else if (msg.what == Constants.MESSAGE_MEDIA_UPDATE_FINISHED) {
                fragment.getUpdatedComicsFromDirectory();
                fragment.getComics();
                //((ComicGridAdapter)fragment.mComicListView.getAdapter()).notifyDataSetChanged();
                //((DownloadedComicGridAdapter)fragment.mComicListView.getAdapter()).notifyDataSetChanged();
                ((DownloadedComicGridAdapter)fragment.mDownloadedComicListView.getAdapter()).notifyDataSetChanged();
                fragment.mRecentComicListView.getAdapter().notifyDataSetChanged();
                //mDownloadedComicListView
                fragment.emptyViewCheck();
                fragment.setLoading(false);
            }
        }
    }

    public List<ComicJSONModel> getListFromCatalogueFile(){

        ComicCatalogueJSONModel cm;
        //StringBuilder json = new StringBuilder();

        try {

            String path = getActivity().getExternalFilesDir(null) + File.separator + "catalogue.json";
            //BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-16"));

            Gson gson = new Gson();
            Type type = new TypeToken<ComicCatalogueJSONModel>() {}.getType();

            cm = gson.fromJson(bufferedReader, type);
            bufferedReader.close();
            return cm.getCatalogue();

        }catch(IOException e){

            return null;

        }



    }
    public void onStartup(){

        String path = getActivity().getExternalFilesDir(null)+File.separator+"media";
        File directory = new File(getActivity().getExternalFilesDir(null)+File.separator+"media");
        if(!directory.exists()) {
            directory.mkdirs();
        }


        List<ComicJSONModel> currentList = getListFromCatalogueFile();
        ArrayList<CatalogueItem> mCatalogueItemList;

        CatalogueStorage mCatalogueStorage = getCatalogueStorage(getActivity());


        currentList = getListFromCatalogueFile();
        mCatalogueStorage = getCatalogueStorage(getActivity());
        //onUpdateRequest();

        if(currentList != null){


            mCatalogueStorage = getCatalogueStorage(getActivity());
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

        loadCatalogue();
        mCatalogueAdapter.notifyDataSetChanged();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);

    }
    private void createCatalogueJSONFile(){

        InputStream is;
        ZipInputStream zis;
        //Check downloaded zipfile
        File thumbsFile = new File((getActivity().getExternalFilesDir(null)), "thumbs.zip" );
        //DefineOutput path
        String path = getActivity().getExternalFilesDir(null) + File.separator + "catalogue.json";
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

    }
    public long x,y;
    public void onUpdateRequest(){


        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://onedrive.live.com/");
         Retrofit retrofit = builder.build();

        Api downloadService = retrofit.create(Api.class);
        //Call<ResponseBody> call = downloadService.downloadFileStream("download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21115&authkey=ANfETR3Sx4cZUoU");
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

                            try {
                                // todo change the file location/name according to your needs
                                File futureStudioIconFile = new File(getActivity().getExternalFilesDir(null) + File.separator + "thumbs.zip");

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
                            //updateProgress2();
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

    }
}
