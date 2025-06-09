#!/bin/bash

# Ecomovil Microservices Deployment Script
# This script helps deploy and manage the Ecomovil microservices environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to check required ports
check_ports() {
    local ports=(8080 8081 8082 3306 3307 5433 6379 4566 8090)
    local occupied_ports=()
    
    print_status "Checking if required ports are available..."
    
    for port in "${ports[@]}"; do
        if lsof -ti:$port > /dev/null 2>&1; then
            occupied_ports+=($port)
        fi
    done
    
    if [ ${#occupied_ports[@]} -gt 0 ]; then
        print_warning "The following ports are occupied: ${occupied_ports[*]}"
        print_warning "Please stop the services using these ports or modify docker-compose.yml"
        read -p "Do you want to continue anyway? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        print_success "All required ports are available"
    fi
}

# Function to create .env file if it doesn't exist
setup_env() {
    if [ ! -f .env ]; then
        print_status "Creating .env file from example..."
        cp .env.example .env
        print_success ".env file created. You can modify it to customize your configuration."
    else
        print_status ".env file already exists"
    fi
}

# Function to build and start services
start_services() {
    print_status "Building and starting Ecomovil microservices..."
    
    # Build and start all services
    docker-compose up -d --build
    
    print_status "Waiting for services to be healthy..."
    
    # Wait for databases to be healthy
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        print_status "Health check attempt $attempt/$max_attempts..."
        
        local healthy_services=0
        local total_services=3
        
        # Check IAM database
        if docker-compose exec -T iam-db pg_isready -U iam_user -d ecomovil_iam_db > /dev/null 2>&1; then
            ((healthy_services++))
        fi
        
        # Check Plans database
        if docker-compose exec -T plans-db mysqladmin ping -h localhost -u plans_user -pplans_password > /dev/null 2>&1; then
            ((healthy_services++))
        fi
        
        # Check Users database
        if docker-compose exec -T users-db mysqladmin ping -h localhost -u users_user -pusers_password > /dev/null 2>&1; then
            ((healthy_services++))
        fi
        
        if [ $healthy_services -eq $total_services ]; then
            print_success "All databases are healthy!"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            print_error "Timeout waiting for services to be healthy"
            print_status "You can check the logs with: docker-compose logs"
            exit 1
        fi
        
        sleep 10
        ((attempt++))
    done
    
    # Additional wait for application services
    print_status "Waiting for application services to start..."
    sleep 30
    
    print_success "Ecomovil microservices are now running!"
}

# Function to show service status
show_status() {
    print_status "Current service status:"
    docker-compose ps
    
    echo
    print_status "Service endpoints:"
    echo "  - IAM Service:    http://localhost:8080"
    echo "  - Plans Service:  http://localhost:8081"
    echo "  - Users Service:  http://localhost:8082"
    echo "  - Adminer:        http://localhost:8090"
    echo
    print_status "Health checks:"
    echo "  - IAM Health:     http://localhost:8080/actuator/health"
    echo "  - Plans Health:   http://localhost:8081/actuator/health"
    echo "  - Users Health:   http://localhost:8082/actuator/health"
}

# Function to run health checks
health_check() {
    print_status "Performing health checks..."
    
    local services=("iam-service:8080" "plans-service:8081" "users-service:8082")
    local healthy=0
    
    for service in "${services[@]}"; do
        local name=$(echo $service | cut -d':' -f1)
        local port=$(echo $service | cut -d':' -f2)
        
        if curl -f -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            print_success "$name is healthy"
            ((healthy++))
        else
            print_error "$name is not responding"
        fi
    done
    
    echo
    if [ $healthy -eq ${#services[@]} ]; then
        print_success "All services are healthy!"
    else
        print_warning "$healthy/${#services[@]} services are healthy"
    fi
}

# Function to stop services
stop_services() {
    print_status "Stopping Ecomovil microservices..."
    docker-compose down
    print_success "Services stopped"
}

# Function to clean up (remove containers, networks, and volumes)
cleanup() {
    print_warning "This will remove all containers, networks, and volumes (including database data)"
    read -p "Are you sure you want to continue? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Cleaning up..."
        docker-compose down -v --remove-orphans
        docker system prune -f
        print_success "Cleanup completed"
    else
        print_status "Cleanup cancelled"
    fi
}

# Function to show logs
show_logs() {
    local service=${1:-""}
    
    if [ -z "$service" ]; then
        print_status "Showing logs for all services..."
        docker-compose logs -f
    else
        print_status "Showing logs for $service..."
        docker-compose logs -f "$service"
    fi
}

# Function to show help
show_help() {
    echo "Ecomovil Microservices Deployment Script"
    echo
    echo "Usage: $0 [COMMAND]"
    echo
    echo "Commands:"
    echo "  start     - Build and start all services"
    echo "  stop      - Stop all services"
    echo "  restart   - Restart all services"
    echo "  status    - Show service status and endpoints"
    echo "  health    - Run health checks on all services"
    echo "  logs      - Show logs for all services"
    echo "  logs [service] - Show logs for specific service"
    echo "  cleanup   - Remove all containers, networks, and volumes"
    echo "  help      - Show this help message"
    echo
    echo "Examples:"
    echo "  $0 start"
    echo "  $0 logs iam-service"
    echo "  $0 health"
}

# Main script logic
main() {
    local command=${1:-"help"}
    
    case $command in
        "start")
            check_docker
            check_ports
            setup_env
            start_services
            show_status
            ;;
        "stop")
            stop_services
            ;;
        "restart")
            stop_services
            sleep 5
            start_services
            show_status
            ;;
        "status")
            show_status
            ;;
        "health")
            health_check
            ;;
        "logs")
            show_logs ${2:-""}
            ;;
        "cleanup")
            cleanup
            ;;
        "help"|*)
            show_help
            ;;
    esac
}

# Run main function with all arguments
main "$@"
