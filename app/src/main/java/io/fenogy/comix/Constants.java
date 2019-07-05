package io.fenogy.comix;

public class Constants {
    public static final int COVER_THUMBNAIL_HEIGHT = 300;
    public static final int COVER_THUMBNAIL_WIDTH = 200;

    public static final int MAX_PAGE_HEIGHT = 1600;
    public static final int MAX_PAGE_WIDTH = 2000;

    public static final int MAX_RECENT_COUNT = 5;

    public static final String SETTINGS_NAME = "SETTINGS_COMICS";
    public static final String SETTINGS_LIBRARY_DIR = "SETTINGS_LIBRARY_DIR";

    public static final String SETTINGS_PAGE_VIEW_MODE = "SETTINGS_PAGE_VIEW_MODE";
    public static final String SETTINGS_READING_LEFT_TO_RIGHT = "SETTINGS_READING_LEFT_TO_RIGHT";

    public static final String SETTINGS_PAGE_ORIENTATION = "SETTINGS_PAGE_ORIENTATION";
    public static final String SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL = "SETTINGS_CATALOGUE_ONEDRIVE_SUB_URL";
    public static final String SETTINGS_CATALOGUE_ONEDRIVE_DEFAULT_SUB_URL = "download?cid=C76F1D4E5DFBD413&resid=C76F1D4E5DFBD413%21112&authkey=AHGNnvdba8raGqc";

    public static final int MESSAGE_MEDIA_UPDATE_FINISHED = 0;
    public static final int MESSAGE_MEDIA_UPDATED = 1;

    public static final String LANDSCAPE = "LANDSCAPE";
    public static final String PORTRAIT = "PORTRAIT";

    public enum PageViewMode {
        ASPECT_FILL(0),
        ASPECT_FIT(1),
        FIT_WIDTH(2);

        private PageViewMode(int n) {
            native_int = n;
        }
        public final int native_int;
    }
}
