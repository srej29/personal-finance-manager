#!/bin/bash

# DEBUGGING VERSION - Only run first few critical tests
BASE_URL="https://personal-finance-manager-mhp9.onrender.com/api"
TEST_TIMESTAMP=$(date +%s)
USER1_EMAIL="john.doe.${TEST_TIMESTAMP}@example.com"

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

# Enhanced API test function with full debugging
api_test_debug() {
    local test_name="$1"
    local expected_status_range="$2"
    local method="$3"
    local endpoint="$4"
    local data="$5"
    local cookie_file="$6"
    local save_cookies="$7"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    echo -e "\n${BLUE}=== TEST $TOTAL_TESTS: $test_name ===${NC}"
    echo "URL: $BASE_URL$endpoint"
    echo "Method: $method"
    echo "Expected Status: $expected_status_range"

    if [ -n "$data" ]; then
        echo "Request Data: $data"
    fi

    if [ -n "$cookie_file" ] && [ -f "$cookie_file" ]; then
        echo "Using cookies from: $cookie_file"
        echo "Cookie contents:"
        cat "$cookie_file" 2>/dev/null || echo "No cookies found"
    fi

    # Build curl command
    local curl_cmd="curl -v -s -w '%{http_code}'"

    if [ -n "$method" ]; then
        curl_cmd="$curl_cmd -X $method"
    fi

    if [ -n "$data" ]; then
        curl_cmd="$curl_cmd -H 'Content-Type: application/json' -d '$data'"
    fi

    if [ -n "$cookie_file" ] && [ -f "$cookie_file" ]; then
        curl_cmd="$curl_cmd -b '$cookie_file'"
    fi

    if [ -n "$save_cookies" ]; then
        curl_cmd="$curl_cmd -c '$save_cookies'"
    fi

    curl_cmd="$curl_cmd '$BASE_URL$endpoint'"

    echo "Curl command: $curl_cmd"
    echo ""

    # Execute curl
    local full_response
    echo "--- CURL OUTPUT START ---"
    full_response=$(eval "$curl_cmd" 2>&1)
    echo "--- CURL OUTPUT END ---"

    # Extract status code and body
    local actual_status="${full_response: -3}"
    local response_body="${full_response%???}"

    echo ""
    echo "Status Code: $actual_status"
    echo "Response Body: $response_body"

    # Check status
    case "$expected_status_range" in
        "2xx")
            if [ "$actual_status" -ge 200 ] && [ "$actual_status" -lt 300 ]; then
                echo -e "${GREEN}✓ PASS${NC}"
                PASSED_TESTS=$((PASSED_TESTS + 1))
                return 0
            else
                echo -e "${RED}✗ FAIL${NC}"
                FAILED_TESTS=$((FAILED_TESTS + 1))
                return 1
            fi
            ;;
        "4xx")
            if [ "$actual_status" -ge 400 ] && [ "$actual_status" -lt 500 ]; then
                echo -e "${GREEN}✓ PASS${NC}"
                PASSED_TESTS=$((PASSED_TESTS + 1))
                return 0
            else
                echo -e "${RED}✗ FAIL${NC}"
                FAILED_TESTS=$((FAILED_TESTS + 1))
                return 1
            fi
            ;;
        *)
            if [ "$actual_status" = "$expected_status_range" ]; then
                echo -e "${GREEN}✓ PASS${NC}"
                PASSED_TESTS=$((PASSED_TESTS + 1))
                return 0
            else
                echo -e "${RED}✗ FAIL${NC}"
                FAILED_TESTS=$((FAILED_TESTS + 1))
                return 1
            fi
            ;;
    esac
}

echo "DEBUGGING PERSONAL FINANCE MANAGER API"
echo "======================================"
echo "Base URL: $BASE_URL"
echo "Test User: $USER1_EMAIL"
echo ""

# Test 1: Registration
api_test_debug "Register User" "2xx" "POST" "/auth/register" "{
    \"username\": \"$USER1_EMAIL\",
    \"password\": \"securePassword123\",
    \"fullName\": \"John Doe\",
    \"phoneNumber\": \"+1234567890\"
}"

# Test 2: Login
api_test_debug "Login User" "2xx" "POST" "/auth/login" "{
    \"username\": \"$USER1_EMAIL\",
    \"password\": \"securePassword123\"
}" "" "user1_session.txt"

# Test 3: Check Categories (Authenticated)
api_test_debug "Get Categories" "2xx" "GET" "/categories" "" "user1_session.txt"

# Test 4: Create Transaction (The critical test)
api_test_debug "Create Transaction" "2xx" "POST" "/transactions" '{
    "amount": 5000.00,
    "date": "2024-01-15",
    "category": "Salary",
    "description": "January Salary"
}' "user1_session.txt"

# Test 5: Get All Transactions
api_test_debug "Get Transactions" "2xx" "GET" "/transactions" "" "user1_session.txt"

# Test 6: Monthly Report
api_test_debug "Monthly Report" "2xx" "GET" "/reports/monthly/2024/1" "" "user1_session.txt"

echo ""
echo "SUMMARY:"
echo "Total Tests: $TOTAL_TESTS"
echo "Passed: $PASSED_TESTS"
echo "Failed: $FAILED_TESTS"

# Cleanup
rm -f user1_session.txt

if [ $FAILED_TESTS -gt 0 ]; then
    echo "Some tests failed. Check the detailed output above."
    exit 1
else
    echo "All tests passed!"
    exit 0
fi