package com.perapplauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Simple launcher that lists launchable apps and attempts to call the Magisk wrapper script as root
 */
public class MainActivity extends Activity implements AppAdapter.OnAppClickListener {

    ListView listView;
    AppAdapter adapter;
    List<AppEntry> apps = new ArrayList<>();
    PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pm = getPackageManager();
        listView = findViewById(R.id.list_apps);
        adapter = new AppAdapter(this, apps, this);
        listView.setAdapter(adapter);

        loadApps();
    }

    private void loadApps() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Intent main = new Intent(Intent.ACTION_MAIN, null);
            main.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> ris = pm.queryIntentActivities(main, 0);
            apps.clear();
            for (ResolveInfo ri : ris) {
                String pkg = ri.activityInfo.packageName;
                String label = ri.loadLabel(pm).toString();
                Drawable icon = ri.loadIcon(pm);
                apps.add(new AppEntry(label, pkg, icon));
            }
            // update UI
            new Handler(Looper.getMainLooper()).post(() -> adapter.notifyDataSetChanged());
        });
    }

    @Override
    public void onAppClick(AppEntry entry) {
        // run on background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean ok = runWrapperAsRoot(entry.packageName);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (ok) {
                    Toast.makeText(MainActivity.this, "Launching (with spoof): " + entry.label, Toast.LENGTH_SHORT).show();
                } else {
                    // fallback to normal launch
                    Toast.makeText(MainActivity.this, "Root failed — launching normally: " + entry.label, Toast.LENGTH_SHORT).show();
                    try {
                        Intent launch = pm.getLaunchIntentForPackage(entry.packageName);
                        if (launch != null) {
                            startActivity(launch);
                        } else {
                            Toast.makeText(MainActivity.this, "No launch activity for " + entry.packageName, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
    }

    /**
     * Attempt to run the module wrapper via su -c.
     * Returns true if the command executed and exit code 0.
     */
    private boolean runWrapperAsRoot(String packageName) {
        String wrapper = "/data/adb/modules/perapp-spoofer/scripts/launchapp";
        // Ensure wrapper exists
        java.io.File f = new java.io.File(wrapper);
        if (!f.exists()) return false;

        String cmd = "su -c \"" + wrapper + " " + packageName + "\"";
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[] { "sh", "-c", cmd });
            int rc = p.waitFor();
            return rc == 0;
        } catch (Exception e) {
            android.util.Log.e("PerAppLauncher", "root launch failed", e);
            return false;
        } finally {
            if (p != null) p.destroy();
        }
    }
}
