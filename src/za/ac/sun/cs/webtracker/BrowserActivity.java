package za.ac.sun.cs.webtracker;

import org.outerj.daisy.diff.DaisyDiff;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class BrowserActivity extends Activity {

	public static String HTML_ORIGINAL_EXTRA = "browser_activity_html_original_extra";
	public static String HTML_REVISED_EXTRA = "browser_activity_html_revised_extra";
	public static String TITLE_EXTRA = "browser_activity_title_extra";

	private String original_html;
	private String revised_html;

	private String title;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser);

		Intent intent = getIntent();
		original_html = intent.getStringExtra(HTML_ORIGINAL_EXTRA);
		revised_html = intent.getStringExtra(HTML_REVISED_EXTRA);
		title = intent.getStringExtra(TITLE_EXTRA);

		ActionBar ab = getActionBar();
		ab.setHomeButtonEnabled(true);
		ab.setTitle(title);

		WebView wv = (WebView) findViewById(R.id.web_view);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setBuiltInZoomControls(true);

		if (original_html.equals("")) {
			wv.loadDataWithBaseURL("", revised_html, "text/html", "UTF-8", "");
			return;
		}

		DaisyDiff dd = new DaisyDiff();
		dd.diffTwoStrings(original_html, revised_html);
		wv.loadDataWithBaseURL("", dd.getHTML(), "text/html", "UTF-8", "");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browser, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		onBackPressed();
		return true;
	}

}
