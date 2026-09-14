#!/bin/sh
set -e

register_offering() {
  curl -s -X POST "${TENANT_MANAGER_HOST}/offerings" \
    -H "Content-Type: application/json" \
    -d "{\"offeringKey\":\"${OFFERING_KEY:-csv-export}\",\"name\":\"CSV Export\",\"description\":\"Export any record type to CSV from the record list.\",\"customizationPoint\":\"${1}\",\"targetServiceDns\":\"${SELF_DNS}\",\"targetPort\":${SELF_PORT},\"version\":\"${VERSION:-1.0.0}\"}" \
    || true
}

register_offering "record.tabs.actions"
register_offering "record.csv-export"

exec nginx -g "daemon off;"
