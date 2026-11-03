package dz.abbas.lumeonhelper;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    SharedPreferences prefs;
    TextView status;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("helper", MODE_PRIVATE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(1);
        box.setPadding(25, 35, 25, 25);
        box.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Lumeon Claim Helper\nافتح Lumeon داخل Telegram");
        title.setTextSize(20);
        box.addView(title);

        Button access = new Button(this);
        access.setText("تفعيل Accessibility");
        access.setOnClickListener(v ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        box.addView(access);

        Button start = new Button(this);
        start.setText("بدء");
        start.setOnClickListener(v -> {
            prefs.edit().putBoolean("running", true).apply();
            refresh();
        });
        box.addView(start);

        Button stop = new Button(this);
        stop.setText("إيقاف");
        stop.setOnClickListener(v -> {
            prefs.edit().putBoolean("running", false).apply();
            refresh();
        });
        box.addView(stop);

        status = new TextView(this);
        status.setTextSize(18);
        box.addView(status);

        setContentView(box);
        refresh();
    }

    void refresh() {
        status.setText(prefs.getBoolean("running", false)
            ? "الحالة: يعمل" : "الحالة: متوقف");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status != null) refresh();
    }
}