package com.scheduler.scheduler;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.scheduler.scheduler.data.MeetingResponse;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class InvitationDetailActivity extends AppCompatActivity {

    MeetingResponse event;
    CheckBox time1,time2,time3,time4,time5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_detail);

        event = (MeetingResponse) getIntent().getSerializableExtra("event");
        TextView textTitle = (TextView) findViewById(R.id.invite_title);
        textTitle.setText(event.getTitle());
        TextView textPriority = (TextView) findViewById(R.id.invite_priority);
        textPriority.setText(event.getPriority());
        TextView textDuration = (TextView) findViewById(R.id.invite_duration);
        textDuration.setText(event.getDuration());
        TextView textLocation = (TextView) findViewById(R.id.invite_location);
        textLocation.setText(event.getLocation());
        TextView textDate = (TextView) findViewById(R.id.invite_date);
        textDate.setText(event.getMeetingDate());

        time1 = (CheckBox) findViewById(R.id.invite_chk_time1);
        time2 = (CheckBox) findViewById(R.id.invite_chk_time2);
        time3 = (CheckBox) findViewById(R.id.invite_chk_time3);
        time4 = (CheckBox) findViewById(R.id.invite_chk_time4);
        time5 = (CheckBox) findViewById(R.id.invite_chk_time5);


        TextView viewTime1 = (TextView) findViewById(R.id.invite_label_time1);
        TextView viewTime2 = (TextView) findViewById(R.id.invite_label_time2);
        TextView viewTime3 = (TextView) findViewById(R.id.invite_label_time3);
        TextView viewTime4 = (TextView) findViewById(R.id.invite_label_time4);
        TextView viewTime5 = (TextView) findViewById(R.id.invite_label_time5);

        if (event.getTime1() != null)
            time1.setText(event.getTime1());
        else {
            time1.setVisibility(View.INVISIBLE);
            viewTime1.setVisibility(View.INVISIBLE);
        }
        if (event.getTime2() != null)
            time2.setText(event.getTime2());
        else {
            time2.setVisibility(View.INVISIBLE);
            viewTime2.setVisibility(View.INVISIBLE);
        }
        if (event.getTime3() != null)
            time3.setText(event.getTime3());
        else {
            time3.setVisibility(View.INVISIBLE);
            viewTime3.setVisibility(View.INVISIBLE);
        }
        if (event.getTime4() != null)
            time4.setText(event.getTime4());
        else {
            time4.setVisibility(View.INVISIBLE);
            viewTime4.setVisibility(View.INVISIBLE);
        }
        if (event.getTime5() != null)
            time5.setText(event.getTime5());
        else {
            time5.setVisibility(View.INVISIBLE);
            viewTime5.setVisibility(View.INVISIBLE);
        }

        Button respondBtn = (Button) findViewById(R.id.button_respond);
        respondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRespondBtnClick();
            }
        });
    }

    public void onRespondBtnClick() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = settings.getString("username", "");

        RespondInviteTask respondInviteTask = new RespondInviteTask(username, event.getMeetingid(),
                time1.isChecked(), time2.isChecked(), time3.isChecked(), time4.isChecked(), time5.isChecked());

        respondInviteTask.execute();

    }

    class RespondInviteTask extends AsyncTask<Void, Void, Boolean> {
        private String username;
        private Long meetingid;
        private boolean time1;
        private boolean time2;
        private boolean time3;
        private boolean time4;
        private boolean time5;

        public RespondInviteTask(String username, Long meetingid, boolean time1, boolean time2, boolean time3, boolean time4, boolean time5) {
            this.username = username;
            this.meetingid = meetingid;
            this.time1 = time1;
            this.time2 = time2;
            this.time3 = time3;
            this.time4 = time4;
            this.time5 = time5;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", username);
            map.add("meetingid", meetingid + "");
            map.add("time1", time1+"");
            map.add("time2", time2+"");
            map.add("time3", time3+"");
            map.add("time4", time4+"");
            map.add("time5", time5+"");

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(Connections.serverUrl + "/acceptInvite", map, String.class);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            finish();
        }
    }
}
