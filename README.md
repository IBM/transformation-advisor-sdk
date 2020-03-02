# Transformation Advisor SDK

This is a tool to help a developer to create and build a middleware data collection plug-in, which can be used to gather the information of an application deployed on a middleware server, and provide modernization help and recommendations.  See [Getting Started Guide](docs/GettingStarted.md)

### Modules

name| description
--- | ---
ta-sdk-spi | Interface of the plug-in framework
ta-sdk-core | Default implementation of SPI framework
ta-sdk-sample | Sample plug-in

### Dependency
Other middleware plug-in project will depend on the ta-sdk-core module.

Need to add this in your pom.xml file.
```
        <dependency>
            <groupId>com.ibm.ta.sdk</groupId>
            <artifactId>ta-sdk-core</artifactId>
            <version>0.5.2</version>
        </dependency>
```
ta-sdk-core module will depend on the ta-sdk-spi module.

### Build
This project uses Maven to build. Download and configure Maven before building this project.

On all platforms, build by running this command:
```bash
mvn clean install
```
Output archive files can be find in target/ directory.

### Run data collector
To run the data data collector:
```
cd target
java -jar ta-sdk-sample-0.5.2.jar help
```

The `help` command shows the usage for the data collector. The usage information for `middleware` shows the list
middleware based on which plug-ins are in the classpath. In the example below, only 1 middleware/plug-in, `sample`, 
is available:
```
Middleware:
  Plug-ins available for these middleware [ sample ]
```

Output artifacts are in `output` directory.

Logs are in `logs` directory.

The `<middleware> help` command shows the command usage for that middleware.
```
java -jar ta-sdk-sample-0.5.2.jar sample help
```

### Contributing to Transformation Advisor SDK
See [CONTRIBUTING.md](CONTRIBUTING.md).

### License
The Transformation Advisor SDK is licensed under the Apache 2.0 license. 

Full license text is available at [LICENSE](./LICENSE).
