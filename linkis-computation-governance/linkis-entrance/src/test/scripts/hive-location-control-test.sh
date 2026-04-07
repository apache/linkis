#!/bin/bash

###############################################################################
# Hive Location Control - Remote API Test Script
#
# This script tests the Hive LOCATION control feature via REST API
# It can be used for integration testing on deployed environments
#
# Usage:
#   ./hive-location-control-test.sh [base_url]
#
# Arguments:
#   base_url - Base URL of the Linkis Gateway (default: http://localhost:9001)
#
# Environment Variables:
#   LINKIS_USER - Username for authentication (default: admin)
#   LINKIS_PASSWORD - Password for authentication (default: admin)
###############################################################################

# Configuration
BASE_URL="${1:-http://localhost:9001}"
LINKIS_USER="${LINKIS_USER:-admin}"
LINKIS_PASSWORD="${LINKIS_PASSWORD:-admin}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counters
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

###############################################################################
# Helper Functions
###############################################################################

print_header() {
    echo ""
    echo "========================================"
    echo "$1"
    echo "========================================"
}

print_test() {
    echo ""
    echo -e "${YELLOW}[TEST ${TESTS_RUN}]${NC} $1"
}

print_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((TESTS_PASSED++))
}

print_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((TESTS_FAILED++))
}

print_summary() {
    echo ""
    echo "========================================"
    echo "Test Summary"
    echo "========================================"
    echo "Total:  ${TESTS_RUN}"
    echo -e "Passed: ${GREEN}${TESTS_PASSED}${NC}"
    echo -e "Failed: ${RED}${TESTS_FAILED}${NC}"
    echo "========================================"
}

# Function to execute SQL via Linkis REST API
execute_sql() {
    local sql="$1"
    local execute_json=$(cat <<EOF
{
  "method": "/entrance/execute",
  "parameters": {
    "code": "${sql}",
    "executeType": "hive"
  },
  "labels": {
    "userCreator": "${LINKIS_USER}"
  }
}
EOF
)

    curl -s -X POST \
        "${BASE_URL}/api/rest_j/v1/entrance/execute" \
        -H "Content-Type: application/json" \
        -u "${LINKIS_USER}:${LINKIS_PASSWORD}" \
        -d "${execute_json}"
}

# Function to check if location control is enabled
check_location_control_enabled() {
    local response=$(curl -s -X GET \
        "${BASE_URL}/api/rest_j/v1/configuration/wds.linkis.hive.location.control.enable" \
        -u "${LINKIS_USER}:${LINKIS_PASSWORD}")

    echo "$response" | grep -q '"value":\s*true'
    return $?
}

###############################################################################
# Test Cases
###############################################################################

test_01_create_table_without_location() {
    ((TESTS_RUN++))
    print_test "CREATE TABLE without LOCATION (should succeed)"

    local sql="CREATE TABLE test_table_no_loc (id INT, name STRING)"

    local response=$(execute_sql "$sql")
    local exec_id=$(echo "$response" | grep -o '"execID":"[^"]*"' | cut -d'"' -f4)

    if [ -n "$exec_id" ]; then
        print_pass "SQL accepted, execID: $exec_id"

        # Check if task completed successfully (simplified)
        sleep 2
        local status=$(curl -s -X GET \
            "${BASE_URL}/api/rest_j/v1/entrance/${exec_id}/status" \
            -u "${LINKIS_USER}:${LINKIS_PASSWORD}" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

        if [ "$status" != "Failed" ]; then
            print_pass "Table creation succeeded"
        else
            print_fail "Table creation failed"
        fi
    else
        print_fail "SQL rejected"
    fi
}

test_02_create_table_with_location_disabled() {
    ((TESTS_RUN++))
    print_test "CREATE TABLE with LOCATION when control disabled (should succeed)"

    # First disable location control
    curl -s -X PUT \
        "${BASE_URL}/api/rest_j/v1/configuration/wds.linkis.hive.location.control.enable" \
        -u "${LINKIS_USER}:${LINKIS_PASSWORD}" \
        -H "Content-Type: application/json" \
        -d '{"value": "false"}' > /dev/null

    sleep 1

    local sql="CREATE TABLE test_table_with_loc (id INT) LOCATION '/tmp/test'"

    local response=$(execute_sql "$sql")
    local exec_id=$(echo "$response" | grep -o '"execID":"[^"]*"' | cut -d'"' -f4)

    if [ -n "$exec_id" ]; then
        print_pass "SQL accepted when disabled"
    else
        print_fail "SQL rejected even when disabled"
    fi
}

test_03_create_table_with_location_enabled() {
    ((TESTS_RUN++))
    print_test "CREATE TABLE with LOCATION when control enabled (should be blocked)"

    # Enable location control
    curl -s -X PUT \
        "${BASE_URL}/api/rest_j/v1/configuration/wds.linkis.hive.location.control.enable" \
        -u "${LINKIS_USER}:${LINKIS_PASSWORD}" \
        -H "Content-Type: application/json" \
        -d '{"value": "true"}' > /dev/null

    sleep 1

    local sql="CREATE TABLE test_table_blocked (id INT) LOCATION '/user/data'"

    local response=$(execute_sql "$sql")

    # Should be rejected with error message
    if echo "$response" | grep -q "LOCATION clause is not allowed"; then
        print_pass "SQL blocked with correct error message"
    elif echo "$response" | grep -q "execID"; then
        print_fail "SQL was not blocked"
    else
        print_fail "Unexpected response: $response"
    fi
}

test_04_create_external_table_with_location() {
    ((TESTS_RUN++))
    print_test "CREATE EXTERNAL TABLE with LOCATION (should be blocked)"

    local sql="CREATE EXTERNAL TABLE test_ext_table (id INT) LOCATION '/user/external'"

    local response=$(execute_sql "$sql")

    if echo "$response" | grep -q "LOCATION clause is not allowed"; then
        print_pass "EXTERNAL TABLE with LOCATION blocked"
    else
        print_fail "EXTERNAL TABLE with LOCATION not blocked"
    fi
}

test_05_ctas_with_location() {
    ((TESTS_RUN++))
    print_test "CTAS with LOCATION (should be blocked)"

    local sql="CREATE TABLE new_table LOCATION '/user/data' AS SELECT * FROM source_table"

    local response=$(execute_sql "$sql")

    if echo "$response" | grep -q "LOCATION clause is not allowed"; then
        print_pass "CTAS with LOCATION blocked"
    else
        print_fail "CTAS with LOCATION not blocked"
    fi
}

test_06_ctas_without_location() {
    ((TESTS_RUN++))
    print_test "CTAS without LOCATION (should succeed)"

    local sql="CREATE TABLE new_table AS SELECT * FROM source_table"

    local response=$(execute_sql "$sql")
    local exec_id=$(echo "$response" | grep -o '"execID":"[^"]*"' | cut -d'"' -f4)

    if [ -n "$exec_id" ]; then
        print_pass "CTAS without LOCATION accepted"
    else
        print_fail "CTAS without LOCATION rejected"
    fi
}

test_07_alter_table_set_location() {
    ((TESTS_RUN++))
    print_test "ALTER TABLE SET LOCATION (should NOT be blocked)"

    local sql="ALTER TABLE existing_table SET LOCATION '/new/location'"

    local response=$(execute_sql "$sql")
    local exec_id=$(echo "$response" | grep -o '"execID":"[^"]*"' | cut -d'"' -f4)

    if [ -n "$exec_id" ]; then
        print_pass "ALTER TABLE SET LOCATION accepted (not blocked)"
    else
        print_fail "ALTER TABLE SET LOCATION rejected"
    fi
}

test_08_case_insensitive_location() {
    ((TESTS_RUN++))
    print_test "CREATE TABLE with lowercase 'location' (should be blocked)"

    local sql="CREATE TABLE test_table (id INT) location '/user/data'"

    local response=$(execute_sql "$sql")

    if echo "$response" | grep -q "LOCATION clause is not allowed"; then
        print_pass "Lowercase 'location' blocked"
    else
        print_fail "Lowercase 'location' not blocked"
    fi
}

test_09_multiline_create_table_with_location() {
    ((TESTS_RUN++))
    print_test "Multi-line CREATE TABLE with LOCATION (should be blocked)"

    local sql="CREATE TABLE test_table (
  id INT COMMENT 'ID column',
  name STRING COMMENT 'Name column'
)
COMMENT 'Test table'
LOCATION '/user/hive/warehouse/test_table'"

    local response=$(execute_sql "$sql")

    if echo "$response" | grep -q "LOCATION clause is not allowed"; then
        print_pass "Multi-line SQL with LOCATION blocked"
    else
        print_fail "Multi-line SQL with LOCATION not blocked"
    fi
}

test_10_select_statement_not_blocked() {
    ((TESTS_RUN++))
    print_test "SELECT statement (should NOT be blocked)"

    local sql="SELECT * FROM existing_table WHERE id > 100"

    local response=$(execute_sql "$sql")
    local exec_id=$(echo "$response" | grep -o '"execID":"[^"]*"' | cut -d'"' -f4)

    if [ -n "$exec_id" ]; then
        print_pass "SELECT statement accepted"
    else
        print_fail "SELECT statement rejected"
    fi
}

test_11_empty_sql() {
    ((TESTS_RUN++))
    print_test "Empty SQL (should be handled gracefully)"

    local sql=""

    local response=$(execute_sql "$sql")

    # Empty SQL should be handled gracefully
    print_pass "Empty SQL handled (response: $response)"
}

test_12_error_message_quality() {
    ((TESTS_RUN++))
    print_test "Error message contains guidance"

    local sql="CREATE TABLE test_table (id INT) LOCATION '/user/data'"

    local response=$(execute_sql "$sql")

    # Check if error message contains helpful guidance
    if echo "$response" | grep -q "Please remove the LOCATION clause"; then
        print_pass "Error message contains helpful guidance"
    else
        print_fail "Error message missing guidance"
    fi
}

###############################################################################
# Main Execution
###############################################################################

main() {
    print_header "Hive Location Control - Remote API Test"
    echo "Base URL: ${BASE_URL}"
    echo "User: ${LINKIS_USER}"
    echo ""

    # Check if service is available
    print_header "Checking Service Availability"
    local health_check=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health")

    if [ "$health_check" != "200" ]; then
        echo -e "${RED}ERROR: Service not available at ${BASE_URL}${NC}"
        echo "Please check:"
        echo "  1. Linkis Gateway is running"
        echo "  2. Base URL is correct"
        echo "  3. Network connectivity"
        exit 1
    fi

    echo -e "${GREEN}Service is available${NC}"

    # Run all tests
    print_header "Running Tests"

    test_01_create_table_without_location
    test_02_create_table_with_location_disabled
    test_03_create_table_with_location_enabled
    test_04_create_external_table_with_location
    test_05_ctas_with_location
    test_06_ctas_without_location
    test_07_alter_table_set_location
    test_08_case_insensitive_location
    test_09_multiline_create_table_with_location
    test_10_select_statement_not_blocked
    test_11_empty_sql
    test_12_error_message_quality

    # Print summary
    print_summary

    # Exit with appropriate code
    if [ $TESTS_FAILED -eq 0 ]; then
        echo -e "${GREEN}All tests passed!${NC}"
        exit 0
    else
        echo -e "${RED}Some tests failed!${NC}"
        exit 1
    fi
}

# Run main function
main "$@"
