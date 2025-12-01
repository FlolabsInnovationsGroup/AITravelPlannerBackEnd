# Latency/Unit/End-to-End Testing

[![FloTravel - Latency Testing](https://github.com/FlomadLabsRD/AITravelPlannerBackEnd/actions/workflows/latency-testing.yml/badge.svg)](https://github.com/FlomadLabsRD/AITravelPlannerBackEnd/actions/workflows/latency-testing.yml)

Latency Test Results

Server Software:        Vercel

Server Hostname:        www.flomadtravel.com

Server Port:            443

SSL/TLS Protocol:       TLSv1.3,TLS_AES_128_GCM_SHA256,2048,128

Server Temp Key:        X25519 253 bits

TLS Server Name:        www.flomadtravel.com

Document Path:          /auth/signin

Document Length:        10078 bytes

Concurrency Level:      10

Time taken for tests:   1.905 seconds

Complete requests:      200

Failed requests:        0

Total transferred:      2137000 bytes

HTML transferred:       2015600 bytes

Requests per second:    105.01 [#/sec] (mean)

Time per request:       95.228 [ms] (mean)

Time per request:       9.523 [ms] (mean, across all concurrent requests)

Transfer rate:          1095.74 [Kbytes/sec] received

Connection Times (ms)

              min  mean[+/-sd] median   max
              
Connect:       15   20   3.9     18      36

Processing:    54   68  15.7     64     165

Waiting:       54   68  15.5     64     165

Total:         71   88  17.1     83     187

Percentage of the requests served within a certain time (ms)

  50%     83
  
  66%     86
  
  75%     89
  
  80%     92
  
  90%    100
  
  95%    121
  
  98%    164
  
  99%    184
  
 100%    187 (longest request)


# Running the Application Locally

This guide will help you set up and run the AI Travel Planner application on your local machine.

## Prerequisites

- Java Development Kit (JDK) 17
- Maven
- MongoDB

## 1. Install Java and Maven

### Install Java

Download and install JDK 17 from either:

- [Oracle JDK](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html)
- [OpenJDK](https://jdk.java.net/21/)

Verify the installation by running the following commands in your terminal:

```bash
java -version
javac -version
```

You should see an output similar to:

```
java 17.0.12 2024-07-16 LTS
Java(TM) SE Runtime Environment (build 17.0.12+8-LTS-286)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.12+8-LTS-286, mixed mode, sharing)
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
Java version: 17.0.12, vendor: Oracle Corporation, runtime: /path/to/jdk-17
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
----------------------------------------------------------------------------------------------------------------------------
# SPRING AI DEVELOPMENT STEPS

Spring AI development in Java leverages the robust Spring framework to build scalable and maintainable applications. Here’s a comprehensive guide to get you started with integrating AI capabilities into a Spring-based Java application.

# 1. Task Understanding

Developing AI functionalities in a Spring application involves:

    * Setting up the Spring Boot project
    * Integrating AI libraries (e.g., TensorFlow, DL4J)
    * Building RESTful APIs to expose AI functionalities
    * Handling data processing and model inference

# 2. Clarification Requests

Do you have a specific AI model or task in mind (e.g., image recognition, NLP)?
Are you using a pre-trained model or training a model from scratch?
What AI library do you prefer (TensorFlow, Deeplearning4j, etc.)?

# 3. Approach Outline

High-Level Steps:

1. Project Setup
    * Initialize a Spring Boot project.
2. Dependency Management
    * Add necessary dependencies for Spring Boot, and AI libraries.
3. Service Layer
    * Implement AI services to handle model loading and inference.
4. Controller Layer
    * Create RESTful endpoints to interact with the AI service.
5. Data Processing
    * Implement data processing utilities.
6. Model Integration
    * Integrate the AI model and handle inference requests.

# Flomad MasterClass
