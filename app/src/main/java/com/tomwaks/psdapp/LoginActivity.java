package com.tomwaks.psdapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    public static final String _settings = "settings";

    EditText et_ip_server, et_api_key;
    TextView tv_error;
    CheckBox chb_save_data;
    ProgressBar pb_loading;
    Button btn_connect;
    ImageView iv_ok;

    String IP, API, URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_ip_server = findViewById(R.id.et_ip_server);
        et_api_key = findViewById(R.id.et_api_key);
        tv_error = findViewById(R.id.tv_error);
        chb_save_data = findViewById(R.id.chb_save_data);
        pb_loading = findViewById(R.id.pb_loading);
        iv_ok = findViewById(R.id.iv_ok);
        btn_connect = findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(f_connect);

        SharedPreferences prefs = getSharedPreferences(_settings, MODE_PRIVATE);
        IP = prefs.getString("IP", "");
        API = prefs.getString("API", "");

        et_ip_server.setText(IP);
        et_api_key.setText(API);

        RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setDuration(3000);
        rotate.setInterpolator(new LinearInterpolator());
        ImageView iv_logo = (ImageView) findViewById(R.id.iv_logo);
        iv_logo.startAnimation(rotate);
    }

    View.OnClickListener f_connect = new View.OnClickListener() {
        public void onClick(View v) {
            IP = et_ip_server.getText().toString().trim();
            API = et_api_key.getText().toString().trim();

            if(!IP.equals("") && !API.equals("")){
                new CheckConnectToServer().execute(IP, API);
            }else{
                tv_error.setText(getResources().getString(R.string.error_login));
            }
        }
    };


    public class CheckConnectToServer extends AsyncTask<String, String, String>{

        OkHttpClient client = new OkHttpClient();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tv_error.setText("");
            btn_connect.setVisibility(View.GONE);
            chb_save_data.setVisibility(View.GONE);
            pb_loading.setVisibility(View.VISIBLE);
            et_ip_server.setFocusable(false);
            et_api_key.setFocusable(false);
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                URL = "http://"+IP+"/check_connect.php?key_api="+API;
                Response response = client.newCall(new Request.Builder().url(URL)
                        .build()).execute();
                if (!response.isSuccessful()) {
                    return "5";
                }
                JSONObject jO = new JSONObject(response.body().string());
                return jO.getString("status");
            } catch (UnknownHostException e) {
                Log.d("status--ConnectExcep", e.toString());
                return "2"+e.toString();
            } catch (ConnectException | SocketTimeoutException e){
                Log.d("status--ConnectExcep", e.toString());
                return "3";
            } catch (IllegalArgumentException e){
                Log.d("status--ConnectExcep", e.toString());
                return "4"+e.toString();
            } catch (Exception e){
                Log.d("status--ConnectExcep", e.toString());
                return "5"+e.toString();
            }
//            catch (Exception e){
//                return e.toString();
//            }
        }


        @Override
        protected void onPostExecute(String result) {
            Log.d("result--", result);

            if(result.equals("-1")){
                tv_error.setText(getResources().getString(R.string.error_login_type_m1));
            }

            if(result.equals("0")){
                tv_error.setText(getResources().getString(R.string.error_login_type_0));
            }

            if(result.equals("1")){
                iv_ok.setVisibility(View.VISIBLE);
                pb_loading.setVisibility(View.GONE);

                if(chb_save_data.isChecked()){
                    SharedPreferences.Editor editor = getSharedPreferences(_settings,0).edit();
                    editor.putString("IP", IP);
                    editor.putString("API", API);
                    editor.apply();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent_connect_activity = new Intent(LoginActivity.this, MainActivity.class);
                        intent_connect_activity.putExtra("IP", IP);
                        intent_connect_activity.putExtra("API", API);
                        LoginActivity.this.startActivity(intent_connect_activity);
                    }
                }, 500);
            }

            if(result.equals("2")){
                tv_error.setText(String.format("%s%s", getResources().getString(R.string.error_login_type_2), IP));
            }

            if(result.equals("3")){
                tv_error.setText(String.format("%s%s", getResources().getString(R.string.error_login_type_3), IP));
            }

            if(result.equals("4")){
                tv_error.setText(String.format("%s%s", getResources().getString(R.string.error_login_type_4), IP));
            }

            if(result.equals("5")){
                tv_error.setText(getResources().getString(R.string.error_login_type_5));
            }

            if(result.equals("6")){
                tv_error.setText(getResources().getString(R.string.error_login_type_6));
            }

            if(!result.equals("1")){
                btn_connect.setVisibility(View.VISIBLE);
                pb_loading.setVisibility(View.GONE);
                chb_save_data.setVisibility(View.VISIBLE);
                et_ip_server.setFocusableInTouchMode(true);
                et_api_key.setFocusableInTouchMode(true);
            }

        }
    }
}