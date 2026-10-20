package nl.apk2tv;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.SharedPreferences;

public class MainActivity extends Activity {
    private LinearLayout root;
    private SharedPreferences prefs;
    private TextView status;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("apk2tv", MODE_PRIVATE);
        root = new LinearLayout(this);
        root.setPadding(30,30,30,30);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.rgb(10,19,35));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
        label("APK2TV", 32, Color.YELLOW);
        label("Android TV afstandsbediening → virtuele touchscreenacties", 19, Color.WHITE);
        label("Testversie zonder root • Alleen de app die je hier kiest wordt bediend.", 16, Color.LTGRAY);
        status = label("", 17, Color.CYAN);
        button("1. Open toegankelijkheidsinstellingen", () -> {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch(Exception e) { toast("Instellingen niet beschikbaar op deze tv"); }
        });
        final Switch enabled = new Switch(this);
        enabled.setText("Touchmapping aanzetten");
        enabled.setTextColor(Color.WHITE);
        enabled.setTextSize(18);
        enabled.setChecked(prefs.getBoolean("enabled",false));
        enabled.setOnCheckedChangeListener((v,checked)-> {
            prefs.edit().putBoolean("enabled",checked).apply();
            refreshStatus();
        });
        root.addView(enabled);
        label("Doel-app: vul de package name in, bijvoorbeeld com.example.game. APK2TV reageert nergens anders.", 15,Color.LTGRAY);
        EditText target=new EditText(this);
        target.setSingleLine(true);
        target.setTextColor(Color.WHITE);
        target.setHintTextColor(Color.GRAY);
        target.setHint("com.example.game");
        target.setText(prefs.getString("target",""));
        root.addView(target);
        button("Doel-app opslaan",()->{
            String pkg=target.getText().toString().trim();
            if(pkg.equals(getPackageName())){toast("Kies een andere app");return;}
            prefs.edit().putString("target",pkg).apply();refreshStatus();
        });
        label("Bediening: pijltje = korte veegbeweging, OK = tik op midden van scherm. Dit is een eerste testprofiel; niet ieder spel gebruikt dezelfde gebaren.",15,Color.WHITE);
        button("Touchmapping uitschakelen",()->{
            prefs.edit().putBoolean("enabled",false).apply();enabled.setChecked(false);refreshStatus();
        });
        label("Werkt de afstandsbediening niet? Sommige tv-firmwares geven de D-pad niet door aan een AccessibilityService. APK2TV kan dit zonder root niet afdwingen.",14,Color.LTGRAY);
        refreshStatus();
    }
    private void refreshStatus(){
        String target=prefs.getString("target","");
        boolean on=prefs.getBoolean("enabled",false);
        status.setText("Mapping: "+(on?"AAN":"UIT")+"  |  App: "+(target.isEmpty()?"niet gekozen":target));
    }
    private TextView label(String s,int size,int color){
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);
        v.setPadding(0,10,0,10);root.addView(v);return v;
    }
    private void button(String s,Runnable action){
        Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setOnClickListener(v->action.run());
        root.addView(b,new LinearLayout.LayoutParams(-1,-2));
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
