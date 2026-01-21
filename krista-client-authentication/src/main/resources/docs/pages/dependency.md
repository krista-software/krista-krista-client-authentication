# Dependencies

## Required Dependencies

### Krista SDK
- **Version**: 3.5.7
- **Purpose**: Core Krista functionality and APIs

### Runtime Dependencies
- **Gson**: 2.10.1 - JSON serialization/deserialization
- **Log4j**: 2.20.0 - Logging framework

## Adding as Authentication Extension Dependency

To add this extension as a dependency in your extension, add the `@Dependency` annotation in your project's area class:

```java
@Dependency(
    name = "Authentication",
    domainId = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7",
    description = "Krista Client Authentication extension dependency"
)
```

## Prerequisites
- Krista workspace with admin rights
- Java 11 or higher
- Gradle 7.0 or higher

## Version
Current Version: **3.5.7**
