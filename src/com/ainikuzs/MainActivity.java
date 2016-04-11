package com.ainikuzs;
import com.ainikuzs.R;

import android.app.Activity;  
import android.content.Context;
import android.content.Intent;  
import android.os.Bundle;  
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;  
import android.widget.Button;  
import android.widget.Toast;  
  
public class MainActivity extends Activity {  
    private Button startBtn,checkserver,startserver;  
    static final String TAG = "kelimain";
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
  
        startBtn = (Button) findViewById(R.id.start); 
        checkserver = (Button) findViewById(R.id.checkserver); 
        startserver = (Button) findViewById(R.id.startserver); 
        startBtn.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                try {  
                    	//打开系统设置中辅助功能  
                    	startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    	Toast.makeText(MainActivity.this, "找到爱你酷辅助，然后开启服务即可", Toast.LENGTH_LONG).show(); 
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }); 
        checkserver.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                //判断服务是否已经启动
                if (!isAccessibilitySettingsOn(getApplicationContext())) {
                	Toast.makeText(MainActivity.this, "服务没有开启", Toast.LENGTH_LONG).show(); 
                }else{
                	Toast.makeText(MainActivity.this, "服务已经开启", Toast.LENGTH_LONG).show(); 
                }			
			}  
        	
        });

    }  
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + EnvelopeService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }
}
