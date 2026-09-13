import json
import urllib.request
from datetime import datetime
from zoneinfo import ZoneInfo

LAT = 35.6892
LON = 51.3890
ALT = 1200
URL = (
    "https://api.met.no/weatherapi/locationforecast/2.0/compact"
    f"?lat={LAT}&lon={LON}&altitude={ALT}"
)
USER_AGENT = "hava-weather-comparison/1.0 (https://github.com/mehdi5naderi-sudo/hava)"

req = urllib.request.Request(URL, headers={"User-Agent": USER_AGENT})
with urllib.request.urlopen(req, timeout=30) as response:
    raw = json.load(response)

items = []
for item in raw.get("properties", {}).get("timeseries", []):
    data = item.get("data", {})
    instant = data.get("instant", {}).get("details", {})
    next1 = data.get("next_1_hours", {}).get("details", {})
    next6 = data.get("next_6_hours", {}).get("details", {})
    items.append({
        "time": item.get("time"),
        "temperature": instant.get("air_temperature"),
        "wind_speed": instant.get("wind_speed"),
        "precipitation_1h": next1.get("precipitation_amount"),
        "precipitation_6h": next6.get("precipitation_amount"),
        "symbol": next1.get("symbol_code"),
    })

result = {
    "source": "MET Norway",
    "location": {"name": "Tehran", "lat": LAT, "lon": LON, "altitude": ALT},
    "fetched_at": datetime.now(ZoneInfo("Asia/Tehran")).isoformat(),
    "timeseries": items,
}

with open("metno.json", "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, separators=(",", ":"))

print(f"Fetched {len(items)} MET Norway forecast points")
