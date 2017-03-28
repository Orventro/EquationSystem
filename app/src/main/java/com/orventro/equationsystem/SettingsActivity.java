package com.orventro.equationsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener{
    SeekBar accuracyBar;
    TextView accuracyTextView;
    ImageView back;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Button toDefault;
    CheckBox addInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sp = getPreferences(MODE_APPEND);
        editor = sp.edit();

        accuracyBar = (SeekBar) findViewById(R.id.accuracy_bar);
        accuracyBar.setProgress((int)(sp.getFloat("accuracy", 1f)*100));
        accuracyBar.setOnSeekBarChangeListener(this);

        addInfo = (CheckBox) findViewById(R.id.add_info);
        addInfo.setChecked(sp.getBoolean("add_info", false));

        accuracyTextView = (TextView) findViewById(R.id.accuracy_textview);
        accuracyTextView.setText(getString(R.string.accuracy)+" "+Double.toString(accuracyBar.getProgress()/100.0));

        toDefault = (Button) findViewById(R.id.to_default);
        toDefault.setOnClickListener(this);

        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
    }

    private void finishActivity() {
        Intent intent = new Intent();
        intent.putExtra("accuracy", accuracyBar.getProgress()/100f);
        editor.putFloat("accuracy", accuracyBar.getProgress()/100f);

        intent.putExtra("add_info", addInfo.isChecked());
        editor.putBoolean("add_info", addInfo.isChecked());

        editor.commit();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
        if (sb == accuracyBar){
            accuracyTextView.setText(getString(R.string.accuracy)+" "+Double.toString(progress/100.0));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        if (v == toDefault) {
            accuracyBar.setProgress(100);
            accuracyTextView.setText(getString(R.string.accuracy)+" "+Double.toString(1));
            addInfo.setChecked(false);
        }

        if (v == back) finishActivity();
    }
}
