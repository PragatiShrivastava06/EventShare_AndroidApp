

package edu.scu.eventshare;
/**
 * Created by Pragati on 2/28/2016.
 */
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

/**
 * An activity to illustrate how to pick a file with the
 * opener intent.
 */
public class PickFileWithOpenerActivity extends BaseDemoActivity {

    private static final String TAG = "PickActivity";

    private static final int REQUEST_CODE_OPENER = 1;
    public static String resId;

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{"application/vnd.ms-excel", "text/plain", "application/msword",
                        "application/pdf", "image/jpeg", "image/png", "image/gif", "text/xml",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
                .build(getGoogleApiClient());
        try {
            startIntentSenderForResult(
                    intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
        } catch (SendIntentException e) {
          Log.w(TAG, "Can't send", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "RequestCode : " + requestCode);
        Log.d(TAG, "ResultCode : " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_OPENER:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Parcelable " + OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    try{
                        DriveId driveId = (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                        showMessage("File Attached Successfully");
                        Log.d(TAG, "Selected file's ID : " + driveId.asDriveFile().getDriveId().toString());
                        Log.d(TAG, "Drive ID getResourceId :" + driveId.getResourceId());
                        resId= "https://drive.google.com/file/d/" + driveId.getResourceId() + "/view?usp=drivesdk";
                        Log.d(TAG, "File View link :" + resId);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            finish();
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
