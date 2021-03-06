package com.assignment.gre.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.assignment.gre.R;
import com.assignment.gre.adapters.RecyclerAdapter;
import com.assignment.gre.backgroundtasks.DataFetcherTask;
import com.assignment.gre.backgroundtasks.ReadDatabaseTask;
import com.assignment.gre.common.Constants;
import com.assignment.gre.common.DatabaseUtil;
import com.assignment.gre.json.JSONHelper;
import com.assignment.gre.listeners.DatabaseListener;
import com.assignment.gre.listeners.GREListListener;
import com.assignment.gre.network.VolleySingleton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentFragment extends Fragment
        implements GREListListener, DatabaseListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //String[] mdataset;
    ArrayList<HashMap<String,Object>> mdataset;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContentFragment newInstance(String param1, String param2) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        //mLayoutManager = new GridLayoutManager(getActivity(),2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        if(DatabaseUtil.isDBEmpty(getActivity()))
            setAdapter();
        else
            new ReadDatabaseTask(this, getActivity().getApplicationContext()).execute();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onContentFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onWordListDownloaded(ArrayList<HashMap<String, Object>> wordlist) {
        setAdapter(wordlist);
    }

    @Override
    public void onDatabaseQueried(ArrayList<HashMap<String, Object>> wordlist) {
        setAdapter(wordlist);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onContentFragmentInteraction(Uri uri);
    }

    private void setAdapter(){

        RequestQueue requestQueue = VolleySingleton.getInstance(getActivity()).getRequestQueue();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.assignmentURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("Response: ", response.toString());
                        mdataset = JSONHelper.jsonParser(response);
                        mAdapter = new RecyclerAdapter(mdataset,getActivity());
                        mRecyclerView.setAdapter(mAdapter);
                        DatabaseUtil.populateDatabase(mdataset,getActivity());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.v("ERROR", error.toString());

                    }
                });

        requestQueue.add(jsObjRequest);
    }

    private void setAdapter(ArrayList<HashMap<String,Object>> wordlist){

        mdataset = wordlist;
        mAdapter = new RecyclerAdapter(mdataset,getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

}
