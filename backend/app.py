
#!/usr/bin/env python3
"""EGBE API — real metadata, honest results, no fabricated surveillance."""

import html
import ipaddress
import os
import re
import secrets
import threading
import time
from datetime import datetime, timezone
from urllib.parse import urlparse

import phonenumbers
import requests
from flask import Flask, jsonify, request, redirect, render_template_string
from flask_cors import CORS
from phonenumbers import carrier, geocoder, number_type, PhoneNumberType

app = Flask(__name__)
app.config["JSON_SORT_KEYS"] = False
app.config["MAX_CONTENT_LENGTH"] = 32 * 1024

allowed_origins = [
    item.strip()
    for item in os.getenv("EGBE_ALLOWED_ORIGINS", "").split(",")
    if item.strip()
]
CORS(app, resources={r"/api/*": {"origins": allowed_origins or []}})

HTTP = requests.Session()
HTTP.headers.update({"User-Agent": "EGBE-OSINT/4.0"})
LOCK = threading.RLock()
CAMPAIGNS = {}
CAMPAIGN_TTL_SECONDS = 30 * 24 * 60 * 60
SATELLITE_CACHE = {"expires": 0, "items": []}


def utc_now():
    return datetime.now(timezone.utc).isoformat()


def error(message, status=400):
    return jsonify({"error": message, "timestamp": utc_now()}), status


def clean_text(value, limit=120):
    if not isinstance(value, str):
        return ""
    return value.strip()[:limit]


def valid_http_url(value):
    if not isinstance(value, str) or len(value) > 2048:
        return False
    try:
        parsed = urlparse(value)
        return (
            parsed.scheme in ("https", "http")
            and bool(parsed.hostname)
            and not parsed.username
            and not parsed.password
        )
    except ValueError:
        return False


@app.get("/")
def index():
    return jsonify({
        "name": "EGBE API",
        "version": "4.0.0",
        "status": "online",
        "documentation": "See the project README for endpoints.",
        "capabilities": [
            "Phone-number metadata",
            "Public IP intelligence",
            "Current satellite orbital data",
            "Transparent click-count campaigns"
        ],
        "limitations": [
            "Phone numbers do not reveal live GPS location.",
            "IP geolocation is approximate.",
            "Satellite visibility requires observer coordinates."
        ]
    })


@app.get("/api/health")
def health():
    return jsonify({
        "status": "ok",
        "service": "egbe-api",
        "timestamp": utc_now()
    })


# ---------------- PHONE NUMBER METADATA ----------------

@app.post("/api/trace")
def trace_phone():
    body = request.get_json(silent=True) or {}
    raw_phone = clean_text(body.get("phone"), 40)
    region = clean_text(body.get("region", "NG"), 2).upper() or "NG"

    if not raw_phone:
        return error("Provide a phone number.")

    try:
        parsed = phonenumbers.parse(raw_phone, region)
    except phonenumbers.NumberParseException:
        return error(
            "Could not parse the number. Use international format, "
            "for example +2348012345678."
        )

    valid = phonenumbers.is_valid_number(parsed)
    possible = phonenumbers.is_possible_number(parsed)

    if not possible or not valid:
        return error("The number is not valid according to numbering metadata.")

    e164 = phonenumbers.format_number(
        parsed, phonenumbers.PhoneNumberFormat.E164
    )
    international = phonenumbers.format_number(
        parsed, phonenumbers.PhoneNumberFormat.INTERNATIONAL
    )

    region_code = phonenumbers.region_code_for_number(parsed) or "ZZ"
    country = geocoder.country_name_for_number(parsed, "en") or "Unknown"
    area_description = geocoder.description_for_number(parsed, "en") or "Unknown"
    carrier_name = carrier.name_for_number(parsed, "en") or "Unknown"
    tz_list = phonenumbers.time_zones_for_number(parsed)

    kind = number_type(parsed)
    if kind == PhoneNumberType.MOBILE:
        line_type = "Mobile"
    elif kind == PhoneNumberType.FIXED_LINE:
        line_type = "Fixed line"
    elif kind == PhoneNumberType.FIXED_LINE_OR_MOBILE:
        line_type = "Fixed line or mobile"
    elif kind == PhoneNumberType.VOIP:
        line_type = "VoIP"
    elif kind == PhoneNumberType.TOLL_FREE:
        line_type = "Toll-free"
    elif kind == PhoneNumberType.PREMIUM_RATE:
        line_type = "Premium rate"
    else:
        line_type = "Other / unknown"

    return jsonify({
        "phone": e164,
        "international": international,
        "country": country,
        "countryCode": region_code,
        "city": area_description,
        "carrier": carrier_name,
        "lineType": line_type,
        "valid": True,
        "riskScore": None,
        "location": None,
        "status": "METADATA_ONLY",
        "timestamp": int(time.time() * 1000),
        "notes": [
            "Source: local phonenumbers numbering-plan metadata.",
            "The displayed area is associated with the number range; "
            "it is not the subscriber's confirmed current location.",
            "Carrier identification may be unavailable or outdated, "
            "especially for ported numbers.",
            "A phone number alone does not provide GPS, IMEI, IMSI, "
            "cell towers, HLR status, device status, or social accounts."
        ],
        "timeZones": list(tz_list)
    })


@app.get("/api/trace/live/<trace_id>")
def no_live_tracking(trace_id):
    return error(
        "Live phone tracking is not supported. A phone number does not "
        "provide access to a device's current GPS location.",
        410
    )


@app.get("/api/trace/history")
def trace_history():
    # No phone-number search history is collected by this endpoint.
    return jsonify([])


# ---------------- IP INTELLIGENCE ----------------

@app.post("/api/geolocate/ip")
def geolocate_ip():
    body = request.get_json(silent=True) or {}
    raw_ip = clean_text(body.get("ip"), 64)

    if not raw_ip:
        return error("Provide an IP address.")

    try:
        address = ipaddress.ip_address(raw_ip)
    except ValueError:
        return error("Invalid IPv4 or IPv6 address.")

    if (
        address.is_private
        or address.is_loopback
        or address.is_link_local
        or address.is_multicast
        or address.is_reserved
        or address.is_unspecified
    ):
        return error(
            "This is not a publicly routable IP address; public geolocation "
            "services cannot reliably locate it."
        )

    try:
        response = HTTP.get(
            f"https://ipwho.is/{address.compressed}",
            timeout=(4, 8)
        )
        response.raise_for_status()
        data = response.json()
    except (requests.RequestException, ValueError):
        return error(
            "The IP intelligence provider is unavailable. No fallback "
            "or fabricated location was substituted.",
            502
        )

    if data.get("success") is False or not data.get("ip"):
        return error(
            clean_text(data.get("message"), 200) or "IP lookup failed.",
            502
        )

    connection = data.get("connection") or {}
    timezone_data = data.get("timezone") or {}

    return jsonify({
        "ip": data.get("ip", address.compressed),
        "city": data.get("city") or "Unknown",
        "region": data.get("region") or "Unknown",
        "country": data.get("country") or "Unknown",
        "countryCode": data.get("country_code") or "",
        "isp": connection.get("isp") or "Unknown",
        "org": connection.get("org") or "Unknown",
        "asName": connection.get("org") or "Unknown",
        "lat": data.get("latitude"),
        "lon": data.get("longitude"),
        "timezone": timezone_data.get("id") or "Unknown",
        "mobile": None,
        "proxy": None,
        "hosting": None,
        "source": "ipwho.is",
        "accuracyNotice": (
            "Approximate network/IP registration location, not a person's "
            "or device's precise live location."
        )
    })


# ---------------- SATELLITE ORBITAL DATA ----------------
# Uses current public TLE data from CelesTrak and Skyfield propagation.
# It does not manufacture positions or claim visibility without coordinates.

@app.get("/api/satellites")
def satellites():
    try:
        from skyfield.api import EarthSatellite, load, wgs84
    except ImportError:
        return error(
            "Satellite calculations are unavailable. Install backend "
            "requirements to enable Skyfield.",
            503
        )

    lat_arg = request.args.get("lat")
    lon_arg = request.args.get("lon")
    observer = None

    if lat_arg is not None or lon_arg is not None:
        try:
            lat = float(lat_arg)
            lon = float(lon_arg)
            if not (-90 <= lat <= 90 and -180 <= lon <= 180):
                raise ValueError()
            observer = (lat, lon)
        except (TypeError, ValueError):
            return error("Observer latitude/longitude are invalid.")

    now = time.time()
    with LOCK:
        cached = SATELLITE_CACHE["items"] if SATELLITE_CACHE["expires"] > now else None

    if cached is None:
        try:
            response = HTTP.get(
                "https://celestrak.org/NORAD/elements/gp.php"
                "?GROUP=stations&FORMAT=tle",
                timeout=(5, 12)
            )
            response.raise_for_status()
            lines = [line.strip() for line in response.text.splitlines() if line.strip()]

            records = []
            index = 0
            while index + 2 < len(lines):
                name, line1, line2 = lines[index:index + 3]
                if line1.startswith("1 ") and line2.startswith("2 "):
                    records.append((name, line1, line2))
                    index += 3
                else:
                    index += 1

            if not records:
                raise ValueError("No valid TLE records were returned.")

            with LOCK:
                SATELLITE_CACHE["items"] = records
                SATELLITE_CACHE["expires"] = now + 900
            cached = records
        except (requests.RequestException, ValueError):
            return error(
                "CelesTrak is unavailable and no simulated orbital data "
                "was substituted.",
                502
            )

    try:
        ts = load.timescale()
        t = ts.now()
        result = []
        visible = []

        for name, line1, line2 in cached[:100]:
            try:
                sat = EarthSatellite(line1, line2, name, ts)
                geocentric = sat.at(t)
                subpoint = wgs84.subpoint(geocentric)
                altitude_km = float(subpoint.elevation.km)
                orbit = sat.model

                inclination = float(orbit.inclo) * 180.0 / 3.141592653589793
                eccentricity = float(orbit.ecco)
                mean_motion = float(orbit.no_kozai) * 1440.0 / (
                    2.0 * 3.141592653589793
                )
                period_min = 1440.0 / mean_motion if mean_motion > 0 else 0.0
                norad_id = int(line1[2:7])

                item = {
                    "name": name,
                    "noradId": norad_id,
                    "internationalDesignator": line1[9:17].strip(),
                    "inclination": inclination,
                    "eccentricity": eccentricity,
                    "altitudeKm": altitude_km,
                    "periodMin": period_min,
                    "visible": False,
                    "type": "Space station",
                    "latitude": float(subpoint.latitude.degrees),
                    "longitude": float(subpoint.longitude.degrees),
                    "source": "CelesTrak TLE + Skyfield",
                    "timestamp": utc_now()
                }

                if observer is not None:
                    observer_position = wgs84.latlon(observer[0], observer[1])
                    difference = sat - observer_position
                    topocentric = difference.at(t)
                    altitude, azimuth, distance = topocentric.altaz()
                    item["elevationDegrees"] = float(altitude.degrees)
                    item["azimuthDegrees"] = float(azimuth.degrees)
                    item["rangeKm"] = float(distance.km)
                    item["visible"] = float(altitude.degrees) > 0
                    if item["visible"]:
                        visible.append(item)

                result.append(item)
            except Exception:
                # One malformed or decayed TLE must not break the entire list.
                continue

        return jsonify({
            "all": result,
            "visible": visible,
            "observerProvided": observer is not None,
            "source": "CelesTrak public TLE data propagated by Skyfield",
            "timestamp": utc_now(),
            "notice": (
                "Geometric elevation above the horizon is reported; "
                "this is not a guarantee of optical visibility."
            )
        })
    except Exception:
        app.logger.exception("Satellite propagation failed")
        return error("Satellite propagation failed; no simulated data returned.", 502)


# ---------------- TRANSPARENT SECURITY-AWARENESS LINKS ----------------
# Counts visits only. No credential collection, GPS collection, or device fingerprinting.
# Campaign storage is intentionally in-memory; it resets when the process restarts.

def prune_campaigns():
    cutoff = time.time() - CAMPAIGN_TTL_SECONDS
    with LOCK:
        expired = [
            key for key, campaign in CAMPAIGNS.items()
            if campaign["createdEpoch"] < cutoff
        ]
        for key in expired:
            CAMPAIGNS.pop(key, None)


@app.post("/api/phishing/create")
def create_campaign():
    body = request.get_json(silent=True) or {}
    title = clean_text(body.get("title"), 100) or "Security Awareness Exercise"
    destination = clean_text(
        body.get("redirect_url") or body.get("redirectUrl"), 2048
    ) or "https://example.com"

    if not valid_http_url(destination):
        return error("Destination must be a valid HTTP or HTTPS URL.")

    parsed = urlparse(destination)
    if parsed.scheme != "https":
        return error(
            "Use an HTTPS destination for security-awareness campaigns."
        )

    campaign_id = secrets.token_urlsafe(12)
    base_url = request.url_root.rstrip("/")

    campaign = {
        "id": campaign_id,
        "title": title,
        "trackingUrl": f"{base_url}/track/{campaign_id}",
        "redirectUrl": destination,
        "createdAt": int(time.time() * 1000),
        "createdEpoch": time.time(),
        "clicks": 0,
        "uniqueClicks": 0,
        "lastClickAt": None
    }

    with LOCK:
        CAMPAIGNS[campaign_id] = campaign

    return jsonify({
        key: value for key, value in campaign.items()
        if key != "createdEpoch"
    }), 201


@app.get("/api/phishing/campaign/<campaign_id>")
def get_campaign(campaign_id):
    prune_campaigns()
    with LOCK:
        campaign = CAMPAIGNS.get(campaign_id)
        if not campaign:
            return error("Campaign not found. It may have expired or the server restarted.", 404)
        result = dict(campaign)

    result.pop("createdEpoch", None)
    return jsonify(result)


@app.get("/track/<campaign_id>")
def campaign_visit(campaign_id):
    prune_campaigns()
    with LOCK:
        campaign = CAMPAIGNS.get(campaign_id)
        if not campaign:
            return error("Campaign not found or expired.", 404)

        campaign["clicks"] += 1
        campaign["uniqueClicks"] = campaign["clicks"]
        campaign["lastClickAt"] = int(time.time() * 1000)
        destination = campaign["redirectUrl"]
        title = html.escape(campaign["title"])

    # Clearly identify the training exercise. Do not silently collect GPS.
    page = """
    <!doctype html>
    <html lang="en">
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width,initial-scale=1">
      <title>Security Awareness Exercise</title>
      <style>
        body{font:16px system-ui;background:#0b1220;color:#e5e7eb;
        display:grid;place-items:center;min-height:100vh;margin:0;padding:24px}
        main{max-width:560px;background:#111c2e;border:1px solid #26364e;
        border-radius:16px;padding:28px}
        h1{color:#67e8f9}p{line-height:1.65;color:#cbd5e1}
        a{color:#67e8f9}
      </style>
    </head>
    <body><main>
      <h1>Security awareness exercise</h1>
      <p>This link is part of an authorized security-awareness exercise.</p>
      <p>Campaign: {{ title }}</p>
      <p>No passwords, GPS coordinates, or device identifiers are collected
      by this page.</p>
      <p><a href="{{ destination }}">Continue to the destination</a></p>
    </main></body></html>
    """
    return render_template_string(
        page, title=title, destination=destination
    )


@app.errorhandler(413)
def too_large(_exc):
    return error("Request body is too large.", 413)


@app.errorhandler(404)
def not_found(_exc):
    return error("Endpoint not found.", 404)


@app.errorhandler(500)
def internal_error(_exc):
    app.logger.exception("Unhandled server error")
    return error("Internal server error.", 500)


if __name__ == "__main__":
    port = int(os.getenv("PORT", "7777"))
    app.run(host="0.0.0.0", port=port, debug=False)
