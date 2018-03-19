package devnik.trancefestivalticker.activity;

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

public class SplashActivity extends AppCompatActivity implements FestivalApi.FestivalApiCompleted,FestivalDetailApi.FestivalDetailApiCompleted {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaoSession daoSession = ((App) getApplicationContext()).getDaoSession();
        new FestivalApi(this,daoSession).execute();
        new FestivalDetailApi(this, daoSession).execute();
        new FestivalDetailImagesApi(daoSession).execute();

    }
    @Override
    public void onFestivalApiCompleted(){
        Toast.makeText(getApplicationContext(), "Festival Api Completed!", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onFestivalDetailApiCompleted(){
        Toast.makeText(getApplicationContext(), "Festival Detail Api Completed!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
