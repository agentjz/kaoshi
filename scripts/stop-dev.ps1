$ErrorActionPreference = "Stop"

. "$PSScriptRoot\docker.ps1"

function Stop-KaoshiPort {
  param(
    [Parameter(Mandatory = $true)]
    [int] $Port,
    [Parameter(Mandatory = $true)]
    [string] $Name
  )

  $processIds = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique

  foreach ($processId in $processIds) {
    if ($processId -ne $PID) {
      Write-Host "Stopping $Name on port $Port, pid $processId."
      Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
  }
}

function Stop-KaoshiProcessByCommandLine {
  $rootPath = (Resolve-Path "$PSScriptRoot\..").Path
  $patterns = @(
    "@playwright\test\cli.js",
    "spring-boot:run",
    "vite --host",
    "run-e2e-stack.ps1",
    "run-backend.ps1",
    "run-frontend.ps1",
    "run-frontend-e2e.ps1"
  )

  Get-CimInstance Win32_Process |
    Where-Object {
      $commandLine = $_.CommandLine
      if ([string]::IsNullOrWhiteSpace($commandLine)) {
        return $false
      }
      if ($_.ProcessId -eq $PID) {
        return $false
      }
      if ($commandLine -notlike "*$rootPath*") {
        return $false
      }
      foreach ($pattern in $patterns) {
        if ($commandLine -like "*$pattern*") {
          return $true
        }
      }
      return $false
    } |
    ForEach-Object {
      Write-Host "Stopping project process pid $($_.ProcessId): $($_.Name)"
      Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
    }
}

Push-Location $PSScriptRoot\..
try {
  Write-Host "== Stop kaoshi local processes =="
  Stop-KaoshiPort -Port 8080 -Name "backend"
  Stop-KaoshiPort -Port 5173 -Name "frontend"
  Stop-KaoshiPort -Port 5174 -Name "e2e frontend"
  Stop-KaoshiProcessByCommandLine

  Write-Host "== Stop kaoshi Docker services =="
  Invoke-KaoshiDocker compose stop mysql redis

  Write-Host "kaoshi local services stopped."
} finally {
  Pop-Location
}
