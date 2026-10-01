#!/usr/bin/env python3
import concurrent.futures
import json
import time
import urllib.request
import urllib.error

BASE_URL = "http://127.0.0.1:8088"

def fetch_status(token):
    url = f"{BASE_URL}/statusantrean"
    headers = {
        "x-username": "admin",
        "x-token": token,
        "Content-Type": "application/json"
    }
    payload = json.dumps({
        "kodepoli": "BED",
        "kodedokter": "217354",
        "tanggalperiksa": "2026-10-05",
        "jampraktek": "08:00-09:30"
    }).encode('utf-8')

    req = urllib.request.Request(url, data=payload, headers=headers, method="POST")
    start = time.perf_counter()
    try:
        with urllib.request.urlopen(req) as resp:
            status = resp.status
    except urllib.error.HTTPError as e:
        status = e.code
    duration_ms = (time.perf_counter() - start) * 1000
    return status, duration_ms

def test_concurrent_booking(token, idx):
    url = f"{BASE_URL}/ambilantrean"
    headers = {
        "x-username": "admin",
        "x-token": token,
        "Content-Type": "application/json"
    }
    # Each thread sends a unique reference number
    payload = json.dumps({
        "nomorkartu": "9999999999999",
        "nik": "9999999999999999",
        "nohp": "08123456789",
        "kodepoli": "INT",
        "norm": "999999",
        "tanggalperiksa": "2026-10-05",
        "kodedokter": "470937",
        "jampraktek": "08:00-12:00",
        "jeniskunjungan": "1",
        "nomorreferensi": f"STRESS-REF-{idx:04d}"
    }).encode('utf-8')

    req = urllib.request.Request(url, data=payload, headers=headers, method="POST")
    start = time.perf_counter()
    try:
        with urllib.request.urlopen(req) as resp:
            status = resp.status
    except urllib.error.HTTPError as e:
        status = e.code
    duration_ms = (time.perf_counter() - start) * 1000
    return status, duration_ms

def main():
    print("=== BPJS Inbound High Concurrency Stress Test ===")

    # 1. Get Token
    auth_req = urllib.request.Request(
        f"{BASE_URL}/auth",
        headers={"x-username": "admin", "x-password": "pass"},
        method="GET"
    )
    with urllib.request.urlopen(auth_req) as resp:
        body = json.loads(resp.read().decode('utf-8'))
        token = body['response']['token']

    # Test 1: 50 concurrent statusantrean reads
    concurrency = 50
    print(f"\n[Test 1] Spawning {concurrency} concurrent requests to /statusantrean...")
    start_total = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [executor.submit(fetch_status, token) for _ in range(concurrency)]
        results = [f.result() for f in futures]

    total_time_ms = (time.perf_counter() - start_total) * 1000
    latencies = [r[1] for r in results]
    success_count = sum(1 for r in results if r[0] == 200)

    avg_latency = sum(latencies) / len(latencies)
    latencies.sort()
    p95_latency = latencies[int(len(latencies) * 0.95)]

    print(f"  Total Requests: {concurrency}")
    print(f"  Success (HTTP 200): {success_count}/{concurrency}")
    print(f"  Total Duration: {total_time_ms:.2f} ms")
    print(f"  Avg Latency per Request: {avg_latency:.2f} ms")
    print(f"  p95 Latency: {p95_latency:.2f} ms")
    print(f"  Throughput: {concurrency / (total_time_ms / 1000.0):.1f} req/sec")

    # Test 2: 50 concurrent booking requests (contention test)
    print(f"\n[Test 2] Spawning {concurrency} concurrent booking requests to /ambilantrean...")
    start_booking = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [executor.submit(test_concurrent_booking, token, i) for i in range(concurrency)]
        booking_results = [f.result() for f in futures]

    total_booking_ms = (time.perf_counter() - start_booking) * 1000
    # Expected code 202 (patient not found) or 200/201 (never 500)
    valid_codes = sum(1 for r in booking_results if r[0] in [200, 201, 202])
    print(f"  Valid Handled Responses: {valid_codes}/{concurrency}")
    print(f"  Total Duration: {total_booking_ms:.2f} ms")
    print(f"  Throughput: {concurrency / (total_booking_ms / 1000.0):.1f} req/sec")

    if success_count == concurrency and valid_codes == concurrency:
        print("\n[ALL STRESS TESTS PASSED]")
    else:
        print("\n[STRESS TEST WARNING/FAILED]")

if __name__ == "__main__":
    main()
