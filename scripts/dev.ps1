$ErrorActionPreference = "Stop"

. "$PSScriptRoot\docker.ps1"

Push-Location $PSScriptRoot\..
try {
  Invoke-KaoshiDocker compose up --detach mysql redis
  Wait-KaoshiTcpPort -HostName "127.0.0.1" -Port 13306 -TimeoutSeconds 90
  Wait-KaoshiTcpPort -HostName "127.0.0.1" -Port 16379 -TimeoutSeconds 90
  Wait-KaoshiMysql -TimeoutSeconds 120
  Wait-KaoshiRedis -TimeoutSeconds 120
  Write-Host "MySQL and Redis are ready through Docker Compose."
  Write-Host "Backend:  powershell -ExecutionPolicy Bypass -File .\scripts\run-backend.ps1"
  Write-Host "Frontend: powershell -ExecutionPolicy Bypass -File .\scripts\run-frontend.ps1"
} finally {
  Pop-Location
}

