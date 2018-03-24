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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
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
        Log.e("SplashActivity","Bundle: "+getIntent().getExtras());
        if(getIntent().getExtras() != null ) {
            if(getIntent().getExtras().get(KEY_SYNC_REQUEST) != null) {
                if (getIntent().getExtras().get(KEY_SYNC_REQUEST).equals("sync")) {
                    //The App is called by firebase message while app was in background
                    Log.e("SplashActivity", "Bundle: " + getIntent().getExtras().get(KEY_SYNC_REQUEST));
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
            }else{
                //getIntent().getExtras().get(KEY_SYNC_REQUEST) is null

                Toast.makeText(this, "regular app start", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }else if(preferenceAccountName == ""){
            //Account exestiert noch nicht
            Log.e("Account", "is New: Trigger Sync");
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
            Toast.makeText(this, "regular app start", Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(this, MainActivity.class);
            //startActivity(intent);
            //finish();
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        Log.e("SplashActivity", "isStarted");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("SplashActivity", "Stop");
    }
    @Override
    public void onStatusChanged(int which){
        Log.d("SplashActivity", "onStatusChanged: "+which);
        Log.e("TAG", "Sync Status " + which);
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
                if (!syncActive && !syncPending){
                    //@TODO Here a Pending dialog mybe
                    //@TODO This is called so often ? why ?

                    Log.e("TAG", "Sync is finished");
                    //Start Main Activity if synced is finish

                    //Prevent double Intent by FCM, if App is already running
                    if (MainActivity.isAppRunning) {
                        Log.e("SyncFinished", "New Results are not in yet, waitin for restart app");
                    } else {
                        // Create the dummy account
                        Toast.makeText(getApplicationContext(), "app is called by fcm", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }



                }
                else {

                }
                //setRefreshActionButtonState(syncActive || syncPending);
            }
        });
    }
    /*@Override
    public void onFestivalApiCompleted(){

    }
    @Override
    public void onFestivalDetailApiCompleted(){

    }
    @Override
    public void onFestivalDetailImagesApiCompleted(){
        Toast.makeText(this,"onFestivalDetailImagesApiCompleted",Toast.LENGTH_SHORT).show();
    }*/

}
