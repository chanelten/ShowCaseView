package smartdevelop.ir.eram.showcaseviewlib.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by John Oliver Magdaleno on 05/11/2018.
 * Copyright © 2018 TenTen Technologies Limited. All rights reserved.
 */

public class PersistentData {

    private static PersistentData singleTon;

    public static PersistentData instance(Context context) {
        if (singleTon == null) {
            singleTon = new PersistentData(context);
        }
        return singleTon;
    }

    private final SharedPreferences sharedPreferences;
    private final String SHOW_VIEW_KEY = "SHOW_VIEW";

    public PersistentData(Context context) {
        sharedPreferences = context.getSharedPreferences("smartdevelop.ir.eram.showcaseview", Context.MODE_PRIVATE);
    }

    public void setShowView(boolean show) {
        sharedPreferences.edit().putBoolean(SHOW_VIEW_KEY, show).apply();
    }

    public boolean showView() {
        return sharedPreferences.getBoolean(SHOW_VIEW_KEY, true);
    }
}
