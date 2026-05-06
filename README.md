# CaelanNetworkDisk

## Tech Stack Desicion:
Spring Boot for framework
Spring Cloud + JWT for gate way and auth
Kafka for Async Events
gRPC for RPC between servers
MySql + Mybatis for metadata storage

AI integration part is pending

## Quick Start

```bash
# 1. Start infrastructure
docker compose up -d

# 2. Build (downloads protoc on first run)
mvn clean install -DskipTests

# 3. Start services (in order)
#    nd-auth → nd-files → nd-ai → nd-gateway
```

