package devnik.trancefestivalticker.firebase;

import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.api.SyncTicketPhases;
import devnik.trancefestivalticker.model.DaoSession;

import static devnik.trancefestivalticker.sync.SyncAdapter.getSyncAccount;


/**
 * Created by niklas on 22.03.18.
 */

public class FirebaseMsgService extends FirebaseMessagingService {
    private final String TAG = "FirebaseMsgService";
    // Constants
    // Incoming Intent key for extended data
    public static final String KEY_SYNC_REQUEST =
            "devnik.trancefestivalticker.KEY_SYNC_REQUEST";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        DaoSession daoSession = ((App) getApplicationContext()).getDaoSession();

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String remoteMessageMethod = remoteMessage.getData().get(KEY_SYNC_REQUEST);
            //!!!!!!!!!!!!!!!!!! Trigger Sync ONLY if the Application is not in background / closed!!!!!!!!!!!!!!!!!
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
