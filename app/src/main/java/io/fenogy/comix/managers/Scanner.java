package io.fenogy.comix.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;

import io.fenogy.comix.Constants;
import io.fenogy.comix.MainApplication;
import io.fenogy.comix.model.*;
import io.fenogy.comix.parsers.Parser;
import io.fenogy.comix.parsers.ParserFactory;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private Thread mUpdateThread;
    private List<Handler> mUpdateHandler;

    private boolean mIsStopped;
    private boolean mIsRestarted;

    private Handler mRestartHandler = new RestartHandler(this);

    private static class RestartHandler extends Handler {
        private WeakReference<Scanner> mScannerRef;

        public RestartHandler(Scanner scanner) {
            mScannerRef = new WeakReference<>(scanner);
        }

        @Override
        public void handleMessage(Message msg) {
            Scanner scanner = mScannerRef.get();
            if (scanner != null) {
                scanner.scanLibrary();
            }
        }
    }

    private static Scanner mInstance;
    public synchronized static Scanner getInstance() {
        if (mInstance == null) {
            mInstance = new Scanner();
        }
        return mInstance;
    }

    private Scanner() {
        mInstance = this;
        mUpdateHandler = new ArrayList<>();
    }

    public boolean isRunning() {
        return mUpdateThread != null &&
                mUpdateThread.isAlive() &&
                mUpdateThread.getState() != Thread.State.TERMINATED &&
                mUpdateThread.getState() != Thread.State.NEW;
    }

    public void stop() {
        mIsStopped = true;
    }

    public void forceScanLibrary() {
        if (isRunning()) {
            mIsStopped = true;
            mIsRestarted = true;
        }
        else {
            scanLibrary();
        }
    }

    public void scanLibrary() {
        if (mUpdateThread == null || mUpdateThread.getState() == Thread.State.TERMINATED) {
            LibraryUpdateRunnable runnable = new LibraryUpdateRunnable();
            mUpdateThread = new Thread(runnable);
            mUpdateThread.setPriority(Process.THREAD_PRIORITY_DEFAULT+Process.THREAD_PRIORITY_LESS_FAVORABLE);
            mUpdateThread.start();
        }
    }

    public void addUpdateHandler(Handler handler) {
        mUpdateHandler.add(handler);
    }

    public void removeUpdateHandler(Handler handler) {
        mUpdateHandler.remove(handler);
    }

    private void notifyMediaUpdated() {
        for (Handler h : mUpdateHandler) {
            h.sendEmptyMessage(Constants.MESSAGE_MEDIA_UPDATED);
        }
    }

    private void notifyLibraryUpdateFinished() {
        for (Handler h : mUpdateHandler) {
            h.sendEmptyMessage(Constants.MESSAGE_MEDIA_UPDATE_FINISHED);
        }
    }

    private class LibraryUpdateRunnable implements Runnable {
        @Override
        public void run() {
            try {
                Context ctx = MainApplication.getAppContext();
                String libDir = MainApplication.getPreferences()
                        .getString(Constants.SETTINGS_LIBRARY_DIR, "");


                if (libDir.equals("")) return;
                Storage storage = Storage.getStorage(ctx);
                Map<File, Comic> storageFiles = new HashMap<>();

                // create list of files available in storage
                for (Comic c : storage.listComics()) {
                    storageFiles.put(c.getFile(), c);
                }

                // search and add comics if necessary
                Deque<File> directories = new ArrayDeque<>();
                directories.add(new File(libDir));
                while (!directories.isEmpty()) {
                    File dir = directories.pop();
                    File[] files = dir.listFiles();
                    if(files != null) {//Mychanges for support when a folder moved
                        Arrays.sort(files);
                        for (File file : files) {
                            if (mIsStopped) return;
                            if (file.isDirectory()) {
                                directories.add(file);
                            }
                            if (storageFiles.containsKey(file)) {
                                storageFiles.remove(file);
                                continue;
                            }
                            Parser parser = ParserFactory.create(file);
                            if (parser == null) continue;
                            if (parser.numPages() > 0) {
                                storage.addBook(file, parser.getType(), parser.numPages());
                                notifyMediaUpdated();
                            }
                        }
                    }
                }

                // delete missing comics
                for (Comic missing : storageFiles.values()) {
                    File coverCache = Utils.getCacheFile(ctx, missing.getFile().getAbsolutePath());
                    coverCache.delete();
                    storage.removeComic(missing.getId());
                }
            }
            finally {
                mIsStopped = false;

                if (mIsRestarted) {
                    mIsRestarted = false;
                    mRestartHandler.sendEmptyMessageDelayed(1, 200);
                }
                else {
                    notifyLibraryUpdateFinished();
                }
            }
        }
    }
}
