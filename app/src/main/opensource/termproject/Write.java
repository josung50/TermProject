package opensource.termproject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IntegerRes;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.FacebookSdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by JSR on 2016-11-25.
 */
public class Write extends Activity {

    ImageView iv; // 카메라를 위한 이미지 뷰
    String FoodPrice = null;
    String FoodName = null; String FoodComment = null; String Location = GoogleMaps.getAddress(this, GoogleMaps.latitude, GoogleMaps.longitude);
    String BigOption = null; String SmallOption = null;

    int flag_Camera_image = 0; // 카메라 이미지가 담겨오면 1로 설정 됨 -> null 방지

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("위치", "위도 ,경도 :" + GoogleMaps.latitude + " " + GoogleMaps.longitude);
        setContentView(R.layout.activity_write);
        iv = (ImageView)findViewById(R.id.imageView);

        //스피너//
        Spinner s1 = (Spinner)findViewById(R.id.spinner1); // 음식 대분류
        s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.i("id", "id: " + parent.getItemAtPosition(position));
                BigOption = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner s2 = (Spinner)findViewById(R.id.spinner2); // 음식 소분류
        s2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.i("id", "id: " + parent.getItemAtPosition(position));
                SmallOption = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // 카메라 호출 버튼
    public void CameraButton(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,1);
    }

    // 저장 버튼
    public void SendButton(View v) {
        EditText text1 = (EditText)findViewById(R.id.FoodName); FoodName = text1.getText().toString();
        EditText text2 = (EditText)findViewById(R.id.FoodPrice); FoodPrice = text2.getText().toString();
        EditText text3 = (EditText)findViewById(R.id.FoodComment);  FoodComment = text3.getText().toString();


        Log.d("데이터" , "대분류:" + BigOption + " 소분류:" + SmallOption);
        Log.d("데이터2", "이름:" + FoodName + " 가격:" + FoodPrice + " 코멘트:" + FoodComment);

        if(FoodName == null || FoodPrice == null || FoodComment == null || flag_Camera_image == 0 || BigOption == null)
            Toast.makeText(getApplicationContext(), "빈 칸 없이 채워주세요.", Toast.LENGTH_LONG).show();
        else {
            new HttpTask().execute(FoodPrice,FoodName,FoodComment,Location,BigOption,SmallOption);
            finish();
        }
    }

    // 카메라 이미지 불러오는 함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data.getData() != null) {
            flag_Camera_image = 1;
            iv.setImageURI(data.getData());
        }
    }


    // 실습 터치
    /*class test implements View.OnTouchListener {
        public boolean onTouch(View v , MotionEvent evnet) {
            if(evnet.getAction() == MotionEvent.ACTION_DOWN) {
                Toast.makeText(getApplication(), "터치 됨.", Toast.LENGTH_LONG).show();
                return true;
            }
            else return false;
        }
    }*/

    class HttpTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try{
                String urlPath = "http://54.187.131.242/ryong/db.php";
                String _id = Login.LR.getAccessToken().getUserId(); double _lat = GoogleMaps.latitude; double _lng = GoogleMaps.longitude;
                String _FoodPrice = params[0]; String _FoodNmae = params[1]; String _FoodComment = params[2];
                String _Location = params[3]; String _BigOption = params[4]; String _SmallOption = params[5];
                Log.d("디버깅" , "데이터터 : " + params[0] + " " + params[1]);
                String data = "_id=" + _id;
                data += "&_lat=" + _lat;
                data += "&_lng=" + _lng;
                data += "&" + URLEncoder.encode("_FoodPrice", "UTF-8") + "=" + URLEncoder.encode(_FoodPrice, "UTF-8");
                data += "&" + URLEncoder.encode("_FoodName", "UTF-8") + "=" + URLEncoder.encode(_FoodNmae, "UTF-8");
                data += "&" + URLEncoder.encode("_FoodComment", "UTF-8") + "=" + URLEncoder.encode(_FoodComment, "UTF-8");
                data += "&" + URLEncoder.encode("_Location", "UTF-8") + "=" + URLEncoder.encode(_Location, "UTF-8");
                data += "&" + URLEncoder.encode("_BigOption", "UTF-8") + "=" + URLEncoder.encode(_BigOption, "UTF-8");
                data += "&" + URLEncoder.encode("_SmallOption", "UTF-8") + "=" + URLEncoder.encode(_SmallOption, "UTF-8");
                //data += "&_FoodPrice=" + _FoodPrice;
                //data += "&_FoodName=" + _FoodNmae;
                //data += "&_FoodComment=" + _FoodComment;
                //data += "&_Location=" + _Location;
                //data += "&_BigOption=" + _BigOption;
                //data += "&" + URLEncoder.encode("_SmallOption", "UTF-8") + "=" + URLEncoder.encode(_SmallOption, "UTF-8");
                //data += "&_SmallOption=" + _SmallOption;
                //data += "&" + URLEncoder.encode("_SmallOption", "UTF-8") + "=" + URLEncoder.encode(_SmallOption, "UTF-8");

                URL url = new URL(urlPath);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    sb.append(line);
                    break;
                }
                Log.d("디버깅","데이터 : " + sb.toString());
                return sb.toString();

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

