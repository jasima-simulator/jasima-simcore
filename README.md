<p align="center">
    <img src="images/logo.png" alt="Jasima Logo" width="300">
</p>
<hr>

# jasima - JAva SImulator for MAnufacturing and logistics

`jasima-simcore` is the core simulation engine for the jasima project (Java Simulator for Manufacturing and Logistics), a framework for developing and analyzing discrete-event simulations, particularly in the context of manufacturing and logistics. It provides the essential building blocks for creating custom simulation models and computer experiments in Java as free and Open Source software (Apache 2.0 license).

## Prerequisites

* Java Development Kit (JDK) 8 or higher – jasima requires Java 8 or later to compile and run. Ensure you have a compatible JDK installed.
* A build tool such as Maven or Gradle – This is optional but recommended for managing the library as a dependency or building from source. Jasima SimCore is distributed via Maven Central, which makes it easy to include in projects using Maven or Gradle. Maven wrapper is included in this project, so a JDK/JRE is the only real prerequisite.

## Installation

### Try Online / Project Template

Examples from the [project template](https://github.com/jasima-simulator/project_template_standalone) can be executed online via [JDoodle](https://www.jdoodle.com/ga/HxrDxfvFpN%2BkbJTqEYUZHg%3D%3D). Fork the project on JDoodle and give it a try!

[project template](https://github.com/jasima-simulator/project_template_standalone) is a standalone project with all required dependencies to be used without a build tool (not recommended for serious use).

### Use as a Dependency

Jasima SimCore is published to Maven Central under the coordinates `net.jasima:jasima-main:<version>`. Replace `<version>` with the latest release (e.g., `3.0.0-RC3`).

#### Maven

To use `jasima-simcore` in your Java project, add the following dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>net.jasima</groupId>
    <artifactId>jasima-main</artifactId>
    <version>3.0.0-RC3</version>
</dependency>
```

#### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'net.jasima:jasima-main:3.0.0-RC3'
}
```

### Build from Source

If you want to build or modify Jasima SimCore:

1. Clone the repository:

    ```sh
    git clone https://github.com/jasima-simulator/jasima-simcore.git
    ```

1. Build using Maven (via the Maven Wrapper):

    ```sh
    cd jasima-simcore
    ./mvnw clean package
    ```

1. (Optional) Install locally:

    ```sh
    ./mvnw install
    ```

1. (Optional) Import into your IDE as a Maven project.

## Examples and Documentation

For more in-depth guides and examples, please refer to the official jasima documentation site: [https://jasima-simulator.github.io/](https://jasima-simulator.github.io/). The documentation provides a comprehensive Getting Started tutorial, explanations of key concepts (such as event-oriented vs. process-oriented simulation), and example models (like an M/M/1 queue and job shop simulations). It also includes a detailed API reference (Javadoc) for all jasima classes and methods.

If you're new to jasima, the documentation's Getting Started section is a great next step after this README. It walks through setting up a project and running more elaborate examples. You’ll also find information on how to create custom experiments, generate random variates for input modeling, and analyze output data.

## Contributing

We welcome contributions! If you find a bug or want to suggest an improvement:

1) Open an Issue: Describe what you found or what you propose.
2) Fork and Submit a Pull Request: Make your changes on a separate branch and open a PR. Please run tests before submitting (./mvnw test).
   
## Contact

If you have questions, need help, or want to report a bug, please use our 
[issue tracker](https://github.com/YourOrganization/YourRepo/issues). We also welcome
feature requests and suggestions there.

For inquiries you’d prefer not to post publicly, you can send an email to:
`jasima [at] thildebrandt [dot] de`
