package devnik.trancefestivalticker.activity;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.Helper;
import devnik.trancefestivalticker.model.DaoSession;

import static devnik.trancefestivalticker.sync.SyncAdapter.getSyncAccount;


/**
 * Created by nik on 15.03.2018.
 */

public class SplashActivity extends AppCompatActivity implements SyncStatusObserver
        //implements FestivalApi.FestivalApiCompleted,
                   //FestivalDetailApi.FestivalDetailApiCompleted,
                   //FestivalDetailImagesApi.FestivalDetailImagesApiCompleted
{
    // Instance fields
    Account mAccount;

    private ContentResolver contentResolver;
    // Incoming Intent key for extended data
    public static final String KEY_SYNC_REQUEST =
            "devnik.trancefestivalticker.KEY_SYNC_REQUEST";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE, this);

        //Create the Dummy Account
        //mAccount = CreateSyncAccount(this);
        DaoSession daoSession = ((App) getApplicationContext()).getDaoSession();
        //new FestivalApi(this, daoSession).execute();
        //new FestivalDetailApi(this, daoSession).execute();
        //new FestivalDetailImagesApi(this, daoSession).execute();
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


                Toast.makeText(this, "regular app start", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }else {

            Toast.makeText(this, "regular app start", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
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
