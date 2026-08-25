[CmdletBinding()]
param(
    [string]$SourceEnv,
    [string]$PythonEnv,
    [string]$BackendEnv
)

$ErrorActionPreference = 'Stop'
$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
if ([string]::IsNullOrWhiteSpace($SourceEnv)) {
    $SourceEnv = Join-Path $scriptRoot '..\.env'
}
if ([string]::IsNullOrWhiteSpace($PythonEnv)) {
    $PythonEnv = Join-Path $scriptRoot '..\..\4-cafe-story-ai-python\.env'
}
if ([string]::IsNullOrWhiteSpace($BackendEnv)) {
    $BackendEnv = Join-Path $scriptRoot '..\..\1-cafe-story-backend-javaspring\src\main\resources\.env'
}

function Read-DotEnvValue {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Name
    )

    $content = [System.IO.File]::ReadAllText($Path)
    $pattern = "(?m)^\s*$([regex]::Escape($Name))\s*=\s*(.*)$"
    $matches = [regex]::Matches($content, $pattern)
    if ($matches.Count -gt 1) {
        throw "Duplicate $Name entries in $Path"
    }
    if ($matches.Count -eq 0) {
        return $null
    }

    return $matches[0].Groups[1].Value.Trim().Trim('"').Trim("'")
}

function Set-DotEnvValue {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$Value
    )

    $content = [System.IO.File]::ReadAllText($Path)
    $pattern = "(?m)^\s*$([regex]::Escape($Name))\s*=.*$"
    $matches = [regex]::Matches($content, $pattern)
    if ($matches.Count -gt 1) {
        throw "Duplicate $Name entries in $Path"
    }

    if ($matches.Count -eq 1) {
        $updated = [regex]::Replace(
            $content,
            $pattern,
            [System.Text.RegularExpressions.MatchEvaluator]{
                param($match)
                return "$Name=$Value"
            }
        )
    }
    else {
        $separator = if ($content.Length -eq 0 -or $content.EndsWith("`n")) { '' } else { [Environment]::NewLine }
        $updated = "$content$separator$Name=$Value$([Environment]::NewLine)"
    }

    $temporaryPath = "$Path.codex-secret-sync-$PID.tmp"
    [System.IO.File]::WriteAllText(
        $temporaryPath,
        $updated,
        [System.Text.UTF8Encoding]::new($false)
    )
    Move-Item -LiteralPath $temporaryPath -Destination $Path -Force
}

function Remove-DotEnvValue {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Name
    )

    $content = [System.IO.File]::ReadAllText($Path)
    $pattern = "(?m)^\s*$([regex]::Escape($Name))\s*=.*(?:\r?\n|$)"
    $matches = [regex]::Matches($content, $pattern)
    if ($matches.Count -gt 1) {
        throw "Duplicate $Name entries in $Path"
    }

    if ($matches.Count -eq 1) {
        $updated = [regex]::Replace($content, $pattern, '')
        $temporaryPath = "$Path.codex-secret-sync-$PID.tmp"
        [System.IO.File]::WriteAllText(
            $temporaryPath,
            $updated,
            [System.Text.UTF8Encoding]::new($false)
        )
        Move-Item -LiteralPath $temporaryPath -Destination $Path -Force
    }
}

$resolvedSource = (Resolve-Path -LiteralPath $SourceEnv).Path
$resolvedPython = (Resolve-Path -LiteralPath $PythonEnv).Path
$resolvedBackend = (Resolve-Path -LiteralPath $BackendEnv).Path

$openAiKey = Read-DotEnvValue -Path $resolvedSource -Name 'OPENAI_API_KEY'
if ([string]::IsNullOrWhiteSpace($openAiKey)) {
    throw 'OPENAI_API_KEY is missing or empty in the source env file'
}

$hmacSecret = Read-DotEnvValue -Path $resolvedSource -Name 'ADMIN_REPORT_AI_HMAC_SECRET'
$hmacGenerated = [string]::IsNullOrWhiteSpace($hmacSecret)
if ($hmacGenerated) {
    $randomBytes = [byte[]]::new(32)
    $randomNumberGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $randomNumberGenerator.GetBytes($randomBytes)
    }
    finally {
        $randomNumberGenerator.Dispose()
    }
    $hmacSecret = ([BitConverter]::ToString($randomBytes)).Replace('-', '').ToLowerInvariant()
    Set-DotEnvValue -Path $resolvedSource -Name 'ADMIN_REPORT_AI_HMAC_SECRET' -Value $hmacSecret
}

Set-DotEnvValue -Path $resolvedPython -Name 'OPENAI_API_KEY' -Value $openAiKey
Remove-DotEnvValue -Path $resolvedBackend -Name 'OPENAI_API_KEY'
Set-DotEnvValue -Path $resolvedBackend -Name 'ADMIN_REPORT_AI_HMAC_SECRET' -Value $hmacSecret

[pscustomobject]@{
    status = 'PASS'
    openAiKeySource = 'docker/.env'
    openAiConsumersUpdated = @('n8n', 'ai-python')
    backendOpenAiKeyRemoved = $true
    sharedHmacConfigured = $true
    hmacGenerated = $hmacGenerated
    secretValuesPrinted = $false
} | ConvertTo-Json
