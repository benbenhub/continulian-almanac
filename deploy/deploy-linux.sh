#!/usr/bin/env bash
set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-continulian-almanac}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
CONTAINER_NAME="${CONTAINER_NAME:-continulian-almanac}"
HOST_PORT="${HOST_PORT:-8080}"
CONTAINER_PORT="${CONTAINER_PORT:-8080}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

docker version >/dev/null
docker build -f "${ROOT_DIR}/deploy/Dockerfile" -t "${IMAGE_NAME}:${IMAGE_TAG}" "${ROOT_DIR}"

if docker ps -a --format '{{.Names}}' | grep -wq "${CONTAINER_NAME}"; then
  docker rm -f "${CONTAINER_NAME}" >/dev/null
fi

docker run -d \
  --name "${CONTAINER_NAME}" \
  -p "${HOST_PORT}:${CONTAINER_PORT}" \
  --restart unless-stopped \
  "${IMAGE_NAME}:${IMAGE_TAG}" >/dev/null

echo "部署完成: http://localhost:${HOST_PORT}"
