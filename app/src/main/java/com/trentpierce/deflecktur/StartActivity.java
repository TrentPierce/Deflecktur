package com.trentpierce.deflecktur;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

public class StartActivity extends ActionBarActivity {

	Button buttonCallForwardOn;
	 Button buttonCallForwardOff;
	 Button buttonBRTest;
	 String OGVM;
    String fCode;
	private int mId;
	boolean isRunning;
    boolean firstRun;
    int currentChoice = -1;
	
	 //TODO: option to disable call receiver. Option to add numbers to block list.  Create a homescreen toggle widget.
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

        SharedPreferences persistentSettings = getSharedPreferences("persistent", 0);
        boolean persistent = persistentSettings.getBoolean("persistent",true);

        SharedPreferences dialogSaveSettings = getSharedPreferences("currentChoice", 0);
        int currentChoice = dialogSaveSettings.getInt("currentChoice", 0);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String fCode = preferences.getString("fCode", "");
        if(!fCode.equalsIgnoreCase(""))
        {
            fCode = "*67*";  /* Edit the value here*/
        }

        SharedPreferences runningSettings = getSharedPreferences("isRunning", 0);
        boolean isRunning;
        if (runningSettings.getBoolean("isRunning", true)) isRunning = true;
        else isRunning = false;

        final SharedPreferences firstRunSettings = getSharedPreferences("firstRun", 0);

        if (firstRunSettings.getBoolean("firstRun", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First Run");
            new AlertDialog.Builder(this)
                    .setTitle("Call Forwarding")
                    .setMessage("Do you have call forwarding enabled?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with carrier selection
                            showCarrierPickerDialog();
                            firstRunSettings.edit().putBoolean("firstRun", false).apply();
                            dialog.dismiss();
                        }


                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            //TODO: Make warning toast
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        DisplayMetrics metrics = new DisplayMetrics();
		 getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF6A00")));
		if ((metrics.xdpi * metrics.ydpi) >= DisplayMetrics.DENSITY_HIGH) {
    	actionBar.setDisplayShowTitleEnabled(false);
		} else {
		actionBar.setDisplayShowTitleEnabled(true);
		}
		
		final Animation vanish = AnimationUtils.loadAnimation(this,R.drawable.vanish);
		

//		buttonBRTest = (Button) findViewById(R.id.buttonBRTest);
//		  buttonBRTest.setOnClickListener(new View.OnClickListener()
//		  {
//		   public void onClick(View v)
//		   {
//				SharedPreferences countSettings = getSharedPreferences("CallCount", 0);
//			// get current counts
//				int CallCount = countSettings.getInt("counts",0);
//				
//			   CallCount++;
//				final SharedPreferences.Editor edit = countSettings.edit();
//				edit.putInt("counts",CallCount);
//				edit.commit();
//				Toast.makeText(getApplicationContext(), "Call Count = " + CallCount,   
//		    		    Toast.LENGTH_SHORT).show();  
//				}
//		  });
//		   
		buttonCallForwardOn = (Button) findViewById(R.id.buttonCallForwardOn);
        final String finalFCode = fCode;
        buttonCallForwardOn.setOnClickListener(new View.OnClickListener()
		  {
		   public void onClick(View v)
		   {   
			   SharedPreferences persistentSettings = getSharedPreferences("persistent", 0);
			   boolean persistent = persistentSettings.getBoolean("persistent",false);
					
			   SharedPreferences runningSettings = getSharedPreferences("isRunning", 0);
					if (runningSettings.getBoolean("isRunning", false)) {
						Toast.makeText(getApplicationContext(), "Service is already running...",   Toast.LENGTH_LONG).show();
					} else {
						 findViewById(R.id.buttonCallForwardOn).startAnimation(vanish);
		       callforward(finalFCode + "6826515262"); // (682)651-5262 is the number you want to forward the calls.
						 Toast.makeText(getApplicationContext(), finalFCode,   Toast.LENGTH_LONG).show();

				
				  if (persistentSettings.getBoolean("persistent", true)) {
					    showNotif();
					    } else {
					   //  Toast.makeText(getApplicationContext(), "Service is Running",   Toast.LENGTH_LONG).show();
					    }
					}
		 }

   });
		   //TODO: If service isnt running, dont allow click to send callforward
		  buttonCallForwardOff = (Button) findViewById(R.id.buttonCallForwardOff);
        final String finalFCode1 = fCode;
        buttonCallForwardOff.setOnClickListener(new View.OnClickListener()
		  {
		   public void onClick(View v)
		   {
               final String finalFCode = finalFCode1;
			   SharedPreferences persistentSettings = getSharedPreferences("persistent", 0);
			   boolean persistent = persistentSettings.getBoolean("persistent",true);
					
			   SharedPreferences runningSettings = getSharedPreferences("isRunning", 0);
			   boolean isRunning = runningSettings.getBoolean("isRunning",false);

			   if (isRunning) {
			   findViewById(R.id.buttonCallForwardOff).startAnimation(vanish);
			   // re enable this before release
		       callforward(finalFCode + OGVM);
			   
			  
				final SharedPreferences.Editor edit = runningSettings.edit();
				edit.putBoolean("isRunning",false);
				edit.apply();
			   } else {
				   Toast.makeText(getApplicationContext(), "Service is not running",   Toast.LENGTH_LONG).show();
			   }
				 if (persistent) {
					    hideNotif();
					    } else {
					    	Toast.makeText(getApplicationContext(), "Service Stopped",   Toast.LENGTH_LONG).show();
				}
		   }
		  });
		 }

    private void showCarrierPickerDialog() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        SharedPreferences choiceSettings = getSharedPreferences("currentChoice", 0);
        final int[] currentChoice = {choiceSettings.getInt("currentChoice", 0)};
        final CharSequence[] items = {"AT&T", "Tmobile", "Verizon", "Sprint", "Other"};
        // Decide which carrier, so we can apply the correct forwarding code.
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select your carrier");
        builder.setIcon(R.drawable.ic_launcher);
        builder.setSingleChoiceItems(items, currentChoice[0],
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        // TODO Auto-generated method stub
                        switch (item) {
                            case 0:
                                Toast.makeText(getApplicationContext(),
                                        items[item], Toast.LENGTH_SHORT).show();
                                // Your code when first option seletced
                                currentChoice[0] = 0;
                                editor.putInt(String.valueOf(currentChoice[0]), 0);
                                editor.putString("fCode", "*67*");
                                editor.apply();
                                break;
                            case 1:
                                // Your code when 2nd option seletced
                                Toast.makeText(getApplicationContext(),
                                        items[item], Toast.LENGTH_SHORT).show();
                                currentChoice[0] = 1;
                                editor.putInt(String.valueOf(currentChoice[0]), 0);
                                editor.putString("fCode", "*67*");
                                editor.apply();
                                break;
                            case 2:
                                // Your code when 2nd option seletced
                                Toast.makeText(getApplicationContext(),
                                        items[item], Toast.LENGTH_SHORT).show();
                                currentChoice[0] = 2;
                                editor.putInt(String.valueOf(currentChoice[0]), 0);
                                editor.putString("fCode", "*67*");
                                editor.apply();
                                break;
                            case 3:
                                // Your code when 2nd option seletced
                                Toast.makeText(getApplicationContext(),
                                        items[item], Toast.LENGTH_SHORT).show();
                                currentChoice[0] = 3;
                                editor.putInt(String.valueOf(currentChoice[0]), 0);
                                editor.putString("fCode", "*67*");
                                editor.apply();
                                break;
                            case 4:
                                // Your code when 2nd option seletced
                                Toast.makeText(getApplicationContext(),
                                        items[item], Toast.LENGTH_SHORT).show();
                                currentChoice[0] = 4;
                                editor.putInt(String.valueOf(currentChoice[0]), 0);
                                editor.putString("fCode","*67*");
                                editor.apply();
                                break;
                        }
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void callforward(String callForwardString)
		    {
		        PhoneCallListener phoneListener = new PhoneCallListener();
		        TelephonyManager telephonyManager = (TelephonyManager)
		         this.getSystemService(Context.TELEPHONY_SERVICE);
		        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
		        OGVM = telephonyManager.getVoiceMailNumber();

                Intent intentCallForward = new Intent(Intent.ACTION_CALL);
		        Uri mmiCode = Uri.fromParts("tel", callForwardString + "#", "#");
		        intentCallForward.setData(mmiCode);
		        startActivity(intentCallForward);

                SharedPreferences runningSettings = getSharedPreferences("isRunning", 0);
                final SharedPreferences.Editor edit = runningSettings.edit();
                edit.putBoolean("isRunning",true);
                edit.apply();
            }
		 private class PhoneCallListener extends PhoneStateListener
		 {
		        private boolean isPhoneCalling = false;

		        @Override
		        public void onCallStateChanged(int state, String incomingNumber)
		        {
		            if (TelephonyManager.CALL_STATE_RINGING == state)
		            {
		                // phone ringing
		            }

		            if (TelephonyManager.CALL_STATE_OFFHOOK == state)
		            {
		                // active
		                isPhoneCalling = true;
		            }

		            if (TelephonyManager.CALL_STATE_IDLE == state)
		            {
		                // run when class initial and phone call ended, need detect flag
		                // from CALL_STATE_OFFHOOK
		                if (isPhoneCalling)
		                {
		                    // restart app
		                    Intent i = getBaseContext().getPackageManager()
		                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
		                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		                    startActivity(i);
		                    isPhoneCalling = false;
		                }
		            }
		        }
		    }
		 private void showNotif() {
				// TODO Auto-generated method stub
			 Intent intent = new Intent(this, StartActivity.class);
		        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		        Notification.Builder builder = new Notification.Builder(getApplicationContext());
		        builder.setContentTitle("Deflecktur");
		        builder.setContentText("currently forwarding collection calls");
		        builder.setContentIntent(pendingIntent);
		        builder.setTicker("Deflecktur Started!");
		        builder.setSmallIcon(R.drawable.ic_launcher);
		        builder.setOngoing(true);
		        builder.setAutoCancel(false);
		        builder.setPriority(0);
		        Notification notification = builder.build();
		        NotificationManager notificationManger =
		                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		        notificationManger.notify(01, notification);

			}
		 private void hideNotif() {
				// TODO Auto-generated method stub
			 NotificationManager notifManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			    notifManager.cancel(01);
			}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case (R.id.action_view_list):
			Intent intentNumberList1=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intentNumberList1);
		return true;
		case (R.id.action_blocker_disable):
			if (item.isChecked()) {
			item.setChecked(false);
			SharedPreferences blockerSettings = getSharedPreferences("blocker", 0);
			final SharedPreferences.Editor edit = blockerSettings.edit();
			edit.putBoolean("blocker",true);
			edit.apply();
			Toast.makeText(getApplicationContext(), "Auto Call Blocker Enabled",   Toast.LENGTH_LONG).show();
			} else {
			item.setChecked(true);
			SharedPreferences blockerSettings = getSharedPreferences("blocker", 0);
			final SharedPreferences.Editor edit = blockerSettings.edit();
			edit.putBoolean("blocker",false);
			edit.apply();
			Toast.makeText(getApplicationContext(), "Auto Call Blocker Disabled",   Toast.LENGTH_LONG).show();
			}
		return true;
		case (R.id.action_notif):
			if (item.isChecked()) {
			item.setChecked(false);
			SharedPreferences persistentSettings = getSharedPreferences("persistent", 0);
			final SharedPreferences.Editor edit = persistentSettings.edit();
			edit.putBoolean("persistent",true);
			edit.apply();
			Toast.makeText(getApplicationContext(), "Persistent Notification Enabled",   Toast.LENGTH_LONG).show();
			} else {
			item.setChecked(true);
			SharedPreferences persistentSettings = getSharedPreferences("persistent", 0);
			final SharedPreferences.Editor edit = persistentSettings.edit();
			edit.putBoolean("persistent",false);
			edit.apply();
			Toast.makeText(getApplicationContext(), "Persistent Notification Disabled",   Toast.LENGTH_LONG).show();
			}
		return true;
		case (R.id.action_reminder):
			if (item.isChecked()) {
			item.setChecked(false);
			SharedPreferences reminderSettings = getSharedPreferences("reminder", 0);
			final SharedPreferences.Editor edit = reminderSettings.edit();
			edit.putBoolean("reminder",true);
			edit.apply();
			Toast.makeText(getApplicationContext(), "Reminder Notification Enabled",   Toast.LENGTH_LONG).show();
			} else {
			item.setChecked(true);
			SharedPreferences reminderSettings = getSharedPreferences("reminder", 0);
			final SharedPreferences.Editor edit = reminderSettings.edit();
			edit.putBoolean("reminder",false);
			edit.apply();
			Toast.makeText(getApplicationContext(), "Reminder Notification Disabled",   Toast.LENGTH_LONG).show();
			}
		return true;
            case (R.id.action_carrier_picker):
                showCarrierPickerDialog();
                return true;
		default:
            return super.onOptionsItemSelected(item);
	}
  }
}
