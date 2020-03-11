package id.posisi.dimana;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

/**
 * @author Dawud Tan - dawud_tan@merahputih.id.
 */
class Cek {
    static boolean permisi(String permisi, View view, int resId, Activity aktivitas, int requestCode) {
        if (ActivityCompat.checkSelfPermission(aktivitas, permisi) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(aktivitas, permisi)) {
                Snackbar.make(view, resId, Snackbar.LENGTH_INDEFINITE).setAction(
                        R.string.ok, v -> ActivityCompat.requestPermissions(aktivitas, new String[]{permisi}, requestCode))
                        .show();
            } else {
                ActivityCompat.requestPermissions(aktivitas, new String[]{permisi}, requestCode);
            }
            return false;
        } else return true;
    }
}