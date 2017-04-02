/*
package com.capstone.petros.cmsc436msdetector;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

*/
/**
 * Created By Shubham Patel
 * This Class manages the communications between the Google sheet and the app
 *//*


public class GoogleSheetManager
        implements EasyPermissions.PermissionCallbacks{

    GoogleAccountCredential mCredential;


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS_READONLY, SheetsScopes.SPREADSHEETS};

    List<String> pulledData;
    Context mContext;

    public GoogleSheetManager(Context context){
        mContext = context;
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                mContext.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

//        comm = new GoogleSheetGetData(mCredential);
    }


    public void initializeCommunication(){
        if (!isGooglePlayServicesAvailable()) {
            Log.d("CRED", "Was not available");
            acquireGooglePlayServices();

        }
        else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        }
        else if (!isDeviceOnline()) {
           Toast.makeText(mContext,"No network connection available.",Toast.LENGTH_LONG).show();
        }
    }


    public void sendData(String sheetID,List<Object> data){
        data.add(0, SheetData.getPID());
        data.add(1, SheetData.getTimeStamp());
        data.add(2,1);
        new GoogleSheetSendData(mCredential,sheetID).execute(data);
    }

    public void sendCustomData(String sheetID,List<Object> data){
        new GoogleSheetSendData(mCredential,sheetID).execute(data);
    }


    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(mContext);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    */
/**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     *//*

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(mContext);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
        initializeCommunication();
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    */
/**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     *//*

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                (Activity) mContext,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    */
/**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     *//*

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                mContext, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = ((Activity) mContext).getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                initializeCommunication();
            } else {
                mContext.startActivity(mCredential.newChooseAccountIntent());
                // Start a dialog from which the user can choose an account
                ((Activity)mContext).startActivityForResult(mCredential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    mContext,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }
    */
/**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     *//*

    public void setServices(int requestCode, int resultCode, Intent data){
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(mContext,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.",
                            Toast.LENGTH_LONG).show();
                } else {
                    initializeCommunication();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:

                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                ((Activity)mContext).getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        initializeCommunication();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    initializeCommunication();
                }
                break;
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    private class GoogleSheetSendData extends AsyncTask<List<Object>,Void,Void>{
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private String sheetID;
        private Exception exception = null;
        GoogleSheetSendData(GoogleAccountCredential credential, String sheetID){
            // The mServices is what is used to access the data sheet data
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)

                    .setApplicationName(SheetData.SPREADSHEET_NAME)
                    .build();
            this.sheetID = sheetID;
        }

        @Override
        protected Void doInBackground(List<Object>... params) {
            try {
                sendData(params[0]);
            } catch (IOException e) {
                Log.d("PROBLEM", "EXCEPTION \n"+ e.toString()+" Occurred");
                exception = e;
                cancel(true);
                return null;
            }

            return null;
        }

        private void sendData(List<Object> l) throws IOException {
            // THIS SECTION IS FROM THE SHEET PROGRAM SENT TO THE WHOLE CLASS
            //Getting the data from the
            ValueRange response = mService.spreadsheets().values().get(
                     SheetData.CENTRAL_SPREADSHEET_ID,
                     sheetID+"!A2:A"
            ).execute();
            List<List<Object>> sheet = response.getValues();
            int idx = 2;
            if(sheet != null){
                for (List r : sheet ) {
                    if(r.get(0).equals(SheetData.getPID())){
                        break;
                    }
                    idx++;
                }
            }

            response = mService.spreadsheets().values()
                    .get(
                            SheetData.CENTRAL_SPREADSHEET_ID,
                            SheetData.getCentralRange(sheetID,idx)
            ).execute();

            sheet = response.getValues();
            String colIdx = "A";
             if(sheet != null){
                 colIdx = columnToLetter(sheet.get(0).size()+1);
             }
            String updateCell = sheetID+"!"+colIdx+idx;

            List<List<Object>> values = new ArrayList<>();
            List<Object> row = new ArrayList<>();

            if(colIdx.equals("A")){
                row.add(SheetData.getPID());
                updateCell+=":B"+idx;
            }

            float val = 0;
            for(Object o: l.subList(2,l.size())){
                System.out.println(o);
//                val+=(float)o;
            }
            val = val/l.size();
            row.add(val);
            values.add(row);

            ValueRange vRange = new ValueRange();
            vRange.setValues(values);

            mService.spreadsheets().values().
                    update(
                        SheetData.CENTRAL_SPREADSHEET_ID,
                        updateCell,
                        vRange)
                    .setValueInputOption("RAW")
                    .execute();


            //add data to send in Value range
            ValueRange valueRange = new ValueRange();
            ArrayList<List<Object>> tList = new ArrayList<>();
            tList.add(l);
            valueRange.setValues(tList);

            //sending value range
            mService.spreadsheets().values().append(
            SheetData.SPREADSHEET_ID,
            SheetData.getRange(sheetID),
            valueRange).setValueInputOption("RAW").execute();

//            mService.
        }



        @Override
        protected void onCancelled() {
            if (exception != null) {
                if (exception instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) exception)
                                    .getConnectionStatusCode());
                } else if (exception instanceof UserRecoverableAuthIOException) {
                    ((Activity)mContext).startActivityForResult(
                            ((UserRecoverableAuthIOException) exception).getIntent(),
                            GoogleSheetManager.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(mContext,"The following error occurred:\n"
                            + exception.getMessage(),Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mContext,"Request cancelled.",Toast.LENGTH_LONG).show();
            }
        }

        private String columnToLetter(int column) {
            int temp;
            String letter = "";
            while (column > 0)
            {
                temp = (column - 1) % 26;
                letter = ((char)(temp + 65)) + letter;
                column = (column - temp - 1) / 26;
            }
            return letter;
        }
    }


}
*/
