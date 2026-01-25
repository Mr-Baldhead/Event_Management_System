# Multi-stage build for Jakarta EE backend

# Stage 1: Build the WAR file
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build WAR file (skip tests)
RUN mvn clean package -DskipTests -Dmaven.test.skip=true


# Stage 2: Deploy to WildFly
FROM quay.io/wildfly/wildfly:34.0.1.Final-jdk21

# Environment variables for database connection
ENV DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=eventmanager \
    DB_USER=eventuser \
    DB_PASSWORD=eventpassword

# Switch to root to install MySQL driver
USER root

# Create directory for MySQL driver
RUN mkdir -p /opt/jboss/wildfly/modules/system/layers/base/com/mysql/main

# Download MySQL JDBC driver
ADD --chown=jboss:jboss https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar \
    /opt/jboss/wildfly/modules/system/layers/base/com/mysql/main/mysql-connector-j-8.3.0.jar

# Copy MySQL module configuration
COPY --chown=jboss:jboss docker/module.xml /opt/jboss/wildfly/modules/system/layers/base/com/mysql/main/module.xml

# Copy CLI script to configure datasource
COPY --chown=jboss:jboss docker/configure-datasource.cli /opt/jboss/wildfly/configure-datasource.cli

# Switch back to jboss user
USER jboss

# Run CLI script to configure WildFly
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/configure-datasource.cli

# Create admin user for management console
RUN /opt/jboss/wildfly/bin/add-user.sh admin admin123 --silent

# Copy the WAR file from builder stage
COPY --from=builder /app/target/*.war /opt/jboss/wildfly/standalone/deployments/event-management.war

# Expose ports
EXPOSE 8080 9990

# Start WildFly in standalone mode with full profile and binding to all interfaces
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]