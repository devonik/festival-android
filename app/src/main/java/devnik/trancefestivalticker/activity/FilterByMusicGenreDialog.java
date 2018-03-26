package devnik.trancefestivalticker.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.MusicGenre;
import devnik.trancefestivalticker.model.MusicGenreDao;

/**
 * Created by niklas on 26.03.18.
 */

public class FilterByMusicGenreDialog extends Spinner implements DialogInterface.OnMultiChoiceClickListener {

    public interface filterByMusicGenreDialogListener{
        public void onOkay(List<MusicGenre> selectedMusicGenres);
        public void onCancel();
    }
    private DaoSession daoSession;
    private MusicGenreDao musicGenreDao;
    private List<MusicGenre> musicGenres;
    private List<MusicGenre> checkedMusicGenres;

    ArrayAdapter<MusicGenre> simple_adapter;

    public FilterByMusicGenreDialog(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        simple_adapter = new ArrayAdapter<MusicGenre>(context, android.R.layout.)
    }
    public void initAlterBuilder(){
        daoSession = ((App)getActivity().getApplicationContext()).getDaoSession();
        musicGenreDao = daoSession.getMusicGenreDao();
        musicGenres = musicGenreDao.queryBuilder().build().list();
        ArrayList<String> strings = new ArrayList<>();
        for (MusicGenre item: musicGenres) {
            strings.add(item.getName());
        }
        Cursor cursor = musicGenreDao.queryBuilder().buildCursor().query();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Filter nach Music Genre")
                .setMultiChoiceItems(musicGenres,checkedMusicGenres,
                        new DialogInterface.OnMultiChoiceClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked){

                            }
                        })

    }

}
