package za.ac.sun.cs.webtracker;

import za.ac.sun.cs.webtracker.database.DatabaseHandler;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class AddWebsiteDialogFragment extends DialogFragment {

	EditText new_website_text = null;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.i("INFO", "createDialog add website");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		builder.setTitle("Add a website");

		View view = inflater.inflate(R.layout.dialog_add_website, null);
		new_website_text = (EditText) view
				.findViewById(R.id.edittext_new_website);

		builder.setView(view);
		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String url = new_website_text.getText().toString();
				Log.i("INFO", "Clicked ADD from dialog : " + url);
				if (!url.contains("http")) {
					url = "http://" + url;
				}

				DatabaseHandler db = new DatabaseHandler(getActivity()
						.getApplicationContext());
				db.newWebsite(url);
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AddWebsiteDialogFragment.this.getDialog().cancel();
					}
				});

		return builder.create();
	}

}
