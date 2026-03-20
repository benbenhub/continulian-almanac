# Deploy

本文档包含两种部署方式：
- 一键脚本部署（推荐）
- 人工手动部署（适合排障和生产手工操作）

## 目录说明

- `deploy/Dockerfile`：镜像构建文件（多阶段构建）
- `deploy/deploy-windows.ps1`：Windows 一键部署脚本
- `deploy/deploy-linux.sh`：Linux 一键部署脚本

---

## 一、前置条件

### 1) 基础环境

- 已安装 Docker（Windows 建议 Docker Desktop，Linux 建议 Docker Engine）
- 当前用户有执行 Docker 命令权限
- 项目根目录包含 `pom.xml`、`tyme4j/`、`almanac/`、`deploy/`

### 2) 网络要求

构建镜像会拉取基础镜像：
- `maven:3.9.9-eclipse-temurin-8`
- `eclipse-temurin:8-jre`

如果拉取超时，请先配置镜像加速或代理后再部署。

---

## 二、一键脚本部署

### Windows

```powershell
.\deploy\deploy-windows.ps1
```

可选参数：

```powershell
.\deploy\deploy-windows.ps1 `
  -ImageName continulian-almanac `
  -ImageTag latest `
  -ContainerName continulian-almanac `
  -HostPort 8080 `
  -ContainerPort 8080
```

### Linux

```bash
chmod +x deploy/deploy-linux.sh
./deploy/deploy-linux.sh
```

可选环境变量：

```bash
IMAGE_NAME=continulian-almanac \
IMAGE_TAG=latest \
CONTAINER_NAME=continulian-almanac \
HOST_PORT=8080 \
CONTAINER_PORT=8080 \
./deploy/deploy-linux.sh
```

---

## 三、人工手动部署（详细步骤）

以下步骤在 Windows PowerShell 和 Linux Bash 都可执行（命令差异很小）。

### 步骤 1：进入项目根目录

```bash
cd <你的项目路径>/continulian-almanac
```

### 步骤 2：构建镜像

```bash
docker build -f deploy/Dockerfile -t continulian-almanac:latest .
```

构建成功后可查看镜像：

```bash
docker images | findstr continulian-almanac
```

Linux 可用：

```bash
docker images | grep continulian-almanac
```

### 步骤 3：删除旧容器（如果存在）

```bash
docker rm -f continulian-almanac
```

如果提示 `No such container` 可忽略。

### 步骤 4：启动新容器

```bash
docker run -d --name continulian-almanac -p 8080:8080 --restart unless-stopped continulian-almanac:latest
```

### 步骤 5：检查容器状态

```bash
docker ps --filter "name=continulian-almanac"
```

### 步骤 6：验证接口

```bash
curl "http://localhost:8080/api/calculate?date=2026-03-20&time=14:38&gender=1"
```

浏览器访问：

```text
http://localhost:8080/
```

---

## 四、运维常用命令

### 查看日志

```bash
docker logs -f continulian-almanac
```

### 重启容器

```bash
docker restart continulian-almanac
```

### 停止容器

```bash
docker stop continulian-almanac
```

### 删除容器

```bash
docker rm -f continulian-almanac
```

### 删除镜像

```bash
docker rmi continulian-almanac:latest
```

---

## 五、常见问题排查

### 1) 拉取基础镜像失败（超时 / 无法连接 Docker Hub）

现象：`failed to resolve source metadata`、`connectex timeout`。

处理：
- 检查本机网络是否可访问 Docker Hub
- 为 Docker 配置代理
- 为 Docker 配置镜像加速后重试构建

### 2) 8080 端口占用

现象：容器启动失败，提示端口已被占用。

处理：

```bash
docker run -d --name continulian-almanac -p 8081:8080 --restart unless-stopped continulian-almanac:latest
```

然后访问 `http://localhost:8081/`。

### 3) 容器启动后接口无响应

处理顺序：
- `docker ps` 确认容器在运行
- `docker logs continulian-almanac` 查看启动日志
- 检查防火墙/安全组是否放行对外端口

---

## 六、升级发布流程（推荐）

```bash
docker build -f deploy/Dockerfile -t continulian-almanac:latest .
docker rm -f continulian-almanac
docker run -d --name continulian-almanac -p 8080:8080 --restart unless-stopped continulian-almanac:latest
```
