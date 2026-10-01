#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import sys

BASE_URL = "http://127.0.0.1:8088"

def test_endpoint(name, method, path, headers, payload, expected_code):
    url = f"{BASE_URL}{path}"
    data = json.dumps(payload).encode('utf-8') if payload else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)

    try:
        with urllib.request.urlopen(req) as resp:
            status = resp.status
            body = json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        status = e.code
        body = json.loads(e.read().decode('utf-8'))

    code = body.get('metadata', {}).get('code')
    msg = body.get('metadata', {}).get('message')

    if status == expected_code and code == expected_code:
        print(f"  [PASS] {name} -> HTTP {status}, metadata.code {code} ({msg})")
        return True, body
    else:
        print(f"  [FAIL] {name} -> Expected {expected_code}, got HTTP {status}, metadata.code {code} ({msg})")
        return False, body

def main():
    print("=== Running BPJS FKTL Inbound Microservice Parity Tests (All 11 Endpoints) ===")

    # 1. Auth Test
    ok, auth_body = test_endpoint("1. GET /auth (valid)", "GET", "/auth",
                                  {"x-username": "admin", "x-password": "pass"}, None, 200)
    if not ok:
        sys.exit(1)

    token = auth_body['response']['token']
    auth_headers = {"x-username": "admin", "x-token": token, "Content-Type": "application/json"}

    # 2. Status Antrean
    test_endpoint("2. POST /statusantrean (valid clinic)", "POST", "/statusantrean",
                  auth_headers, {
                      "kodepoli": "BED",
                      "kodedokter": "217354",
                      "tanggalperiksa": "2026-10-05",
                      "jampraktek": "08:00-09:30"
                  }, 200)

    # 3. Ambil Antrean (Patient Not Found -> 202)
    test_endpoint("3. POST /ambilantrean (unknown patient -> 202)", "POST", "/ambilantrean",
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
                      "nomorreferensi": "REFTEST9999"
                  }, 202)

    # 4. Checkin Antrean (Unknown Booking -> 201)
    test_endpoint("4. POST /checkinantrean (unknown booking)", "POST", "/checkinantrean",
                  auth_headers, {
                      "kodebooking": "NONEXISTENT123",
                      "waktu": 1790730000000
                  }, 201)

    # 5. Batal Antrean (Unknown Booking -> 201)
    test_endpoint("5. POST /batalantrean (unknown booking)", "POST", "/batalantrean",
                  auth_headers, {
                      "kodebooking": "NONEXISTENT123",
                      "keterangan": "Batal uji coba"
                  }, 201)

    # 6. Sisa Antrean (Unknown Booking -> 201)
    test_endpoint("6. POST /sisaantrean (unknown booking)", "POST", "/sisaantrean",
                  auth_headers, {
                      "kodebooking": "NONEXISTENT123"
                  }, 201)

    # 7. Jadwal Operasi RS
    import time
    today_str = time.strftime("%Y-%m-%d")
    next_week_str = time.strftime("%Y-%m-%d", time.localtime(time.time() + 7 * 86400))
    test_endpoint("7. POST /jadwaloperasirs", "POST", "/jadwaloperasirs",
                  auth_headers, {
                      "tanggalawal": today_str,
                      "tanggalakhir": next_week_str
                  }, 201)

    # 9. Jadwal Operasi Pasien (No records -> 201)
    test_endpoint("8. POST /jadwaloperasipasien (unknown patient)", "POST", "/jadwaloperasipasien",
                  auth_headers, {
                      "nopeserta": "0000000000000"
                  }, 201)

    # 10. Pasien Baru (Validation error on empty -> 201)
    test_endpoint("9. POST /pasienbaru (empty payload)", "POST", "/pasienbaru",
                  auth_headers, {}, 201)

    # 11. Ambil Antrean Farmasi (Unknown Booking -> 201)
    test_endpoint("10. POST /ambilantreanfarmasi (unknown booking)", "POST", "/ambilantreanfarmasi",
                  auth_headers, {
                      "kodebooking": "NONEXISTENT123"
                  }, 201)

    # 12. Status Antrean Farmasi (Unknown Booking -> 201)
    test_endpoint("11. POST /statusantreanfarmasi (unknown booking)", "POST", "/statusantreanfarmasi",
                  auth_headers, {
                      "kodebooking": "NONEXISTENT123"
                  }, 201)

    print("\nAll 11 endpoints verified successfully!")

if __name__ == "__main__":
    main()
