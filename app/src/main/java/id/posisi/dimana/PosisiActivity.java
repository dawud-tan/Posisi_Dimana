package id.posisi.dimana;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import dmax.dialog.SpotsDialog;

/**
 * @author Dawud Tan - dawud_tan@merahputih.id.
 */
public class PosisiActivity extends AppCompatActivity {

    private SwitchCompat saklarPosisi;
    private LocationManager locationManager;
    private PosisiListener posisiListener;
    private TextView akurasiTextView;
    private AlertDialog mDialog;
    private CoordinatorLayout koordinator;
    private Intent shareIntent;
    private String geo_pesan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posisi);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        akurasiTextView = findViewById(R.id.akurasi);
        saklarPosisi = findViewById(R.id.saklarPencarian);
        koordinator = findViewById(R.id.koordinator);
        android.location.Location l = new android.location.Location("");
        l.setLatitude(40.75889d);
        l.setLongitude(-73.985131d);
        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        mDialog = new SpotsDialog.Builder()
                .setContext(PosisiActivity.this)
                .setMessage(R.string.searching_for_gps)
                .setCancelable(false)
                .build();
    }

    void updateAkurasi(float akurasi) {
        akurasiTextView.setText(new StringBuilder(getString(R.string.akurasi)).append(" ").append(String.format("%.0f", akurasi)).append(" ").append(getString(R.string.meter)));
    }


    void dismissDialog() {
        mDialog.dismiss();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (Cek.permisi(Manifest.permission.ACCESS_FINE_LOCATION, koordinator, R.string.pesan_gps, this, 1)) {
                    posisiListener.setHitungan();
                    mDialog.show();
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, posisiListener);
                }
                break;
            case 2:
                if (Cek.permisi(Manifest.permission.ACCESS_FINE_LOCATION, koordinator, R.string.pesan_gps, this, 2))
                    locationManager.removeUpdates(posisiListener);
                break;
            case 3:
                if (Cek.permisi(Manifest.permission.WRITE_EXTERNAL_STORAGE, koordinator, R.string.pesan_storage, PosisiActivity.this, 3)) {
                    try {
                        shareImage(encodeAsBitmap(geo_pesan));
                    } catch (WriterException e) {
                        Snackbar.make(koordinator, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            private Circle circle;
            private Marker marker;

            @Override
            public void onMapReady(GoogleMap googleMap) {
                LatLng lokasiku = new LatLng(-7.588694, 110.663541);
                circle = googleMap.addCircle(new CircleOptions().center(lokasiku).radius(1000000000).strokeColor(Color.BLUE).fillColor(0x550000ff));
                akurasiTextView.setText("1.000.000.000 " + getString(R.string.meter));
                marker = googleMap.addMarker(new MarkerOptions()
                        .position(lokasiku)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mylocation))
                        .title(getString(R.string.title_activity_lokasiku)));
                posisiListener = new PosisiListener(PosisiActivity.this, googleMap, circle, marker);
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                saklarPosisi.setEnabled(true);
                saklarPosisi.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                            try {
                                if (isChecked) {
                                    if (Cek.permisi(Manifest.permission.ACCESS_FINE_LOCATION, koordinator, R.string.pesan_gps, PosisiActivity.this, 1)) {
                                        posisiListener.setHitungan();
                                        mDialog.show();
                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, posisiListener);
                                    }
                                } else {
                                    if (Cek.permisi(Manifest.permission.ACCESS_FINE_LOCATION, koordinator, R.string.pesan_gps, PosisiActivity.this, 2))
                                        locationManager.removeUpdates(posisiListener);
                                }
                            } catch (SecurityException e) {
                                Snackbar.make(koordinator, e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                );
                setLocationToShare(lokasiku);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(lokasiku));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(3.0f));
                googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        setLocationToShare(marker.getPosition());
                        circle.setCenter(marker.getPosition());
                        circle.setRadius(1.0);
                        akurasiTextView.setText("1 " + getString(R.string.meter));
                    }
                });
                saklarPosisi.setChecked(true);
            }
        });
        return true;
    }


    void setLocationToShare(LatLng lokasiku) {
        geo_pesan = "geo:" + lokasiku.latitude + "," + lokasiku.longitude + "?z=21";
        shareIntent.putExtra(Intent.EXTRA_TEXT, geo_pesan);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_qr_code:
                if (Cek.permisi(Manifest.permission.ACCESS_FINE_LOCATION, koordinator, R.string.pesan_gps, PosisiActivity.this, 2))
                    locationManager.removeUpdates(posisiListener);
                if (Cek.permisi(Manifest.permission.WRITE_EXTERNAL_STORAGE, koordinator, R.string.pesan_storage, PosisiActivity.this, 3)) {
                    try {
                        shareImage(encodeAsBitmap(geo_pesan));
                    } catch (WriterException e) {
                        Snackbar.make(koordinator, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
                saklarPosisi.setChecked(false);
                return true;
            case R.id.action_share:
                try {
                    if (Cek.permisi(Manifest.permission.ACCESS_FINE_LOCATION, koordinator, R.string.pesan_gps, PosisiActivity.this, 2))
                        locationManager.removeUpdates(posisiListener);
                    Intent chooserIntent = Intent.createChooser(shareIntent, getString(R.string.bagikan_ke));
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(chooserIntent);
                    } catch (Exception e) {
                        Snackbar.make(koordinator, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                    saklarPosisi.setChecked(false);
                } catch (Exception e) {
                    Snackbar.make(koordinator, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result = null;
        try {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 400, 400, null);
        } catch (IllegalArgumentException e) {
            Snackbar.make(koordinator, e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public void shareImage(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, geo_pesan, null);
        try {
            Intent wall_intent = new Intent(Intent.ACTION_SEND);
            wall_intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
            wall_intent.setType("image/jpeg");
            Intent chooserIntent = Intent.createChooser(wall_intent, getString(R.string.bagikan_ke));
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(chooserIntent);
            } catch (Exception e) {
                Snackbar.make(koordinator, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Snackbar.make(koordinator, e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }
}