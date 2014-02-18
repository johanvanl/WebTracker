package za.ac.sun.cs.webtracker;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import za.ac.sun.cs.webtracker.database.DatabaseHandler;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A fragment representing a single Website detail screen. This fragment is
 * either contained in a {@link WebsiteListActivity} in two-pane mode (on
 * tablets) or a {@link WebsiteDetailActivity} on handsets.
 */
public class WebsiteDetailFragment extends Fragment {

	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private int websiteID;
	private String title;
	private List<Integer> times;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public WebsiteDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			Log.i("INFO", "onCreate in WebsiteDetailFragment");
			int position = getArguments().getInt(ARG_ITEM_ID);

			Log.i("INFO", "position : " + position);

			websiteID = DatabaseHandler.getWebsitesList(getActivity())
					.get(position).getID();

			SharedPreferences prefs = getActivity().getSharedPreferences(
					Constants.APP_ID, Context.MODE_PRIVATE);
			prefs.edit().putBoolean(Integer.toString(websiteID), false)
					.commit();

			DatabaseHandler db = new DatabaseHandler(getActivity()
					.getApplicationContext());
			times = db.getTimesForHistoryOfAWebsite(websiteID);
			Collections.reverse(times);

			ActionBar ab = getActivity().getActionBar();
			title = db.getWebsiteTitle(websiteID);
			ab.setTitle(title);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_website_detail,
				container, false);

		String[] list = new String[times.size()];
		for (int i = 0; i < list.length; i++) {
			Date date = new Date(times.get(i) * 1000L);
			String formattedTime = date.toString();
			list[i] = formattedTime;
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, android.R.id.text1, list);

		ListView lv = (ListView) rootView
				.findViewById(R.id.website_detail_listview);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i("INFO", "onItemClick in WebsiteDetailFragment");

				Intent intent = new Intent(getActivity(), BrowserActivity.class);

				DatabaseHandler db = new DatabaseHandler(getActivity()
						.getApplicationContext());

				String revised_html = db.getStoredPageForWebsite(websiteID,
						times.get(position));

				String original_html = "";
				if (position < times.size() - 1) {
					original_html = db.getStoredPageForWebsite(websiteID,
							times.get(position + 1));
				}

				intent.putExtra(BrowserActivity.HTML_ORIGINAL_EXTRA,
						original_html);
				intent.putExtra(BrowserActivity.HTML_REVISED_EXTRA,
						revised_html);
				intent.putExtra(BrowserActivity.TITLE_EXTRA, title);

				startActivity(intent);
			}

		});

		return rootView;
	}
	
}
