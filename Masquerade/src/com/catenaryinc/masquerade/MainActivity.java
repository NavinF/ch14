package com.catenaryinc.masquerade;

import org.json.JSONObject;

import com.firebase.client.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.*;



public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
	private static final String firebase_base = "https://masquerades.firebaseio.com";
	Firebase myFirebaseRef;
	static volatile String u_id;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase(firebase_base+"/new/");
        final String temp_id = "s_"+Math.abs(new Random().nextLong());
        Map<String, Object> request_id = new HashMap<String, Object>();
        request_id.put("approve", false);
        request_id.put("latitude", "-1000");
        request_id.put("longitude", "-1000");
        request_id.put("temp_id", temp_id);
        request_id.put("time_stamp", System.currentTimeMillis());
        request_id.put("type", 0);
        
        myFirebaseRef.child("unprocessed").child(temp_id).setValue(request_id);
        myFirebaseRef.child("processed").child(temp_id).addValueEventListener(new ValueEventListener() {
        	  @Override
        	  public void onDataChange(DataSnapshot snapshot) {
        		 final Map<String, Object> snap = (Map<String, Object>)snapshot.getValue();
				if (snap != null)
					System.out.println();
				if (snap != null) {
					u_id = (String) snap.get("u_id");
					snapshot.getRef().removeValue();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							((Button) findViewById(R.id.btnSend)).setEnabled(true);
						}
					});
					final Firebase myFirebaseRef = new Firebase(firebase_base);
					myFirebaseRef.child("users").child(u_id.substring(0, 9)).child("server")
							.addValueEventListener(new ValueEventListener() {

								@Override
								public void onDataChange(DataSnapshot arg0) {
									System.out.println("qq "+arg0.getValue());

								}

								@Override
								public void onCancelled(FirebaseError arg0) {
									// TODO Auto-generated
									// method stub

								}
							});
				}
			}

			@Override
			public void onCancelled(FirebaseError error) {
			}
		});
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	Intent intent = new Intent(this, SettingsActivity.class);
        	startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final ListView log = (ListView)rootView.findViewById(R.id.listView1);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1);
            log.setAdapter(adapter);
            Button send = (Button)rootView.findViewById(R.id.btnSend);
            send.setOnClickListener(new OnClickListener() {
            	final Firebase myFirebaseRef = new Firebase(firebase_base);
				@Override
				public void onClick(View v) {
					EditText editText = (EditText)rootView.findViewById(R.id.textInput);
					String text = editText.getText().toString();
					Map<String, Object> text_submission = new HashMap<String, Object>();
					text_submission.put("body", text);
					myFirebaseRef.child("users/"+MainActivity.u_id+"/user").push().setValue(text_submission);
					adapter.add(text);
					editText.setText("");
				}
			});
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
