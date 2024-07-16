# Running the Application Locally

This guide will help you set up and run the application on your local machine.

## Prerequisites

- Java Development Kit (JDK) 21
- Maven
- MongoDB

## 1. Install Java and Maven

### Install Java

Download and install JDK 21 from either:

- [Oracle JDK](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html)
- [OpenJDK](https://jdk.java.net/21/)

Verify the installation by running the following commands in your terminal:

```bash
java -version
javac -version
```

You should see an output similar to:

```
java version "21.0.1"
Java(TM) SE Runtime Environment (build 21.0.1+12)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.1+12, mixed mode)
```

### Install Maven

Download and install Maven from the [Maven Download Page](https://maven.apache.org/download.cgi).

Follow the installation instructions on the [Maven Installation Guide](https://maven.apache.org/install.html).

Verify the installation by running the following command in your terminal:

```bash
mvn -version
```

You should see an output similar to:

```
Apache Maven 3.8.6 (b8d79a7c59fda6978d92783121db5dcac4f82f89)
Maven home: /path/to/maven
Java version: 21.0.1, vendor: Oracle Corporation, runtime: /path/to/jdk-21
Default locale: en_US, platform encoding: UTF-8
OS name: "your_os_name", version: "your_os_version", arch: "your_os_architecture", family: "your_os_family"
```

## 2. Install MongoDB

To install MongoDB, follow these simple steps:

1. Download the installer from the [MongoDB Download Center](https://www.mongodb.com/try/download/community).
2. Run the installer and follow the instructions, using the default options.
3. No need to create any users or databases manually; the application properties are pre-configured to handle
   everything.

## 3. Clone the Repository

Clone the repository to your local machine using the following command:

```bash
git clone <repository-url>
```

## 4. Build the Application

Navigate to the project directory and build the application using Maven:

```bash
cd <project-directory>
mvn clean install
```

## 5. Run the Application

### Using Maven

You can run the application using the Maven Spring Boot plugin:

```bash
mvn spring-boot:run
```

### Using IntelliJ IDEA

1. Open IntelliJ IDEA and select `File > Open`.
2. Navigate to the project directory and open it.
3. Once the project is loaded, locate the main application class (e.g., `com.springboot.FlomadAIplanner.Application`).
4. Right-click the main class and select `Run 'Application'`.

## 6. Access the Application

After the application starts, it will be accessible at [http://localhost:8080](http://localhost:8080) by default.

## Additional Notes

- Ensure that MongoDB is running before starting the application.
- The application properties file is pre-configured with the necessary MongoDB connection settings:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017
spring.data.mongodb.database=testdb
```

By following these steps, you should be able to run the application locally without any issues.
