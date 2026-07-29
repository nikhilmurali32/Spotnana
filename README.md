# SkyPath Flight Search Engine

## Overview & Quick Start
SkyPath is a modern, high-performance flight connection search engine that efficiently calculates the most optimal travel routes across the globe, elegantly displaying layovers, price totals, and date-line crossings.

Sample UI
<img width="1915" height="1054" alt="image" src="https://github.com/user-attachments/assets/ec374c8c-c4ff-4564-b01a-882635df2b5a" />

To run the full application stack locally, use Docker Compose:

```bash
docker-compose up --build
```

This will initialize and link both the backend and frontend containers. Once the services are healthy and running, navigate your browser to `http://localhost:3000` to access the SkyPath UI.

## Architecture Decisions
- **Backend (Spring Boot 3 / Java)**: Selected for its robust enterprise ecosystem, powerful dependency injection, and excellent performance when managing complex object graphs and business rules (such as intricate timezone and layover calculations).
- **Frontend (Next.js & React)**: Chosen to deliver a fast, responsive, and aesthetically premium user interface. The client-rendered architecture seamlessly manages complex state mapping for multi-segment itineraries, styled with modern Tailwind CSS.
- **Graph Search Implementation**: The core routing engine employs a best-first graph traversal algorithm. We utilize a **Priority Queue** to continuously prioritize and expand itineraries with the shortest total travel time. To guarantee rapid API response times and prevent deep combinatorial explosions, the traversal depth is strictly bounded to a maximum of 3 flight segments (2 layovers) per itinerary.

## Tradeoffs Considered
### In-Memory Data Storage
The current prototype parses the JSON flight dataset precisely once on startup and stores it entirely in-memory within the Java service layer. 
- **Pros**: This approach yields blazingly fast `O(1)` lookup speeds for airports and completely eliminates the network latency and connection pooling overhead associated with querying a traditional relational database. It is incredibly efficient for a rapid prototype.
- **Cons**: This architecture fundamentally limits horizontal scalability. If the airline dataset scales to millions of global flights, memory consumption will become a severe bottleneck. Additionally, it lacks durability and live-update capabilities—incorporating new flights currently requires a full backend application restart.

## Future Scalability (What I'd do with more time)
To evolve SkyPath from a functional prototype into a production-grade, globally resilient platform, I would transition the system toward a distributed architecture:

1. **Event-Driven Data Pipeline**: I would introduce a high-throughput message broker (e.g., **Apache Kafka**) to asynchronously ingest real-time flight schedule changes, cancellations, and price fluctuations. This event-driven approach would allow the search instances to hydrate and update their states instantly without requiring a hard restart.
2. **Decoupled Search Index**: I would decouple the in-memory dataset from the application servers. By offloading the routing data to a distributed in-memory cache (like **Redis**) or a dedicated, purpose-built Graph Database (like **Neo4j** or **Amazon Neptune**), we could horizontally scale the search APIs independently to handle massive bursts of concurrent users while keeping search latencies remarkably low.
