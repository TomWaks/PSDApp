package com.tomwaks.psdapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    ImageView iv_refresh;
    DrawerLayout drawer_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView iv_logo = findViewById(R.id.iv_logo);
        AnimationImage(iv_logo);

        ImageView btn_nav = findViewById(R.id.iv_nav);
        btn_nav.setOnClickListener(f_nav);

        drawer_layout = findViewById(R.id.drawer_layout);

        iv_refresh = findViewById(R.id.iv_refresh);
//        iv_refresh.setOnClickListener(f_refresh);

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
}