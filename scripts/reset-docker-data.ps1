$ErrorActionPreference = "Stop"

. "$PSScriptRoot\docker.ps1"

Push-Location $PSScriptRoot\..
try {
  Write-Host "== Reset kaoshi Docker containers and volumes =="
  Invoke-KaoshiDocker compose down --volumes --remove-orphans
  Remove-KaoshiDockerContainers -Names @("kaoshi-frontend", "kaoshi-backend", "kaoshi-mysql", "kaoshi-redis")
  Remove-KaoshiDockerVolumes -Names @("kaoshi_mysql", "kaoshi_redis", "gaokao_kaoshi_mysql", "gaokao_kaoshi_redis")

  Write-Host "== Start MySQL and Redis =="
  Invoke-KaoshiDocker compose up --detach mysql redis

  Write-Host "== Wait for host ports =="
  Wait-KaoshiTcpPort -HostName "127.0.0.1" -Port 13306 -TimeoutSeconds 90
  Wait-KaoshiTcpPort -HostName "127.0.0.1" -Port 16379 -TimeoutSeconds 90
  Wait-KaoshiMysql -TimeoutSeconds 120
  Wait-KaoshiRedis -TimeoutSeconds 120

  Write-Host "Docker MySQL is available on localhost:13306."
  Write-Host "Docker Redis is available on localhost:16379."
  Write-Host "Database name: kaoshi"
  Write-Host "Username: root"
  Write-Host "Password: password"
} finally {
  Pop-Location
}

