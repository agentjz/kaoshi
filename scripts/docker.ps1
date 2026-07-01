$ErrorActionPreference = "Stop"

function Get-KaoshiDockerCommand {
  $command = Get-Command docker -ErrorAction SilentlyContinue
  if ($command) {
    return $command.Source
  }

  $dockerDesktopCommand = "C:\Program Files\Docker\Docker\resources\bin\docker.exe"
  if (Test-Path -LiteralPath $dockerDesktopCommand) {
    $dockerDesktopBin = Split-Path -Parent $dockerDesktopCommand
    $pathEntries = $env:Path -split ";" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    if ($pathEntries -notcontains $dockerDesktopBin) {
      $env:Path = "$dockerDesktopBin;$env:Path"
    }
    return $dockerDesktopCommand
  }

  throw "Docker CLI was not found. Start Docker Desktop and reopen VSCode, or add Docker Desktop resources\bin to PATH."
}

function Invoke-KaoshiDocker {
  param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Arguments
  )

  $docker = Get-KaoshiDockerCommand
  & $docker @Arguments
  if ($LASTEXITCODE -ne 0) {
    throw "Docker command failed: docker $($Arguments -join ' ')"
  }
}

function Remove-KaoshiDockerContainers {
  param(
    [Parameter(Mandatory = $true)]
    [string[]] $Names
  )

  $docker = Get-KaoshiDockerCommand
  $existingContainers = @(& $docker ps --all --format "{{.Names}}")
  if ($LASTEXITCODE -ne 0) {
    throw "Docker command failed: docker ps --all --format {{.Names}}"
  }
  foreach ($name in $Names) {
    if ($existingContainers -contains $name) {
      & $docker rm --force $name | Out-Null
      if ($LASTEXITCODE -ne 0) {
        throw "Docker command failed: docker rm --force $name"
      }
    }
  }
}

function Remove-KaoshiDockerVolumes {
  param(
    [Parameter(Mandatory = $true)]
    [string[]] $Names
  )

  $docker = Get-KaoshiDockerCommand
  $existingVolumes = @(& $docker volume ls --format "{{.Name}}")
  if ($LASTEXITCODE -ne 0) {
    throw "Docker command failed: docker volume ls --format {{.Name}}"
  }
  foreach ($name in $Names) {
    if ($existingVolumes -contains $name) {
      & $docker volume rm --force $name | Out-Null
      if ($LASTEXITCODE -ne 0) {
        throw "Docker command failed: docker volume rm --force $name"
      }
    }
  }
}

function Wait-KaoshiTcpPort {
  param(
    [Parameter(Mandatory = $true)]
    [string] $HostName,
    [Parameter(Mandatory = $true)]
    [int] $Port,
    [int] $TimeoutSeconds = 60
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  do {
    try {
      $client = [System.Net.Sockets.TcpClient]::new()
      $connect = $client.BeginConnect($HostName, $Port, $null, $null)
      if ($connect.AsyncWaitHandle.WaitOne(1000)) {
        $client.EndConnect($connect)
        $client.Close()
        return
      }
      $client.Close()
    } catch {
      Start-Sleep -Seconds 1
    }
  } while ((Get-Date) -lt $deadline)

  throw "Timed out waiting for ${HostName}:${Port}."
}

function Wait-KaoshiMysql {
  param(
    [int] $TimeoutSeconds = 120
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  do {
    try {
      Invoke-KaoshiDocker exec kaoshi-mysql mysqladmin ping -h 127.0.0.1 -uroot -ppassword --silent
      return
    } catch {
      Start-Sleep -Seconds 2
    }
  } while ((Get-Date) -lt $deadline)

  throw "Timed out waiting for kaoshi MySQL to accept authenticated connections."
}

function Wait-KaoshiRedis {
  param(
    [int] $TimeoutSeconds = 120
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  do {
    try {
      Invoke-KaoshiDocker exec kaoshi-redis redis-cli ping | Out-Null
      return
    } catch {
      Start-Sleep -Seconds 2
    }
  } while ((Get-Date) -lt $deadline)

  throw "Timed out waiting for kaoshi Redis to respond."
}

