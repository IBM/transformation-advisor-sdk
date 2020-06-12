<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.ibm.ta</groupId>
  <artifactId>[=metadata_assessmentUnit_json.assessmentUnitName]</artifactId>
  <version>1.0.0</version>
  <packaging>war</packaging>

  <name>[=metadata_assessmentUnit_json.assessmentUnitName]</name>

  <parent><!--required parent POM-->
      <groupId>dev.appsody</groupId>
      <artifactId>java-microprofile</artifactId>
      <version>[0.2, 0.3)</version>
      <relativePath/>
  </parent>

  <dependencies>
      <!-- Open Liberty Features -->
      <dependency>
          <groupId>io.openliberty.features</groupId>
          <artifactId>microProfile-3.0</artifactId>
          <type>esa</type>
      </dependency>
      <!-- For tests -->
      <dependency>
          <groupId>junit</groupId>
          <artifactId>junit</artifactId>
          <version>4.12</version>
          <scope>test</scope>
      </dependency>
      <dependency>
          <groupId>org.apache.cxf</groupId>
          <artifactId>cxf-rt-rs-client</artifactId>
          <version>3.2.6</version>
          <scope>test</scope>
      </dependency>
      <dependency>
          <groupId>org.apache.cxf</groupId>
          <artifactId>cxf-rt-rs-extension-providers</artifactId>
          <version>3.2.6</version>
          <scope>test</scope>
      </dependency>
      <dependency>
          <groupId>org.glassfish</groupId>
          <artifactId>javax.json</artifactId>
          <version>1.0.4</version>
          <scope>test</scope>
      </dependency>
      <!-- Support for JDK 9 and above -->
      <dependency>
          <groupId>javax.xml.bind</groupId>
          <artifactId>jaxb-api</artifactId>
          <version>2.3.1</version>
	  <scope>test</scope>
      </dependency>
      <dependency>
          <groupId>com.sun.xml.bind</groupId>
          <artifactId>jaxb-core</artifactId>
          <version>2.3.0.1</version>
	  <scope>test</scope>
      </dependency>
      <dependency>
          <groupId>com.sun.xml.bind</groupId>
          <artifactId>jaxb-impl</artifactId>
          <version>2.3.2</version>
	  <scope>test</scope>
      </dependency>
      <dependency>
          <groupId>javax.activation</groupId>
          <artifactId>activation</artifactId>
          <version>1.1.1</version>
	  <scope>test</scope>
      </dependency>
  </dependencies>
  <build>
      <finalName>${project.artifactId}</finalName>
  </build>
</project>