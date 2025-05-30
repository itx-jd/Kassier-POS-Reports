package com.coderium.pos;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.net.MalformedURLException;
import java.net.URL;

public class Constant {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void vibrator(Context context){

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    public static void downloadFileFromURL(Context context, String fileURL,String name,String version){

//        System.out.println(FilenameUtils.getBaseName(url.getPath())); // -> file
//        System.out.println(FilenameUtils.getExtension(url.getPath())); // -> xml
//        System.out.println(FilenameUtils.getName(url.getPath())); // -> file.xml

        // Downloading File from Url

        try {

            URL url = new URL(fileURL);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileURL + ""));
            request.setTitle(name+" "+version);
            request.setMimeType(MimeTypeMap.getFileExtensionFromUrl(fileURL));
            request.allowScanningByMediaScanner();
            request.setAllowedOverMetered(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name+" "+version+".apk");
            DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(context, "Downloading "+name+" "+version, Toast.LENGTH_SHORT).show();
            Toast.makeText(context, "Great Job \uD83E\uDD19", Toast.LENGTH_SHORT).show();


        } catch (MalformedURLException e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


}
