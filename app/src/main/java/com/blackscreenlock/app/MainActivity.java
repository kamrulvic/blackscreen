package com.blackscreenlock.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    private int dp(float v) { return (int)(getResources().getDisplayMetrics().density*v+.5f); }
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(26),dp(56),dp(26),dp(28)); root.setBackgroundColor(Color.rgb(7,14,19));
        TextView title = new TextView(this); title.setText("●  BlackScreen Lock"); title.setTextSize(28); title.setTypeface(null,Typeface.BOLD); title.setTextColor(Color.rgb(136,239,198)); root.addView(title);
        TextView body = new TextView(this);
        body.setText("Keep a dark touch guard over your screen while listening to media.\n\n1. Allow 'Display over other apps'.\n2. Tap Start floating button.\n3. Tap the floating lock to darken the screen.\n4. Hold UNLOCK to return.\n5. Hold the floating lock to show Stop.\n\nAndroid system controls may remain accessible. Audio playback depends on the media app. This app does not switch off the display.");
        body.setTextSize(16); body.setTextColor(Color.WHITE); body.setLineSpacing(dp(5),1); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,0,1); bp.topMargin=dp(26);root.addView(body,bp);
        status=new TextView(this); status.setTextColor(Color.rgb(166,193,190)); root.addView(status);
        Button allow=button("1  Allow overlay permission"); root.addView(allow); allow.setOnClickListener(v->{Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));startActivity(i);});
        Button start=button("2  Start floating lock"); root.addView(start);start.setOnClickListener(v->{
            if(!Settings.canDrawOverlays(this)){status.setText("Allow overlay permission first.");return;}
            if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=getPackageManager().PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},8);}
            Intent i=new Intent(this,OverlayService.class);i.setAction(OverlayService.START);startForegroundService(i);status.setText("Floating lock is running. Open your media app.");
        });
        Button stop=button("Stop and remove overlay");root.addView(stop);stop.setOnClickListener(v->{Intent i=new Intent(this,OverlayService.class);i.setAction(OverlayService.STOP);startService(i);status.setText("Stopped.");});
        setContentView(root);
    }
    private Button button(String t){Button b=new Button(this);b.setText(t);b.setAllCaps(false);return b;}
    @Override protected void onResume(){super.onResume();if(status!=null)status.setText(Settings.canDrawOverlays(this)?"Overlay permission: granted":"Overlay permission: required");}
}
