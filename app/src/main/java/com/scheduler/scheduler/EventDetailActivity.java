package com.scheduler.scheduler;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.scheduler.scheduler.data.MeetingResponse;
import com.scheduler.scheduler.data.UserResponse;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    MeetingResponse event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        event = (MeetingResponse) getIntent().getSerializableExtra("event");
        TextView textTitle = (TextView) findViewById(R.id.detail_title);
        textTitle.setText(event.getTitle());
        TextView textPriority = (TextView) findViewById(R.id.detail_priority);
        textPriority.setText(event.getPriority());
        TextView textDuration = (TextView) findViewById(R.id.detail_duration);
        textDuration.setText(event.getDuration());
        TextView textLocation = (TextView) findViewById(R.id.detail_location);
        textLocation.setText(event.getLocation());
        TextView textDate = (TextView) findViewById(R.id.detail_date);
        textDate.setText(event.getMeetingDate());
        TextView textPreferredTime = (TextView) findViewById(R.id.detail_preferred_time);
        textPreferredTime.setText(event.getPreferredTime());

        if(event.isFinalized()){
            TextView lblPreferredTime = (TextView) findViewById(R.id.lbl_most_preferred_time);
            lblPreferredTime.setText("Finalized Time");
            textPreferredTime.setText(event.getFinalTime());
        }

        List<String> attendees = new ArrayList<>();

        for (UserResponse response : event.getAttendees()) {
            String accept = response.getFullname();
            if (response.isTime1())
                accept += " " + event.getTime1();
            if (response.isTime2())
                accept += " " + event.getTime2();
            if (response.isTime3())
                accept += " " + event.getTime3();
            if (response.isTime4())
                accept += " " + event.getTime4();
            if (response.isTime5())
                accept += " " + event.getTime5();
            attendees.add(accept);
        }

        ListView listAttendees = (ListView) findViewById(R.id.attendees_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EventDetailActivity.this, android.R.layout.simple_list_item_1, attendees);
        listAttendees.setAdapter(adapter);


        Button btnFinalize = (Button) findViewById(R.id.btn_finalize);
        btnFinalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFinalizeBtnClick();
            }
        });
        if(event.isFinalized()){
            btnFinalize.setVisibility(View.INVISIBLE);
        }

    }

    public void onFinalizeBtnClick() {
        FinalizeMeetingTask finalizeMeetingTask = new FinalizeMeetingTask(event.getMeetingid());
        finalizeMeetingTask.execute();

    }

    public class FinalizeMeetingTask extends AsyncTask<Void, Void, Boolean> {
        Long meetingid;

        FinalizeMeetingTask(Long meetingid) {
            this.meetingid = meetingid;
        }


        @Override
        protected Boolean doInBackground(Void... voids) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("meetingid", meetingid + "");

            RestTemplate restTemplate = new RestTemplate();

            restTemplate.postForObject(Connections.serverUrl + "/finalize", map, String.class);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            finish();
        }
    }
}
