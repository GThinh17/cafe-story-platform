# Fix log

## MOB-SEARCH-001 — fixed

- Replaced client-side full-list search with a bounded aggregate backend endpoint.
- Added minimal cross-app search DTO/types and kept existing navigation behavior.
- Added service and controller tests.
- Verified runtime improvement from more than 31 seconds stuck to about 1.38 seconds for `cafe`.

## MOB-TEST-001 — fixed

- Corrected PowerShell quoting for Maven's comma-separated test selector.
- Focused suite passed 4/4.

## MOB-TEST-002 — fixed

- Classified the 0-test JUnit discovery failure from the Surefire dump as generated-class corruption, not a Search assertion failure.
- Used Maven Clean on generated `target` output and rebuilt with `javac`.
- The clean focused rerun passed 4/4.

## MOB-RUNTIME-001 / MOB-CORS-001 — environment resolved

- Started the current backend and verified HTTP health.
- Used Expo's configured CORS-allowed origin at port 8081.
- No source workaround was added for an incorrect local origin.

## MOB-RUNTIME-002 — environment resolved

- Confirmed the port-8080 listener was the Spring Boot process started by this verification.
- Restarted only that process after Maven Clean.
- Final clean-build runtime: Search about 1.57 seconds, Feed and Comments PASS.
- Stopped the task-owned backend process after verification.

## MOB-TEST-DATA-001 — not auto-fixed

- Confirmed the temporary test account remains.
- Did not perform direct database deletion because the public delete flow returned HTTP 500 and no safe admin delete contract exists.
