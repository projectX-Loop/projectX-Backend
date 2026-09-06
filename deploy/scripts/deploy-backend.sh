#!/usr/bin/env bash
set -euo pipefail

JAR_FILE="${1:?Pass the absolute path to the executable JAR.}"
: "${OCI_HOST:?OCI_HOST is required}"
: "${OCI_USER:?OCI_USER is required}"
: "${OCI_SSH_KEY:?OCI_SSH_KEY is required}"

test -f "$JAR_FILE" || { echo "JAR file was not found: $JAR_FILE" >&2; exit 1; }
test -r "$OCI_SSH_KEY" || { echo "SSH key cannot be read: $OCI_SSH_KEY" >&2; exit 1; }

REMOTE="$OCI_USER@$OCI_HOST"
scp -i "$OCI_SSH_KEY" "$JAR_FILE" "$REMOTE:/tmp/projectx-backend.jar"
ssh -i "$OCI_SSH_KEY" "$REMOTE" '
  set -eu
  sudo install -o projectx -g projectx -m 0644 /tmp/projectx-backend.jar /opt/projectx/projectx-backend.jar
  rm -f /tmp/projectx-backend.jar
  sudo systemctl restart projectx-backend
  sudo systemctl --no-pager --full status projectx-backend
  curl --fail --silent --show-error http://127.0.0.1:8080/api/health >/dev/null
'
