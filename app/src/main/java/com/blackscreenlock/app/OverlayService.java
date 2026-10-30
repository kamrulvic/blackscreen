package com.blackscreenlock.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OverlayService extends Service {
    public static final String START="com.blackscreenlock.START", STOP="com.blackscreenlock.STOP";
    private static final String CHANNEL="blackscreen_active";
    private WindowManager wm;
    private View floating, blackout;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Runnable clockTick;
    private boolean locked=false;
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    @Override public void onCreate(){super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);createChannel();}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null && STOP.equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        if(!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY;}
        try { startForeground(1001,notification()); if(floating==null && blackout==null) showFloating(); }
        catch(Exception ex){stopSelf();return START_NOT_STICKY;}
        return START_STICKY;
    }
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"BlackScreen Lock running",NotificationManager.IMPORTANCE_LOW);c.setDescription("Persistent notification while touch guard is active");getSystemService(NotificationManager.class).createNotificationChannel(c);}}
    private Notification notification(){
        Intent stop=new Intent(this,OverlayService.class);stop.setAction(STOP);
        PendingIntent pi=PendingIntent.getService(this,1,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent open=new Intent(this,MainActivity.class);
        PendingIntent openPi=PendingIntent.getActivity(this,2,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_lock_lock).setContentTitle("BlackScreen Lock is active")
            .setContentText("Tap to open controls").setContentIntent(openPi).setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,"Stop",pi).build();
    }
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private WindowManager.LayoutParams params(int w,int h,int gravity){
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,android.graphics.PixelFormat.TRANSLUCENT);
        p.gravity=gravity;return p;
    }
    private void showFloating(){
        locked=false;
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);
        TextView lock=new TextView(this);lock.setText("🔒");lock.setTextSize(25);lock.setGravity(Gravity.CENTER);lock.setTextColor(Color.WHITE);
        lock.setBackground(bg(0xD9284E4A,30));panel.addView(lock,new LinearLayout.LayoutParams(dp(64),dp(64)));
        floating=panel;
        WindowManager.LayoutParams p=params(dp(64),WindowManager.LayoutParams.WRAP_CONTENT,Gravity.TOP|Gravity.END);p.x=dp(12);p.y=dp(160);
        wm.addView(floating,p);
        lock.setOnClickListener(v->showBlack());
        lock.setOnLongClickListener(v->{showStopPanel();return true;});
    }
    private void showStopPanel(){
        if(floating==null || !(floating instanceof LinearLayout))return;
        LinearLayout panel=(LinearLayout)floating;
        if(panel.getChildCount()>1)return;
        TextView stop=new TextView(this);stop.setText("STOP APP");stop.setTextColor(Color.WHITE);stop.setGravity(Gravity.CENTER);stop.setTextSize(13);stop.setBackground(bg(0xEF9E3131,12));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(96),dp(48));lp.topMargin=dp(8);panel.addView(stop,lp);
        stop.setOnClickListener(v->stopSelf());
        TextView cancel=new TextView(this);cancel.setText("Cancel");cancel.setTextColor(Color.WHITE);cancel.setGravity(Gravity.CENTER);cancel.setBackground(bg(0xEF263E42,12));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(96),dp(40));cp.topMargin=dp(5);panel.addView(cancel,cp);
        cancel.setOnClickListener(v->{panel.removeView(stop);panel.removeView(cancel);});
        WindowManager.LayoutParams p=(WindowManager.LayoutParams)panel.getLayoutParams();p.width=dp(104);wm.updateViewLayout(panel,p);
    }
    private void showBlack(){
        if(floating!=null){wm.removeView(floating);floating=null;}
        locked=true;
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setBackgroundColor(Color.BLACK);
        TextView clock=new TextView(this);clock.setTextColor(0xFFB9D8CE);clock.setTextSize(36);clock.setGravity(Gravity.CENTER);root.addView(clock);
        TextView hint=new TextView(this);hint.setText("Touch guard active");hint.setTextColor(0xFF697A77);hint.setTextSize(12);hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(42));root.addView(hint,hp);
        View space=new View(this);root.addView(space,new LinearLayout.LayoutParams(1,dp(90)));
        TextView unlock=new TextView(this);unlock.setText("🔓  HOLD TO UNLOCK");unlock.setTextSize(14);unlock.setTextColor(Color.WHITE);unlock.setGravity(Gravity.CENTER);unlock.setBackground(bg(0xFF19302B,22));
        LinearLayout.LayoutParams up=new LinearLayout.LayoutParams(dp(205),dp(58));root.addView(unlock,up);
        unlock.setOnLongClickListener(v->{showFloatingFromBlack();return true;});
        unlock.setOnClickListener(v->Toast.makeText(this,"Press and hold to unlock",Toast.LENGTH_SHORT).show());
        blackout=root;
        WindowManager.LayoutParams p=params(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,Gravity.TOP|Gravity.START);
        p.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        wm.addView(blackout,p);
        clockTick=new Runnable(){public void run(){if(locked && blackout!=null){clock.setText(new SimpleDateFormat("hh:mm a",Locale.getDefault()).format(new Date()));handler.postDelayed(this,1000);}}};clockTick.run();
    }
    private void showFloatingFromBlack(){locked=false;if(clockTick!=null){handler.removeCallbacks(clockTick);clockTick=null;}if(blackout!=null){wm.removeView(blackout);blackout=null;}showFloating();}
    @Override public void onDestroy(){locked=false;if(clockTick!=null)handler.removeCallbacks(clockTick);if(blackout!=null){try{wm.removeView(blackout);}catch(Exception ignored){}blackout=null;}if(floating!=null){try{wm.removeView(floating);}catch(Exception ignored){}floating=null;}super.onDestroy();}
    @Override public android.os.IBinder onBind(Intent intent){return null;}
}
