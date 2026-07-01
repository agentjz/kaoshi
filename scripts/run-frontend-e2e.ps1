$ErrorActionPreference = "Stop"

Push-Location $PSScriptRoot\..\frontend
try {
  npm.cmd install
  npm.cmd run dev -- --host 127.0.0.1 --port 5174 --strictPort
} finally {
  Pop-Location
}
