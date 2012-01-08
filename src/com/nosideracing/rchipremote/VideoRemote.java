package com.nosideracing.rchipremote;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class VideoRemote extends Activity implements OnClickListener {

	private Button button_quit;
	private Button button_mute;
	private Button button_pause;
	private Button button_foward;
	private Button button_rewind;
	private CompoundButton button_fullscreen;
	private CompoundButton button_playpause;
	private TextView topText;
	private boolean firstPlay;
	private String loc;
	private String showString;
	private String destHost;
	private String phoneNumber;
	private long ID = -1;
	private JSON jSon;
	PowerManager.WakeLock wl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.watch);
		Log.d(Consts.LOG_TAG, "onCreate: VideoRemote");
		jSon = new JSON(getApplicationContext());
		Bundle incoming = getIntent().getExtras();
		showString = incoming.getString("showString");
		ID = incoming.getLong("showID");
		loc = incoming.getString("Location");
		destHost = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getString("serverhostname", "Tomoya");
		TelephonyManager tManager = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		phoneNumber = tManager.getLine1Number();
		/* If there is no phone number, we use (111) 111-1111 */
		if (phoneNumber == null) {
			phoneNumber = "1111111111";
		}
		topText = (TextView) findViewById(R.id.MSWMTopText);
		topText.setText(showString);
		topText.setSelected(true);
		firstPlay = true;
		createButtons();
		// bind to the service

	}

	public void onPause() {
		super.onPause();
		Log.d(Consts.LOG_TAG, "onPause: MS_Watch_Movie");
	}

	public void onResume() {
		super.onResume();
		Log.d(Consts.LOG_TAG, "onResume: MS_Watch_Movie");
		wl = (WakeLock) ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
						| PowerManager.ON_AFTER_RELEASE, "VideoRemote");
		wl.acquire();
		set_sizes();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(Consts.LOG_TAG, "onStop: MS_Watch_Movie");
		wl.release();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		quit(false);
		Log.d(Consts.LOG_TAG, "onDestroy msmusic");
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu_child, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(Consts.LOG_TAG, "onOptionsItemSelected: rchip");
		int calledMenuItem = item.getItemId();
		if (calledMenuItem == R.id.settings) {
			startActivity(new Intent(this, Preferences.class));
			return true;
		} else if (calledMenuItem == R.id.quit) {
			quit(true);
			return true;
		} else if (calledMenuItem == R.id.wifiset) {
			SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext())
					.edit();
			editor.putString("internalnetname",
					((WifiManager) getSystemService(Context.WIFI_SERVICE))
							.getConnectionInfo().getSSID());
			editor.commit();
			return true;
		}
		return false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// See which child activity is calling us back.
		if (resultCode == Consts.QUITREMOTE) {
			quit(true);
		}
	}

	public void onClick(View v) {
		CompoundButton btn = (ToggleButton) v;
		if (btn.getId() == R.id.MSWMfullscreen) {
			if (btn.isChecked()) {
				new runCmd().execute("FULLONSM", "");
			} else {
				new runCmd().execute("FULLONSM", "");
			}
		} else if (btn.getId() == R.id.MSWMplaypause) {

			if (btn.isChecked()) {
				if (firstPlay) {
					String rootPath = new JSON(getApplicationContext())
							.getRootPath();
					try {
						// rootPath = jSon.getRootValue(destHost);
						Log.d(Consts.LOG_TAG, "hostname:"
								+ RemoteMain.msb_desthost);
						Log.d(Consts.LOG_TAG, "rootpath:" + rootPath);
						loc = loc.replace("/mnt/raid/", rootPath);
						Log.d(Consts.LOG_TAG, "Location:" + loc);
						new runCmd().execute("OPENSM", loc);
						firstPlay = false;
					} catch (Exception e) {
						Log.e(Consts.LOG_TAG, "Problem with playing", e);
					}

				}
				try {
					jSon.UpdateSongInfo();
					if (jSon.getIsPlaying() == 1) {
						Log.v(Consts.LOG_TAG, "Stopping Music to play Video");
						new runCmd().execute("STOPRB", "");
					}
				} catch (Exception e) {
				}
				new runCmd().execute("PLAYSM", "");
			} else {
				new runCmd().execute("PAUSESM", "");
			}
		}
	};

	private void createButtons() {

		button_fullscreen = (ToggleButton) findViewById(R.id.MSWMfullscreen);
		button_fullscreen.setOnClickListener(this);
		button_playpause = (ToggleButton) findViewById(R.id.MSWMplaypause);
		button_playpause.setOnClickListener(this);
		button_rewind = (Button) findViewById(R.id.MSWMrewind);
		button_rewind.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/* Perform action on clicks */
				new runCmd().execute("SKIPBSM", "");
			}
		});
		button_foward = (Button) findViewById(R.id.MSWMfoward);
		button_foward.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/* Perform action on clicks */
				new runCmd().execute("SKIPFSM", "");
			}
		});
		button_pause = (Button) findViewById(R.id.MSWMstop);
		button_pause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/* Perform action on clicks */
				new runCmd().execute("STOPSM", "");
				// TODO: click play and fullscreen button off
				CompoundButton btn = (ToggleButton) findViewById(R.id.MSWMfullscreen);
				if (btn.isChecked()) {
					btn.setChecked(false);
				}
				btn = (ToggleButton) findViewById(R.id.MSWMplaypause);
				if (btn.isChecked()) {
					btn.setChecked(false);
				}
			}
		});
		button_mute = (Button) findViewById(R.id.MSWMmute);
		button_mute.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/* Perform action on clicks */
				new runCmd().execute("MUTESM", "");
			}
		});
		button_quit = (Button) findViewById(R.id.MSWMquit);
		button_quit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/* Perform action on clicks */
				new runCmd().execute("QUITSM", "");
				quit(false);
			}
		});
		set_sizes();
	}

	private void set_sizes() {
		int width = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getWidth();
		int height = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getHeight();

		LinearLayout ll1 = (LinearLayout) findViewById(R.id.LL_buttons_first);
		ll1.setMinimumHeight(height / 3);
		LinearLayout ll2 = (LinearLayout) findViewById(R.id.LL_buttons_second);
		ll2.setMinimumHeight(height / 3);
		button_rewind.setWidth(width / 3);
		button_foward.setWidth(width / 3);
		button_pause.setWidth(width / 3);
		button_mute.setWidth(width / 3);
		button_quit.setWidth(width / 3);
		button_fullscreen.setWidth(width / 3);
		button_playpause.setWidth(width / 3);
	}

	private void quit(Boolean quitProgram) {
		Log.i(Consts.LOG_TAG, "Quitting");
		if (quitProgram) {
			exit(1);
		} else {
			AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
			alt_bld.setMessage("Have you finished Watching this Show?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									exit(2);

								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									exit(0);
								}
							});
			AlertDialog alert = alt_bld.create();
			// Title for AlertDialog
			alert.setTitle(showString);
			// Icon for AlertDialog
			alert.setIcon(R.drawable.icon);
			alert.show();
		}

	}

	private void exit(int flag) {
		if (flag == 1) {
			setResult(Consts.QUITREMOTE);
		} else if (flag == 2) {
			Intent i = new Intent();
			i.putExtra("showID", ID);
			setResult(Consts.REMOVESHOW, i);
		}
		this.finish();
	}

	private class runCmd extends AsyncTask<String, Integer, Boolean> {

		protected Boolean doInBackground(String... incoming) {
			String cmd = incoming[0];
			String cmdTxt = incoming[1];
			Log.i(Consts.LOG_TAG, cmdTxt);
			Map<String, String> params = new HashMap<String, String>();
			params.put("command", cmd);
			params.put("command_text", cmdTxt);
			params.put("source_hostname", phoneNumber);
			params.put("destination_hostname", destHost);
			Log.i(Consts.LOG_TAG, params.get("command_text"));
			new JSON(getApplicationContext())
					.JSONSendCmd("sendcommand", params);
			return true;
		}
	}
}
