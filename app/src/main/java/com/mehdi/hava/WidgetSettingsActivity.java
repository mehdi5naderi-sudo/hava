package com.mehdi.hava;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WidgetSettingsActivity extends Activity {
    // Order matches v2 defaults: dollar, euro, gold18, coin, ons first
    private static final String[] KEYS = {
            "price_dollar_rl", "price_eur", "geram18", "sekee", "ons",
            "crypto-tether-irr", "price_aed", "oil_brent", "ime_fund_kahroba", "ime_fund_ayar"
    };
    private static final String[] NAMES = {
            "دلار", "یورو", "طلا ۱۸", "سکه", "انس",
            "تتر", "درهم", "نفت برنت", "کهربا", "عیار"
    };

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private boolean launchedFromIcon = false;
    private Spinner[] spinners = new Spinner[5];
    private EditText priceSize, pctSize, padding, bgColor;
    private Spinner language;
    private Switch showPct;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            launchedFromIcon = true;
            int[] ids = getWidgetIds();
            widgetId = ids.length > 0 ? ids[0] : 0;
        }
        setResult(RESULT_CANCELED);
        buildUi();
        load();
    }

    private int[] getWidgetIds() {
        return AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, HavaWidgetProvider.class));
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(20));
        scroll.addView(root);

        TextView title = label("تنظیمات هوا — ارز و طلا");
        title.setTextSize(20);
        title.setTextColor(Color.parseColor("#D4AF37"));
        root.addView(title, lp());

        root.addView(label("ترتیب ۵ ردیف"), lpTop());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, NAMES);
        for (int i = 0; i < 5; i++) {
            spinners[i] = new Spinner(this);
            spinners[i].setAdapter(adapter);
            root.addView(spinners[i], lp());
        }

        root.addView(label("اندازه فونت"), lpTop());
        priceSize = field(root, "قیمت (sp)", "16");
        pctSize = field(root, "درصد (sp)", "11");

        root.addView(label("زبان"), lpTop());
        language = spinner(root, new String[]{"فارسی", "English"});

        showPct = sw(root, "نمایش درصد تغییر", true);

        root.addView(label("ظاهر"), lpTop());
        bgColor = field(root, "رنگ پس‌زمینه (HEX)", "#0D0D0D");
        padding = field(root, "فاصله داخلی (dp)", "8");

        Button save = new Button(this);
        save.setText("ذخیره و اعمال");
        save.setOnClickListener(v -> save());
        root.addView(save, lpTop());

        setContentView(scroll);
    }

    private Spinner spinner(LinearLayout root, String[] values) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values));
        root.addView(s, lp());
        return s;
    }

    private Switch sw(LinearLayout root, String text, boolean val) {
        Switch s = new Switch(this);
        s.setText(text);
        s.setChecked(val);
        root.addView(s, lpTop());
        return s;
    }

    private EditText field(LinearLayout root, String hint, String val) {
        root.addView(label(hint), lp());
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setText(val);
        e.setSelectAllOnFocus(true);
        root.addView(e, lp());
        return e;
    }

    private TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(14);
        t.setTextColor(Color.DKGRAY);
        return t;
    }

    private LinearLayout.LayoutParams lp() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams lpTop() {
        LinearLayout.LayoutParams p = lp();
        p.topMargin = dp(12);
        return p;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void load() {
        android.content.SharedPreferences p = getSharedPreferences("widget_" + widgetId, Context.MODE_PRIVATE);
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < KEYS.length; i++) idx.put(KEYS[i], i);

        String[] defaults = {"price_dollar_rl", "price_eur", "geram18", "sekee", "ons"};
        for (int i = 0; i < 5; i++) {
            String key = p.getString("key" + i, defaults[i]);
            spinners[i].setSelection(idx.containsKey(key) ? idx.get(key) : i);
        }

        priceSize.setText(String.valueOf(p.getInt("priceSize", 16)));
        pctSize.setText(String.valueOf(p.getInt("pctSize", 11)));
        language.setSelection("en".equals(p.getString("lang", "fa")) ? 1 : 0);
        showPct.setChecked(p.getBoolean("showPct", true));
        try {
            bgColor.setText(String.format(Locale.US, "#%06X", p.getInt("bgColor", 0x0D0D0D) & 0xFFFFFF));
        } catch (Exception e) {
            bgColor.setText("#0D0D0D");
        }
        padding.setText(String.valueOf(p.getInt("padding", 8)));
    }

    private int num(EditText e, int def, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(e.getText().toString().trim())));
        } catch (Exception x) {
            return def;
        }
    }

    private int color(String s, int def) {
        try {
            return Color.parseColor(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private void save() {
        android.content.SharedPreferences.Editor e = getSharedPreferences("widget_" + widgetId, Context.MODE_PRIVATE).edit();
        for (int i = 0; i < 5; i++) {
            e.putString("key" + i, KEYS[spinners[i].getSelectedItemPosition()]);
        }
        e.putInt("priceSize", num(priceSize, 16, 10, 28))
                .putInt("pctSize", num(pctSize, 11, 8, 18))
                .putString("lang", language.getSelectedItemPosition() == 1 ? "en" : "fa")
                .putBoolean("showPct", showPct.isChecked())
                .putInt("bgColor", color(bgColor.getText().toString(), 0xFF0D0D0D))
                .putInt("padding", num(padding, 8, 2, 20))
                .apply();

        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        if (launchedFromIcon) {
            for (int id : getWidgetIds()) {
                if (id != widgetId) copySettings(widgetId, id);
                manager.updateAppWidget(id, HavaWidgetProvider.buildViews(this, id));
            }
        } else if (widgetId != 0) {
            manager.updateAppWidget(widgetId, HavaWidgetProvider.buildViews(this, widgetId));
        }

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private void copySettings(int fromId, int toId) {
        android.content.SharedPreferences from = getSharedPreferences("widget_" + fromId, Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor to = getSharedPreferences("widget_" + toId, Context.MODE_PRIVATE).edit();
        for (int i = 0; i < 5; i++) {
            to.putString("key" + i, from.getString("key" + i, KEYS[i]));
        }
        to.putInt("priceSize", from.getInt("priceSize", 16))
                .putInt("pctSize", from.getInt("pctSize", 11))
                .putString("lang", from.getString("lang", "fa"))
                .putBoolean("showPct", from.getBoolean("showPct", true))
                .putInt("bgColor", from.getInt("bgColor", 0xFF0D0D0D))
                .putInt("padding", from.getInt("padding", 8))
                .apply();
    }
}
