Cumulocity OPC-UA Agent for Java
---------------

This repository contains [Cumulocity] [1] OPC-UA agent for Java. For more information on [Cumulocity] [1] visit [http://www.cumulocity.com] [1].

Access to [Cumulocity] [1] [Maven repository] [2] is required to build the code.


Building with Maven
---------------

Please add [Cumulocity] [1] [Maven repository] [2] to your `settings.xml` like this:

    <settings>
      <activeProfiles>
        <activeProfile>cumulocity</activeProfile>
      </activeProfiles>

      <profiles>
        <profile>
          <id>cumulocity</id>
          <repositories>
            <repository>
              <id>cumulocity-maven-repo</id>
              <url>http://resources.cumulocity.com/maven/repository</url>
            </repository>
          </repositories>
          <pluginRepositories>
            <pluginRepository>
              <id>cumulocity-plugins-repo</id>
              <url>http://resources.cumulocity.com/maven/repository</url>
            </pluginRepository>
          </pluginRepositories>
        </profile>
      </profiles>
    </settings>

External Dependencies
---------------
Download Prosys-OPC-UA-Java-SDK-Client-Server jar of version 2.2.4 from [Prosys OPC UA Java SDK] [4] (jar name should be similar to `Prosys-OPC-UA-Java-SDK-Client-Server-Binary-2.2.4-674.jar`).

Download OPC-UA stack jar of version 1.02.337 from [OPC Foundation UA-Java Releases] [5] (jar name should be similar to `Opc.Ua.Stack-1.02.337.10.jar`).

Copy two jars to resources folder of your `gateway` and `simulator` modules (folder path `./opcua-agent/gateway/src/main/resources/lib/` and `./opcua-agent/simulator/src/main/resources/lib/`).

Add below given dependencies to your `gateway` and `simulator` `pom.xml` like this:
    
    <dependency>
        <groupId>com.prosys.ua</groupId>
        <artifactId>Prosys-OPC-UA-Java-SDK-Client-Server-Evaluation</artifactId>
        <version>--version--</version>
        <scope>system</scope>
        <systemPath>${basedir}/src/main/resources/lib/Prosys-OPC-UA-Java-SDK-Client-Server-Binary-<version>.jar
        </systemPath>
    </dependency>

    <dependency>
        <groupId>org.opcfoundation.ua</groupId>
        <artifactId>Opc.Ua.Stack</artifactId>
        <version>--version--</version>
        <scope>system</scope>
        <systemPath>${basedir}/src/main/resources/lib/Opc.Ua.Stack-<version>.jar</systemPath>
    </dependency>
         

  [1]: http://www.cumulocity.com
  [2]: http://maven.apache.org/
  [3]: https://www.cumulocity.com/guides
  [4]: https://www.prosysopc.com/products/opc-ua-java-sdk/
  [5]: https://github.com/OPCFoundation/UA-Java/releases/

------------------------------

These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.
