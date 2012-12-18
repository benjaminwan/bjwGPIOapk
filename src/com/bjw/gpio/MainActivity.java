package com.bjw.gpio;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	Button btnPin,btnLED,btnList,btnOsc,btnTest,btnQuit;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btnPin=(Button)findViewById(R.id.btnPin);
        btnLED=(Button)findViewById(R.id.btnLED);
        btnList=(Button)findViewById(R.id.btnList);
        btnQuit=(Button)findViewById(R.id.btnQuit);
        btnOsc=(Button)findViewById(R.id.btnOSC);
        btnTest=(Button)findViewById(R.id.btnTest);
        btnPin.setOnClickListener(new btnClickListener());
        btnLED.setOnClickListener(new btnClickListener());
        btnList.setOnClickListener(new btnClickListener());
        btnQuit.setOnClickListener(new btnClickListener());
        btnOsc.setOnClickListener(new btnClickListener());
        btnTest.setOnClickListener(new btnClickListener());
        String appName = getString(R.string.app_name);
        try {
			PackageInfo pinfo = getPackageManager().getPackageInfo("com.bjw.gpio", PackageManager.GET_CONFIGURATIONS);
			String versionName = pinfo.versionName;
			setTitle(appName+" V"+versionName);
        } catch (NameNotFoundException e) {
        	e.printStackTrace();
        }
    }


    class btnClickListener implements View.OnClickListener{

		public void onClick(View v)
		{
			if (v==btnPin)
			{
				Intent intent_pin = new Intent(MainActivity.this, PinActivity.class);
				startActivity(intent_pin);
			}else if (v==btnLED) {
				Intent intent_led = new Intent(MainActivity.this, LedActivity.class);
				startActivity(intent_led);
			}else if (v==btnList) {
				Intent intent_list = new Intent(MainActivity.this, ListActivity.class);
				startActivity(intent_list);
			}else if (v==btnOsc) {
				Intent intent_osc = new Intent(MainActivity.this, OscilloscopeActivity.class);
				startActivity(intent_osc);
			}else if (v==btnTest) {
				Intent intent_test = new Intent(MainActivity.this, TestActivity.class);
				startActivity(intent_test);
			}else if (v==btnQuit) {
				android.os.Process.killProcess(android.os.Process.myPid());
//				System.exit(0);
			}
		}
    }
}
