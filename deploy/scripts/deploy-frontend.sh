#!/usr/bin/env bash
set -euo pipefail

DIST_DIR="${1:?Pass the absolute frontend build directory.}"
: "${AWS_S3_BUCKET:?AWS_S3_BUCKET is required}"
: "${AWS_CLOUDFRONT_DISTRIBUTION_ID:?AWS_CLOUDFRONT_DISTRIBUTION_ID is required}"

test -f "$DIST_DIR/index.html" || { echo "index.html was not found in $DIST_DIR" >&2; exit 1; }

aws s3 sync "$DIST_DIR/" "s3://$AWS_S3_BUCKET/" --delete \
  --exclude 'index.html' \
  --cache-control 'public,max-age=31536000,immutable'
aws s3 cp "$DIST_DIR/index.html" "s3://$AWS_S3_BUCKET/index.html" \
  --cache-control 'no-cache,no-store,must-revalidate' \
  --content-type 'text/html; charset=utf-8'
aws cloudfront create-invalidation \
  --distribution-id "$AWS_CLOUDFRONT_DISTRIBUTION_ID" \
  --paths '/*'
