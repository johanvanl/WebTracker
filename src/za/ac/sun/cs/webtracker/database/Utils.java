package za.ac.sun.cs.webtracker.database;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import za.ac.sun.cs.webtracker.WebsiteListFragment;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Utils {

	private static class SaveFavicon extends AsyncTask<Void, Void, Void> {

		String url;
		Context context;

		public SaveFavicon(String url, Context context) {
			this.url = url;
			this.context = context;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.i("INFO", "SaveFavicon doInBackground");
			DatabaseHandler db = new DatabaseHandler(context);
			int ID = db.getWebsiteID(url);

			Log.i("INFO", "SaveFavicon doInBackground ID : " + ID);

			URL url_object;
			try {
				url_object = new URL("http://g.etfv.co/" + url);
			} catch (MalformedURLException e) {
				return null;
			}

			InputStream input = null;
			FileOutputStream output = null;
			try {
				String outputName = Integer.toString(ID) + ".ico";

				input = url_object.openConnection().getInputStream();
				output = context.openFileOutput(outputName,
						Context.MODE_PRIVATE);

				int read;
				byte[] data = new byte[1024];
				while ((read = input.read(data)) != -1) {
					output.write(data, 0, read);
				}

			} catch (IOException e) {
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
					}
				}
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			WebsiteListFragment.notifyChange();
		}

	}

	public static void saveWebsiteFavicon(String url, Context context) {
		SaveFavicon f = new SaveFavicon(url, context);
		f.execute();
	}

	/**
	 * Obtain the html source code for a specific url.
	 * 
	 * @param url
	 *            The url of the page that needs to be fetched.
	 * @return The source code or and empty String if any problems occured.
	 */
	public static String getHtml(String url) {
		URL url_object = null;
		try {
			url_object = new URL(url);
		} catch (MalformedURLException e) {
			Log.i("INFO", "getHtml url catch");
			return "";
		}

		boolean timeout = false;
		InputStream is = null;
		try {
			HttpURLConnection huc = (HttpURLConnection) url_object
					.openConnection();
			huc.setInstanceFollowRedirects(true);
			huc.setConnectTimeout(10 * 1000); // 10 seconds
			huc.connect();
			is = huc.getInputStream();
		} catch (IOException e) {
			timeout = true;
			Log.i("INFO", "getHtml inputstream catch can be a timeout");
		}

		if (timeout) {
			return "";
		}

		StringBuilder html = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line = br.readLine();
			while (line != null) {
				html.append(line);
				html.append('\n');
				line = br.readLine();
			}
		} catch (IOException e) {
			Log.i("INFO", "getHtml bufferedreader catch : " + e.getMessage());
			html = new StringBuilder("");
		} finally {
			try {
				is.close();
			} catch (IOException e1) {
			}
			try {
				br.close();
			} catch (IOException e) {
			}
		}

		return html.toString();
	}

	public static String getHtmlTitle(String html, String url) {
		if (!html.equals("")) {
			Pattern p = Pattern.compile("<title>(.*?)</title>");
			Matcher m = p.matcher(html);
			if (m.find()) {
				String title = m.group(1);
				if (!title.equals("")) {
					return title;
				}
			}
		}

		if (url.contains("/")) {
			String[] split = url.split("/");
			return split[split.length - 1];
		}

		return url;
	}

}
