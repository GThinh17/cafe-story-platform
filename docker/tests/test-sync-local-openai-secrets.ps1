$ErrorActionPreference = 'Stop'

$testRoot = Join-Path ([System.IO.Path]::GetTempPath()) "cafestory-secret-sync-$([guid]::NewGuid())"
$sourceEnv = Join-Path $testRoot 'docker.env'
$pythonEnv = Join-Path $testRoot 'python.env'
$backendEnv = Join-Path $testRoot 'backend.env'

try {
    New-Item -ItemType Directory -Path $testRoot | Out-Null
    [System.IO.File]::WriteAllText(
        $sourceEnv,
        "OPENAI_API_KEY=test-new-key`r`nOPENAI_DECISION_MODEL=test-model`r`n",
        [System.Text.UTF8Encoding]::new($false)
    )
    [System.IO.File]::WriteAllText(
        $pythonEnv,
        "OPENAI_API_KEY=test-old-python-key`r`nGOOGLE_API_KEY=test-google-key`r`n",
        [System.Text.UTF8Encoding]::new($false)
    )
    [System.IO.File]::WriteAllText(
        $backendEnv,
        "OPENAI_API_KEY=test-old-backend-key`r`nDB_URL=test-db`r`n",
        [System.Text.UTF8Encoding]::new($false)
    )

    $result = & (Join-Path $PSScriptRoot 'sync-local-openai-secrets.ps1') `
        -SourceEnv $sourceEnv `
        -PythonEnv $pythonEnv `
        -BackendEnv $backendEnv | ConvertFrom-Json

    $sourceContent = [System.IO.File]::ReadAllText($sourceEnv)
    $pythonContent = [System.IO.File]::ReadAllText($pythonEnv)
    $backendContent = [System.IO.File]::ReadAllText($backendEnv)

    if ($result.status -ne 'PASS') {
        throw 'Sync script did not report PASS'
    }
    if ($pythonContent -notmatch '(?m)^OPENAI_API_KEY=test-new-key\r?$') {
        throw 'Python OPENAI_API_KEY was not synchronized'
    }
    if ($backendContent -match '(?m)^OPENAI_API_KEY=') {
        throw 'Backend OPENAI_API_KEY was not removed'
    }
    if ($sourceContent -notmatch '(?m)^ADMIN_REPORT_AI_HMAC_SECRET=([0-9a-f]{64})\r?$') {
        throw 'Source HMAC was not generated as 64 lowercase hex characters'
    }
    $hmac = $Matches[1]
    if ($backendContent -notmatch "(?m)^ADMIN_REPORT_AI_HMAC_SECRET=$hmac\r?$") {
        throw 'Backend HMAC does not match the source HMAC'
    }
    if ($pythonContent -notmatch '(?m)^GOOGLE_API_KEY=test-google-key\r?$') {
        throw 'Unrelated Python env value was modified'
    }
    if ($backendContent -notmatch '(?m)^DB_URL=test-db\r?$') {
        throw 'Unrelated Backend env value was modified'
    }

    'SYNC_LOCAL_OPENAI_SECRETS_TEST=PASS'
}
finally {
    if (Test-Path -LiteralPath $testRoot) {
        $resolvedTestRoot = (Resolve-Path -LiteralPath $testRoot).Path
        $resolvedTempRoot = (Resolve-Path -LiteralPath ([System.IO.Path]::GetTempPath())).Path
        if (-not $resolvedTestRoot.StartsWith($resolvedTempRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "Refusing to remove unexpected test path: $resolvedTestRoot"
        }
        Remove-Item -LiteralPath $resolvedTestRoot -Recurse -Force
    }
}
