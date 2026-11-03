package dz.abbas.lumeonhelper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.Locale;

public class ClaimAccessibilityService extends AccessibilityService {
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean busy = false;
    private long lastClick = 0;

    private Runnable scanner = new Runnable() {
        @Override
        public void run() {
            if (!running()) {
                busy = false;
                handler.postDelayed(this, 1000);
                return;
            }

            if (!busy) {
                AccessibilityNodeInfo root = getRootInActiveWindow();

                if (root != null && telegram(root.getPackageName())) {
                    AccessibilityNodeInfo node = findClaim(root);

                    if (node != null &&
                        System.currentTimeMillis() - lastClick > 3000) {

                        if (clickNode(node)) {
                            busy = true;
                            lastClick = System.currentTimeMillis();

                            handler.postDelayed(() -> {
                                if (running()) {
                                    performGlobalAction(GLOBAL_ACTION_BACK);
                                }

                                handler.postDelayed(() -> {
                                    busy = false;
                                }, 1800);

                            }, 6000);
                        }
                    }
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    boolean running() {
        return getSharedPreferences("helper", MODE_PRIVATE)
            .getBoolean("running", false);
    }

    boolean telegram(CharSequence p) {
        if (p == null) return false;
        String s = p.toString();
        return s.equals("org.telegram.messenger")
            || s.equals("org.telegram.messenger.beta");
    }

    AccessibilityNodeInfo findClaim(AccessibilityNodeInfo n) {
        if (n == null) return null;

        CharSequence t = n.getText();
        CharSequence d = n.getContentDescription();

        if ((claim(t) || claim(d)) &&
            n.isVisibleToUser() && n.isEnabled()) {
            if (n.isClickable()) return n;

            AccessibilityNodeInfo p = n.getParent();
            while (p != null) {
                if (p.isClickable() && p.isEnabled()) return p;
                p = p.getParent();
            }
        }

        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo found = findClaim(n.getChild(i));
            if (found != null) return found;
        }
        return null;
    }

    boolean claim(CharSequence s) {
        return s != null &&
            s.toString().trim().toLowerCase(Locale.ROOT).equals("claim");
    }

    boolean clickNode(AccessibilityNodeInfo n) {
        try {
            if (n.isClickable() &&
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                return true;

            AccessibilityNodeInfo p = n.getParent();
            while (p != null) {
                if (p.isClickable() &&
                    p.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                    return true;
                p = p.getParent();
            }
        } catch (Exception e) {
            Log.e("LumeonHelper", "Click error", e);
        }
        return false;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        handler.post(scanner);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {}

    @Override
    public void onInterrupt() {
        handler.removeCallbacks(scanner);
        busy = false;
    }
}