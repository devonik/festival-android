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

public class SplashActivity extends AppCompatActivity
        implements FestivalApi.FestivalApiCompleted,
                   FestivalDetailApi.FestivalDetailApiCompleted,
                   FestivalDetailImagesApi.FestivalDetailImagesApiCompleted
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaoSession daoSession = ((App) getApplicationContext()).getDaoSession();
        new FestivalApi(this, daoSession).execute();
        new FestivalDetailApi(this, daoSession).execute();
        new FestivalDetailImagesApi(this, daoSession).execute();


    }
    @Override
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
    }
}
