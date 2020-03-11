package id.posisi.dimana;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * @author Dawud Tan - dawud_tan@merahputih.id.
 */
class PosisiListener implements LocationListener {
    private int hitungan = 1;
    private PosisiActivity posisiActivity;
    private GoogleMap googleMap;
    private Circle circle;
    private Marker marker;

    void setHitungan() {
        this.hitungan = 1;
    }

    PosisiListener(PosisiActivity posisiActivity, GoogleMap googleMap, Circle circle, Marker marker) {
        this.posisiActivity = posisiActivity;
        this.googleMap = googleMap;
        this.circle = circle;
        this.marker = marker;
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng lokasiku = new LatLng(location.getLatitude(), location.getLongitude());
        if (hitungan++ == 1) {
            posisiActivity.dismissDialog();
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        posisiActivity.setLocationToShare(lokasiku);
        posisiActivity.updateAkurasi(location.getAccuracy());
        circle.setCenter(lokasiku);
        circle.setRadius(location.getAccuracy());
        marker.setPosition(lokasiku);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiku, 20.0f));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(posisiActivity, posisiActivity.getString(R.string.gps_aktif), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        posisiActivity.startActivity(intent);
        Toast.makeText(posisiActivity, posisiActivity.getString(R.string.gps_mati), Toast.LENGTH_SHORT).show();
    }
}