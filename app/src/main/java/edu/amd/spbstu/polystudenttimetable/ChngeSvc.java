package edu.amd.spbstu.polystudenttimetable;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.events.CompletionEvent;
import com.google.android.gms.drive.events.DriveEventService;

/**
 * Created by artem on 3/29/16.
 */
public class ChngeSvc extends DriveEventService {
    private static final String TAG = "polytable_log";
    static final String PREF_FILE_ID = "poly_table_file_id";

    @Override
    public void onCompletion(CompletionEvent event) {  super.onCompletion(event);
        DriveId driveId = event.getDriveId();

        SharedPreferences.Editor editor = getSharedPreferences(
                "edu.amd.spbstu.polystudenttimetable", Context.MODE_PRIVATE).edit();
        editor.putString(PREF_FILE_ID, driveId.encodeToString());
        editor.apply();
        Log.d(TAG, "onComplete: " + driveId.getResourceId());

        switch (event.getStatus()) {
            case CompletionEvent.STATUS_CONFLICT:  Log.d(TAG, "STATUS_CONFLICT"); event.dismiss(); break;
            case CompletionEvent.STATUS_FAILURE:   Log.d(TAG, "STATUS_FAILURE");  event.dismiss(); break;
            case CompletionEvent.STATUS_SUCCESS:   Log.d(TAG, "STATUS_SUCCESS "); event.dismiss(); break;
        }
    }
}