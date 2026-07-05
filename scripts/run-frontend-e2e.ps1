$ErrorActionPreference = "Stop"

Push-Location $PSScriptRoot\..\frontend
try {
  if (-not (Test-Path -LiteralPath "node_modules")) {
    Write-Host "node_modules is missing. Installing frontend dependencies."
    npm.cmd install
  } else {
    Write-Host "node_modules exists. Skipping frontend dependency install."
  }
  npm.cmd run dev -- --host 127.0.0.1 --port 5174 --strictPort
} finally {
  Pop-Location
}
