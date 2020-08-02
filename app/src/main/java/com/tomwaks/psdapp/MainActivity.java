package com.tomwaks.psdapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawer_layout;
    ConstraintLayout cl_main, cl_search, cl_more;
    LinearLayout ll_main, ll_global, ll_no_objects;
    ProgressBar pb_global, pb_storage;
    TextView tv_search, tv_root, tv_storage, tv_info;
    ImageView iv_refresh, iv_refresh_more, iv_close, iv_back, iv_nav;
    EditText et_search;

    String URL = "", IP, API, PATH = "/", LAST_PHRASE="", CURRENT_PHRASE="";
    Boolean STATUS_SEARCH = false, SEARCH_ACTIVE = false, ENABLE_BACK_BUTTON = true;
    int INDEX_OBJECT;
    long STORAGE_USED = 0;
    long STORAGE_FREE = 0;

    List<String> objects = new ArrayList<String>(); // folders and files
    List<Integer> type_objects = new ArrayList<Integer>(); // 1=folder and 0=file
    List<String> size_objects = new ArrayList<String>(); // size of file or -1 for folder
    List<String> date_objects = new ArrayList<String>(); // date of file or date of folder
    List<Integer> numbs_objects = new ArrayList<Integer>(); // numbers of files and folder in main folder or -1 for files
    List<String> path_objects = new ArrayList<String>(); // path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IP =  getIntent().getStringExtra("IP");
        API =  getIntent().getStringExtra("API");

        cl_main = findViewById(R.id.cl_main);
        cl_search = findViewById(R.id.cl_search);
        cl_more = findViewById(R.id.cl_more);


        ImageView iv_logo = findViewById(R.id.iv_logo);
        AnimationImage(iv_logo);

        iv_nav = findViewById(R.id.iv_nav);
        iv_nav.setOnClickListener(f_nav);

        drawer_layout = findViewById(R.id.drawer_layout);

        ll_no_objects = findViewById(R.id.ll_no_objects);
        tv_info = findViewById(R.id.tv_info);

        iv_refresh = findViewById(R.id.iv_refresh);
        iv_refresh_more = findViewById(R.id.iv_refresh_more);
        iv_refresh.setOnClickListener(f_refresh);
        iv_refresh_more.setOnClickListener(f_refresh);

        iv_close = findViewById(R.id.iv_close_search);
        iv_close.setOnClickListener(f_close_search);
//
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(f_back);
//
////        iv_back = findViewById(R.id.iv_back);
//        iv_back.setOnClickListener(f_close_search);

        tv_search = findViewById(R.id.tv_search);
        tv_search.setOnClickListener(f_search);

        tv_root = findViewById(R.id.tv_root);

        et_search = findViewById(R.id.et_search);
        et_search.addTextChangedListener(f_et_search);


        ll_global = findViewById(R.id.ll_global);
        ll_main = findViewById(R.id.ll_main);
        pb_global = findViewById(R.id.pb_global);


        /**
         * drawer
         */
        tv_storage = drawer_layout.findViewById(R.id.tv_storage);
        pb_storage = drawer_layout.findViewById(R.id.pb_storage);

        new ObjectsListing().execute();
        new Storage().execute();

    }

    private class ObjectsListing extends AsyncTask<String, Integer, String> {

        OkHttpClient client = new OkHttpClient();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ENABLE_BACK_BUTTON = false;
            iv_refresh.setEnabled(false);
            iv_refresh_more.setEnabled(false);
            tv_search.setEnabled(false);
            ll_main.removeAllViews();
            pb_global.setVisibility(View.VISIBLE);
            ll_no_objects.setVisibility(View.GONE);

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
            ll_no_objects.setVisibility(View.GONE);
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
                                cl_more.setVisibility(View.VISIBLE);
                                cl_main.setVisibility(View.GONE);
                                cl_main.setVisibility(View.GONE);

                                tv_root.setText(objects.get(index));
                                new ObjectsListing().execute();
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

                        INDEX_OBJECT = i;


                        iv_icon_action.setOnClickListener(f_more_action_file);

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



                if(objects.size() == 0){
                    ll_no_objects.setVisibility(View.VISIBLE);
                    tv_info.setText(getResources().getString(R.string.main_no_objects));
                }
            }

            ll_global.setVisibility(View.VISIBLE);
            pb_global.setVisibility(View.GONE);
            iv_refresh.setEnabled(true);
            iv_refresh_more.setEnabled(true);
            tv_search.setEnabled(true);
            ENABLE_BACK_BUTTON = true;

        }
    }

    TextWatcher f_et_search = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            CURRENT_PHRASE = et_search.getText().toString();
            ll_main.removeAllViews();
            if(!STATUS_SEARCH) {
                STATUS_SEARCH = true;
                new SearchObjectListing().execute();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private class SearchObjectListing extends AsyncTask<String, Integer, String> {

        OkHttpClient client = new OkHttpClient();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ENABLE_BACK_BUTTON = false;
            objects.clear();
            type_objects.clear();
            path_objects.clear();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                LAST_PHRASE = CURRENT_PHRASE;
                String search_url = ("http://"+ IP +"/search.php?key_api="+ API +"&&phrase="+CURRENT_PHRASE).replaceAll(" ", "%20");
                Response response = client.newCall(new Request.Builder()
                        .url(search_url)
                        .build()).execute();
                if (!response.isSuccessful()) {
                    Log.d("executed-url-result", "null");
                    return null;
                }

                Log.d("URLURLURL", search_url);
                JSONObject jA = new JSONObject(response.body().string());
                JSONArray jData = new JSONArray(jA.get("data").toString());

                if(jA.get("status").equals(1)){
                    for(int i=0; i < jData.length(); i++){
                        Log.d("status--", jA.get("counter").toString());
                        objects.add(jData.getJSONObject(i).getString("name_object"));
                        type_objects.add(jData.getJSONObject(i).getInt("type_object"));
                        path_objects.add(jData.getJSONObject(i).getString("path_object"));
                    }



                    Log.d("123123123--------", objects.toString());
                    Log.d("123123123--------", type_objects.toString());
                    Log.d("123123123--------", size_objects.toString());
                    Log.d("123123123--------", date_objects.toString());
                    Log.d("123123123--------", numbs_objects.toString());
                    Log.d("123123123--------", path_objects.toString());
                    return "1";
                }else{
                    return "0";
                }
            } catch (UnknownHostException e) {
                Log.d("status--ConnectExcep", e.toString());
                return "-1";
            } catch (ConnectException | SocketTimeoutException e){
                Log.d("status--ConnectExcep", e.toString());
                return "-2";
            } catch (IllegalArgumentException e){
                Log.d("status--ConnectExcep", e.toString());
                return "-3";
            } catch (Exception e){
                Log.d("status--ConnectExcep", e.toString());
                return "-4";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("qwertyuiop", result);
            if(result.equals("1")) {
                ll_main.removeAllViews();
                ll_no_objects.setVisibility(View.GONE);

                if(objects.size() == 0){
                    ll_main.removeAllViews();
                    ll_no_objects.setVisibility(View.VISIBLE);
                }

                for (int i = 0; i < objects.size(); i++) {
                    if (type_objects.get(i) == 1) {
                        ConstraintLayout ll_object = (ConstraintLayout) View.inflate(MainActivity.this, R.layout.ll_object_found, null);
                        ImageView iv_icon_object = ll_object.findViewById(R.id.iv_icon_object);
                        iv_icon_object.setImageResource(R.drawable.ic_folder);
                        ImageView iv_icon_action = ll_object.findViewById(R.id.iv_icon_action);
                        iv_icon_action.setImageResource(R.drawable.ic_more);
                        TextView tv_name_object = ll_object.findViewById(R.id.tv_name_object);
                        tv_name_object.setText(objects.get(i));
                        TextView tv_path_object = ll_object.findViewById(R.id.tv_path_object);
                        tv_path_object.setText("Lokalizacja: " + path_objects.get(i));
                        ll_main.addView(ll_object);
                    }else{
                        ConstraintLayout ll_object = (ConstraintLayout) View.inflate(MainActivity.this, R.layout.ll_object_found, null);
                        ImageView iv_icon_object = ll_object.findViewById(R.id.iv_icon_object);
                        iv_icon_object.setImageResource(R.drawable.ic_file);
                        ImageView iv_icon_action = ll_object.findViewById(R.id.iv_icon_action);
                        iv_icon_action.setImageResource(R.drawable.ic_more);
                        TextView tv_name_object = ll_object.findViewById(R.id.tv_name_object);
                        tv_name_object.setText(objects.get(i));
                        TextView tv_path_object = ll_object.findViewById(R.id.tv_path_object);
                        tv_path_object.setText("Lokalizacja: " + path_objects.get(i));
                        ll_main.addView(ll_object);
                    }
                }
            }
//
//            if(result.equals("0")){
//                ll_main.removeAllViews();
//                ConstraintLayout ll_info = (ConstraintLayout) View.inflate(SearchActivity.this, R.layout.ll_info, null);
//                TextView tv_info = ll_info.findViewById(R.id.tv_info);
//                tv_info.setText("Błąd po stronie serwera");
//                ll_main.addView(ll_info);
//            }
//
//            if(result.equals("-1")){
//                ll_main.removeAllViews();
//                ConstraintLayout ll_info = (ConstraintLayout) View.inflate(SearchActivity.this, R.layout.ll_info, null);
//                TextView tv_info = ll_info.findViewById(R.id.tv_info);
//                tv_info.setText("Podaj poprawne IP serwera");
//                ll_main.addView(ll_info);
//            }
//
//            if(result.equals("-2")){
//                ll_main.removeAllViews();
//                ConstraintLayout ll_info = (ConstraintLayout) View.inflate(SearchActivity.this, R.layout.ll_info, null);
//                TextView tv_info = ll_info.findViewById(R.id.tv_info);
//                tv_info.setText("Nie udało się połączyć z serwerem");
//                ll_main.addView(ll_info);
//            }
//
//            if(result.equals("-3")){
//                ll_main.removeAllViews();
//                ConstraintLayout ll_info = (ConstraintLayout) View.inflate(SearchActivity.this, R.layout.ll_info, null);
//                TextView tv_info = ll_info.findViewById(R.id.tv_info);
//                tv_info.setText("Format URL jest nieprawidłowy");
//                ll_main.addView(ll_info);
//            }
//
//            iv_refresh.setEnabled(true);
//            status_search = false;
//
            STATUS_SEARCH = false;
            if(!LAST_PHRASE.equals(CURRENT_PHRASE)){
                STATUS_SEARCH = true;
                new SearchObjectListing().execute();
            }

            ENABLE_BACK_BUTTON = true;

        }
    }


    private class Storage extends AsyncTask<String, Integer, String> {

        OkHttpClient client = new OkHttpClient();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String search_url = ("http://"+ IP +"/storage.php?key_api="+ API).replaceAll(" ", "%20");
                Response response = client.newCall(new Request.Builder()
                        .url(search_url)
                        .build()).execute();
                if (!response.isSuccessful()) {
                    Log.d("executed-url-result", "null");
                    return null;
                }

                JSONObject jA = new JSONObject(response.body().string());

                if(jA.getInt("status") == 1){
                    Log.d("qwertyuiop", jA.toString());
                    STORAGE_USED = jA.getLong("storage_used");
                    STORAGE_FREE = jA.getLong("storage_free");
                    return "1";
                }else{
                    return "0";
                }
            } catch (UnknownHostException e) {
                Log.d("status--ConnectExcep", e.toString());
                return "-1";
            } catch (ConnectException | SocketTimeoutException e){
                Log.d("status--ConnectExcep", e.toString());
                return "-2";
            } catch (IllegalArgumentException e){
                Log.d("status--ConnectExcep", e.toString());
                return "-3";
            } catch (Exception e){
                Log.d("status--ConnectExcep", e.toString());
                return "-4";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result.equals("1")){
                Log.d("qwertyuiop", STORAGE_USED + "/" + STORAGE_FREE);

                tv_storage.setText(humanReadableByteCountBin(STORAGE_USED) + " of " + humanReadableByteCountBin (STORAGE_FREE) + " (" + 100*STORAGE_USED/STORAGE_FREE + "% full)");
                pb_storage.setProgress((int) (100*STORAGE_USED/STORAGE_FREE));

            }


        }
    }

    @Override
    public void onBackPressed() {
        if(ENABLE_BACK_BUTTON){
            if(!SEARCH_ACTIVE){
                if(!PATH.equals("/")){
                    String [] path = PATH.split("/");
                    PATH = TextUtils.join("/", Arrays.copyOf(path, path.length-1))+"/";
                    if(PATH.equals("/")){
                        cl_main.setVisibility(View.VISIBLE);
                        cl_more.setVisibility(View.GONE);
                    }else{
                        cl_more.setVisibility(View.VISIBLE);
                        cl_main.setVisibility(View.GONE);
                        tv_root.setText(path[path.length-2]);
                    }
                    cl_search.setVisibility(View.GONE);
                    new ObjectsListing().execute();
                }else{
                    this.finishAffinity();
                }
            }else{
                SEARCH_ACTIVE = false;
                cl_main.setVisibility(View.VISIBLE);
                cl_more.setVisibility(View.GONE);
                cl_search.setVisibility(View.GONE);
                PATH = "/";
                new ObjectsListing().execute();
            }
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

    View.OnClickListener f_more_action_file = new View.OnClickListener() {
        public void onClick(View v) {
            LayoutInflater inflater = (LayoutInflater) MainActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_more_actions_file,
                    (ViewGroup) findViewById(R.id.cl_popup_more_action));
            final PopupWindow pw = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            pw.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

            TextView iv_name = layout.findViewById(R.id.tv_name);
            iv_name.setText(objects.get(INDEX_OBJECT));
        }
    };

    View.OnClickListener f_search = new View.OnClickListener() {
        public void onClick(View v) {
            et_search.setText("");
            cl_search.setVisibility(View.VISIBLE);
            cl_main.setVisibility(View.GONE);
            cl_more.setVisibility(View.GONE);
            ll_main.removeAllViews();
            SEARCH_ACTIVE = true;
        }
    };

    View.OnClickListener f_close_search = new View.OnClickListener() {
        public void onClick(View v) {
            cl_main.setVisibility(View.VISIBLE);
            cl_search.setVisibility(View.GONE);
            cl_more.setVisibility(View.GONE);
            PATH = "/";
            SEARCH_ACTIVE = false;
            new ObjectsListing().execute();
        }
    };


    View.OnClickListener f_back = new View.OnClickListener() {
        public void onClick(View v) {
            if(ENABLE_BACK_BUTTON){
                if(!PATH.equals("/")){
                    String [] path = PATH.split("/");
                    PATH = TextUtils.join("/", Arrays.copyOf(path, path.length-1))+"/";
                    if(PATH.equals("/")){
                        cl_main.setVisibility(View.VISIBLE);
                        cl_more.setVisibility(View.GONE);
                    }else{
                        cl_more.setVisibility(View.VISIBLE);
                        cl_main.setVisibility(View.GONE);
                        tv_root.setText(path[path.length-2]);
                    }
                    cl_search.setVisibility(View.GONE);
                    new ObjectsListing().execute();
                }
            }
        }
    };

    View.OnClickListener f_refresh = new View.OnClickListener() {
        public void onClick(View v) {
            new ObjectsListing().execute();
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

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }
}