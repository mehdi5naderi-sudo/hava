import json
import urllib.request
from datetime import datetime
from zoneinfo import ZoneInfo

LAT = 35.6892
LON = 51.3890
URL = (
    "https://www.7timer.info/bin/api.pl"
    f"?lon={LON}&lat={LAT}&product=meteo&output=json&unit=metric&lang=en"
)

with urllib.request.urlopen(URL, timeout=30) as response:
    raw = json.load(response)

if not raw.get("dataseries"):
    raise RuntimeError("7Timer returned no dataseries")

result = {
    "source": "7Timer!",
    "location": {"name": "Tehran", "lat": LAT, "lon": LON},
    "fetched_at": datetime.now(ZoneInfo("Asia/Tehran")).isoformat(),
    "data": raw,
}

with open("7timer.json", "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, separators=(",", ":"))

print(f"Fetched {len(raw['dataseries'])} 7Timer forecast points")
