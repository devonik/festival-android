package devnik.trancefestivalticker.firebase;

import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static devnik.trancefestivalticker.sync.SyncAdapter.getSyncAccount;

/**
 * Created by niklas on 22.03.18.
 */

public class FirebaseMsgService extends FirebaseMessagingService {
    private final String TAG = "JSA-FCM";
    // Constants
    // Content provider authority
    public static final String AUTHORITY = "devnik.trancefestivalticker.provider";
    // Account type
    public static final String ACCOUNT_TYPE = "devnik.trancefestivalticker";
    // Account
    public static final String ACCOUNT = "default_account";
    // Incoming Intent key for extended data
    public static final String KEY_SYNC_REQUEST =
            "devnik.trancefestivalticker.KEY_SYNC_REQUEST";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: "+remoteMessage.getFrom());



        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (remoteMessage.getData().get(KEY_SYNC_REQUEST).toString().equals("sync")) {
                ContentResolver.requestSync(getSyncAccount(this), AUTHORITY, new Bundle());
                Log.d(TAG, "SyncAdapter triggered: ");
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }

        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }
}
