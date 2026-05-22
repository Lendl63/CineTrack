package com.tp.cinetrack;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

  private Handler splashHandler;
  private Runnable splashRunnable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    // Blocage de la rotation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    // Handler utilisant le looper principal
    splashHandler = new Handler(getMainLooper());
    splashRunnable = new Runnable() {
      @Override
      public void run() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Ferme l'activité Splash
      }
    };
    splashHandler.postDelayed(splashRunnable, 3500); // 3,5 secondes
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Évite l'exécution du callback si l'activité est détruite
    if (splashHandler != null && splashRunnable != null) {
      splashHandler.removeCallbacks(splashRunnable);
    }
  }
}