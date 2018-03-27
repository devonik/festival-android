package devnik.trancefestivalticker;

import android.accounts.Account;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.greendao.database.Database;

import devnik.trancefestivalticker.model.DaoMaster;
import devnik.trancefestivalticker.model.DaoMaster.DevOpenHelper;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.sync.SyncAdapter;

import static devnik.trancefestivalticker.sync.SyncAdapter.getSyncAccount;


/**
 * Created by nik on 03.03.2018.
 */

public class App extends Application{
    /** A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher. */
    public static final boolean ENCRYPTED = true;

    private DaoSession daoSession;
    private ProgressDialog progressDialog;
    private Account mAccount;
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(getApplicationContext());
        SyncAdapter.initializeSyncAdapter(this);
        FirebaseMessaging.getInstance().subscribeToTopic("news");



        //Initialize Progress Dialog properties

        DevOpenHelper helper = new DevOpenHelper(this, "festival-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();


    }
    public DaoSession getDaoSession() {
        return daoSession;
    }
}
