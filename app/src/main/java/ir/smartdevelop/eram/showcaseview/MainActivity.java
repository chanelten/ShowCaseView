package ir.smartdevelop.eram.showcaseview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.Position;

public class MainActivity extends AppCompatActivity {

    private GuideView mGuideView;
    private GuideView.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View view1 = findViewById(R.id.view1);
        final View view2 = findViewById(R.id.view2);
        final View view3 = findViewById(R.id.view3);
        final View view4 = findViewById(R.id.view4);
        final View view5 = findViewById(R.id.view5);

        builder = new GuideView.Builder(MainActivity.this)
                .setTitle(getString(R.string.title))
                .setTitleTextSize(15)
                .setTitleTextColor(getResources().getColor(R.color.colorAccent))
                .setTitleGravity(Gravity.RIGHT)
                .setContentText(getString(R.string.description))
                .setContentTextColor(getResources().getColor(R.color.colorPrimary))
                .setContentGravity(Gravity.LEFT)
                .setRadius(20)
                .setBorder(getResources().getColor(R.color.colorBorder), 10.0f)
                .setGravity(GuideView.Gravity.center)
                .setDismissType(GuideView.DismissType.outside)
                .setTargetView(view1)
                //.setBackgroundColor(getResources().getColor(android.R.color.transparent))
                .setIndicator(R.mipmap.arrow)
                .setIndicatorMarginStart(0)
                .setCloseButton(Position.Right, R.mipmap.close)
                .setGuideListener(new GuideView.GuideListener() {
                    @Override
                    public boolean onDismiss(View view) {
                        switch (view.getId()){
                            case R.id.view1:
                                builder.setTargetView(view2).build();
                                break;
                            case R.id.view2:
                                builder.setTargetView(view3).build();
                                break;
                            case R.id.view3:
                                builder.setTargetView(view4).build();
                                break;
                            case R.id.view4:
                                builder.setTargetView(view5).build();
                                break;
                            case R.id.view5:
                                return true;
                        }
                        mGuideView = builder.build();
                        mGuideView.show();
                        return false;
                    }
                });

        mGuideView = builder.build();
        mGuideView.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}
