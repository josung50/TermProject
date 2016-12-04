package opensource.termproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by JSR on 2016-11-21.
 */
public class Login extends Activity {
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    public static LoginResult LR; // 유저의 정보를 담고 있다.

    // 프로필 사진 관련
    ImageView imageview;
    static Bitmap profilepic; // 프로필 사진을 닮고 있다.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // SDK 초기화 (setContentView 보다 먼저 실행되어야합니다. 안그럼 에러납니다.)
        setContentView(R.layout.activity_facebook_login);

        imageview = (ImageView) findViewById(R.id.iv);

        callbackManager = CallbackManager.Factory.create();  //로그인 응답을 처리할 콜백 관리자
        loginButton = (LoginButton)findViewById(R.id.buttonId); //페이스북 로그인 버튼
        //유저 정보, 친구정보, 이메일 정보등을 수집하기 위해서는 허가(퍼미션)를 받아야 합니다.
        loginButton.setReadPermissions("public_profile", "user_friends","email");
        //버튼에 바로 콜백을 등록하는 경우 LoginManager에 콜백을 등록하지 않아도됩니다.
        //반면에 커스텀으로 만든 버튼을 사용할 경우 아래보면 CustomloginButton OnClickListener안에 LoginManager를 이용해서
        //로그인 처리를 해주어야 합니다.
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) { //로그인 성공시 호출되는 메소드
                Log.d("토큰", loginResult.getAccessToken().getToken());
                Log.d("유저아이디", loginResult.getAccessToken().getUserId());
                Log.d("퍼미션 리스트", loginResult.getAccessToken().getPermissions() + "");

                //loginResult.getAccessToken() 정보를 가지고 유저 정보를 가져올수 있습니다.
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    Log.d("user profile", object.toString());
                                    Log.d("이름",object.getString("name"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                LR = loginResult;

                //프로필 사진 가져오기//
                profilepic = getUserPic(LR.getAccessToken().getUserId());
                imageview.setImageBitmap(profilepic);
                request.executeAsync();
                /*Intent intent = new Intent(Login.this, GoogleMaps.class);
                startActivity(intent);
                finish();*/
            }

            @Override
            public void onError(FacebookException error) {
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    // 유저의 프로필 사진을 가져오는 함수
    // User ID로 연결시 redirect 현상 발생 -> 이에 대한 URL을 새로 구해주어야 한다....
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
            imageview.setImageBitmap(myBitmap);
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

    // 선택된 계정으로 접속하기
    public void Connect(View v) {
        if(LR != null) {
            Intent intent = new Intent(Login.this, GoogleMaps.class);
            startActivity(intent);
        }
        else
            Toast.makeText(getApplication(), "로그인을 해주세요.", Toast.LENGTH_LONG).show();
    }
}
