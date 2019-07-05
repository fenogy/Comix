package io.fenogy.comix;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

//import io.fenogy.comix.fragment.AboutFragment;
import io.fenogy.comix.fragment.BrowserFragment;
import io.fenogy.comix.fragment.LibraryBrowserFragment;
import io.fenogy.comix.fragment.LibraryFragment;
import io.fenogy.comix.managers.LocalCoverHandler;
import io.fenogy.comix.managers.Scanner;
import io.fenogy.comix.managers.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    private final static String STATE_CURRENT_MENU_ITEM = "STATE_CURRENT_MENU_ITEM";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurrentNavItem;
    private Picasso mPicasso;
    public static String MEDIA_LIBRARY_PATH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (Utils.isLollipopOrLater()) {
            //toolbar.setElevation(8);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setHomeButtonEnabled(true);
            //actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mPicasso = new Picasso.Builder(this)
                .addRequestHandler(new LocalCoverHandler(this))
                .build();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
//        setupNavigationView(navigationView);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle); //My Changes
///////////////////////////mychanges
        SharedPreferences preferences = getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        MEDIA_LIBRARY_PATH = getExternalFilesDir(null) + File.separator +"media";
        editor.putString(Constants.SETTINGS_LIBRARY_DIR, MEDIA_LIBRARY_PATH);
        editor.apply();
        //MEDIA_LIBRARY_PATH = getExternalFilesDir(null) + File.separator +"media";

////////////////////////////////////////
        Scanner.getInstance().scanLibrary();

        if (savedInstanceState == null) {
            //setFragment(new LibraryFragment());
            //Changed here
            setFragment(LibraryBrowserFragment.create(MEDIA_LIBRARY_PATH));
            //setNavBar();
            mCurrentNavItem = R.id.drawer_menu_library;
            //navigationView.getMenu().findItem(mCurrentNavItem).setChecked(true);
        }
        else {
            onBackStackChanged();  // force-call method to ensure indicator is shown properly
            mCurrentNavItem = savedInstanceState.getInt(STATE_CURRENT_MENU_ITEM);
            //navigationView.getMenu().findItem(mCurrentNavItem).setChecked(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_MENU_ITEM, mCurrentNavItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    private void setNavBar() {
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.header, new HeaderFragment())
//                .commit();
        int x = R.id.header;
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() >= 1) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    public void pushFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    private boolean popFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    private void setupNavigationView(NavigationView view) {
        view.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (mCurrentNavItem == menuItem.getItemId()) {
                    mDrawerLayout.closeDrawers();
                    return true;
                }

                switch (menuItem.getItemId()) {
                    case R.id.drawer_menu_library:
                        setFragment(new LibraryFragment());
                        break;
                    case R.id.drawer_menu_browser:
                        setFragment(new BrowserFragment());
                        break;
                    case R.id.drawer_menu_about:
//                        setTitle(R.string.menu_about);
//                        setFragment(new AboutFragment());
                        break;
                }

                mCurrentNavItem = menuItem.getItemId();
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        mDrawerToggle.setDrawerIndicatorEnabled(getSupportFragmentManager().getBackStackEntryCount() == 0);
    }

    @Override
    public void onBackPressed() {
        if (!popFragment()) {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (!popFragment()) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                mDrawerLayout.closeDrawers();
            else
                mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onSupportNavigateUp();
    }




//    public void showPopupMenu(View v) {
//
//        PopupMenu popup = new PopupMenu(this, v);
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//
//                //LibraryBrowserFragment.
//                switch (item.getItemId()) {
////                    case io.fenogy.horoscope.R.id.menu_item_main_modify:
////                        callModifyActivity();
////                        return true;
////                    case io.fenogy.horoscope.R.id.menu_item_main_match:
////                        callPartnerActivity();
////                        return true;
////                    case io.fenogy.horoscope.R.id.menu_item_main_history:
////                        callHistoryActivity();
////                        return true;
////                    case R.id.menu_item_main_order:
////                        callBrowser(ORDER_CARD);
////                        return true;
////                    case io.fenogy.horoscope.R.id.menu_item_main_help:
////                        callHelpActivity();
////                        return true;
////                    case R.id.menu_item_main_find_astrologer:
////                        callBrowser(CONSULT_PRO);
////                        return true;
////                    case io.fenogy.horoscope.R.id.menu_item_main_weekly:
////                        callPalapalaActivity();
////                        return true;
//                    default:
//                        return false;
//                }
//            }
//        });
//
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.browser, popup.getMenu());
//        popup.show();
//    }
}
