package com.tomwaks.psdapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawer_layout;
    LinearLayout ll_main, ll_global;
    ProgressBar pb_global;
    TextView tv_search, tv_root;
    ImageView iv_refresh;

    String URL = "", IP, API, PATH = "/";

    List<String> objects = new ArrayList<String>(); // folders and files
    List<Integer> type_objects = new ArrayList<Integer>(); // 1=folder and 0=file
    List<String> size_objects = new ArrayList<String>(); // size of file or -1 for folder
    List<String> date_objects = new ArrayList<String>(); // date of file or date of folder
    List<Integer> numbs_objects = new ArrayList<Integer>(); // numbers of files and folder in main folder or -1 for files

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IP =  getIntent().getStringExtra("IP");
        API =  getIntent().getStringExtra("API");

        ImageView iv_logo = findViewById(R.id.iv_logo);
        AnimationImage(iv_logo);

        ImageView btn_nav = findViewById(R.id.iv_nav);
        btn_nav.setOnClickListener(f_nav);

        drawer_layout = findViewById(R.id.drawer_layout);

        iv_refresh = findViewById(R.id.iv_refresh);
//        iv_refresh.setOnClickListener(f_refresh);

        tv_search = findViewById(R.id.tv_search);
        tv_root = findViewById(R.id.tv_root);
//        tv_search.setOnClickListener(f_refresh);


        ll_global = findViewById(R.id.ll_global);
        ll_main = findViewById(R.id.ll_main);
        pb_global = findViewById(R.id.pb_global);

        new ObjectsListing().execute();

    }

    private class ObjectsListing extends AsyncTask<String, Integer, String> {

        OkHttpClient client = new OkHttpClient();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            iv_refresh.setEnabled(false);
            tv_search.setEnabled(false);
            ll_main.removeAllViews();
            pb_global.setVisibility(View.VISIBLE);

            objects.clear();
            type_objects.clear();
            size_objects.clear();
            date_objects.clear();
            numbs_objects.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                URL = "http://"+IP+"/listing.php?key_api="+API+"&&path="+PATH;
                Response response = client.newCall(new Request.Builder().url(URL)
                        .build()).execute();
                if (!response.isSuccessful()) {
                    return "5";
                }

                JSONObject jA = new JSONObject(response.body().string());
                JSONArray jData = new JSONArray(jA.get("data").toString());

                if(jA.get("status").equals(1)){
                    for(int i=0; i < jData.length(); i++){
                        objects.add(jData.getJSONObject(i).getString("name_object"));
                        type_objects.add(jData.getJSONObject(i).getInt("type_object"));
                        size_objects.add(jData.getJSONObject(i).getString("size_object"));
                        date_objects.add(jData.getJSONObject(i).getString("date_object"));
                        numbs_objects.add(jData.getJSONObject(i).getInt("numb_of_objects"));
                    }
                    return "1";
                }else{
                    return "0";
                }
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
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("result--", result);


            if(result.equals("1")){
                for (int i = 0; i < objects.size(); i++) {
                    if (type_objects.get(i) == 1) {
                        ConstraintLayout ll_object = (ConstraintLayout) View.inflate(MainActivity.this, R.layout.ll_object, null);
                        ImageView iv_icon_object = ll_object.findViewById(R.id.iv_icon_object);
                        iv_icon_object.setImageResource(R.drawable.ic_folder);
                        ImageView iv_icon_action = ll_object.findViewById(R.id.iv_icon_action);
                        iv_icon_action.setImageResource(R.drawable.ic_more);
                        TextView tv_name_object = ll_object.findViewById(R.id.tv_name_object);
                        tv_name_object.setText(objects.get(i));
                        TextView tv_date_object = ll_object.findViewById(R.id.tv_date_object);
                        tv_date_object.setText("Data: " + date_objects.get(i));
                        TextView tv_details_object = ll_object.findViewById(R.id.tv_details_object);
                        tv_details_object.setText("Liczba plików: " + numbs_objects.get(i));
                        ll_main.addView(ll_object);

                        final int index = i;
                        ll_object.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PATH += objects.get(index) + "/";
                                tv_search.setVisibility(View.GONE);
                                tv_root.setVisibility(View.VISIBLE);
                                tv_root.setText(objects.get(index));
                                new ObjectsListing().execute();
//                                Intent intent_main_activity = new Intent(MainActivity.this, ListingActivity.class);
//                                Log.d("status--", last_URL+objects.get(index) + "/");
//                                intent_main_activity.putExtra("URL", last_URL+objects.get(index) + "/");
//                                intent_main_activity.putExtra("IP", IP);
//                                intent_main_activity.putExtra("API", API);
//                                intent_main_activity.putExtra("DIR", objects.get(index));
//                                intent_main_activity.putExtra("FIRST_DIR", objects.get(index));
//                                MainActivity.this.startActivity(intent_main_activity);
                            }
                        });

                        iv_icon_action.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                LayoutInflater inflater = (LayoutInflater) MainActivity.this
//                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                                View layout = inflater.inflate(R.layout.popup_more_actions_folder,
//                                        (ViewGroup) findViewById(R.id.cl_popup_more_action));
//                                final PopupWindow pw = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
//                                pw.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
//
//                                TextView iv_name = layout.findViewById(R.id.tv_name);
//                                iv_name.setText(objects.get(index));
                            }
                        });
                    }
                }

                for (int i = 0; i < objects.size(); i++) {
                    if (type_objects.get(i) == 0) {
                        ConstraintLayout ll_object = (ConstraintLayout) View.inflate(MainActivity.this, R.layout.ll_object, null);
                        ImageView iv_icon_object = ll_object.findViewById(R.id.iv_icon_object);
                        iv_icon_object.setImageResource(R.drawable.ic_file);
                        ImageView iv_icon_action = ll_object.findViewById(R.id.iv_icon_action);
                        iv_icon_action.setImageResource(R.drawable.ic_more);
                        TextView tv_name_object = ll_object.findViewById(R.id.tv_name_object);
                        tv_name_object.setText(objects.get(i));
                        TextView tv_date_object = ll_object.findViewById(R.id.tv_date_object);
                        tv_date_object.setText("Data: " + date_objects.get(i));
                        TextView tv_details_object = ll_object.findViewById(R.id.tv_details_object);
                        tv_details_object.setText("Rozmiar: " + add_white_character(size_objects.get(i)));
                        ll_main.addView(ll_object);

//                        final int index = i;
//                        iv_icon_action.setOnClickListener( new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                LayoutInflater inflater = (LayoutInflater) MainActivity.this
//                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                                View layout = inflater.inflate(R.layout.popup_more_actions_file,
//                                        (ViewGroup) findViewById(R.id.cl_popup_more_action));
//                                final PopupWindow pw = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
//                                pw.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
//
//                                TextView iv_name = layout.findViewById(R.id.tv_name);
//                                iv_name.setText(objects.get(index));
//
//                                ConstraintLayout cl_rename_object = layout.findViewById(R.id.cl_rename_object);
//                                cl_rename_object.setOnClickListener( new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Log.d("onclick-file", "rename");
//                                        v.setBackgroundColor(ContextCompat.getColor(MainActivity.this, colorPrimary));
//
//                                        pw.dismiss();
//
//                                        LayoutInflater inflater = (LayoutInflater) MainActivity.this
//                                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                                        View layout = inflater.inflate(R.layout.popup_rename,
//                                                (ViewGroup) findViewById(R.id.cl_popup_rename));
//                                        final PopupWindow pw = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
//                                        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
//
//                                        EditText et_name = layout.findViewById(R.id.et_name);
//                                        et_name.setText(objects.get(index));
//
//                                    }
//                                });
//
//                                ConstraintLayout cl_share_object = layout.findViewById(R.id.cl_share_object);
//                                cl_share_object.setOnClickListener( new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Log.d("onclick-file", "share");
//                                        v.setBackgroundColor(ContextCompat.getColor(MainActivity.this, colorPrimary));
//                                        Intent intent = new Intent(Intent.ACTION_SEND);
//                                        intent.setType("text/plain");
//                                        intent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
//                                        intent.putExtra(Intent.EXTRA_TEXT, "This is my text");
//                                        startActivity(Intent.createChooser(intent, "choose one"));
//
//                                    }
//                                });
//
//                                ConstraintLayout cl_move_object = layout.findViewById(R.id.cl_move_object);
//                                cl_move_object.setOnClickListener( new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Log.d("onclick-file", "move");
//                                        v.setBackgroundColor(ContextCompat.getColor(MainActivity.this, colorPrimary));
//
//                                    }
//                                });
//
//                                ConstraintLayout cl_download_object = layout.findViewById(R.id.cl_download_object);
//                                cl_download_object.setOnClickListener( new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Log.d("onclick-file", "download");
//                                        v.setBackgroundColor(ContextCompat.getColor(MainActivity.this, colorPrimary));
//
//                                        new DownloadFile().execute("", objects.get(index).trim());
//
//                                        pw.dismiss();
//                                        Toast.makeText(MainActivity.this, "Trwa pobieranie pliku " + objects.get(index), Toast.LENGTH_SHORT).show();
//
//
//                                    }
//                                });
//
//                                final ConstraintLayout cl_details_object = layout.findViewById(R.id.cl_details_object);
//                                cl_details_object.setOnClickListener( new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Log.d("onclick-file", "details");
//                                        v.setBackgroundColor(ContextCompat.getColor(MainActivity.this, colorPrimary));
//
//                                    }
//                                });
//
//                                ConstraintLayout cl_remove_object = layout.findViewById(R.id.cl_remove_object);
//                                cl_remove_object.setOnClickListener( new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Log.d("onclick-file", "remove");
//                                        v.setBackgroundColor(ContextCompat.getColor(MainActivity.this, colorPrimary));
//
//                                    }
//                                });
//
//                                ImageView iv_close = layout.findViewById(R.id.iv_close);
//                                iv_close.setOnClickListener( new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        pw.dismiss();
//
//                                    }
//                                });
//
//                            }
//                        });
                    }
                }
            }

            ll_global.setVisibility(View.VISIBLE);
            pb_global.setVisibility(View.GONE);
            iv_refresh.setEnabled(true);
            tv_search.setEnabled(true);

        }
    }

    @Override
    public void onBackPressed() {
        if(!PATH.equals("/")){
            String [] path = PATH.split("/");
            PATH = TextUtils.join("/", Arrays.copyOf(path, path.length-1))+"/";
            if(PATH.equals("/")){
                tv_search.setVisibility(View.VISIBLE);
                tv_root.setVisibility(View.GONE);
            }else{
                tv_search.setVisibility(View.GONE);
                tv_root.setVisibility(View.VISIBLE);
                Log.d("ppp", path[path.length-2]);
                tv_root.setText(path[path.length-2]);
            }
            new ObjectsListing().execute();
        }else{
            this.finishAffinity();
        }
    }

    void AnimationImage(ImageView iv){
        RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setDuration(3000);
        rotate.setInterpolator(new LinearInterpolator());
        iv.startAnimation(rotate);
    }

    View.OnClickListener f_nav = new View.OnClickListener() {
        public void onClick(View v) {
            drawer_layout.openDrawer(Gravity.LEFT);
        }
    };

    public String add_white_character(String __str){
        int __numb = 8 -__str.length();
        StringBuilder __result = new StringBuilder();
        for(int i=0; i>__numb; i++){
            __result.append(" ");
        }
        return __result + __str;
    }
}