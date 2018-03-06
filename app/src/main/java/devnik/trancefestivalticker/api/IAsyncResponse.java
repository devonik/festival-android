package devnik.trancefestivalticker.api;

import java.util.ArrayList;

import devnik.trancefestivalticker.model.Festival;

/**
 * Created by nik on 06.03.2018.
 */

public interface IAsyncResponse {
   void processFinish(Festival[] festivals);
}
