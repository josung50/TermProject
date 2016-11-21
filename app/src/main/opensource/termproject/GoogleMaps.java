package opensource.termproject;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class GoogleMaps extends FragmentActivity implements OnMapReadyCallback{

    private final int ID_REQUEST_PERMISSION = 0x00;
    public static int flag_FINE_LOCATION = 0;
    public static int flag_COARSE_LOCATION = 0;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    static MapFragment mapFragment;
    LocationManager manager;

    static double latitude = 0.0;
    static double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        Log.e("유저아이디2",Login.LR.getAccessToken().getUserId());
        // 권한
        String[] REQUEST_PERMISSIONS = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUEST_PERMISSIONS, ID_REQUEST_PERMISSION);
        }

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }//

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // 마커의 윈도우 클릭하면 확대
        /*map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
            }
        });

        // map 터치 시
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(GoogleMaps.this);
                alert_confirm.setMessage("이 위치에서 일지를 작성하시겠습니까?").setCancelable(false).setPositiveButton("작성",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();

            }
        });*/

        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void startLocationService(){

        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        long minTime = 1000;
        float minDistance = 1;

        if(checkPermission()) // 권한 비허용이면 실행
            Toast.makeText(getApplicationContext(), "Plz allows permission.1", Toast.LENGTH_SHORT).show();

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, mLocationListener);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, mLocationListener);

    }

    public  void stopLocationService(){
        if(checkPermission())
            Toast.makeText(this, "Plz allows permission.2", Toast.LENGTH_SHORT).show();
        manager.removeUpdates(mLocationListener);

    }

    public final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            stopLocationService();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permission[], int[] grantResults) {
        if (requestCode == ID_REQUEST_PERMISSION) {
            Log.i("리퀘스트코드", "성공" + requestCode + " " + permission[0] + " " +
                    Manifest.permission.ACCESS_FINE_LOCATION + " " +
                    grantResults[0] + " " + PackageManager.PERMISSION_GRANTED);

            if (permission[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("플래그fine" , "설정");
                flag_FINE_LOCATION = 1;
            }
            else if (permission[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Plz allows permissions3", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (permission[1] == android.Manifest.permission.ACCESS_COARSE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("플래그corse" , "설정");
                flag_COARSE_LOCATION = 1;
            }
            else if (permission[1] == android.Manifest.permission.ACCESS_COARSE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Plz allows permissions4", Toast.LENGTH_SHORT).show();
                finish();
            }

            super.onRequestPermissionsResult(requestCode, permission, grantResults);
            flag_COARSE_LOCATION = 1;
            flag_FINE_LOCATION = 1;
        }
    }

    private boolean checkPermission() {
        if (flag_FINE_LOCATION == 0 && flag_COARSE_LOCATION == 0)
            return true;
        else
            return false;
    }
//
    // 좌표로 주소 얻기
    public static String getAddress(Context mContext, double lat, double lng) {
        String LocationName = "";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREAN);
        List<Address> address;
        try {
            // 한 좌표에 대해 두개 이상의 이름이 존재 할 수 있기에
            // 주소배열을 리턴 받기 위한 최대 갯수
            if(geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if(address != null && address.size()>0) {
                    // 주소 받아오기
                    LocationName = address.get(0).getAddressLine(0).toString();
                }
            }
        }
        catch (IOException e) {
            Toast.makeText(mContext, "Can't find address." , Toast.LENGTH_LONG).show();
        }
        return LocationName;
    }
}
