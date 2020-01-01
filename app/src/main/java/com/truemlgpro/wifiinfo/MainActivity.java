package com.truemlgpro.wifiinfo;

import android.app.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.v4.widget.*;
import android.widget.*;
import android.content.*;
import android.net.wifi.*;
import android.net.*;
import android.view.*;
import android.support.design.widget.*;
import android.app.AlertDialog;
import android.support.v4.app.*;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.pm.*;
import android.util.*;
import android.location.*;
import android.provider.*;
import android.Manifest;
import java.util.*;
import me.anwarshahriar.calligrapher.*;
import java.net.*;

public class MainActivity extends AppCompatActivity 
{
	
	private Toolbar toolbar;
	private DrawerLayout mDrawerLayout;
	private TextView textview;
	// private Button btn;
	
	private LocationManager locationManager;
	private NetworkInfo WiFiCheck;
	private ConnectivityManager CM;
	private Context context;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		textview = (TextView) findViewById(R.id.textview1);
		// btn = (Button) findViewById(R.id.button1);
		
		// Service goes here //
		
		Intent ServiceIntent = new Intent(MainActivity.this, NotificationService.class);
		startService(ServiceIntent);
			
		// END //
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		int Permission_All = 1;
		String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
		if(!hasPermissions(this, Permissions)) {
			Toast toast = Toast.makeText(this, "Location permission is needed to show SSID and BSSID on Android 8+, grant it to get full info", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER|Gravity.FILL_HORIZONTAL, 0, 50);
			toast.show();
			
			ActivityCompat.requestPermissions(this, Permissions, Permission_All);
		}
		
		if(android.os.Build.VERSION.SDK_INT > 25) {
			// Notify User if GPS is disabled
			isLocationEnabled();
			if(!isLocationEnabled()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Location is Disabled")
				.setMessage("Wi-Fi Info needs Location to show SSID (network name) and BSSID (network MAC address) on Android 8+ \n\nClick Enable to grant Wi-Fi Info permission to show SSID and BSSID")
					.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					showToastOnEnable();
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					showToastOnCancel();
					dialog.cancel();
				}
			});
			builder.setCancelable(false);
			AlertDialog alert = builder.create();
			alert.show();
			}
		}
		
		
		Calligrapher calligrapher = new Calligrapher(this);
		calligrapher.setFont(this, "fonts/GoogleSans-Medium.ttf", true);
		
		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(false);
		actionbar.setSubtitle("Release v1.3_debug");
		actionbar.setElevation(20);
	    }
	
	public void onInfoGet() {
        ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
        if (!WiFiCheck.isConnected()) {
			Toast.makeText(this, "No Connection", Toast.LENGTH_LONG).show();
			Intent ServiceIntent = new Intent(MainActivity.this, NotificationService.class);
			stopService(ServiceIntent);
        } else {
			WifiManager mainWifi;
			mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = mainWifi.getConnectionInfo();
			String ssid = wInfo.getSSID();
			String macAdd = getMacAddr();
			String bssd = wInfo.getBSSID().toUpperCase();
			int rssi = wInfo.getRssi();
			int freq = wInfo.getFrequency();
			int networkSpeed = wInfo.getLinkSpeed();
			// Check mainWifi for more info
			int ipAddress = wInfo.getIpAddress();
			String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
			SupplicantState supState = wInfo.getSupplicantState();
			int network_id = wInfo.getNetworkId();
			String info = "MAC Address: " + macAdd + "\n" + "BSSID: " + bssd + "\n" + "SSID: " + ssid + "\n" + "IP Address: " + ip + "\n" +
				"RSSI (Signal Strength): " + rssi + "dBm" + "\n" + "Frequency: " + freq + "MHz" + "\n" + "Network Speed: " + networkSpeed + "MB/s" + "\n" + "Network ID: " + network_id + "\n" + "Supplicant State: " + supState;
			textview.setText(info);
		}

	}
	
	public void showToastOnEnable() {
		Toast toast = Toast.makeText(this, "Enable Location to show SSID and BSSID of current network", Toast.LENGTH_LONG);
		toast.show();
	}
	
	public void showToastOnCancel() {
		Toast toast = Toast.makeText(this, "SSID and BSSID of current network won't be shown", Toast.LENGTH_LONG);
		toast.show();
	}
	
	protected boolean isLocationEnabled() {
		String ls = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) getSystemService(ls);
		if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean hasPermissions(Context context, String... permissions) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
			for(String permission: permissions) {
				if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static String getMacAddr() {
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
				
				byte[] macBytes = nif.getHardwareAddress();
				if (macBytes == null) {
					return "";
				}
				
				StringBuilder res1 = new StringBuilder();
				for (byte b : macBytes) { 
					// res1.append(Integer.toHexString(b & 0xFF) + ":"); 
					res1.append(String.format("%02X:",b));
				}
				
				if (res1.length() > 0) {
					res1.deleteCharAt(res1.length() - 1);
				}
				
				return res1.toString();
			}
		} catch (Exception ex) {
			//handle exception
		}
		
		return "";
	}
	
	
	private Handler handler = new Handler();

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			onInfoGet();
			handler.postDelayed(runnable, 1000);
		}
	};
	
	@Override
	protected void onStart()
	{
 		// TODO: Implement this method
		super.onStart();
		handler.post(runnable);
	}


	@Override
	protected void onStop()
	{
		// TODO: Implement this method
		super.onStop();
		handler.removeCallbacks(runnable);
	}
	
	@Override 
	public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.dev_info) {
            Intent intent = new Intent(this, DevInfoActivity.class);
            startActivity(intent);
        }
		
		if (item.getItemId() == R.id.discord_servers) {
			Intent intent = new Intent(this, DiscordServersActivity.class);
			startActivity(intent);
		}
		
		if (item.getItemId() == R.id.supporters) {
			Intent intent = new Intent(this, SupportersActivity.class);
			startActivity(intent);
		}
		
		if (item.getItemId() == R.id.settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
		return true;
    }
	
	
}