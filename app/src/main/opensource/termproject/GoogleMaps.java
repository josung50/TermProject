package opensource.termproject;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.lang.Double.*;
import java.util.Objects;

public class GoogleMaps extends FragmentActivity implements OnMapReadyCallback{

    private final int ID_REQUEST_PERMISSION = 0x00;
    public static int flag_FINE_LOCATION = 0;
    public static int flag_COARSE_LOCATION = 0;
    public static int flag_CAMERA = 0;
    public static int flag_WRITE_EXTERNAL_STORAGE = 0;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    static MapFragment mapFragment;
    LocationManager manager;

    static double latitude = 0.0;
    static double longitude = 0.0;

    public static String[] list; // DB의 결과를 받아오는 변수
    public static String[] temp;
    public static String tp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        Log.e("유저아이디2",Login.LR.getAccessToken().getUserId());
        // 권한
        String[] REQUEST_PERMISSIONS = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUEST_PERMISSIONS, ID_REQUEST_PERMISSION);
        }

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Write.BigOption = "Big";
        Write.SmallOption= "Small";
    }//

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // 마커의 윈도우 클릭하면 확대
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String MustSplit = marker.getSnippet(); // index를 추출해야 한다.
                String[] split = MustSplit.split("\n");

                Intent intent = new Intent(GoogleMaps.this, WindowView.class);
                intent.putExtra("index" , split[1]);
                Log.d("MustSplit", "string: " + split[1]);
                startActivity(intent);
            }
        });

        /*
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
                Log.i("flag_FINE_LOCATION" , "설정");
                flag_FINE_LOCATION = 1;
            }
            else if (permission[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Plz allows permissions3", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (permission[1] == android.Manifest.permission.ACCESS_COARSE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("flag_COARES_LOCATION" , "설정");
                flag_COARSE_LOCATION = 1;
            }
            else if (permission[1] == android.Manifest.permission.ACCESS_COARSE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Plz allows permissions4", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (permission[2] == Manifest.permission.CAMERA &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("flag_CAMERA", "설정");
                flag_CAMERA = 1;
            }
            else if (permission[2] == Manifest.permission.CAMERA &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Plz allows permissions5", Toast.LENGTH_SHORT).show();
                finish();
            }
            if (permission[3] == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("flag_WRITE_EXTERNAL_STORAGE", "설정");
                flag_CAMERA = 1;
            }
            else if (permission[3] == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Plz allows permissions6", Toast.LENGTH_SHORT).show();
                finish();
            }

            super.onRequestPermissionsResult(requestCode, permission, grantResults);
            flag_WRITE_EXTERNAL_STORAGE = 1;
            flag_COARSE_LOCATION = 1;
            flag_FINE_LOCATION = 1;
            flag_CAMERA = 1;
        }
    }

    private boolean checkPermission() {
        if (flag_FINE_LOCATION == 0 && flag_COARSE_LOCATION == 0 && flag_CAMERA == 0 && flag_WRITE_EXTERNAL_STORAGE == 0)
            return true;
        else
            return false;
    }
//
    // 좌표로 주소 얻기
    public static String getAddress(Context mContext, double lat, double lng) {
        String LocationName = "";
        Geocoder geocoder = new Geocoder(mContext, Locale.US);
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

    // 현재 위치 기준으로 500M 반경 검색
    public void CurrentLocationButton(View v) {
        startLocationService();
        LatLng CP = new LatLng(latitude, longitude);
        mMap.clear();
        Log.d("latlng","latlng:"+latitude+" "+longitude);
        if(longitude == 0 || latitude == 0) {
            Toast.makeText(getApplicationContext(), "현재 위치를 알 수 없습니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            new HttpTask().execute(); // 문자열 정보 전송
            // 데이터 받아오는 시간 벌기 //
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(list == null) {
                Toast.makeText(getApplicationContext(), "등록된 맛집이 없네요.", Toast.LENGTH_LONG).show();
                return;
            }
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(CP)
                    .title("Current:"+getAddress(this, latitude, longitude)));
            marker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CP,17.0f));

            for (int i = 0; i < list.length; i++) {
                temp = split(list[i]); // 1/1360872253931928/37.5619148/126.8570243/368000/computer/good job/1482 Gayang-dong/Japanese/Rice/1360872253931928_2016_12_05_02_14_2
                                           //  0   1               2           3           4       5       6       7                   8      9    10
                marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(temp[2]),Double.parseDouble(temp[3])))
                        .title("FoodName: "+temp[5]+"    Price: "+temp[4])
                        .snippet(".\n"+ i));
                Log.d("list", "list" + i + ":" + list[i]);
                }
        }
    }

    // 글 작성 버튼
    public void WriteButton(View v) {
        startLocationService(); // 위도경도 얻어옴
        if(longitude == 0.0 || latitude == 0.0)
            Toast.makeText(getApplicationContext(), "현재 위치를 알 수 없습니다. 다시 시도해 주세요." , Toast.LENGTH_LONG).show();
        else {
            Intent intent = new Intent(GoogleMaps.this, Write.class);
            startActivity(intent);
        }
    }


    // PHP 검색 쿼리 보내는 class
    class HttpTask extends AsyncTask<String,Void,String> {
        /* Bitmap bitmap , String image는 전역변수 */
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try{
                String urlPath = "http://54.187.131.242/ryong/db3.php";

                String data = "lat=" + latitude;
                data += "&lng=" + longitude;

                URL url = new URL(urlPath);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                // 문자열 전송
                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String CheckNull = "0";
                String line = null;

                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                CheckNull = sb.toString();
                Log.d("디버깅현재위치", "답:" + latitude + " " + longitude + " " + CheckNull);
                Log.d("디버깅쿼리3", "test:" + sb.toString()); // 1/1360872253931928/37.5619148/126.8570243/368000/computer/good job/1482 Gayang-dong/Japanese/Rice/1360872253931928_2016_12_05_02_14_2////

                if(sb.toString() != "") {
                    Log.d("디버깅123", "sb.toString:"+sb.toString());
                    list = sb.toString().split("////");
                    Log.d("list??" , "list:"+list);
                    return sb.toString();
                }
                else {
                    Log.d("지나감","ㅇㅇ");
                    return null;
                }

            }catch(UnsupportedEncodingException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
            //오류시 null 반환
            return null;
        }

        //asyonTask 3번째 인자와 일치 매개변수값 -> doInBackground 리턴값이 전달됨
        //AsynoTask 는 preExcute - doInBackground - postExecute 순으로 자동으로 실행됩니다.
        //ui는 여기서 변경
        protected void onPostExecute(String value){
            super.onPostExecute(value);
        }
    }

    // list를 /단위로 끊어서 반환
    public String[] split(String temp123) {
        String[] temp2 = temp123.split("/");
        return temp2;
    }

    // 검색 버튼
    public void SearchButton(View v) {
        EditText Ed = (EditText) findViewById(R.id.search);
        if ( Ed.length() == 0)
            tp = "null";
        else
            tp = Ed.getText().toString();
        Log.d("tp" , "tp의 값:" + tp);

        // 스피터 변수 받아오기
        //스피너//
        Spinner s1 = (Spinner)findViewById(R.id.BigOptionButton); // 음식 대분류
        s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Write.BigOption = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner s2 = (Spinner)findViewById(R.id.SmallOptionButton); // 음식 소분류
        s2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Write.SmallOption = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Log.d("스피너" , "big:" + Write.BigOption + "  small:" + Write.SmallOption);

        new HttpTask2().execute(); // 쿼리 전송 (Big ,Small을 포함한)
        try {
            Thread.sleep(500); // 데이터 받아오는 시간 딜레이
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(list == null) {
            Toast.makeText(getApplicationContext(), "등록된 맛집이 없네요.", Toast.LENGTH_LONG).show();
            return;
        }

        mMap.clear();
        Marker marker = null;
        for (int i = 0; i < list.length; i++) {
            temp = split(list[i]); // 1/1360872253931928/37.5619148/126.8570243/368000/computer/good job/1482 Gayang-dong/Japanese/Rice/1360872253931928_2016_12_05_02_14_2
            //  0   1               2           3           4       5       6       7                   8      9    10
            marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(temp[2]),Double.parseDouble(temp[3])))
                    .title("FoodName: "+temp[5]+"    Price: "+temp[4])
                    .snippet(".\n"+ i));
            Log.d("list22", "list" + i + ":" + list[i]);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(temp[2]),Double.parseDouble(temp[3])), 17.0f));
        marker.showInfoWindow();
    }

    // PHP 검색 쿼리 보내는 class - Big , Small option을 통한 검색
    class HttpTask2 extends AsyncTask<String,Void,String> {
        /* Bitmap bitmap , String image는 전역변수 */
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try{
                String urlPath = "http://54.187.131.242/ryong/db4.php";

                String data = "BigOption=" + Write.BigOption;
                data += "&SmallOption=" + Write.SmallOption;
                data += "&TP=" + tp;
                Log.d("임시데이터" , data);
                URL url = new URL(urlPath);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                // 문자열 전송
                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String CheckNull = "0";
                String line = null;

                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                CheckNull = sb.toString();
                Log.d("디버깅쿼리4", "test:" + sb.toString()); // 1/1360872253931928/37.5619148/126.8570243/368000/computer/good job/1482 Gayang-dong/Japanese/Rice/1360872253931928_2016_12_05_02_14_2////

                if(sb.toString() != "") {
                    list = sb.toString().split("////");
                    return sb.toString();
                }
                else {
                    Log.d("지나감","ㅇㅇ");
                    return null;
                }

            }catch(UnsupportedEncodingException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
            //오류시 null 반환
            return null;
        }

        //asyonTask 3번째 인자와 일치 매개변수값 -> doInBackground 리턴값이 전달됨
        //AsynoTask 는 preExcute - doInBackground - postExecute 순으로 자동으로 실행됩니다.
        //ui는 여기서 변경
        protected void onPostExecute(String value){
            super.onPostExecute(value);
        }
    }
}
