package za.ac.sun.cs.webtracker;

import za.ac.sun.cs.webtracker.database.DatabaseHandler;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

/**
 * A list fragment representing a list of Websites. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link WebsiteDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class WebsiteListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private static WebsitesArrayAdapter waa = null;

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	protected Object mActionMode;

	private int selectedPosition = -1;
	private static View selectedItem = null;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public WebsiteListFragment() {
	}

	public static void notifyChange() {
		if (waa != null) {
			waa.notifyDataSetChanged();
		}
		Log.i("INFO", "notifyChange");
	}

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(int id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(int id) {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("INFO", "onCreate in WebsiteListFragment");
		setHasOptionsMenu(true);

		waa = new WebsitesArrayAdapter(getActivity(), R.id.secondLine,
				DatabaseHandler.getWebsitesList(getActivity()));
		setListAdapter(waa);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}

		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (mActionMode != null) {
					return false;
				}

				selectedPosition = position;

				mActionMode = getActivity()
						.startActionMode(mActionModeCallback);
				selectedItem = view;
				selectedItem.setBackgroundColor(selectedItem.getResources()
						.getColor(R.color.select_blue));
				return true;
			}
		});

	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.delete_context_menu, menu);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.action_delete:
				deleteCurrentItem();
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			selectedItem.setBackgroundColor(selectedItem.getResources()
					.getColor(R.color.white));
			selectedPosition = -1;
			mActionMode = null;
		}
	};

	private void deleteCurrentItem() {
		Log.i("INFO", "deleteCurrentItem : " + selectedPosition);
		String url = DatabaseHandler.getWebsitesList(getActivity())
				.get(selectedPosition).getUrl();
		DatabaseHandler db = new DatabaseHandler(getActivity()
				.getApplicationContext());
		db.deleteWebsite(url);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.i("INFO", "onCreateOptionsMenu in WebsiteListFragment");
		inflater.inflate(R.menu.main_actions, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Log.i("INFO",
				"onOptionsItemSelected in WebsiteListFragment : "
						+ item.getItemId());

		if (item.getItemId() == R.id.action_help) {
			HelpDialogFragment df = new HelpDialogFragment();
			df.show(getFragmentManager(), "HELP");
			return true;
		}
		
		if (item.getItemId() == R.id.action_add) {
			DialogFragment df = new AddWebsiteDialogFragment();
			df.show(getFragmentManager(), "ADD_WEBSITE");
			return true;
		}
		
		if (item.getItemId() == R.id.action_refresh) {
			Log.i("INFO", "action refresh clicked");
			DatabaseHandler db = new DatabaseHandler(getActivity()
					.getApplicationContext());
			db.updateAll();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}
