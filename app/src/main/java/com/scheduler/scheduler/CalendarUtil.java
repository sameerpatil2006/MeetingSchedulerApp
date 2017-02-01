package com.scheduler.scheduler;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.scheduler.scheduler.data.MeetingResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarUtil {
    public static List<Event> getCalenderEvents(int year, int month, int day, AppCompatActivity activity) {

        List<Event> events = new ArrayList<>();

        ///check calender read permission
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CALENDAR},
                    1);

        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1, 0, 0, 0);
        long startDay = calendar.getTimeInMillis();
        calendar.set(year, month, day, 23, 59, 59);
        long endDay = calendar.getTimeInMillis();

        String[] projection = new String[]{BaseColumns._ID, CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART};
        String selection = CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTSTART + "<= ?";
        String[] selectionArgs = new String[]{Long.toString(startDay), Long.toString(endDay)};

        try {
            Cursor cur = activity.getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null);

            while (cur.moveToNext()) {
                Event e = new Event(cur.getString(1), cur.getString(2));
                events.add(e);
            }
            cur.close();

        } catch (SecurityException ex) {
            Log.e("HomeActivity", "Requires permission to get calender events", ex);
        }

        return events;
    }

    public static void addEventToCalender(MeetingResponse meetingResponse, AppCompatActivity activity) {
        String meetingDate[] = meetingResponse.getMeetingDate().split("/");
        Calendar beginTime = Calendar.getInstance();
        int day = Integer.parseInt(meetingDate[0]);
        int month = Integer.parseInt(meetingDate[1]) - 1;
        int year = Integer.parseInt(meetingDate[2]);
        String meetingTime[] = meetingResponse.getFinalTime().split(":");
        int hr = Integer.parseInt(meetingTime[0]);
        int mm = Integer.parseInt(meetingTime[1]);
        beginTime.set(year, month, day, hr, mm);

        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, day, hr + Integer.parseInt(meetingResponse.getDuration()), mm);

        ContentValues event = new ContentValues();
        event.put("calendar_id", getCalendarId(activity.getApplicationContext()));
        event.put("title", meetingResponse.getTitle());

        event.put("description", meetingResponse.getTitle());
        event.put("eventLocation", meetingResponse.getLocation());
        event.put("dtstart", beginTime.getTimeInMillis());
        event.put("dtend", beginTime.getTimeInMillis());
        event.put("allDay", 0);
        event.put("rrule", "FREQ=YEARLY");
        // event.put("eventStatus", 1); //confirmed

        event.put("eventTimezone", "India");
        Uri eventUri = Uri.parse("content://com.android.calendar/events");
        Uri uri = activity.getContentResolver()
                .insert(eventUri, event);
    }

    private static int getCalendarId(Context context) {

        Cursor cursor = null;
        ContentResolver contentResolver = context.getContentResolver();
        Uri calendars = CalendarContract.Calendars.CONTENT_URI;

        String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT,                 // 3
                CalendarContract.Calendars.IS_PRIMARY                     // 4
        };

        int PROJECTION_ID_INDEX = 0;
        int PROJECTION_DISPLAY_NAME_INDEX = 2;
        int PROJECTION_VISIBLE = 4;

        try {
            cursor = contentResolver.query(calendars, EVENT_PROJECTION, null, null, null);
        } catch (SecurityException ex) {
            Log.e("HomeActivity", "Requires permission to get calender events", ex);
            Toast.makeText(context, "Requires permission to get calender events", Toast.LENGTH_SHORT).show();
            return 0;
        }

        if (cursor.moveToFirst()) {
            String calName;
            long calId = 0;
            String visible;

            do {
                calName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX);
                calId = cursor.getLong(PROJECTION_ID_INDEX);
                visible = cursor.getString(PROJECTION_VISIBLE);
                if (visible.equals("1")) {
                    return (int) calId;
                }
                Log.e("Calendar Id : ", "" + calId + " : " + calName + " : " + visible);
            } while (cursor.moveToNext());

            return (int) calId;
        }
        return 1;
    }
}
