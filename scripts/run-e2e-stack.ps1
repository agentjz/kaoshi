$ErrorActionPreference = "Stop"

. "$PSScriptRoot\docker.ps1"

$root = Resolve-Path "$PSScriptRoot\.."
$logDir = Join-Path $root "logs\e2e"
$backendLog = Join-Path $logDir "backend.out.log"
$backendErrorLog = Join-Path $logDir "backend.err.log"
$frontendLog = Join-Path $logDir "frontend.out.log"
$frontendErrorLog = Join-Path $logDir "frontend.err.log"

function Wait-KaoshiHttp {
  param(
    [Parameter(Mandatory = $true)]
    [string] $Url,
    [Parameter(Mandatory = $true)]
    [string] $Name,
    [Parameter(Mandatory = $true)]
    [System.Diagnostics.Process] $Process,
    [Parameter(Mandatory = $true)]
    [string] $LogPath,
    [int] $TimeoutSeconds = 180
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  do {
    if ($Process.HasExited) {
      Write-KaoshiLogTail -Name $Name -LogPath $LogPath
      throw "$Name exited before becoming ready."
    }
    try {
      $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
      if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
        return
      }
    } catch {
      Start-Sleep -Seconds 2
    }
  } while ((Get-Date) -lt $deadline)

  Write-KaoshiLogTail -Name $Name -LogPath $LogPath
  throw "Timed out waiting for $Name at $Url."
}

function Write-KaoshiLogTail {
  param(
    [Parameter(Mandatory = $true)]
    [string] $Name,
    [Parameter(Mandatory = $true)]
    [string] $LogPath
  )

  Write-Host "== $Name log tail =="
  if (Test-Path -LiteralPath $LogPath) {
    Get-Content -LiteralPath $LogPath -Tail 120
  } else {
    Write-Host "Log file does not exist: $LogPath"
  }
}

function Write-KaoshiProcessLogs {
  param(
    [Parameter(Mandatory = $true)]
    [string] $Name,
    [Parameter(Mandatory = $true)]
    [string] $OutputLog,
    [Parameter(Mandatory = $true)]
    [string] $ErrorLog
  )

  Write-KaoshiLogTail -Name "$Name stdout" -LogPath $OutputLog
  Write-KaoshiLogTail -Name "$Name stderr" -LogPath $ErrorLog
}

function Start-KaoshiLoggedProcess {
  param(
    [Parameter(Mandatory = $true)]
    [string] $ScriptPath,
    [Parameter(Mandatory = $true)]
    [string] $OutputLog,
    [Parameter(Mandatory = $true)]
    [string] $ErrorLog
  )

  Start-Process powershell `
    -ArgumentList "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $ScriptPath `
    -PassThru `
    -WindowStyle Hidden `
    -WorkingDirectory $root `
    -RedirectStandardOutput $OutputLog `
    -RedirectStandardError $ErrorLog
}

$backendProcess = $null
$frontendProcess = $null

try {
  Push-Location $root
  New-Item -ItemType Directory -Force -Path $logDir | Out-Null
  Remove-Item -LiteralPath $backendLog, $backendErrorLog, $frontendLog, $frontendErrorLog -Force -ErrorAction SilentlyContinue

  Write-Host "== Reset real Docker database for Playwright E2E =="
  Invoke-KaoshiDocker compose down --volumes --remove-orphans
  Remove-KaoshiDockerContainers -Names @("kaoshi-frontend", "kaoshi-backend", "kaoshi-mysql", "kaoshi-redis")
  Remove-KaoshiDockerVolumes -Names @("kaoshi_mysql", "kaoshi_redis", "gaokao_kaoshi_mysql", "gaokao_kaoshi_redis")
  Invoke-KaoshiDocker compose up --detach mysql redis
  Wait-KaoshiTcpPort -HostName "127.0.0.1" -Port 13306 -TimeoutSeconds 120
  Wait-KaoshiTcpPort -HostName "127.0.0.1" -Port 16379 -TimeoutSeconds 120
  Wait-KaoshiMysql -TimeoutSeconds 120
  Wait-KaoshiRedis -TimeoutSeconds 120

  Write-Host "== Start real backend =="
  $backendProcess = Start-KaoshiLoggedProcess -ScriptPath "$PSScriptRoot\run-backend.ps1" -OutputLog $backendLog -ErrorLog $backendErrorLog
  Wait-KaoshiHttp -Url "http://127.0.0.1:8080/actuator/health" -Name "backend" -Process $backendProcess -LogPath $backendLog -TimeoutSeconds 240

  Write-Host "== Start real frontend =="
  $frontendProcess = Start-KaoshiLoggedProcess -ScriptPath "$PSScriptRoot\run-frontend-e2e.ps1" -OutputLog $frontendLog -ErrorLog $frontendErrorLog
  Wait-KaoshiHttp -Url "http://127.0.0.1:5174/" -Name "frontend" -Process $frontendProcess -LogPath $frontendLog -TimeoutSeconds 240

  Write-Host "Real E2E stack is ready."
  while ($true) {
    if ($backendProcess.HasExited) {
      Write-KaoshiProcessLogs -Name "backend" -OutputLog $backendLog -ErrorLog $backendErrorLog
      throw "Backend exited during E2E."
    }
    if ($frontendProcess.HasExited) {
      Write-KaoshiProcessLogs -Name "frontend" -OutputLog $frontendLog -ErrorLog $frontendErrorLog
      throw "Frontend exited during E2E."
    }
    Start-Sleep -Seconds 2
  }
} finally {
  if ($frontendProcess -and -not $frontendProcess.HasExited) {
    Stop-Process -Id $frontendProcess.Id -Force -ErrorAction SilentlyContinue
  }
  if ($backendProcess -and -not $backendProcess.HasExited) {
    Stop-Process -Id $backendProcess.Id -Force -ErrorAction SilentlyContinue
  }
  Pop-Location
}
