package com.muzima.view;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.PopupMenu;

import com.muzima.R;
import com.muzima.view.forms.RegistrationFormsActivity;
import com.muzima.view.preferences.SettingsActivity;

public class FormListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_form_list);
		getOverflowMenu();

		ActionBar actionBar = getActionBar();
		// actionBar.hide();
		actionBar.setDisplayShowTitleEnabled(true);
		// actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		// View cView
		// =getLayoutInflater().inflate(R.layout.actionbar_dashboard,null);
		// actionBar.setCustomView(cView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dashboard, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.action_settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_help:
			intent = new Intent(this, HelpActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_logout:
			intent = new Intent(this, LogoutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void getOverflowMenu() {

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			java.lang.reflect.Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause(){
		super.onPause();
	}


	/** Called when the user clicks the Forms area */
	public void formsList(View view) 
	{
		Intent intent = new Intent(this, FormListActivity.class);
		startActivity(intent);
	}

	
	/** Called when the user clicks the Register Client Button */
	public void registerClient(View view) 
	{
		Intent intent = new Intent(this, RegistrationFormsActivity.class);
		startActivity(intent);
	}
	
	public void showForms(View v) {
	    PopupMenu popup = new PopupMenu(this, v);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.form_list, popup.getMenu());
	    popup.show();
	}

}
