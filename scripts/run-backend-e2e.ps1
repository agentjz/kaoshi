$ErrorActionPreference = "Stop"

Push-Location $PSScriptRoot\..
try {
  powershell -ExecutionPolicy Bypass -File .\scripts\reset-docker-data.ps1
  powershell -ExecutionPolicy Bypass -File .\scripts\run-backend.ps1
} finally {
  Pop-Location
}
