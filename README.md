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
* Download Prosys Client & Server Binary from [Prosys OPC UA Java SDK] [4]
* Create OPC-UA stack jar by following steps mentioned [here] [5]
* Refer above two jars in `gateway` and `simulator` projects

  [1]: http://www.cumulocity.com
  [2]: http://maven.apache.org/
  [3]: https://www.cumulocity.com/guides
  [4]: https://www.prosysopc.com/products/opc-ua-java-sdk/
  [5]: https://github.com/OPCFoundation/UA-Java/blob/master/README.md

