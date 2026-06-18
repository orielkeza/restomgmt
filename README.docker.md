# Docker Setup Guide

## Project Structure

```
RestoManagement/
├── backend/
│   ├── docker-compose.yml      # Backend + Infrastructure
│   └── restomgmt/
│       └── Dockerfile
├── frontend/
│   ├── docker-compose.yml      # Frontend (dev/prod)
│   └── restomgmtReact/
│       ├── Dockerfile
│       └── nginx.conf
├── docker-compose.prod.yml     # Full stack (production)
├── .env                        # Environment variables
└── README.docker.md           # This file
```

## Development Setup

### Option 1: Backend Only
Run backend services (API + Infrastructure):
```bash
cd backend
docker-compose up -d
```
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432
- MongoDB: localhost:27017
- RabbitMQ Dashboard: http://localhost:15672

### Option 2: Frontend Only (Dev)
Run frontend with local backend:
```bash
cd frontend
# First, build the frontend image
docker build -t resto-frontend:dev ./restomgmtReact

# Update docker-compose.yml to reference local backend
docker-compose up -d
```
- Frontend: http://localhost

### Option 3: Full Stack (Production)
Run all services together:
```bash
docker-compose -f docker-compose.prod.yml up -d
```
- Frontend: http://localhost
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432
- MongoDB: localhost:27017
- RabbitMQ: http://localhost:15672

## Running Services

### Start Services
```bash
# Backend stack
cd backend && docker-compose up -d

# Or full stack
docker-compose -f docker-compose.prod.yml up -d
```

### View Logs
```bash
docker-compose logs -f backend        # Backend logs
docker-compose logs -f frontend       # Frontend logs
docker-compose logs -f postgres       # Database logs
```

### Stop Services
```bash
docker-compose down                  # Stop containers
docker-compose down -v               # Stop and remove volumes
```

## Environment Variables

Configure in `.env` file:
- `DB_USERNAME`, `DB_PASSWORD` - PostgreSQL credentials
- `MONGO_USERNAME`, `MONGO_PASSWORD` - MongoDB credentials
- `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` - RabbitMQ credentials
- `JWT_SECRET` - JWT token secret

## Networking

All services communicate via the `resto-network` bridge network:
- Frontend can reach Backend at `http://backend:8080`
- Backend can reach PostgreSQL at `postgres:5432`
- Backend can reach MongoDB at `mongodb:27017`
- Backend can reach RabbitMQ at `rabbitmq:5672`

## Building Images

### Build Backend Image
```bash
cd backend/restomgmt
docker build -t resto-backend:latest .
```

### Build Frontend Image
```bash
cd frontend/restomgmtReact
docker build -t resto-frontend:latest .
```

## Development Workflow

1. **Code in IDE**: Use VS Code or your IDE for development
2. **Docker for Dependencies**: Run infrastructure services in Docker
3. **Run Apps Locally**: Use `npm run dev` (frontend) and Maven (backend)
4. **Container Testing**: Build images and test in containers

### Local Development (Recommended)
```bash
# Terminal 1: Infrastructure only
cd backend
docker-compose up postgres mongodb rabbitmq -d

# Terminal 2: Run backend with Maven
cd backend/restomgmt
mvn spring-boot:run

# Terminal 3: Run frontend with Vite
cd frontend/restomgmtReact
npm run dev
```

## Troubleshooting

### Container won't start
```bash
docker-compose logs <service-name>
```

### Port already in use
Change ports in docker-compose.yml or kill the process:
```bash
# Find process using port 8080
lsof -i :8080
kill -9 <PID>
```

### Database connection issues
- Verify `SPRING_DATASOURCE_URL` matches container names
- Ensure services are on the same network
- Check database credentials in `.env`

### Network issues
```bash
# Inspect network
docker network inspect resto-network

# Restart network
docker network prune
```

## Production Deployment

For production, use `docker-compose.prod.yml`:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

Features included:
- Multi-stage builds (optimized image sizes)
- Health checks
- Proper environment configurations
- Volume persistence
- Network isolation

## Quick Reference

| Component | Dev Port | Prod Port | Container Name |
|-----------|----------|-----------|---|
| Frontend | 3000/5173 | 80 | resto_frontend |
| Backend | 8080 | 8080 | resto_backend |
| PostgreSQL | 5432 | 5432 | resto_postgres |
| MongoDB | 27017 | 27017 | resto_mongodb |
| RabbitMQ | 5672, 15672 | 5672, 15672 | resto_rabbitmq |
