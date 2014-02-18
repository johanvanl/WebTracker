package za.ac.sun.cs.webtracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import za.ac.sun.cs.webtracker.database.Website;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WebsitesArrayAdapter extends ArrayAdapter<Website> {

	private final Context context;

	public WebsitesArrayAdapter(Context context, int textViewResourceId,
			List<Website> websites) {
		super(context, textViewResourceId, websites);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.websites_list_item, parent,
					false);
		}

		Website website = getItem(position);
		Log.i("INFO", "getView " + website.toString());

		String fn = Integer.toString(website.getID()) + ".ico";
		Log.i("INFO", "getview filename : " + fn);
		File file = getContext().getFileStreamPath(fn);
		if (file.exists()) {
			ImageView image = (ImageView) convertView.findViewById(R.id.icon);
			FileInputStream fis = null;
			try {
				fis = context.openFileInput(fn);
			} catch (FileNotFoundException e) {
			}
			Bitmap bmp = BitmapFactory.decodeStream(fis);
			image.setImageBitmap(bmp);
		}

		TextView textView = (TextView) convertView.findViewById(R.id.firstLine);
		textView.setText(website.getTitle());

		SharedPreferences prefs = context.getSharedPreferences(
				Constants.APP_ID, Context.MODE_PRIVATE);
		boolean isUpdated = prefs.getBoolean(Integer.toString(website.getID()),
				false);

		if (isUpdated) {
			textView.setTextColor(textView.getResources().getColor(
					R.color.green));
		}

		textView = (TextView) convertView.findViewById(R.id.secondLine);
		textView.setText(website.getUrl());

		return convertView;
	}

}
