package io.fenogy.comix.model;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashSet;


public class CatalogueStorage {

    public static abstract class CatalogueBook implements BaseColumns {
        public static final String TABLE_NAME = "book";

        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FILENAME = "name";
        public static final String COLUMN_NAME_FILEURI = "uri";
        public static final String COLUMN_NAME_NUM_PAGES = "num_pages";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_AUTHORID = "author_id";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_LANGUAGE = "language";
        public static final String COLUMN_NAME_DOWNLOADED = "downloaded_flag";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_THUMB = "thumb";

        public static final String[] columns = {
                CatalogueBook.COLUMN_NAME_ID,
                CatalogueBook.COLUMN_NAME_FILENAME,
                CatalogueBook.COLUMN_NAME_FILEURI,
                CatalogueBook.COLUMN_NAME_NUM_PAGES,
                CatalogueBook.COLUMN_NAME_AUTHOR,
                CatalogueBook.COLUMN_NAME_AUTHORID,
                CatalogueBook.COLUMN_NAME_DESCRIPTION,
                CatalogueBook.COLUMN_NAME_SIZE,
                CatalogueBook.COLUMN_NAME_LANGUAGE,
                CatalogueBook.COLUMN_NAME_DOWNLOADED,
                CatalogueBook.COLUMN_NAME_CATEGORY,
                CatalogueBook.COLUMN_NAME_THUMB
        };
    }

    public class CatalogueDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "catalogue.db";

        public CatalogueDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            final String sql = "CREATE TABLE " + CatalogueBook.TABLE_NAME + " ("
                    + CatalogueBook.COLUMN_NAME_ID + " INTEGER PRIMARY KEY,"
                    + CatalogueBook.COLUMN_NAME_FILENAME + " TEXT,"
                    + CatalogueBook.COLUMN_NAME_FILEURI + " TEXT,"
                    + CatalogueBook.COLUMN_NAME_NUM_PAGES + " INTEGER,"
                    + CatalogueBook.COLUMN_NAME_AUTHOR + " TEXT,"
                    + CatalogueBook.COLUMN_NAME_AUTHORID + " INTEGER,"
                    + CatalogueBook.COLUMN_NAME_DESCRIPTION + " TEXT,"
                    + CatalogueBook.COLUMN_NAME_SIZE + " TEXT,"
                    + CatalogueBook.COLUMN_NAME_LANGUAGE + " TEXT,"
                    + CatalogueBook.COLUMN_NAME_DOWNLOADED + " TEXT,"
                    + CatalogueBook.COLUMN_NAME_CATEGORY + " TEXT,"
                    + CatalogueBook.COLUMN_NAME_THUMB + " TEXT"
                    + ")";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE " + CatalogueBook.TABLE_NAME + " ADD COLUMN " + CatalogueBook.COLUMN_NAME_DOWNLOADED + " INTEGER");
            }
        }
    }

    private CatalogueDbHelper mDbHelper;
    private static CatalogueStorage mSharedInstance;

    private static final String SORT_ORDER = "lower(" + CatalogueBook.COLUMN_NAME_ID + "|| '/' || " + CatalogueBook.COLUMN_NAME_FILENAME + ") ASC";

    //Constructor for singleton
    private CatalogueStorage(Context context) {
        mDbHelper = new CatalogueDbHelper(context);
    }

    public static CatalogueStorage getCatalogueStorage(Context context) {
        if (mSharedInstance == null) {
            synchronized (CatalogueStorage.class) {
                if (mSharedInstance == null) {
                    mSharedInstance = new CatalogueStorage(context);
                }
            }
        }
        return mSharedInstance;
    }

    public void clearStorage() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(CatalogueBook.TABLE_NAME, null, null);
    }

    public void addToCatalogue(String name, String uri, int numPages, String author, int author_id,
                               String description,String size, String language, String category,
                               String thumb) {


        ContentValues cv = new ContentValues();
        cv.put(CatalogueBook.COLUMN_NAME_FILENAME, name);
        cv.put(CatalogueBook.COLUMN_NAME_FILEURI, uri);
        cv.put(CatalogueBook.COLUMN_NAME_NUM_PAGES, numPages);
        cv.put(CatalogueBook.COLUMN_NAME_AUTHOR, author);
        cv.put(CatalogueBook.COLUMN_NAME_AUTHORID, author_id);
        cv.put(CatalogueBook.COLUMN_NAME_DESCRIPTION, description);
        cv.put(CatalogueBook.COLUMN_NAME_SIZE, size);
        cv.put(CatalogueBook.COLUMN_NAME_LANGUAGE, language);
        cv.put(CatalogueBook.COLUMN_NAME_CATEGORY, category);
        cv.put(CatalogueBook.COLUMN_NAME_THUMB, thumb);




        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.insert(CatalogueBook.TABLE_NAME, "null", cv);
    }

    private ComicJSONModel comicJSONModelFromCursor(Cursor c) {

        int id          = c.getInt(c.getColumnIndex(CatalogueBook.COLUMN_NAME_ID));
        String name	    =c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_FILENAME));
        String uri	    =c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_FILEURI));
        int numPages	=c.getInt(c.getColumnIndex(CatalogueBook.COLUMN_NAME_NUM_PAGES));
        String author	=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_AUTHOR));
        int author_id	=c.getInt(c.getColumnIndex(CatalogueBook.COLUMN_NAME_AUTHORID));
        String description=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_DESCRIPTION));
        String size	    =c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_SIZE));
        String language	=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_LANGUAGE));
        String category	=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_CATEGORY));
        String thumb	=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_THUMB));

        return new ComicJSONModel(id, name,author,author_id,numPages,
        size, description, category,language,false,uri);
    }

    private CatalogueItem catalogueItemFromCursor(Cursor c) {

        int id          = c.getInt(c.getColumnIndex(CatalogueBook.COLUMN_NAME_ID));
        String name	    =c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_FILENAME));
        String uri	    =c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_FILEURI));
        int numPages	=c.getInt(c.getColumnIndex(CatalogueBook.COLUMN_NAME_NUM_PAGES));
        String author	=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_AUTHOR));
        int author_id	=c.getInt(c.getColumnIndex(CatalogueBook.COLUMN_NAME_AUTHORID));
        String description=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_DESCRIPTION));
        String size	    =c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_SIZE));
        String language	=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_LANGUAGE));
        String category	=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_CATEGORY));
        String thumb	=c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_THUMB));
        String downloaded = c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_DOWNLOADED));
        return new CatalogueItem(id, name,author,author_id,
                size, description, category,language,downloaded,uri,numPages,thumb);
    }


    public CatalogueItem getCatalogueItem(int catalogueId) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String order = CatalogueBook.COLUMN_NAME_FILENAME + " DESC";
        String selection = CatalogueBook.COLUMN_NAME_ID + "=" + Integer.toString(catalogueId);
        Cursor c = db.query(CatalogueBook.TABLE_NAME, CatalogueBook.columns, selection, null, null, null, order);

        if (c.getCount() != 1) {
            return null;
        }

        c.moveToFirst();

        CatalogueItem comic = catalogueItemFromCursor(c);
        c.close();
        return comic;
    }

    public ArrayList<CatalogueItem> getCatalogueItems(int startId, int count) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String order = CatalogueBook.COLUMN_NAME_FILENAME + " DESC";
        String selection = CatalogueBook.COLUMN_NAME_ID + "=" + Integer.toString(startId);
        Cursor c = db.query(CatalogueBook.TABLE_NAME, CatalogueBook.columns, selection, null, null, null, order);

        if (c.getCount() != 1) {
            return null;
        }
        ArrayList<CatalogueItem> catalogue = new ArrayList<>();

        c.moveToFirst();
        int x = 0;
        do {
            catalogue.add(catalogueItemFromCursor(c));
            x++;
        } while (c.moveToNext() && (x <= count));

        c.close();

        return catalogue;

    }

    public ArrayList<CatalogueItem> listFullCatalogue() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor c = db.query(
                CatalogueBook.TABLE_NAME, CatalogueBook.columns, null, null,
                null, null,
                SORT_ORDER);

        ArrayList<CatalogueItem> catalogue = new ArrayList<>();
        if (c.getCount() == 0) return catalogue;

        HashSet<String> group = new HashSet<>();
        c.moveToFirst();
        do {
//            String filepath = c.getString(c.getColumnIndex(CatalogueBook.COLUMN_NAME_FILEPATH));
//            if (group.contains(filepath))
//                continue;
//            group.add(filepath);
            catalogue.add(catalogueItemFromCursor(c));
        } while (c.moveToNext());

        c.close();

        return catalogue;
    }

    public boolean itemsAvailable(){

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor c = db.query(
                CatalogueBook.TABLE_NAME, CatalogueBook.columns, null, null,
                null, null,
                SORT_ORDER);

        if (c.getCount() == 0) return false;
        return true;
    }

}
