#!/usr/bin/env bash
set -euo pipefail

# Config
API_BASE="${API_BASE:-http://localhost:8080}"
USER_EMAIL="${USER_EMAIL:-test.user@example.com}"
USER_PASSWORD="${USER_PASSWORD:-Password@123}"
USER_NAME="${USER_NAME:-Test User}"
USER_PHONE="${USER_PHONE:-9999999999}"
CID="${CID:-$(uuidgen 2>/dev/null || echo "cid-$(date +%s)-$RANDOM")}" # Correlation ID

echo "Using API_BASE=$API_BASE"
echo "Using X-Correlation-Id=$CID"

curl_json() {
  local method="${1-}"; shift || true
  local url="${1-}"; shift || true
  local data=""
  # parse optional -d JSON
  if [[ "${1-}" == "-d" ]]; then
    shift || true
    data="${1-}"
    shift || true
  fi
  if [[ -n "$data" ]]; then
    curl -sS -X "$method" "$url" \
      -H "Content-Type: application/json" \
      -H "X-Correlation-Id: $CID" \
      -H "Accept: application/json" \
      "$@" \
      -d "$data"
  else
    curl -sS -X "$method" "$url" \
      -H "X-Correlation-Id: $CID" \
      -H "Accept: application/json" \
      "$@"
  fi
}

json_get() {
  local key="$1"
  python3 - "$key" 2>/dev/null <<'PY'
import sys, json
key = sys.argv[1]
try:
    data = json.load(sys.stdin)
except Exception:
    print("")
    sys.exit(0)

def get(d, path):
    parts = path.split('.')
    cur = d
    for p in parts:
        if isinstance(cur, dict) and p in cur:
            cur = cur[p]
        else:
            print("")
            sys.exit(0)
    if isinstance(cur, (dict, list)):
        import json as j
        print(j.dumps(cur))
    else:
        print(cur)

get(data, key)
PY
}

extract_token() {
  local json_input
  if [[ $# -ge 1 ]]; then
    json_input="$1"
  else
    json_input="$(cat)"
  fi
  echo "$json_input" | json_get 'data.accessToken'
}

echo "[1/9] Health checks"
curl_json GET "$API_BASE/api-gateway/actuator/health" | sed -e 's/.*/  gateway: &/' || true
curl_json GET "$API_BASE/user-service/actuator/health" | sed -e 's/.*/  user: &/' || true
curl_json GET "$API_BASE/product-service/actuator/health" | sed -e 's/.*/  product: &/' || true
curl_json GET "$API_BASE/cart-service/actuator/health" | sed -e 's/.*/  cart: &/' || true
curl_json GET "$API_BASE/order-service/actuator/health" | sed -e 's/.*/  order: &/' || true
curl_json GET "$API_BASE/notification-service/actuator/health" | sed -e 's/.*/  notification: &/' || true

echo "[2/9] Login (or register if needed)"
LOGIN_JSON=$(curl_json POST "$API_BASE/user-service/api/auth/login" -d "{\"email\":\"$USER_EMAIL\",\"password\":\"$USER_PASSWORD\"}") || true
ACCESS_TOKEN=$(echo "$LOGIN_JSON" | extract_token)

if [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]]; then
  echo "  Login failed, attempting registration..."
  REG_JSON=$(curl_json POST "$API_BASE/user-service/api/auth/register" -d "{\"name\":\"$USER_NAME\",\"email\":\"$USER_EMAIL\",\"password\":\"$USER_PASSWORD\",\"phone\":\"$USER_PHONE\"}")
  ACCESS_TOKEN=$(echo "$REG_JSON" | extract_token)
fi

if [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]]; then
  echo "ERROR: Could not obtain access token" >&2
  echo "Response: $LOGIN_JSON" >&2
  exit 1
fi
echo "  Access token acquired"

AUTH_HEADER=( -H "Authorization: Bearer $ACCESS_TOKEN" )

echo "[3/9] Get current user"
curl_json GET "$API_BASE/user-service/api/auth/me" "${AUTH_HEADER[@]}" | sed -e 's/.*/  me: &/'

echo "[4/9] List products"
PRODS_JSON=$(curl_json GET "$API_BASE/product-service/api/products?size=5" "${AUTH_HEADER[@]}")
FIRST_PRODUCT_ID=$(echo "$PRODS_JSON" | json_get 'data.content' | python3 - <<'PY'
import sys, json
try:
    arr = json.load(sys.stdin)
    print(arr[0]['id'] if arr and isinstance(arr, list) else "")
except Exception:
    print("")
PY
)
if [[ -z "$FIRST_PRODUCT_ID" ]]; then
  echo "  No products found, attempting search fallback"
  PRODS_JSON=$(curl_json GET "$API_BASE/product-service/api/products/search?size=5" "${AUTH_HEADER[@]}")
  FIRST_PRODUCT_ID=$(echo "$PRODS_JSON" | json_get 'data.content' | python3 - <<'PY'
import sys, json
try:
    arr = json.load(sys.stdin)
    print(arr[0]['id'] if arr and isinstance(arr, list) else "")
except Exception:
    print("")
PY
)
fi
echo "  Using productId=$FIRST_PRODUCT_ID"

echo "[5/9] Add to cart"
ADD_JSON=$(curl_json POST "$API_BASE/cart-service/api/cart/add" -d "{\"productId\":\"$FIRST_PRODUCT_ID\",\"quantity\":1}" "${AUTH_HEADER[@]}")
echo "  Added: $(echo "$ADD_JSON" | json_get 'message')"

echo "[6/9] Get cart summary"
CART_JSON=$(curl_json GET "$API_BASE/cart-service/api/cart" "${AUTH_HEADER[@]}")
echo "  Items in cart: $(echo "$CART_JSON" | json_get 'data.totalItems')"

echo "[7/9] Validate cart"
VAL_JSON=$(curl_json POST "$API_BASE/cart-service/api/cart/validate" -d "{}" "${AUTH_HEADER[@]}")
echo "  Validation: $(echo "$VAL_JSON" | json_get 'message')"

echo "[8/9] Create order (COD)"
ORDER_REQ=$(cat <<JSON
{
  "deliveryAddress": {
    "addressType": "home",
    "addressName": "Home",
    "street": "123 Hill Road",
    "city": "Mahabaleshwar",
    "state": "MH",
    "pincode": "412806",
    "landmark": "Near Market"
  },
  "payment": {
    "paymentMethod": "COD"
  },
  "specialInstructions": "Leave at door"
}
JSON
)
ORDER_JSON=$(curl_json POST "$API_BASE/order-service/api/orders" -d "$ORDER_REQ" "${AUTH_HEADER[@]}")
ORDER_ID=$(echo "$ORDER_JSON" | json_get 'data.id')
echo "  Order created: id=$ORDER_ID"

echo "[9/9] My orders"
MY_JSON=$(curl_json GET "$API_BASE/order-service/api/orders/my-orders?size=5" "${AUTH_HEADER[@]}")
echo "$MY_JSON" | sed -e 's/.*/  response: &/' > /dev/null
echo "Done. Correlation Id: $CID"


