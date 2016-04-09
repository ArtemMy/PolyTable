package edu.amd.spbstu.polystudenttimetable;

/**
 * Created by root on 4/4/16.
 */
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.drive.DriveId;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class REST { private REST() {}
    interface ConnectCBs {
        void onConnFail(Exception ex);
        void onConnOK();
    }
    private static Drive mGOOSvc;
    private static ConnectCBs mConnCBs;
    private static boolean mConnected;
    private static final String TAG = "polytable_log";


    /************************************************************************************************
     * initialize Google Drive Api
     * @param act   activity context
     */
    static boolean init(Activity act){                    //UT.lg( "REST init " + email);
        if (act != null) try {
            String email = UT.AM.getEmail();
            if (email != null) {
                mConnCBs = (ConnectCBs)act;
                mGOOSvc = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                        GoogleAccountCredential.usingOAuth2(UT.acx, Collections.singletonList(DriveScopes.DRIVE))
                                .setSelectedAccountName(email)
                ).build();
                return true;
            }
        } catch (Exception e) {UT.le(e);}
        return false;
    }
    /**
     * connect
     */
    static void connect() {
        if (UT.AM.getEmail() != null && mGOOSvc != null) {
            mConnected = false;
            new AsyncTask<Void, Void, Exception>() {
                @Override
                protected Exception doInBackground(Void... nadas) {
                    try {
                        // GoogleAuthUtil.getToken(mAct, email, DriveScopes.DRIVE_FILE);   SO 30122755
                        mGOOSvc.files().get("root").setFields("title").execute();
                        mConnected = true;
                    } catch (UserRecoverableAuthIOException uraIOEx) {  // standard authorization failure - user fixable
                        return uraIOEx;
                    } catch (GoogleAuthIOException gaIOEx) {  // usually PackageName /SHA1 mismatch in DevConsole
                        return gaIOEx;
                    } catch (IOException e) {   // '404 not found' in FILE scope, consider connected
                        if (e instanceof GoogleJsonResponseException) {
                            if (404 == ((GoogleJsonResponseException) e).getStatusCode())
                                mConnected = true;
                        }
                    } catch (Exception e) {  // "the name must not be empty" indicates
                        UT.le(e);           // UNREGISTERED / EMPTY account in 'setSelectedAccountName()' above
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Exception ex) {
                    super.onPostExecute(ex);
                    if (mConnected) {
                        mConnCBs.onConnOK();
                    } else {  // null indicates general error (fatal)
                        mConnCBs.onConnFail(ex);
                    }
                }
            }.execute();
        }
    }
    /**
     * disconnect    disconnects GoogleApiClient
     */
    static void disconnect() {}

    /************************************************************************************************
     * find file/folder in GOODrive
     * @param prnId   parent ID (optional), null searches full drive, "root" searches Drive root
     * @param titl    file/folder name (optional)
     * @param mime    file/folder mime type (optional)
     * @return        arraylist of found objects
     */
    static ArrayList<ContentValues> search(String prnId, String titl, String mime) {
        ArrayList<ContentValues> gfs = new ArrayList<>();
        if (mGOOSvc != null && mConnected) try {
            // add query conditions, build query
            String qryClause = "'me' in owners and ";
            if (prnId != null) qryClause += "'" + prnId + "' in parents and ";
            if (titl != null) qryClause += "title = '" + titl + "' and ";
            if (mime != null) qryClause += "mimeType = '" + mime + "' and ";
            qryClause = qryClause.substring(0, qryClause.length() - " and ".length());
            Drive.Files.List qry = mGOOSvc.files().list().setQ(qryClause)
                    .setFields("items(id,mimeType,labels/trashed,title),nextPageToken");
            String npTok = null;
            if (qry != null) do {
                FileList gLst = qry.execute();
                if (gLst != null) {
                    for (File gFl : gLst.getItems()) {
                        if (gFl.getLabels().getTrashed()) continue;
                        gfs.add( UT.newCVs(gFl.getTitle(), gFl.getId(), gFl.getMimeType()));
                    }                                                                 //else UT.lg("failed " + gFl.getTitle());
                    npTok = gLst.getNextPageToken();
                    qry.setPageToken(npTok);
                }
            } while (npTok != null && npTok.length() > 0);                     //UT.lg("found " + vlss.size());
        } catch (Exception e) { UT.le(e); }
        return gfs;
    }

    /************************************************************************************************
     * create file/folder in GOODrive
     * @param prnId  parent's ID, (null or "root") for root
     * @param titl  file name
     * @return      file id  / null on fail
     */
    static String createFolder(String prnId, String titl) {
        String rsId = null;
        if (mGOOSvc != null && mConnected && titl != null) try {
            File meta = new File();
            meta.setParents(Collections.singletonList(new ParentReference().setId(prnId == null ? "root" : prnId)));
            meta.setTitle(titl);
            meta.setMimeType(UT.MIME_FLDR);

            File gFl = null;
            try { gFl = mGOOSvc.files().insert(meta).execute();
            } catch (Exception e) { UT.le(e); }
            if (gFl != null && gFl.getId() != null) {
                rsId = gFl.getId();
            }
        } catch (Exception e) { UT.le(e); }
        return rsId;
    }

    /************************************************************************************************
     * create file/folder in GOODrive
     * @param prnId  parent's ID, (null or "root") for root
     * @param titl  file name
     * @param mime  file mime type
     * @param file  file (with content) to create
     * @return      file id  / null on fail
     */
    static String createFile(String prnId, String titl, String mime, java.io.File file) {
        String rsId = null;
        if (mGOOSvc != null && mConnected && titl != null && mime != null && file != null) try {
            File meta = new File();
            meta.setParents(Collections.singletonList(new ParentReference().setId(prnId == null ? "root" : prnId)));
            meta.setTitle(titl);
            meta.setMimeType(mime);

            File gFl = mGOOSvc.files().insert(meta, new FileContent(mime, file)).execute();
            if (gFl != null)
                rsId = gFl.getId();
        } catch (Exception e) { UT.le(e); }
        return rsId;
    }

    /************************************************************************************************
     * get file contents
     * @param resId  file driveId
     * @return       file's content  / null on fail
     */
    static byte[] read(String resId) {
        Log.d("polytable_log", String.valueOf(mGOOSvc != null));
        Log.d("polytable_log", String.valueOf(mConnected));
        if (mGOOSvc != null && mConnected && resId != null) try {
            File gFl = mGOOSvc.files().get(resId).setFields("downloadUrl").execute();
            if (gFl != null){
                String strUrl = gFl.getDownloadUrl();
                return UT.is2Bytes(mGOOSvc.getRequestFactory().buildGetRequest(new GenericUrl(strUrl)).execute().getContent());
            }
        } catch (Exception e) { UT.le(e); }
        return null;
    }

    /************************************************************************************************
     * share file contents
     */
    static boolean share(String resId, String emailStr, JsonBatchCallback<Permission> callback) {
        if(resId == null || resId == "" || emailStr == null || emailStr == "")
            return false;
        Log.d("polytable_log", String.valueOf(mGOOSvc != null));

        BatchRequest batch = mGOOSvc.batch();
        Permission userPermission = new Permission()
                .setType("user")
                .setRole("writer")
                .setValue(emailStr);

        Log.d(TAG, "sharing" + resId);
        try {
            mGOOSvc.permissions().insert(resId, userPermission)
                    .queue(batch, callback);
            batch.execute();
        } catch( IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return false;
        }
        Log.d(TAG, "shared" + resId);
        return true;
    }

    /************************************************************************************************
     * query shared file contents
     */
    static String query(String name) {
        if(name == null || name == "")
            return null;
        try {
            Log.d(TAG, "title = '" + name + "' and sharedWithMe = true");
            FileList result = mGOOSvc.files().list()
//                    .setSpaces("drive")
                    .setQ("title = '" + name + "' and sharedWithMe = true")
//                    .setFields("nextPageToken, files(id, name)")
//                    .setPageToken(null)
                    .execute();

            List<File> files = result.getItems();
            if(files.size() == 0)
                return null;
            Log.d(TAG, "queried " + String.valueOf(files.size()) + " files");
            Log.d(TAG, "0: " + files.get(0).getId());

            return files.get(0).getId();
/*            File fileMetadata = new File();
            fileMetadata.setTitle("Shared PolyTable data");
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            File file = mGOOSvc.files().insert(fileMetadata)
                    .setFields("id")
                    .execute();

            ParentReference pr = new ParentReference();
            pr.setId(file.getId());
            queriedFile.getParents().add(pr); */
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    /************************************************************************************************
     * update file in GOODrive,  see https://youtu.be/r2dr8_Mxr2M (WRONG?)
     * see https://youtu.be/r2dr8_Mxr2M   .... WRONG !!!
     * @param resId  file  id
     * @param titl  new file name (optional)
     * @param mime  new mime type (optional, "application/vnd.google-apps.folder" indicates folder)
     * @param file  new file content (optional)
     * @return      file id  / null on fail
     */
    static String update(String resId, String titl, String mime, String desc, java.io.File file){
        File gFl = null;
        if (mGOOSvc != null && mConnected && resId != null) try {
            File meta = new File();
            if (titl != null) meta.setTitle(titl);
            if (mime != null) meta.setMimeType(mime);
            if (desc != null) meta.setDescription(desc);

            if (file == null)
                gFl = mGOOSvc.files().patch(resId, meta).execute();
            else
                gFl = mGOOSvc.files().update(resId, meta, new FileContent(mime, file)).execute();

        } catch (Exception e) { UT.le(e); }
        return gFl == null ? null : gFl.getId();
    }

    /************************************************************************************************
     * trash file in GOODrive
     * @param resId  file  id
     * @return       success status
     */
    static boolean trash(String resId) {
        if (mGOOSvc != null && mConnected && resId != null) try {
            return null != mGOOSvc.files().trash(resId).execute();
        } catch (Exception e) {UT.le(e);}
        return false;
    }

    /**
     * FILE / FOLDER type object inquiry
     * @param cv oontent values
     * @return TRUE if FOLDER, FALSE otherwise
     */
    static boolean isFolder(ContentValues cv) {
        String mime = cv.getAsString(UT.MIME);
        return mime != null && UT.MIME_FLDR.equalsIgnoreCase(mime);
    }

}