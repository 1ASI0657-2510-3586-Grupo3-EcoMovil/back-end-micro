#!/bin/bash

# Ecomovil Microservices Test Script
# This script tests all the microservices endpoints and functionality

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[FAIL]${NC} $1"
}

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to run a test
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    print_status "Running: $test_name"
    ((TOTAL_TESTS++))
    
    if eval "$test_command"; then
        print_success "$test_name"
        ((PASSED_TESTS++))
        return 0
    else
        print_error "$test_name"
        ((FAILED_TESTS++))
        return 1
    fi
}

# Function to test HTTP endpoint
test_http_endpoint() {
    local name="$1"
    local url="$2"
    local expected_status="$3"
    
    local actual_status=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")
    
    if [ "$actual_status" = "$expected_status" ]; then
        return 0
    else
        echo "Expected status $expected_status, got $actual_status for $url" >&2
        return 1
    fi
}

# Function to test JSON endpoint
test_json_endpoint() {
    local name="$1"
    local url="$2"
    local expected_field="$3"
    
    local response=$(curl -s "$url" 2>/dev/null || echo "{}")
    
    if echo "$response" | jq -e "$expected_field" > /dev/null 2>&1; then
        return 0
    else
        echo "Expected field $expected_field not found in response from $url" >&2
        return 1
    fi
}

# Function to wait for services
wait_for_services() {
    print_status "Waiting for services to be ready..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "http://localhost:8080/actuator/health" > /dev/null 2>&1 && \
           curl -f -s "http://localhost:8081/actuator/health" > /dev/null 2>&1 && \
           curl -f -s "http://localhost:8082/actuator/health" > /dev/null 2>&1; then
            print_success "All services are ready!"
            return 0
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            print_error "Timeout waiting for services to be ready"
            return 1
        fi
        
        echo -n "."
        sleep 5
        ((attempt++))
    done
}

# Test database connectivity
test_databases() {
    print_status "Testing database connectivity..."
    
    # Test IAM database (PostgreSQL)
    run_test "IAM Database Connection" \
        "docker-compose exec -T iam-db pg_isready -U iam_user -d ecomovil_iam_db"
    
    # Test Plans database (MySQL)
    run_test "Plans Database Connection" \
        "docker-compose exec -T plans-db mysqladmin ping -h localhost -u plans_user -pplans_password"
    
    # Test Users database (MySQL)
    run_test "Users Database Connection" \
        "docker-compose exec -T users-db mysqladmin ping -h localhost -u users_user -pusers_password"
}

# Test application health endpoints
test_health_endpoints() {
    print_status "Testing health endpoints..."
    
    run_test "IAM Service Health Check" \
        "test_http_endpoint 'IAM Health' 'http://localhost:8080/actuator/health' '200'"
    
    run_test "Plans Service Health Check" \
        "test_http_endpoint 'Plans Health' 'http://localhost:8081/actuator/health' '200'"
    
    run_test "Users Service Health Check" \
        "test_http_endpoint 'Users Health' 'http://localhost:8082/actuator/health' '200'"
}

# Test application endpoints
test_application_endpoints() {
    print_status "Testing application endpoints..."
    
    # Test Plans service endpoints
    run_test "Plans Service - Get All Plans" \
        "test_http_endpoint 'Get Plans' 'http://localhost:8081/api/v1/plans' '200'"
    
    # Test Users service endpoints  
    run_test "Users Service - Get All Profiles" \
        "test_http_endpoint 'Get Profiles' 'http://localhost:8082/api/v1/profiles' '200'"
}

# Test inter-service communication
test_inter_service_communication() {
    print_status "Testing inter-service communication..."
    
    # Test if Users service can communicate with Plans service
    run_test "Users Service can reach Plans Service" \
        "docker-compose exec -T users-service curl -f http://plans-service:8081/actuator/health"
}

# Test infrastructure services
test_infrastructure() {
    print_status "Testing infrastructure services..."
    
    # Test Redis
    run_test "Redis Connection" \
        "docker-compose exec -T redis redis-cli ping"
    
    # Test LocalStack
    run_test "LocalStack Health" \
        "test_http_endpoint 'LocalStack' 'http://localhost:4566' '200'"
    
    # Test Adminer
    run_test "Adminer Web Interface" \
        "test_http_endpoint 'Adminer' 'http://localhost:8090' '200'"
}

# Test sample data
test_sample_data() {
    print_status "Testing sample data..."
    
    # Check if default plans exist
    run_test "Default Plans Exist" \
        "test_json_endpoint 'Plans' 'http://localhost:8081/api/v1/plans' 'length > 0'"
}

# Function to generate test report
generate_report() {
    echo
    echo "======================================"
    echo "         TEST RESULTS SUMMARY"
    echo "======================================"
    echo "Total Tests: $TOTAL_TESTS"
    echo "Passed: $PASSED_TESTS"
    echo "Failed: $FAILED_TESTS"
    echo "Success Rate: $(( PASSED_TESTS * 100 / TOTAL_TESTS ))%"
    echo "======================================"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        print_success "All tests passed! 🎉"
        return 0
    else
        print_error "Some tests failed! ❌"
        return 1
    fi
}

# Function to run all tests
run_all_tests() {
    echo "🧪 Starting Ecomovil Microservices Test Suite"
    echo "=============================================="
    
    # Wait for services to be ready
    if ! wait_for_services; then
        print_error "Services are not ready. Please ensure they are running with './deploy.sh start'"
        exit 1
    fi
    
    # Run test suites
    test_databases
    echo
    test_health_endpoints
    echo
    test_application_endpoints
    echo
    test_inter_service_communication
    echo
    test_infrastructure
    echo
    test_sample_data
    echo
    
    # Generate report
    generate_report
}

# Function to run specific test category
run_category_tests() {
    local category="$1"
    
    case $category in
        "db"|"database")
            test_databases
            ;;
        "health")
            test_health_endpoints
            ;;
        "api"|"endpoints")
            test_application_endpoints
            ;;
        "communication"|"comm")
            test_inter_service_communication
            ;;
        "infrastructure"|"infra")
            test_infrastructure
            ;;
        "data")
            test_sample_data
            ;;
        *)
            echo "Unknown test category: $category"
            echo "Available categories: db, health, api, communication, infrastructure, data"
            exit 1
            ;;
    esac
    
    generate_report
}

# Function to show help
show_help() {
    echo "Ecomovil Microservices Test Script"
    echo
    echo "Usage: $0 [CATEGORY]"
    echo
    echo "Categories:"
    echo "  all          - Run all tests (default)"
    echo "  db           - Test database connectivity"
    echo "  health       - Test health endpoints"
    echo "  api          - Test application endpoints"
    echo "  communication - Test inter-service communication"
    echo "  infrastructure - Test infrastructure services"
    echo "  data         - Test sample data"
    echo "  help         - Show this help message"
    echo
    echo "Examples:"
    echo "  $0              # Run all tests"
    echo "  $0 health       # Run only health tests"
    echo "  $0 api          # Run only API tests"
}

# Main script logic
main() {
    local category=${1:-"all"}
    
    # Check if jq is installed (required for JSON parsing)
    if ! command -v jq &> /dev/null; then
        print_error "jq is required but not installed. Please install it first:"
        echo "  macOS: brew install jq"
        echo "  Ubuntu: sudo apt-get install jq"
        exit 1
    fi
    
    case $category in
        "all"|"")
            run_all_tests
            ;;
        "help")
            show_help
            ;;
        *)
            run_category_tests "$category"
            ;;
    esac
}

# Run main function with all arguments
main "$@"
