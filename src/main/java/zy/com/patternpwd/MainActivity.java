package zy.com.patternpwd;

import android.graphics.PixelFormat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final PatternView view = (PatternView) findViewById(R.id.patternview);
        view.setZOrderOnTop(true);
        view.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        view.setListener(new PwdListener() {
            @Override
            public void patternStart() {

            }

            @Override
            public void patternAdd(int point) {

            }

            @Override
            public void patternEnd(List<Integer> pattern) {
                StringBuilder builder = new StringBuilder();
                for (Integer i : pattern){
                    builder.append(i).append(",");
                }
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("pattern finish : " + builder.toString())
                        .create();
                dialog.show();
                view.clearAll();
            }
        });
    }
}
