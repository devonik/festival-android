package devnik.trancefestivalticker.activity;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.api.SyncTicketPhases;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.FestivalDao;

import static devnik.trancefestivalticker.sync.SyncAdapter.getSyncAccount;


/**
 * Created by nik on 15.03.2018.
 */

public class SplashActivity extends AppCompatActivity// implements SyncStatusObserver
{
    // Incoming Intent key for extended data
    public static final String KEY_SYNC_REQUEST =
            "devnik.trancefestivalticker.KEY_SYNC_REQUEST";
    public static final String ACTION_FINISHED_SYNC = "devnik.trancefestivalticker.ACTION_FINISHED_SYNC";
    private static IntentFilter syncIntentFilter = new IntentFilter(ACTION_FINISHED_SYNC);
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_bar);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);

        DaoSession daoSession1 = ((App) this.getApplication()).getDaoSession();
        FestivalDao festivalDao = daoSession1.getFestivalDao();


        //ContentResolver.addStatusChangeListener(
         //       ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE, this);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.devnik_trancefestivalticker_account_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String preferenceAccountName = sharedPref.getString(getString(R.string.devnik_trancefestivalticker_account_name), "");
        Account mAccount = getSyncAccount(this);

        DaoSession daoSession = ((App) getApplicationContext()).getDaoSession();
        Log.e("INTENT",""+getIntent().getExtras());
        if (preferenceAccountName == "") {
            //Account is new
            if (!isDeviceOnline()) {
                showNoConnectionDialog();
                return;
            }
            //Account exestiert noch nicht
            /*Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);*/
            /*
             * Request the sync for the default account, authority, and
             * manual sync settings
             */
            //ContentResolver.requestSync(mAccount, getApplicationContext().getString(R.string.content_authority), settingsBundle);
            assert mAccount != null;
            editor.putString(getString(R.string.devnik_trancefestivalticker_account_name), mAccount.name);
            editor.apply();
        }
        else if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().get(KEY_SYNC_REQUEST) != null) {

                if (Objects.requireNonNull(getIntent().getExtras().get(KEY_SYNC_REQUEST)).equals("sync")) {
                    //The App is called by firebase message while app was in background
                    //if (isDeviceOnline()) {
                        //Synce die Daten, falls der User netz hat

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
                    /*}
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();*/

                } else if (Objects.requireNonNull(getIntent().getExtras().get(KEY_SYNC_REQUEST)).equals("newTicketPhase")) {
                    new SyncTicketPhases(daoSession).execute();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                //INTENT Extra are not null but no firebase message
                if(festivalDao.queryBuilder().list().size()>0) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    //Device hat keine festivals im lokalen speicher
                    Bundle settingsBundle = new Bundle();
                    settingsBundle.putBoolean(
                            ContentResolver.SYNC_EXTRAS_MANUAL, true);
                    settingsBundle.putBoolean(
                            ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                    ContentResolver.requestSync(getSyncAccount(this), getApplicationContext().getString(R.string.content_authority), settingsBundle);
                }
            }
        }else {
            //Normal open app
            if(festivalDao.queryBuilder().list().size()>0){
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                //Device hat keine festivals im lokalen speicher
                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                ContentResolver.requestSync(getSyncAccount(this), getApplicationContext().getString(R.string.content_authority), settingsBundle);
            }
        }

    }
    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainActivity.isAppRunning) {
                Log.e("SyncFinished", "New Results are not in yet, waitin for restart app");

            } else {
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };
    @Override
    protected void onResume(){
        super.onResume();
        // register for sync
        registerReceiver(syncFinishedReceiver, syncIntentFilter);
        // do your resuming magic
    }
    @Override
    protected void onPause() {
        unregisterReceiver(syncFinishedReceiver);
        super.onPause();
    }

    private void showNoConnectionDialog(){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(SplashActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(SplashActivity.this);
        }
        builder.setTitle("Keine Verbindung")
                .setMessage("Die Daten konnten leider nicht geladen werden. Gehe sicher, dass du eine Internetverbindung hast und starte die App erneut.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(R.drawable.no_internet)
                .show();
    }
    public boolean isDeviceOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
