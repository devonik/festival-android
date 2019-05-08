package devnik.trancefestivalticker.helper;

import android.media.AudioManager;
import android.view.KeyEvent;

public interface IActivityListeners {
    void onDispatchKeyEvent(KeyEvent event, AudioManager audioManager);
    void onBackPressed();
}
