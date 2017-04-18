package com.capstone.petros.cmsc436msdetector.Sheets;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

public class UploadToDriveTask extends AsyncTask<UploadToDriveTask.DrivePayload, Void, Exception> {

    private com.google.api.services.drive.Drive driveService = null;
    private Sheets.Host host;
    private Activity hostActivity;

    public UploadToDriveTask(GoogleAccountCredential credential, String applicationName, Sheets.Host host, Activity hostActivity) {

        this.host = host;
        this.hostActivity = hostActivity;

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        driveService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(applicationName)
                .build();
    }

    @Override
    protected Exception doInBackground(DrivePayload... params) {
        for (DrivePayload payload : params) {
            try {
                File fileMetadata = new File();
                fileMetadata.setName(payload.fileName);
                fileMetadata.setParents(Collections.singletonList(payload.folderId));

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                payload.image.compress(Bitmap.CompressFormat.JPEG, 69, outputStream);
                ByteArrayContent content = new ByteArrayContent("image/jpeg", outputStream.toByteArray());

                // To get the the ID of the uploaded image, set the following call to a File variable
                // and call getId() on it.
                driveService.files().create(fileMetadata, content)
                        .setFields("id, parents")
                        .execute();
            } catch (Exception e) {
                return e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute (Exception e) {
        if (e != null && e instanceof GooglePlayServicesAvailabilityIOException) {
            Sheets.showGooglePlayErrorDialog(host, hostActivity);
        } else if (e != null && e instanceof UserRecoverableAuthIOException) {
            hostActivity.startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(),
                    host.getRequestCode(Sheets.Action.REQUEST_AUTHORIZATION));
        } else {
            host.notifyFinished(e);
        }
    }

    static class DrivePayload {
        String folderId;
        String fileName;
        Bitmap image;

        DrivePayload(String folderId, String fileName, Bitmap image) {
            this.folderId = folderId;
            this.fileName = fileName;
            this.image = image;
        }
    }
}