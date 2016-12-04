package opensource.termproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.IntegerRes;
import android.support.v4.util.Pair;
import android.util.Base64;
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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by JSR on 2016-11-25.
 */
public class Write extends Activity {

    ImageView iv; // 카메라를 위한 이미지 뷰
    String FoodPrice = null;
    String FoodName = null; String FoodComment = null; String Location = GoogleMaps.getAddress(this, GoogleMaps.latitude, GoogleMaps.longitude);
    String BigOption = null; String SmallOption = null;


    // 카메라 , 이미지 관련 변수 //
    int flag_Camera_image = 0; // 카메라 이미지가 담겨오면 1로 설정 됨 -> null 방지
    Bitmap bitmap; // 사진 촬영의 url -> bitmap 이미지로 받는 변수
    String _Image; // 이미지 uri의 절대경로
    Uri imageUri;
    String uploadFileName = null; // ex- ~~~.jpg
    String uploadFilePath = null; // ex- /storage/emulated/0/DCIM/Camera/
    //ProgressDialog dialog = null; // 업로드 로딩바
    int serverResponseCode = 0; // 서버 상태 반환

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
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // 카메라 호출 버튼
    public void CameraButton(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    // 저장 버튼
    public void SendButton(View v) {
        //dialog = ProgressDialog.show(Write.this, "", "Uploading file...", true);

        EditText text1 = (EditText)findViewById(R.id.FoodName); FoodName = text1.getText().toString();
        EditText text2 = (EditText)findViewById(R.id.FoodPrice); FoodPrice = text2.getText().toString();
        EditText text3 = (EditText)findViewById(R.id.FoodComment);  FoodComment = text3.getText().toString();


        //Log.d("데이터" , "대분류:" + BigOption + " 소분류:" + SmallOption);
        //Log.d("데이터2", "이름:" + FoodName + " 가격:" + FoodPrice + " 코멘트:" + FoodComment);

        if(FoodName == null || FoodPrice == null || FoodComment == null || flag_Camera_image == 0 || BigOption == null)
            Toast.makeText(getApplicationContext(), "빈 칸 없이 채워주세요.", Toast.LENGTH_LONG).show();
        else {
            new HttpTask().execute(FoodPrice, FoodName, FoodComment, Location, BigOption, SmallOption); // 문자열 정보 전송
            uploadFile(uploadFilePath + "" + uploadFileName); // 이미지 전송
            finish();
        }
    }

    // 카메라 이미지 불러오는 함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == RESULT_CANCELED) // 취소
            return;

        if(data.getData() != null) {
            flag_Camera_image = 1;
            imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                //_Image = getStringImage(bitmap);
                _Image = ImageUtil.getRealPathFromURI(this, imageUri); // 절대경로 구하기
                // 회전
                ExifInterface exif = new ExifInterface(_Image);
                int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,	ExifInterface.ORIENTATION_NORMAL);
                int exifDegree = ImageUtil.exifOrientationToDegrees(exifOrientation);
                bitmap = ImageUtil.rotateBitmap(bitmap, exifDegree);
                split(); // 절대경로로 부터 경로와 파일명을 분리 -> uploadFileName & uploadFilePath에 저장
                iv.setImageBitmap(bitmap);
                // test : string to bitmap
                /*byte[] decodedString = Base64.decode(_Image, Base64.DEFAULT);
                Bitmap bitmap2 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                iv.setImageBitmap(bitmap2);*/
            } catch (IOException e) {
                e.printStackTrace();
            }
            //iv.setImageURI(data.getData());
            //Log.d("디버깅url" , "url:" + data.getData() + "\nstring:" + Image);
        }
    }

    class HttpTask extends AsyncTask<String,Void,String> {
        /* Bitmap bitmap , String image는 전역변수 */
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try{
                String urlPath = "http://54.187.131.242/ryong/db.php";
                String _id = Login.LR.getAccessToken().getUserId(); double _lat = GoogleMaps.latitude; double _lng = GoogleMaps.longitude;
                String _FoodPrice = params[0]; String _FoodNmae = params[1]; String _FoodComment = params[2];
                String _Location = params[3]; String _BigOption = params[4]; String _SmallOption = params[5];

                String data = "_id=" + _id;
                data += "&_lat=" + _lat;
                data += "&_lng=" + _lng;
                data += "&" + URLEncoder.encode("_FoodPrice", "UTF-8") + "=" + URLEncoder.encode(_FoodPrice, "UTF-8");
                data += "&" + URLEncoder.encode("_FoodName", "UTF-8") + "=" + URLEncoder.encode(_FoodNmae, "UTF-8");
                data += "&" + URLEncoder.encode("_FoodComment", "UTF-8") + "=" + URLEncoder.encode(_FoodComment, "UTF-8");
                data += "&" + URLEncoder.encode("_Location", "UTF-8") + "=" + URLEncoder.encode(_Location, "UTF-8");
                data += "&" + URLEncoder.encode("_BigOption", "UTF-8") + "=" + URLEncoder.encode(_BigOption, "UTF-8");
                data += "&" + URLEncoder.encode("_SmallOption", "UTF-8") + "=" + URLEncoder.encode(_SmallOption, "UTF-8");

                URL url = new URL(urlPath);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                // 문자열 전송
                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = reader.readLine()) != null)
                {
                    sb.append(line);
                    break;
                }
                Log.d("디버깅", "데이터 : " + sb.toString());
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

    // bitmap to String
    /*public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }*/

    private int uploadFile(String sourceFileUri) {
        String upLoadServerUri = "http://54.187.131.242/ryong/db2.php";

        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) { // 파일이 존재 하지 않으면
            //dialog.dismiss();
            Log.e("uploadFile", "Source File not exist :"
                    +uploadFilePath + "" + uploadFileName);
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(Write.this, "Source File not exist :" + uploadFilePath + "" + uploadFileName,  Toast.LENGTH_SHORT).show();
                }
            });
            return 0;
        }

        else // 존재 하면
        {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                // 서버에 저장 될 파일명 -> 작성자 페이스북 id_xx_xx_xx.jpg
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
                String nameTophp = Login.LR.getAccessToken().getUserId() +"_"+ sdf.format(d).toString() + ".jpg";

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + nameTophp + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(Write.this, "File Upload Complete.",  Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                //dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(Write.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                //dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(Write.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
            }

            //dialog.dismiss();
            return serverResponseCode;
        } // End else block
    }

    // 절대경로에서 경로 + 파일명으로 구분 //
    // String uploadFileName -> 파일 이름
    // _Image -> 경로 (원래 이 녀석은 경로+파일이름)
    // /storage/emulated/0/DCIM/Camera/20161204_235502.jpg (원래 절대경로)
    public void split() {
        String[] temp = _Image.split("/");
        uploadFilePath = temp[0] + "/" + temp[1] + "/" + temp[2] + "/" + temp[3] + "/" + temp[4] + "/" + temp[5] + "/"; // /storage/emulated/0/DCIM/Camera/
        uploadFileName = temp[6]; // ~~~.jpg
        Log.d("File_", _Image);
        Log.d("File_" , uploadFilePath);
        Log.d("File_" , uploadFileName);
    }
}

