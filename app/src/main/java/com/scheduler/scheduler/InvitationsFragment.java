package com.scheduler.scheduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scheduler.scheduler.data.MeetingResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InvitationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InvitationsFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    public static List<MeetingResponse> items = new ArrayList<>();
    RecyclerView recyclerView;

    public InvitationsFragment() {
    }

    public static InvitationsFragment newInstance(String param1, String param2) {
        InvitationsFragment fragment = new InvitationsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String username = settings.getString("username", "");

        LoadInvitationsTask task = new LoadInvitationsTask(username);
        task.execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_myevents_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new EventRecyclerViewAdapter(items, mListener));

            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(getActivity(), InvitationDetailActivity.class).putExtra("event", items.get(position));
                    startActivity(intent);
                }

                @Override
                public void onLongItemClick(View view, int position) {
                }
            }));

        }
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    class LoadInvitationsTask extends AsyncTask<Void, Void, Boolean> {

        String username;

        public LoadInvitationsTask(String username) {
            this.username = username;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

                ResponseEntity<MeetingResponse[]> result = restTemplate.exchange(Connections.serverUrl + "/getInvitations?user=" + username,
                        HttpMethod.GET, new HttpEntity<Object>(httpHeaders), MeetingResponse[].class);

                items = Arrays.asList(result.getBody());
                InvitationsFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshList();
                    }
                });

            } catch (HttpStatusCodeException ex) {
                ex.printStackTrace();
                return false;
            }

            return true;
        }

        void refreshList() {
            recyclerView.setAdapter(new EventRecyclerViewAdapter(items, mListener));
        }

    }
}
