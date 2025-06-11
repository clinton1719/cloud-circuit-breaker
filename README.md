# ☁️ Cloud Circuit Breaker

> Distributed, cloud-native circuit breaker library for Spring Boot applications running on AWS. Backed by DynamoDB. Built for resilience at scale.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.clinton1719/cloud-circuitbreaker-core?color=blue&label=Maven%20Central)](https://search.maven.org/search?q=g:io.github.clinton1719)
[![License](https://img.shields.io/github/license/clinton1719/cloud-circuitbreaker?color=green)](https://github.com/clinton1719/cloud-circuitbreaker/blob/main/LICENSE)
[![Source Code](https://img.shields.io/badge/source-github-blue?logo=github)](https://github.com/clinton1719/cloud-circuitbreaker/tree/main/cloud-circuitbreaker-core)

---

## 🔧 Why this library?

Most circuit breaker libraries (like Resilience4j or Netflix Hystrix) are **in-memory** and **local-instance scoped**, making them unreliable in distributed, auto-scaling environments.

This library was built from the ground up to:
- Work **across instances and containers** (e.g., ECS, EKS, Lambda)
- Use **DynamoDB** as a centralized, low-latency state store
- Support **method-level annotations** for fast adoption in Spring Boot
- Be **configurable via environment variables or YAML**
- Keep **cold starts fast** for AWS Lambda environments

---

## 📦 Modules

- **`cloud-circuitbreaker-core`** – Core logic and DynamoDB-backed circuit state tracking
- **`cloud-circuitbreaker-starter`** – Spring Boot autoconfiguration and annotations

---

## 🚀 Quick Start

Add the dependency in your Spring Boot project:

```xml
<!-- pom.xml -->
<dependency>
  <groupId>com.yourorg</groupId>
  <artifactId>cloud-circuitbreaker-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Enable circuit breaker on a service method:
```java
@CloudCircuitBreaker(name = "get-user-profile", fallback = "fallbackService")
public UserProfile fetchUserProfile(String userId) {
    // Your remote call logic here
}

public String fallbackService() {
        return "Fallback!";
}
```

## 🛠️ Configuration
```.properties
cloudcb.serviceName = order-service
cloudcb.tableName = circuit-breaker-states
cloudcb.tableType = dynamodb
cloudcb.region = us-east-1
cloudcb.failureThreshold = 2
cloudcb.resetTimeoutSeconds = 240
```