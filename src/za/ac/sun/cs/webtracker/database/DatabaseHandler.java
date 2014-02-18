package za.ac.sun.cs.webtracker.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import za.ac.sun.cs.webtracker.Constants;
import za.ac.sun.cs.webtracker.WebsiteListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static List<Website> websites = null;

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "webtracker";

	private static final String TABLE_WEBSITES = "websites";
	private static final String KEY_ID = "id";
	private static final String KEY_URL = "url";
	private static final String KEY_TITLE = "title";

	private static final String TABLE_HISTORY = "history";
	private static final String KEY_WEBSITE = "website";
	private static final String KEY_HTML = "html";
	private static final String KEY_TIME = "time";

	private Context context = null;

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	public static List<Website> getWebsitesList(Context context) {
		if (websites == null) {
			DatabaseHandler db = new DatabaseHandler(context);
			websites = db.getWebsites();
		}
		return websites;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String create_table = "CREATE TABLE IF NOT EXISTS " + TABLE_WEBSITES
				+ " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_URL + " TEXT, " + KEY_TITLE + " TEXT);";
		db.execSQL(create_table);
		create_table = "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " ("
				+ KEY_WEBSITE + " INTEGER, " + KEY_HTML + " TEXT, " + KEY_TIME
				+ " INTEGER);";
		db.execSQL(create_table);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

	public void newWebsite(String url) {
		int ID = getWebsiteID(url);
		if (ID > -1) {
			Toast.makeText(context, "The website has already been added!",
					Toast.LENGTH_LONG).show();
			return;
		}

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_URL, url);
		values.put(KEY_TITLE, "");

		db.insert(TABLE_WEBSITES, null, values);
		db.close();

		Utils.saveWebsiteFavicon(url, context);

		ID = getWebsiteID(url);
		websites.add(new Website(ID, url, ""));

		updateWebsite(url);
	}

	private void updateWebsiteTitle(int ID, String title) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, title);

		db.update(TABLE_WEBSITES, values, KEY_ID + " = ?",
				new String[] { Integer.toString(ID) });
		db.close();

		for (Website w : websites) {
			if (w.getID() == ID) {
				w.setTitle(title);
				break;
			}
		}

		WebsiteListFragment.notifyChange();
	}

	private void updateWebsite(String url) {
		Log.i("INFO", "updateWebsite : " + url);
		GetHTML g = new GetHTML();
		g.execute(url);
	}

	private void updateWebsiteAfterPageRetrieved(String currentPage, String url) {
		Log.i("INFO", "updateWebsiteAfterPageRetrieved");
		int websiteID = getWebsiteID(url);
		String latestStoredPage = getLatestStoredPageForWebsite(url);

		if (!currentPage.equals("") && latestStoredPage.equals(currentPage)) {
			return;
		}

		if (currentPage.equals("")) {
			if (getWebsiteTitle(url).equals("")) {
				String title = Utils.getHtmlTitle(currentPage, url);
				updateWebsiteTitle(websiteID, title);
			}
			Log.i("INFO",
					"updateWebsiteAfterPageRetrieved : currentpage = empty");
			return; // XXX
		}

		String title = Utils.getHtmlTitle(currentPage, url);
		updateWebsiteTitle(websiteID, title);

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_WEBSITE, websiteID);
		values.put(KEY_HTML, currentPage);
		Long time = System.currentTimeMillis() / 1000L;
		values.put(KEY_TIME, time.intValue());

		db.insert(TABLE_HISTORY, null, values);
		db.close();

		SharedPreferences prefs = context.getSharedPreferences(
				Constants.APP_ID, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(Integer.toString(websiteID), true).commit();

		WebsiteListFragment.notifyChange();
	}

	private class GetHTML extends AsyncTask<String, Void, String> {

		String url = null;

		@Override
		protected String doInBackground(String... params) {
			url = params[0];
			return Utils.getHtml(url);
		}

		@Override
		protected void onPostExecute(String result) {
			updateWebsiteAfterPageRetrieved(result, url);
		}

	}

	public void updateAll() {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + KEY_URL + " from "
				+ TABLE_WEBSITES + ";", null);

		if (cursor.moveToFirst()) {
			do {
				updateWebsite(cursor.getString(0));
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();

	}

	public void deleteWebsite(String url) {
		int websiteID = getWebsiteID(url);

		if (websiteID < 0) {
			Log.i("INFO", "deleteWebsite websiteID < 0");
			return;
		}

		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_WEBSITES, KEY_ID + " = ?",
				new String[] { Integer.toString(websiteID) });
		db.delete(TABLE_HISTORY, KEY_WEBSITE + " = ?",
				new String[] { Integer.toString(websiteID) });

		db.close();

		for (int i = 0; i < websites.size(); i++) {
			if (websites.get(i).getUrl().equals(url)) {
				websites.remove(i);
				break;
			}
		}

		String fn = Integer.toString(websiteID) + ".ico";
		File file = context.getFileStreamPath(fn);
		if (file.exists()) {
			context.deleteFile(fn);
		}

		WebsiteListFragment.notifyChange();
	}

	/**
	 * Return an empty String if there is no previous stored version or when
	 * something goes wrong.
	 * 
	 * @param url
	 * @return The html
	 */
	public String getLatestStoredPageForWebsite(String url) {
		String out = "";

		int websiteID = getWebsiteID(url);

		if (websiteID < 0) {
			Log.i("INFO", "getLatestStoredPageForWebsite websiteID < 0");
			return out;
		}

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_HISTORY, new String[] { KEY_HTML },
				KEY_WEBSITE + "=?",
				new String[] { Integer.toString(websiteID) }, null, null,
				KEY_TIME, null);
		if (cursor != null && cursor.moveToFirst()) {
			out = cursor.getString(0);
		}

		cursor.close();
		db.close();

		return out;
	}

	public String getStoredPageForWebsite(int ID, int time) {
		String out = "";

		if (ID < 0) {
			Log.i("INFO", "getStoredPageForWebsite ID < 0");
			return out;
		}

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_HISTORY, new String[] { KEY_HTML },
				KEY_WEBSITE + "=? AND " + KEY_TIME + "=?", new String[] {
						Integer.toString(ID), Integer.toString(time) }, null,
				null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			out = cursor.getString(0);
		}

		cursor.close();
		db.close();

		return out;
	}

	public List<Integer> getTimesForHistoryOfAWebsite(int ID) {
		List<Integer> times = new ArrayList<Integer>();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_HISTORY, new String[] { KEY_TIME },
				KEY_WEBSITE + "=?", new String[] { Integer.toString(ID) },
				null, null, KEY_TIME, null);

		if (cursor.moveToFirst()) {
			do {
				times.add(cursor.getInt(0));
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();

		return times;
	}

	public List<Website> getWebsites() {
		List<Website> websites = new ArrayList<Website>();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + KEY_ID + "," + KEY_URL + ","
				+ KEY_TITLE + " from " + TABLE_WEBSITES + ";", null);

		if (cursor.moveToFirst()) {
			do {
				websites.add(new Website(cursor.getInt(0), cursor.getString(1),
						cursor.getString(2)));
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();

		return websites;
	}

	public int getWebsiteID(String url) {
		Log.i("INFO", "getWebsiteID");
		int ID = -1;

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_WEBSITES, new String[] { KEY_ID },
				KEY_URL + "=?", new String[] { url }, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			ID = cursor.getInt(0);
		}

		cursor.close();
		db.close();

		return ID;
	}

	public String getWebsiteTitle(String url) {
		String title = "";

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_WEBSITES, new String[] { KEY_TITLE },
				KEY_URL + "=?", new String[] { url }, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			title = cursor.getString(0);
		}

		cursor.close();
		db.close();

		return title;
	}

	public String getWebsiteTitle(int ID) {
		String title = "";

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_WEBSITES, new String[] { KEY_TITLE },
				KEY_ID + "=?", new String[] { Integer.toString(ID) }, null,
				null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			title = cursor.getString(0);
		}

		cursor.close();
		db.close();

		return title;
	}

}
