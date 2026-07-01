$ErrorActionPreference = "Stop"

$env:DEBUG = "false"
$env:KAOSHI_DB_URL = "jdbc:mysql://localhost:13306/kaoshi?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
$env:KAOSHI_DB_USERNAME = "root"
$env:KAOSHI_DB_PASSWORD = "password"
$env:KAOSHI_REDIS_HOST = "localhost"
$env:KAOSHI_REDIS_PORT = "16379"

Push-Location $PSScriptRoot\..\backend
try {
  mvn spring-boot:run
} finally {
  Pop-Location
}

