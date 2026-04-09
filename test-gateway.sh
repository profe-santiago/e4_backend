#!/bin/bash

# Test script for API Gateway
# This script tests rate limiting, CORS, and basic routing

GATEWAY_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================="
echo "API Gateway Test Suite"
echo "========================================="
echo ""

# Test 1: Health Check
echo -e "${YELLOW}Test 1: Health Check${NC}"
HEALTH=$(curl -s -w "\n%{http_code}" ${GATEWAY_URL}/actuator/health)
HTTP_CODE=$(echo "$HEALTH" | tail -n1)
BODY=$(echo "$HEALTH" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✓ Health check passed${NC}"
    echo "$BODY" | jq '.'
else
    echo -e "${RED}✗ Health check failed (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# Test 2: CORS Headers
echo -e "${YELLOW}Test 2: CORS Preflight${NC}"
CORS_RESPONSE=$(curl -s -I -X OPTIONS ${GATEWAY_URL}/api/v1/events \
    -H "Origin: http://localhost:3000" \
    -H "Access-Control-Request-Method: GET")

if echo "$CORS_RESPONSE" | grep -i "access-control-allow-origin" > /dev/null; then
    echo -e "${GREEN}✓ CORS headers present${NC}"
    echo "$CORS_RESPONSE" | grep -i "access-control"
else
    echo -e "${RED}✗ CORS headers missing${NC}"
fi
echo ""

# Test 3: Route to Event Service
echo -e "${YELLOW}Test 3: Route to Event Service${NC}"
EVENT_RESPONSE=$(curl -s -w "\n%{http_code}" ${GATEWAY_URL}/api/v1/events)
HTTP_CODE=$(echo "$EVENT_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 401 ]; then
    echo -e "${GREEN}✓ Routing to event-service working (HTTP $HTTP_CODE)${NC}"
else
    echo -e "${RED}✗ Routing failed (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# Test 4: Rate Limiting Headers
echo -e "${YELLOW}Test 4: Rate Limiting Headers${NC}"
RATE_LIMIT=$(curl -s -I ${GATEWAY_URL}/api/v1/events)

if echo "$RATE_LIMIT" | grep -i "X-RateLimit-Limit" > /dev/null; then
    echo -e "${GREEN}✓ Rate limit headers present${NC}"
    echo "$RATE_LIMIT" | grep -i "X-RateLimit"
else
    echo -e "${RED}✗ Rate limit headers missing${NC}"
fi
echo ""

# Test 5: Rate Limiting Enforcement (simulate burst)
echo -e "${YELLOW}Test 5: Rate Limiting Enforcement${NC}"
echo "Sending 105 requests rapidly (should hit limit at ~100 for anonymous users)..."

SUCCESS_COUNT=0
RATE_LIMITED_COUNT=0

for i in {1..105}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" ${GATEWAY_URL}/api/v1/events)
    if [ "$HTTP_CODE" -eq 200 ]; then
        ((SUCCESS_COUNT++))
    elif [ "$HTTP_CODE" -eq 429 ]; then
        ((RATE_LIMITED_COUNT++))
    fi
done

echo "Successful requests: $SUCCESS_COUNT"
echo "Rate limited requests: $RATE_LIMITED_COUNT"

if [ $RATE_LIMITED_COUNT -gt 0 ]; then
    echo -e "${GREEN}✓ Rate limiting is enforcing limits${NC}"
else
    echo -e "${YELLOW}⚠ No rate limiting detected (might need to adjust burst capacity)${NC}"
fi
echo ""

# Test 6: Circuit Breaker Status
echo -e "${YELLOW}Test 6: Circuit Breaker Status${NC}"
CB_STATUS=$(curl -s ${GATEWAY_URL}/actuator/circuitbreakers)

if echo "$CB_STATUS" | jq '.' > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Circuit breakers endpoint accessible${NC}"
    echo "$CB_STATUS" | jq '.circuitBreakers' 2>/dev/null || echo "$CB_STATUS"
else
    echo -e "${YELLOW}⚠ Circuit breakers endpoint not available${NC}"
fi
echo ""

# Summary
echo "========================================="
echo "Test Suite Complete"
echo "========================================="
