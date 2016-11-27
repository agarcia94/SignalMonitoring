package com.example.alexperez.alarmnotifier;

/**
 * Created by Alex Perez on 10/7/2016.
 */
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.splash);
        final MediaPlayer intro = MediaPlayer.create(this,R.raw.attis);

        final ImageView iv = (ImageView) findViewById(R.id.imageView);
        final Animation an = AnimationUtils.loadAnimation(getBaseContext(), R.anim.hyperspace_jump);
        final Animation an2 = AnimationUtils.loadAnimation(getBaseContext(),R.anim.disappear);

        iv.startAnimation(an);
        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //Audio Manager to set Boot Music at Maximum
                //-----------------------------------------------------------------------------------------------------------------------
                final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                //final int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                //-----------------------------------------------------------------------------------------------------------------------

                intro.setAudioStreamType(AudioManager.STREAM_MUSIC);
                //intro.setVolume(1.0f, 1.0f);
                intro.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv.startAnimation(an2);
                finish();
                intro.stop();
                Intent i = new Intent(getBaseContext(),Login.class);
                startActivity(i);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}

