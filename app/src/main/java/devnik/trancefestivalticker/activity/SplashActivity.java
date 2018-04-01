package devnik.trancefestivalticker.activity;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncStatusObserver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.greendao.annotation.Id;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.api.SyncTicketPhases;
import devnik.trancefestivalticker.model.DaoSession;

import static devnik.trancefestivalticker.sync.SyncAdapter.getSyncAccount;


/**
 * Created by nik on 15.03.2018.
 */

public class SplashActivity extends AppCompatActivity implements SyncStatusObserver
{
    // Instance fields
    private Account mAccount;
    private ProgressBar progressBar;
    // Incoming Intent key for extended data
    public static final String KEY_SYNC_REQUEST =
            "devnik.trancefestivalticker.KEY_SYNC_REQUEST";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_bar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);

        ContentResolver.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE, this);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.devnik_trancefestivalticker_account_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String preferenceAccountName = sharedPref.getString(getString(R.string.devnik_trancefestivalticker_account_name), "");
        mAccount = getSyncAccount(this);

        DaoSession daoSession = ((App) getApplicationContext()).getDaoSession();

        if(getIntent().getExtras() != null ) {
            if(getIntent().getExtras().get(KEY_SYNC_REQUEST) != null) {
                if (getIntent().getExtras().get(KEY_SYNC_REQUEST).equals("sync")) {
                    //The App is called by firebase message while app was in background
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

                }
                else if (getIntent().getExtras().get(KEY_SYNC_REQUEST).equals("newTicketPhase")) {
                    new SyncTicketPhases(daoSession).execute();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }else{
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }else if(preferenceAccountName == ""){
            //Account exestiert noch nicht
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                    /*
                     * Request the sync for the default account, authority, and
                     * manual sync settings
                     */
            ContentResolver.requestSync(mAccount, getApplicationContext().getString(R.string.content_authority), settingsBundle);
            editor.putString(getString(R.string.devnik_trancefestivalticker_account_name), mAccount.name);
            editor.commit();
        }
        else {
            //getIntent().getExtras() is null
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
    @Override
    public void onStatusChanged(int which){
        Log.d("SplashActivity", "onStatusChanged: "+which);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Account account = getSyncAccount(getApplicationContext());
                if (account == null) {
                    // GetAccount() returned an invalid value. This shouldn't happen, but
                    // we'll set the status to "not refreshing".
                    //setRefreshActionButtonState(false);
                    return;
                }

                // Test the ContentResolver to see if the sync adapter is active or pending.
                // Set the state of the refresh button accordingly.
                boolean syncActive = ContentResolver.isSyncActive(account, getApplicationContext().getString(R.string.content_authority));
                boolean syncPending = ContentResolver.isSyncPending(
                        account, getApplicationContext().getString(R.string.content_authority));


                Log.e("TAG", "SYNC PENDING " + syncPending);
                Log.e("TAG", "SYNC ACTIVE " + syncActive);
                if (!syncActive && !syncPending){
                    //@TODO Here a Pending dialog mybe
                    //@TODO This is called so often ? why ?

                    Log.e("TAG", "Sync is finished");
                    //Start Main Activity if synced is finish

                    //Prevent double Intent by FCM, if App is already running
                    if (MainActivity.isAppRunning) {
                        Log.e("SyncFinished", "New Results are not in yet, waitin for restart app");

                    } else {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }else if((syncActive || syncPending) && !MainActivity.isAppRunning){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Daten werden aktualisiert ... ",
                                    Toast.LENGTH_LONG).show();
                        }
                    }, 4000);
                }
            }
        });
    }
}
