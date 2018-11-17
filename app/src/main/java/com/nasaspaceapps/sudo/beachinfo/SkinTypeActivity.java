package com.nasaspaceapps.sudo.beachinfo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class SkinTypeActivity extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_type);
        imageView = findViewById(R.id.imageView1);

        Glide.with(this).load(R.drawable.skintype).into(imageView);
    }
}
