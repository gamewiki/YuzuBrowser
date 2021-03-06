package jp.hazuki.yuzubrowser.bookmark.netscape;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;

public class BookmarkHtmlExportTask extends AsyncTaskLoader<Boolean> {

    private final File dest;
    private BookmarkFolder folder;

    public BookmarkHtmlExportTask(Context context, File dest, BookmarkFolder folder) {
        super(context);
        this.dest = dest;
        this.folder = folder;
    }

    @Override
    public Boolean loadInBackground() {
        if (!dest.getParentFile().exists()) {
            if (!dest.getParentFile().mkdirs()) {
                return Boolean.FALSE;
            }
        }

        try (FileWriter fileWriter = new FileWriter(dest);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {

            NetscapeBookmarkCreator creator = new NetscapeBookmarkCreator(folder);
            creator.create(writer);
            return Boolean.TRUE;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
