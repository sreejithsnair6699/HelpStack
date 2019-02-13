package centennial.ca.helpstack;

import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        final Intent intent = new Intent(this, StartPage.class);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(SplashScreen.this, findViewById(R.id.app_logo),
                                "transition_logo");
                        startActivity(intent, compat.toBundle());

                        finishAffinity();
                    }
                }, 1000);
    }
}
