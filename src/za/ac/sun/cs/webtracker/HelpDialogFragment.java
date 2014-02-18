package za.ac.sun.cs.webtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class HelpDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.i("INFO", "createDialog help");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		builder.setTitle("Welcome to WebTracker");

		View view = inflater.inflate(R.layout.dialog_help, null);

		builder.setView(view);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				HelpDialogFragment.this.getDialog().cancel();
			}
		});

		return builder.create();
	}

}
