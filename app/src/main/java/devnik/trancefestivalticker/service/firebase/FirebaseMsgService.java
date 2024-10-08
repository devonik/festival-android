package devnik.trancefestivalticker.service.firebase;

import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.model.DaoSession;

import static devnik.trancefestivalticker.service.sync.SyncAdapter.getSyncAccount;


/**
 * Created by niklas on 22.03.18.
 */

public class FirebaseMsgService extends FirebaseMessagingService {
    private final String TAG = "FirebaseMsgService";
    // Constants
    // Incoming Intent key for extended data
    private static final String KEY_SYNC_REQUEST =
            "devnik.trancefestivalticker.KEY_SYNC_REQUEST";
    @Override
    public void onNewToken(String s) {
        Log.e("NEW_TOKEN", s);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        DaoSession daoSession = ((App) getApplicationContext()).getDaoSession();

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String remoteMessageMethod = remoteMessage.getData().get(KEY_SYNC_REQUEST);
            //!!!!!!!!!!!!!!!!!! Trigger Sync ONLY if the Application is not in background / closed!!!!!!!!!!!!!!!!!
            assert remoteMessageMethod != null;
            if (remoteMessageMethod.equals("sync")) {
                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                /*
                 * Request the sync for the default account, authority, and
                 * manual sync settings
                 */
                ContentResolver.requestSync(getSyncAccount(this), getApplicationContext().getString(R.string.content_authority), settingsBundle);

            } else if (remoteMessageMethod.equals("newTicketPhase")){
                Log.e("fcm","New Ticket Phase Message Incoming...");
            }

        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }
}
