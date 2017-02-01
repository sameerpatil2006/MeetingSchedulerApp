package com.scheduler.scheduler;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class CreateMeetingActivity extends AppCompatActivity {

    private static final int REQUEST_READ_CONTACTS = 0;
    ArrayList<String> allEmails;

    CreateMeetingTask createMeetingTask = null;

    EditText editTitle;
    EditText editPriority;
    EditText editDuration;
    EditText editLocation;
    TextView viewDate;
    TextView viewTime1;
    TextView viewTime2;
    TextView viewTime3;
    TextView viewTime4;
    TextView viewTime5;
    AutoCompleteTextView editAttendees;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        editTitle = (EditText) findViewById(R.id.text_title);
        editPriority = (EditText) findViewById(R.id.text_priority);
        editDuration = (EditText) findViewById(R.id.text_duration);
        editLocation = (EditText) findViewById(R.id.text_location);
        viewDate = (TextView) findViewById(R.id.text_date);
        viewTime1 = (TextView) findViewById(R.id.text_time1);
        viewTime2 = (TextView) findViewById(R.id.text_time2);
        viewTime3 = (TextView) findViewById(R.id.text_time3);
        viewTime4 = (TextView) findViewById(R.id.text_time4);
        viewTime5 = (TextView) findViewById(R.id.text_time5);
        editAttendees = (AutoCompleteTextView) findViewById(R.id.text_attendees);

        populateAutoComplete();

    }

    void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        LoadEmailsTask task = new LoadEmailsTask();
        task.execute();
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(editAttendees, "Contacts permissions are needed", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    public void onDatePickerClick(View view) {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void onTimePickerClick(View view) {
        int id = view.getId();
        TimePickerFragment timeFragment = new TimePickerFragment();
        timeFragment.setViewId(id);
        timeFragment.show(getSupportFragmentManager(), "timePicker");

    }

    public void onInviteButtonClick(View view) {

        String title = editTitle.getText().toString();
        String priority = editPriority.getText().toString();
        String duration = editDuration.getText().toString();
        String location = editLocation.getText().toString();
        String date = viewDate.getText().toString();
        String time[] = new String[5];
        time[0] = viewTime1.getText().toString();
        time[1] = viewTime2.getText().toString();
        time[2] = viewTime3.getText().toString();
        time[3] = viewTime4.getText().toString();
        time[4] = viewTime5.getText().toString();
        String attendees = editAttendees.getText().toString();

        StringBuilder timelist = new StringBuilder();
        for (int i = 0; i < time.length; i++) {
            if (time[i].isEmpty())
                break;
            timelist.append(time[i] + ",");
        }
        //delete last comma
        if (timelist.length() > 0)
            timelist.deleteCharAt(timelist.length() - 1);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String organizer = settings.getString("username", "");


        createMeetingTask = new CreateMeetingTask(organizer, title, priority, duration, location, date, timelist.toString(), attendees);
        createMeetingTask.execute();
    }

    private void setDate(int year, int month, int day) {
        viewDate.setText(day + "/" + month + "/" + year);
    }

    private void setTime(int id, int hourOfDay, int minute) {
        TextView timeView = (TextView) findViewById(id);
        timeView.setText(hourOfDay + ":" + minute);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            ((CreateMeetingActivity) getActivity()).setDate(year, month + 1, day);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        int id;

        public void setViewId(int id) {
            this.id = id;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            ((CreateMeetingActivity) getActivity()).setTime(id, hourOfDay, minute);
        }
    }

    public class CreateMeetingTask extends AsyncTask<Void, Void, Boolean> {

        private String mOrganizer;
        private String mTitle;
        private String mPriority;
        private String mDuration;
        private String mLocation;
        private String mDate;
        private String mTime;
        private String mAttendees;

        public CreateMeetingTask(String mOrganizer, String mTitle, String mPriority, String mDuration,
                                 String mLocation, String mDate, String time, String mAttendees) {
            this.mOrganizer = mOrganizer;
            this.mTitle = mTitle;
            this.mPriority = mPriority;
            this.mDuration = mDuration;
            this.mLocation = mLocation;
            this.mDate = mDate;
            this.mTime = time;
            this.mAttendees = mAttendees;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("organizer", mOrganizer);
            map.add("title", mTitle);
            map.add("priority", mPriority);
            map.add("duration", mDuration);
            map.add("location", mLocation);
            map.add("date", mDate);
            map.add("time", mTime);
            map.add("attendees", mAttendees);

            RestTemplate restTemplate = new RestTemplate();

            String result = restTemplate.postForObject(Connections.serverUrl + "/createMeeting", map, String.class);
            if (result != null && result.equals("success"))
                return true;
            else
                return false;


        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success) {
                Toast.makeText(CreateMeetingActivity.this, "Meeting invite created!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CreateMeetingActivity.this, "Error occured!", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }


    class LoadEmailsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {

            allEmails = getContactEmails();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            List<String> emailAddressCollection = getContactEmails();
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(CreateMeetingActivity.this,
                            android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

            editAttendees.setAdapter(adapter);


        }
    }

    public ArrayList<String> getContactEmails() {
        ArrayList<String> emails = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (cur1.moveToNext()) {
                    String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    if (email != null) {
                        emails.add(email);
                    }
                }
                cur1.close();
            }
        }
        return emails;
    }

}
