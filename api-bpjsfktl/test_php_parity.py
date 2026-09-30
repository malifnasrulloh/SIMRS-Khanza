#!/usr/bin/env python3
import json
import time
import urllib.request
import urllib.error
import sys

BASE_URL = "http://127.0.0.1:8089"

def test_endpoint(name, method, path, headers, payload, expected_code):
    url = f"{BASE_URL}/{path}" if not path.startswith("/") else f"{BASE_URL}{path}"
    data = json.dumps(payload).encode('utf-8') if payload else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)

    start = time.perf_counter()
    try:
        with urllib.request.urlopen(req) as resp:
            status = resp.status
            body = json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        status = e.code
        try:
            body = json.loads(e.read().decode('utf-8'))
        except Exception:
            body = {"raw": "non-json"}
    duration_ms = (time.perf_counter() - start) * 1000

    code = body.get('metadata', {}).get('code')
    msg = body.get('metadata', {}).get('message')

    passed = (status == expected_code and code == expected_code)
    status_str = "[PASS]" if passed else "[FAIL]"
    print(f"  {status_str} {name} -> HTTP {status}, metadata.code {code} ({msg}) [{duration_ms:.1f}ms]")
    return passed, body, duration_ms

def main():
    print("=== Testing PHP api-bpjsfktl on 127.0.0.1:8089 ===")

    # 1. Auth Test
    ok, auth_body, auth_time = test_endpoint("1. GET /auth (valid)", "GET", "auth",
                                             {"x-username": "admin", "x-password": "pass"}, None, 200)
    if not ok:
        print("Auth failed, exiting")
        sys.exit(1)

    print(f"\n>> Baseline Auth TTFB: {auth_time:.2f} ms <<\n")

    token = auth_body['response']['token']
    auth_headers = {"x-username": "admin", "x-token": token, "Content-Type": "application/json"}

    # 2. Status Antrean
    test_endpoint("2. POST /statusantrean", "POST", "statusantrean",
                  auth_headers, {
                      "kodepoli": "BED",
                      "kodedokter": "217354",
                      "tanggalperiksa": "2026-10-05",
                      "jampraktek": "08:00-09:30"
                  }, 200)

    # 3. Ambil Antrean (Unknown patient -> 202)
    test_endpoint("3. POST /ambilantrean (unknown patient)", "POST", "ambilantrean",
                  auth_headers, {
                      "nomorkartu": "9999999999999",
                      "nik": "9999999999999999",
                      "nohp": "08123456789",
                      "kodepoli": "BED",
                      "norm": "999999",
                      "tanggalperiksa": "2026-10-05",
                      "kodedokter": "217354",
                      "jampraktek": "08:00-09:30",
                      "jeniskunjungan": "1",
                      "nomorreferensi": "REFPHP001"
                  }, 202)

    # 4. Checkin Antrean (Unknown booking -> 201)
    test_endpoint("4. POST /checkinantrean", "POST", "checkinantrean",
                  auth_headers, {"kodebooking": "NONEXISTENT123", "waktu": 1790730000000}, 201)

    # 5. Batal Antrean (Unknown booking -> 201)
    test_endpoint("5. POST /batalantrean", "POST", "batalantrean",
                  auth_headers, {"kodebooking": "NONEXISTENT123", "keterangan": "Batal"}, 201)

    # 6. Sisa Antrean (Unknown booking -> 201)
    test_endpoint("6. POST /sisaantrean", "POST", "sisaantrean",
                  auth_headers, {"kodebooking": "NONEXISTENT123"}, 201)

    # 7. Jadwal Operasi RS (Empty schedule in range -> 201)
    today_str = time.strftime("%Y-%m-%d")
    next_week_str = time.strftime("%Y-%m-%d", time.localtime(time.time() + 7 * 86400))
    test_endpoint("7. POST /jadwaloperasirs", "POST", "jadwaloperasirs",
                  auth_headers, {"tanggalawal": today_str, "tanggalakhir": next_week_str}, 201)

    # 8. Jadwal Operasi Pasien (No records -> 201)
    test_endpoint("8. POST /jadwaloperasipasien", "POST", "jadwaloperasipasien",
                  auth_headers, {"nopeserta": "0000000000000"}, 201)

    # 9. Pasien Baru (Empty body -> 201)
    test_endpoint("9. POST /pasienbaru", "POST", "pasienbaru", auth_headers, {}, 201)

    # 10. Ambil Antrean Farmasi (Unknown booking -> 201)
    test_endpoint("10. POST /ambilantreanfarmasi", "POST", "ambilantreanfarmasi",
                  auth_headers, {"kodebooking": "NONEXISTENT123"}, 201)

    # 11. Status Antrean Farmasi (Unknown booking -> 201)
    test_endpoint("11. POST /statusantreanfarmasi", "POST", "statusantreanfarmasi",
                  auth_headers, {"kodebooking": "NONEXISTENT123"}, 201)

if __name__ == "__main__":
    main()
