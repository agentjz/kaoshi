$ErrorActionPreference = "Stop"

Push-Location $PSScriptRoot\..\frontend
try {
  npm.cmd install
  npm.cmd run dev
} finally {
  Pop-Location
}
