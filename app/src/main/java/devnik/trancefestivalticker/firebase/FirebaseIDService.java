package devnik.trancefestivalticker.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by niklas on 22.03.18.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh(){
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Registration Token: = "+token);

        sendRegistationToServer(token);
    }
    public void sendRegistationToServer(String token){

    }
}
