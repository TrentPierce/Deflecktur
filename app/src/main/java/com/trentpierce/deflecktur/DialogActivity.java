package com.trentpierce.deflecktur;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DialogActivity extends Activity {

	Button button_start;
	Button button_disable;
	Button button_cancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		showDialog(0);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this); 
    	LinearLayout layout = new LinearLayout(this);
    	TextView tvMessage = new TextView(this); 
    	layout.setOrientation(LinearLayout.VERTICAL); 
    	layout.addView(tvMessage); 
    	alert.setTitle("Friendly reminder"); alert.setView(layout);
    	alert.setMessage("If you disable this, remember to remain vigilant until all of the collectors have stopped");
    				
    		          //Exit button to close box
    		          alert.setNegativeButton("Disable Reminder",new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog,int id) {
    						dialog.dismiss();
    						SharedPreferences reminderSettings = getSharedPreferences("reminder", 0);
    						// get current counts
    							//boolean reminder = reminderSettings.getBoolean("reminder",true);
    							final SharedPreferences.Editor edit = reminderSettings.edit();
    							edit.putBoolean("reminder",false);
    							edit.commit();
    						    finish();
    						
    					}
    				  });
    		          //Positive button submits answer and checks for activation.
    				  alert.setPositiveButton("Open Deflecktur",new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog,int id) {
    						Intent intentNumberList=new Intent(getApplicationContext(),StartActivity.class);
    			            startActivity(intentNumberList);dialog.dismiss();
        						finish();
    						
    			            dialog.dismiss();
    			            finish();    			        
    					}
    				  });
    				return alert.show();
	}

	
	//Save and restart the app
	 private void savePreferences(String key, boolean value) {
		 //Save booleans to sharedPreferences
         SharedPreferences sharedPreferences = PreferenceManager
                 .getDefaultSharedPreferences(this);
         Editor editor = sharedPreferences.edit();
         editor.putBoolean("unlocked", true);
         editor.putBoolean("ad_pref", true);
         editor.commit();
         //Restart app
         Intent i = getBaseContext().getPackageManager()
                 .getLaunchIntentForPackage( getBaseContext().getPackageName() );
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(i);
         
     }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dialog, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
