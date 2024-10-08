package devnik.trancefestivalticker.service.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.activity.SplashActivity;
import devnik.trancefestivalticker.api.SyncAllData;
import devnik.trancefestivalticker.model.AppInfo;
import devnik.trancefestivalticker.model.AppInfoDao;
import devnik.trancefestivalticker.model.DaoSession;

/*
  Created by niklas on 22.03.18.
 */
/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    // Global variables
    // Define a variable to contain a content resolver instance
    private final ContentResolver mContentResolver;
    // (60 seconds * 24 Hours) = 1440 seconds * 60 = 86400 seconds (24 hours)
    // (60 seconds (1 minute) * 60) / 60 / 60 = 1 hours
    // (60 seconds (1 minute) * 10) / 60 = 10 min
    private static final int SYNC_INTERVAL = 60 * 1440;
    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }
    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
        /*
          If the device's Provider is successfully updated (or is already up-to-date), the method returns normally.

          If the device's Google Play services library is out of date, the method throws GooglePlayServicesRepairableException.
          The app can then catch this exception and show the user an appropriate dialog box to update Google Play services.

          If a non-recoverable error occurs, the method throws GooglePlayServicesNotAvailableException to indicate that it is unable to update the Provider.
          The app can then catch the exception and choose an appropriate course of action, such as displaying the standard fix-it flow diagram.

         */
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(getContext());
        googleApiAvailability.showErrorNotification(getContext(),status);
        Log.e("SyncAdapter", "Starting sync");
        Log.e("SyncAdapter", "onPerformSync for account[" + account.name + "], extras ["+extras+"], "+
                "authority ["+authority+"], provider ["+provider+"], syncResult ["+syncResult+"]");
        DaoSession daoSession = ((App) getContext()).getDaoSession();
        try{
            new SyncAllData(daoSession, getContext());
            AppInfoDao appInfoDao = daoSession.getAppInfoDao();
            AppInfo appInfo = appInfoDao.queryBuilder().where(AppInfoDao.Properties.Id.eq(1L)).build().unique();
            if(appInfo == null){
                //User is new - new Meta Info needed
                Log.e("new App","test");
                appInfo = new AppInfo();
                appInfo.setId(1L);
            }
            SimpleDateFormat readDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN);
            readDate.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
            appInfo.setLastSync(readDate.format(new Date()));
            Log.e("App is getting sync",readDate.format(new Date()));
            appInfoDao.insertOrReplace(appInfo);
            /*SharedPreferences sharedPref = getContext().getSharedPreferences(
                    getContext().getPackageName() + "_preferences",
                    Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
            sharedPrefEditor.putString(getContext().getString(R.string.devnik_trancefestivalticker_preference_last_sync), DateFormat.format("dd.MM.yyyy HH:mm:ss",new Date()).toString());
            sharedPrefEditor.apply();*/
        }catch(Exception e){
            Log.e("SyncAdapter", "Cant get Data: "+e);
        }
        getContext().sendBroadcast(new Intent(SplashActivity.ACTION_FINISHED_SYNC));
    }
    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    private static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        assert accountManager != null;
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Log.e("onAccountCreated","onAccountCreated");
        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        //SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.addPeriodicSync(newAccount,
                context.getString(R.string.content_authority), new Bundle(), SYNC_INTERVAL);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
