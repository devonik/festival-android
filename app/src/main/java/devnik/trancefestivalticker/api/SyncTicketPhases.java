package devnik.trancefestivalticker.api;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalTicketPhaseDao;

public class SyncTicketPhases extends AsyncTask<Void, Void, FestivalTicketPhase[]> {

        private final FestivalTicketPhaseDao festivalTicketPhaseDao;
        public SyncTicketPhases(DaoSession daoSession){
            festivalTicketPhaseDao = daoSession.getFestivalTicketPhaseDao();
        }
        @Override
        protected FestivalTicketPhase[] doInBackground(Void... params) {
            try {
                String url = "https://festivalticker.herokuapp.com/api/v1/ticketPhase";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                FestivalTicketPhase[] festivalTicketPhases = restTemplate.getForObject(url, FestivalTicketPhase[].class);
                if(festivalTicketPhases.length > 0){
                    updateSQLite(festivalTicketPhases);
                }
                return festivalTicketPhases;
            }
            catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        //Completion Handler
        @Override
        protected void onPostExecute(FestivalTicketPhase[] festivalTicketPhases) {
        }
        private void updateSQLite(FestivalTicketPhase[] festivalTicketPhases){
            for(FestivalTicketPhase remoteItem : festivalTicketPhases){
                FestivalTicketPhase existingFestivalTicketPhase = festivalTicketPhaseDao.queryBuilder()
                        .where(FestivalTicketPhaseDao.Properties.Festival_ticket_phase_id
                                .eq(remoteItem.getFestival_ticket_phase_id()))
                        .unique();
                if(existingFestivalTicketPhase != null){
                    //ticket exist in sqlite
                    if(!existingFestivalTicketPhase.equals(remoteItem)){
                        festivalTicketPhaseDao.update(remoteItem);
                    }
                }
                if(existingFestivalTicketPhase == null){
                    //new Item
                    festivalTicketPhaseDao.insert(remoteItem);
                }
            }
        }
    }
