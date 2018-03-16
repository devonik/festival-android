package devnik.trancefestivalticker;

import android.app.Application;
import android.app.ProgressDialog;

import org.greenrobot.greendao.database.Database;

import devnik.trancefestivalticker.activity.MainActivity;
import devnik.trancefestivalticker.api.FestivalApi;
import devnik.trancefestivalticker.api.FestivalDetailApi;
import devnik.trancefestivalticker.api.FestivalDetailImagesApi;
import devnik.trancefestivalticker.api.OnTaskCompleted;
import devnik.trancefestivalticker.model.DaoMaster;
import devnik.trancefestivalticker.model.DaoMaster.DevOpenHelper;
import devnik.trancefestivalticker.model.DaoSession;


/**
 * Created by nik on 03.03.2018.
 */

public class App extends Application{
    /** A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher. */
    public static final boolean ENCRYPTED = true;

    private DaoSession daoSession;
    private ProgressDialog progressDialog;
    @Override
    public void onCreate() {
        super.onCreate();
        //Initialize Progress Dialog properties

        DevOpenHelper helper = new DevOpenHelper(this, "festival-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        new FestivalApi(daoSession).execute();
        new FestivalDetailApi(daoSession).execute();
        new FestivalDetailImagesApi(daoSession).execute();
    }
    public DaoSession getDaoSession() {
        return daoSession;
    }
}
