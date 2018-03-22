package devnik.trancefestivalticker.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.api.FestivalApi;
import devnik.trancefestivalticker.api.FestivalDetailApi;
import devnik.trancefestivalticker.api.FestivalDetailImagesApi;
import devnik.trancefestivalticker.model.DaoSession;

/**
 * Created by nik on 15.03.2018.
 */

public class SplashActivity extends AppCompatActivity
        //implements FestivalApi.FestivalApiCompleted,
                   //FestivalDetailApi.FestivalDetailApiCompleted,
                   //FestivalDetailImagesApi.FestivalDetailImagesApiCompleted
{
    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.example.android.datasync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "devnik.festivalticker";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaoSession daoSession = ((App) getApplicationContext()).getDaoSession();
        //new FestivalApi(this, daoSession).execute();
        //new FestivalDetailApi(this, daoSession).execute();
        //new FestivalDetailImagesApi(this, daoSession).execute();

        // Create the dummy account
    }
    /*@Override
    public void onFestivalApiCompleted(){

    }
    @Override
    public void onFestivalDetailApiCompleted(){
        Toast.makeText(this,"onFestivalDetailApiCompleted",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onFestivalDetailImagesApiCompleted(){
        Toast.makeText(this,"onFestivalDetailImagesApiCompleted",Toast.LENGTH_SHORT).show();
    }*/

}
