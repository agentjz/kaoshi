$ErrorActionPreference = "Stop"

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysql) {
  throw "MySQL CLI was not found on PATH."
}

$database = $env:KAOSHI_DB_NAME
if ([string]::IsNullOrWhiteSpace($database)) {
  $database = "kaoshi"
}

$username = $env:KAOSHI_DB_USERNAME
if ([string]::IsNullOrWhiteSpace($username)) {
  $username = "root"
}

$password = $env:KAOSHI_DB_PASSWORD
if ([string]::IsNullOrWhiteSpace($password)) {
  $password = "password"
}

Write-Host "== Reset local MySQL database '$database' =="
mysql "-u$username" "-p$password" -e "DROP DATABASE IF EXISTS ``$database``; CREATE DATABASE ``$database`` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
Write-Host "Local MySQL database '$database' is ready."

