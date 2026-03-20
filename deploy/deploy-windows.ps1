param(
    [string]$ImageName = "continulian-almanac",
    [string]$ImageTag = "latest",
    [string]$ContainerName = "continulian-almanac",
    [int]$HostPort = 8080,
    [int]$ContainerPort = 8080
)

$ErrorActionPreference = "Stop"

$rootDir = Split-Path $PSScriptRoot -Parent

docker version
if ($LASTEXITCODE -ne 0) {
    throw "docker 命令执行失败: docker version"
}

docker build -f "$rootDir\deploy\Dockerfile" -t "${ImageName}:${ImageTag}" "$rootDir"
if ($LASTEXITCODE -ne 0) {
    throw "docker 命令执行失败: docker build"
}

$containers = docker ps -a --format "{{.Names}}"
if ($LASTEXITCODE -ne 0) {
    throw "docker 命令执行失败: docker ps -a --format {{.Names}}"
}
$exists = $containers | Where-Object { $_ -eq $ContainerName }
if ($exists) {
    docker rm -f $ContainerName
    if ($LASTEXITCODE -ne 0) {
        throw "docker 命令执行失败: docker rm -f $ContainerName"
    }
}

docker run -d --name $ContainerName -p "${HostPort}:${ContainerPort}" --restart unless-stopped "${ImageName}:${ImageTag}"
if ($LASTEXITCODE -ne 0) {
    throw "docker 命令执行失败: docker run"
}

Write-Host "部署完成: http://localhost:$HostPort"
