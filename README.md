### Description 

A simple plugin for generating an xml schema from the compiled classes.

### Usage
<plugin>
    <groupId>com.idvp.plugins</groupId>
    <artifactId>xsd-maven-plugin</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <phase>process-classes</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <sources>
            <source>
                <basePackage>com.example.schema</basePackage>
            </source>
        </sources>
        <outputPath>${basedir}/target/generated-resources/schema</outputPath>
        <schemas>
            <schema>
                <namespace>http://example.com/schema/v1</namespace>
                <fileName>schema-v1.0.xsd</fileName>
            </schema>
        </schemas>
    </configuration>
</plugin>
