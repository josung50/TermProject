package opensource.termproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by JSR on 2016-12-05.
 */
public class WindowView extends Activity {

    ImageView imageView2 , imageView3;
    TextView v1,v2,v3, v4; // 음식이름, 가격, 코멘트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        Intent intent = getIntent();
        String index_S = intent.getStringExtra("index");
        int index = Integer.parseInt(index_S);
        GoogleMaps.temp = GoogleMaps.list[index].split("/");
        // 1/1360872253931928/37.5619148/126.8570243/368000/computer/good job/1482 Gayang-dong/Japanese/Rice/1360872253931928_2016_12_05_02_14_2
        //  0   1               2           3           4       5       6       7                   8      9    10 변수는 GoogleMaps의 temp 이용

        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView3 = (ImageView) findViewById(R.id.imageView3);
        v1 = (TextView) findViewById(R.id.View1);
        v2 = (TextView) findViewById(R.id.View2);
        v3 = (TextView) findViewById(R.id.View3);
        v4 = (TextView) findViewById(R.id.View4);

        getPic();
        v1.setText("Food Name: " + GoogleMaps.temp[5]);
        v2.setText("Food Price: " + GoogleMaps.temp[4]);
        v3.setText(GoogleMaps.temp[6]);
        v4.setText(GoogleMaps.temp[1]);
        getUserPic(GoogleMaps.temp[1]); // 작성자 사진 가져오기
    }

    public Bitmap getPic() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        HttpURLConnection connection = null;
        String imageURL;
        imageURL = "http://54.187.131.242/ryong/uploads/"+GoogleMaps.temp[10];
        Log.e("이미지", imageURL);
        try {
            URL url = new URL(imageURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            Bitmap myBitmap = BitmapFactory.decodeStream(bis);

            // 회전
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
            imageView2.setImageBitmap(myBitmap);

            Log.e("이미지", "성공" + myBitmap);
            return myBitmap;
        } catch (IOException e) {
            Log.e("이미지" , "실패");
            e.printStackTrace();
            return null;
        }finally{
            Log.e("이미지","커밋성공");
            if(connection!=null)connection.disconnect();
        }//
    }

    public Bitmap getUserPic(String userID) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        HttpURLConnection connection = null;
        String imageURL;
        imageURL = "http://graph.facebook.com/"+userID+"/picture?type=large";
        Log.e("이미지", imageURL);
        try {
            URL url = new URL(imageURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            Log.e("URL" , connection.getHeaderField("Location"));
            imageURL = connection.getHeaderField("Location");
            URL url2 = new URL(imageURL);
            connection.disconnect();
            connection = (HttpURLConnection) url2.openConnection();
            connection.setDoInput(true);
            connection.connect();
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            Bitmap myBitmap = BitmapFactory.decodeStream(bis);
            imageView3.setImageBitmap(myBitmap);
            Log.e("이미지", "성공" + myBitmap);
            return myBitmap;
        } catch (IOException e) {
            Log.e("이미지" , "실패");
            e.printStackTrace();
            return null;
        }finally{
            Log.e("이미지","커밋성공");
            if(connection!=null)connection.disconnect();
        }//
    }
}
