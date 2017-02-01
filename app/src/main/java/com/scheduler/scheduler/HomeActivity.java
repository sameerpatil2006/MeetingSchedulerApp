package com.scheduler.scheduler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.roomorama.caldroid.CaldroidFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.scheduler.scheduler.data.MeetingResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements OnFragmentInteractionListener, OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCreateEventActivity();
            }
        });

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);


        //tabs
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_home) {
                    Fragment homeFragment = new HomeFragment();
                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    t.replace(R.id.container_homepage, homeFragment);
                    t.commit();
                    addCalender();
                } else if (tabId == R.id.tab_invitations) {
                    Fragment invitationFragment = new InvitationsFragment();
                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    t.replace(R.id.container_homepage, invitationFragment);
                    t.commit();
                } else if (tabId == R.id.tab_myevents) {
                    Fragment myEventsFragment = new MyEventsFragment();
                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    t.replace(R.id.container_homepage, myEventsFragment);
                    t.commit();
                }
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = settings.getString("username", "");
        SyncMeetingsTask syncMeetingsTask = new SyncMeetingsTask(username);
        syncMeetingsTask.execute();
    }

    private void addCalender() {
        CaldroidFragment caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);
        ColorDrawable blue = new ColorDrawable(Color.CYAN);

        List<Event> events = CalendarUtil.getCalenderEvents(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getActualMaximum(Calendar.DAY_OF_MONTH), this);

        for (Event event : events) {
            Date eventDate = new Date(Long.parseLong(event.dstart));
            caldroidFragment.setBackgroundDrawableForDate(blue, eventDate);
        }
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar_home, caldroidFragment);
        t.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(MeetingResponse item) {

    }

    private void launchCreateEventActivity() {
        Intent intent = new Intent(this, CreateMeetingActivity.class);
        startActivity(intent);
    }

    class SyncMeetingsTask extends AsyncTask<Void, Void, Boolean> {

        private String username;

        public SyncMeetingsTask(String username) {
            this.username = username;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

            ResponseEntity<MeetingResponse[]> result = restTemplate.exchange(Connections.serverUrl + "/syncMeetings?user=" + username,
                    HttpMethod.GET, new HttpEntity<Object>(httpHeaders), MeetingResponse[].class);

            List<MeetingResponse> items = Arrays.asList(result.getBody());

            for (MeetingResponse meeting : items) {
                CalendarUtil.addEventToCalender(meeting, HomeActivity.this);
            }

            return true;
        }
    }
}
