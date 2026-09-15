# Hava — ویجت قیمت ارز و طلا

ویجت صفحه اصلی اندروید برای نمایش لحظه‌ای قیمت ارز، طلا، سکه و چند شاخص دیگر از [tgju.org](https://www.tgju.org).

## ویژگی‌ها
- ۵ ردیف قابل تنظیم (دلار، طلای ۱۸، تتر، انس، نفت و ...)
- درصد تغییر با رنگ سبز/قرمز/زرد
- تنظیمات کامل: اندازه فونت، رنگ پس‌زمینه، زبان فارسی/انگلیسی
- رفرش با لمس ویجت
- بدون تبلیغات

## ساخت APK

### با GitHub Actions
1. تب **Actions** → ورک‌فلو **Build Hava Widget APK**
2. **Run workflow**
3. از Artifacts فایل APK را دانلود کن

### محلی
```bash
git clone https://github.com/mehdi5naderi-sudo/hava.git
cd hava
gradle assembleDebug
# خروجی: app/build/outputs/apk/debug/app-debug.apk
```

## نصب
1. APK را نصب کن
2. صفحه اصلی → Widgets → **Hava**
3. برای تنظیمات روی ⚙ داخل ویجت بزن

منبع داده: `api.tgju.org`
