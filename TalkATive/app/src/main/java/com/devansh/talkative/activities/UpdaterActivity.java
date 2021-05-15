package com.devansh.talkative.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devansh.talkative.BuildConfig;
import com.devansh.talkative.R;

import java.io.File;
import java.util.Objects;

public class UpdaterActivity extends AppCompatActivity {

    private String app_update;
    private String update_link;
    private String changelog;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updater);
        app_update = getIntent().getStringExtra("name");
        update_link = getIntent().getStringExtra("link");
        changelog = getIntent().getStringExtra("info");
        ((TextView)findViewById(R.id.changelog)).setText("Version "+app_update+"\n\n"+changelog);
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.update_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    public void download(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Toast.makeText(getApplicationContext(), "Allow Talk-A-Tive to install apk", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:"+ BuildConfig.APPLICATION_ID)));
                    }
                }, 1000);
                return;
            }
        }
        if (new File(getApplicationContext().getExternalFilesDir(null), "update.apk").exists()) {
            installUpdate();
            return;
        }
        dialog.show();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(update_link));
        request.setTitle("Entertainment");
        request.setDescription("Version " + app_update);
        request.allowScanningByMediaScanner();
        request.setDestinationInExternalFilesDir(getApplicationContext(), "",
                "update.apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(false);
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = Objects.requireNonNull(manager).enqueue(request);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    try {
                        Cursor cursor = manager.query(query);
                        cursor.moveToFirst();
                        final int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        final int progress = (100 * bytes_downloaded) / bytes_total;
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                        }
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                if (bytes_total >= bytes_downloaded) {
                                    ((TextView)dialog.findViewById(R.id.text)).setText("Updating Talk-A-Tive..."+progress+"%");
                                    String current = "", total = "";
                                    current = String.valueOf((double) bytes_downloaded / (1024 * 1024));
                                    total = String.valueOf((double) bytes_total / (1024 * 1024));
                                    try {
                                        current = current.substring(0, current.indexOf('.') + 3);
                                        total = total.substring(0, total.indexOf('.') + 3);
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                        return;
                                    }
                                    if (current.equals(total)) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                installUpdate();
                                            }
                                        }, 540);
                                    }
                                }
                            }
                        });
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void installUpdate() {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri updateFileUri = FileProvider.getUriForFile(getApplicationContext(),BuildConfig.APPLICATION_ID+".provider",
                new File(getApplicationContext().getExternalFilesDir(null),"update.apk"));
        install.setDataAndType(updateFileUri,"application/vnd.android.package-archive");
        startActivity(install);
        dialog.dismiss();
    }
}