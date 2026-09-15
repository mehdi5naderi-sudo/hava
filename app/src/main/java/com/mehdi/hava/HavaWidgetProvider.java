package com.mehdi.hava;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HavaWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_REFRESH = "com.mehdi.hava.REFRESH";

    private static final String[] DEFAULT_KEYS = {
            "price_dollar_rl", "price_eur", "geram18", "sekee", "ons"
    };
    private static final String[] DEFAULT_LABELS_FA = {
            "دلار", "یورو", "طلا ۱۸", "سکه", "انس"
    };
    private static final String[] DEFAULT_LABELS_EN = {
            "USD", "EUR", "Gold 18", "Coin", "Ounce"
    };

    private static final int[] LABEL_IDS = {R.id.label1, R.id.label2, R.id.label3, R.id.label4, R.id.label5};
    private static final int[] PRICE_IDS = {R.id.price1, R.id.price2, R.id.price3, R.id.price4, R.id.price5};
    private static final int[] PCT_IDS = {R.id.pct1, R.id.pct2, R.id.pct3, R.id.pct4, R.id.pct5};

    private static final int GREEN = Color.rgb(61, 220, 151);
    private static final int RED = Color.rgb(255, 107, 107);
    private static final int GOLD = Color.rgb(201, 162, 39);
    private static final int GOLD_DIM = Color.rgb(139, 115, 85);
    private static final int BG = Color.rgb(13, 13, 13);

    private static final Map<String, String> LABELS_FA = new HashMap<>();
    private static final Map<String, String> LABELS_EN = new HashMap<>();
    static {
        LABELS_FA.put("price_dollar_rl", "دلار");
        LABELS_FA.put("price_eur", "یورو");
        LABELS_FA.put("price_aed", "درهم");
        LABELS_FA.put("geram18", "طلا ۱۸");
        LABELS_FA.put("sekee", "سکه");
        LABELS_FA.put("ons", "انس");
        LABELS_FA.put("crypto-tether-irr", "تتر");
        LABELS_FA.put("oil_brent", "نفت");
        LABELS_FA.put("ime_fund_kahroba", "کهربا");
        LABELS_FA.put("ime_fund_ayar", "عیار");

        LABELS_EN.put("price_dollar_rl", "USD");
        LABELS_EN.put("price_eur", "EUR");
        LABELS_EN.put("price_aed", "AED");
        LABELS_EN.put("geram18", "Gold 18");
        LABELS_EN.put("sekee", "Coin");
        LABELS_EN.put("ons", "Ounce");
        LABELS_EN.put("crypto-tether-irr", "USDT");
        LABELS_EN.put("oil_brent", "Brent");
        LABELS_EN.put("ime_fund_kahroba", "Kahroba");
        LABELS_EN.put("ime_fund_ayar", "Ayar");
    }

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) {
            m.updateAppWidget(id, buildViews(c, id));
            refresh(c, new int[]{id});
        }
    }

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        if (ACTION_REFRESH.equals(i.getAction())) {
            int[] ids = AppWidgetManager.getInstance(c)
                    .getAppWidgetIds(new ComponentName(c, HavaWidgetProvider.class));
            refresh(c, ids);
        }
    }

    public static RemoteViews buildViews(Context c, int id) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        android.content.SharedPreferences p = c.getSharedPreferences("widget_" + id, Context.MODE_PRIVATE);

        int bg = p.getInt("bgColor", BG);
        int muted = p.getInt("mutedColor", GOLD_DIM);
        int pad = p.getInt("padding", 8);
        float priceSize = p.getInt("priceSize", 16);
        float pctSize = p.getInt("pctSize", 11);
        float labelSize = p.getInt("labelSize", 12);
        boolean showPct = p.getBoolean("showPct", true);
        boolean en = "en".equals(p.getString("lang", "fa"));

        v.setInt(R.id.root, "setBackgroundColor", bg);
        v.setViewPadding(R.id.root, pad, pad, pad, pad);
        v.setTextColor(R.id.headerTitle, GOLD);
        v.setTextViewText(R.id.headerTitle, en ? "Hava" : "هوا");
        v.setTextColor(R.id.requestTime, muted);
        v.setTextColor(R.id.settingsButton, muted);

        for (int i = 0; i < 5; i++) {
            String key = p.getString("key" + i, DEFAULT_KEYS[i]);
            String label = en
                    ? LABELS_EN.getOrDefault(key, DEFAULT_LABELS_EN[i])
                    : LABELS_FA.getOrDefault(key, DEFAULT_LABELS_FA[i]);
            v.setTextViewText(LABEL_IDS[i], label);
            boolean isGold = "geram18".equals(key) || "sekee".equals(key) || "ons".equals(key);
            v.setTextColor(LABEL_IDS[i], isGold ? GOLD : GOLD_DIM);
            v.setTextViewTextSize(LABEL_IDS[i], 2, labelSize);
            v.setTextViewTextSize(PRICE_IDS[i], 2, priceSize);
            v.setTextViewTextSize(PCT_IDS[i], 2, pctSize);
            v.setViewVisibility(PCT_IDS[i], showPct ? View.VISIBLE : View.GONE);
        }

        Intent r = new Intent(c, HavaWidgetProvider.class)
                .setAction(ACTION_REFRESH)
                .setData(Uri.parse("hava://refresh/" + id));
        PendingIntent rp = PendingIntent.getBroadcast(c, id, r,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.root, rp);

        Intent s = new Intent(c, WidgetSettingsActivity.class)
                .setAction("com.mehdi.hava.SETTINGS")
                .setData(Uri.parse("hava://settings/" + id))
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        PendingIntent sp = PendingIntent.getActivity(c, id + 10000, s,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.settingsButton, sp);

        return v;
    }

    private void refresh(Context c, int[] ids) {
        if (ids == null || ids.length == 0) return;
        new Thread(() -> {
            android.content.SharedPreferences p = c.getSharedPreferences("widget_" + ids[0], Context.MODE_PRIVATE);
            String[] keys = new String[5];
            StringBuilder list = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                keys[i] = p.getString("key" + i, DEFAULT_KEYS[i]);
                if (i > 0) list.append(',');
                list.append(keys[i]);
            }
            Map<String, JSONObject> data = new HashMap<>();
            boolean success = false;
            String requestTime = now();
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL("https://api.tgju.org/v1/widget/tmp?keys=" + list).openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(10000);
                con.setReadTimeout(10000);
                con.setUseCaches(false);
                con.setRequestProperty("Cache-Control", "no-cache");
                if (con.getResponseCode() != 200) throw new Exception();
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                JSONArray a = new JSONObject(sb.toString()).optJSONObject("response").optJSONArray("indicators");
                if (a == null) throw new Exception();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.optJSONObject(i);
                    if (o != null) putAliases(data, o);
                }
                for (String k : keys) if (data.containsKey(k)) success = true;
            } catch (Exception ignored) {
            } finally {
                if (con != null) con.disconnect();
            }
            final boolean ok = success;
            new Handler(Looper.getMainLooper()).post(() -> {
                AppWidgetManager m = AppWidgetManager.getInstance(c);
                if (ok) for (int id : ids) update(c, m, id, data, requestTime);
            });
        }).start();
    }

    private static void putAliases(Map<String, JSONObject> d, JSONObject o) {
        for (String f : new String[]{"key", "slug", "name", "symbol", "id"}) {
            String x = o.optString(f, "");
            if (!x.isEmpty()) d.put(x, o);
        }
    }

    private void update(Context c, AppWidgetManager m, int id, Map<String, JSONObject> d, String request) {
        RemoteViews v = buildViews(c, id);
        android.content.SharedPreferences p = c.getSharedPreferences("widget_" + id, Context.MODE_PRIVATE);
        boolean en = "en".equals(p.getString("lang", "fa"));

        for (int i = 0; i < 5; i++) {
            String k = p.getString("key" + i, DEFAULT_KEYS[i]);
            JSONObject o = d.get(k);
            String price = "—";
            String pct = "—";
            int color = Color.LTGRAY;

            if (o != null) {
                price = price(o, k);
                double dp = o.optDouble("dp", Double.NaN);
                String dt = o.optString("dt", "");
                if (!Double.isNaN(dp)) {
                    String arrow = "high".equalsIgnoreCase(dt) ? "▲ " : "low".equalsIgnoreCase(dt) ? "▼ " : "● ";
                    pct = arrow + String.format(Locale.US, "%.2f%%", Math.abs(dp));
                    color = "high".equalsIgnoreCase(dt) ? GREEN : "low".equalsIgnoreCase(dt) ? RED : GOLD;
                }
            }

            v.setTextViewText(PRICE_IDS[i], digits(price, en));
            v.setTextColor(PRICE_IDS[i], Color.rgb(245, 240, 230));
            v.setTextViewText(PCT_IDS[i], digits(pct, en));
            v.setTextColor(PCT_IDS[i], color);
        }

        v.setTextViewText(R.id.requestTime, digits(request, en));
        m.updateAppWidget(id, v);
    }

    private static String price(JSONObject o, String k) {
        try {
            double n;
            if ("ons".equals(k) || "oil_brent".equals(k)) {
                n = Double.parseDouble(o.optString("p", "0").replace(",", ""));
            } else if ("crypto-tether-irr".equals(k) && o.has("p_irr")) {
                n = Double.parseDouble(o.optString("p_irr", "0").replace(",", "")) / 10.0;
            } else {
                n = Double.parseDouble(o.optString("p", "0").replace(",", "")) / 10.0;
            }
            return new DecimalFormat(("ons".equals(k) || "oil_brent".equals(k)) ? "#,##0.00" : "#,##0").format(n);
        } catch (Exception e) {
            return "—";
        }
    }

    private static String now() {
        return new java.text.SimpleDateFormat("HH:mm", Locale.US).format(new java.util.Date());
    }

    private static String digits(String s, boolean en) {
        if (s == null) return "—";
        if (en) {
            return s.replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3')
                    .replace('۴', '4').replace('۵', '5').replace('۶', '6').replace('۷', '7')
                    .replace('۸', '8').replace('۹', '9').replace('٬', ',').replace('٫', '.').replace('٪', '%');
        }
        return s.replace('0', '۰').replace('1', '۱').replace('2', '۲').replace('3', '۳')
                .replace('4', '۴').replace('5', '۵').replace('6', '۶').replace('7', '۷')
                .replace('8', '۸').replace('9', '۹').replace(',', '٬').replace('.', '٫').replace('%', '٪');
    }
}
