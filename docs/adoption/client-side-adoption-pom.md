---

layout: default
title: Client-Side Build Setup (Maven Plugins & Dependencies)
parent: Client-Side Adoption
nav_order: 1
------------

# Client-Side Build Setup — Maven Plugins & Dependencies

This guide ensures your client module is correctly configured to generate **type‑safe, generics‑aware OpenAPI clients** using custom Mustache templates. It aligns your build pipeline with modern OpenAPI Generator practices.

---

## ⚙️ 1. Core Dependencies

Add these dependencies to your client module:

```xml
<dependencies>
    <!-- Spring Boot (provided by host application) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <scope>provided</scope>
    </dependency>

    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
        <scope>provided</scope>
    </dependency>

    <dependency>
        <groupId>jakarta.annotation</groupId>
        <artifactId>jakarta.annotation-api</artifactId>
        <scope>provided</scope>
    </dependency>

    <!-- HTTP client (used by generated code) -->
    <dependency>
        <groupId>org.apache.httpcomponents.client5</groupId>
        <artifactId>httpclient5</artifactId>
        <version>5.5</version>
    </dependency>

    <!-- Test dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>mockwebserver</artifactId>
        <version>5.1.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 🧩 2. Maven Properties

Define reusable properties to simplify plugin management and template resolution.

```xml
<properties>
    <openapi.generator.version>7.16.0</openapi.generator.version>
    <openapi.templates.upstream>${project.build.directory}/openapi-templates-upstream</openapi.templates.upstream>
    <openapi.templates.effective>${project.build.directory}/openapi-templates-effective</openapi.templates.effective>
</properties>
```

---

## 🏗️ 3. Maven Plugins — Full Build Pipeline

These plugins work in sequence to **unpack, overlay, and compile** OpenAPI templates.

```xml
<build>
    <plugins>
        <!-- 1️⃣ Unpack upstream OpenAPI templates -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <executions>
                <execution>
                    <id>unpack-openapi-upstream-templates</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>unpack</goal>
                    </goals>
                    <configuration>
                        <artifactItems>
                            <artifactItem>
                                <groupId>org.openapitools</groupId>
                                <artifactId>openapi-generator</artifactId>
                                <version>${openapi.generator.version}</version>
                                <type>jar</type>
                                <includes>templates/Java/**</includes>
                                <outputDirectory>${openapi.templates.upstream}</outputDirectory>
                            </artifactItem>
                        </artifactItems>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- 2️⃣ Overlay local Mustache templates -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-resources-plugin</artifactId>
            <executions>
                <execution>
                    <id>copy-upstream-to-effective</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>copy-resources</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>${openapi.templates.effective}</outputDirectory>
                        <resources>
                            <resource>
                                <directory>${openapi.templates.upstream}/templates</directory>
                                <includes>
                                    <include>Java/**</include>
                                </includes>
                            </resource>
                        </resources>
                    </configuration>
                </execution>
                <execution>
                    <id>overlay-local-templates</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>copy-resources</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>${openapi.templates.effective}/Java</outputDirectory>
                        <overwrite>true</overwrite>
                        <resources>
                            <resource>
                                <directory>src/main/resources/openapi-templates</directory>
                                <includes>
                                    <include>**/*.mustache</include>
                                </includes>
                            </resource>
                        </resources>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- 3️⃣ Generate OpenAPI client code -->
        <plugin>
            <groupId>org.openapitools</groupId>
            <artifactId>openapi-generator-maven-plugin</artifactId>
            <version>${openapi.generator.version}</version>
            <executions>
                <execution>
                    <id>generate-client</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yaml</inputSpec>
                        <generatorName>java</generatorName>
                        <library>restclient</library>
                        <output>${project.build.directory}/generated-sources/openapi</output>

                        <apiPackage>your.base.openapi.client.generated.api</apiPackage>
                        <modelPackage>your.base.openapi.client.generated.dto</modelPackage>
                        <invokerPackage>your.base.openapi.client.generated.invoker</invokerPackage>

                        <templateDirectory>${openapi.templates.effective}/Java</templateDirectory>
                        <generateSupportingFiles>true</generateSupportingFiles>
                        <generateApiTests>false</generateApiTests>
                        <generateModelTests>false</generateModelTests>

                        <configOptions>
                            <useSpringBoot3>true</useSpringBoot3>
                            <useJakartaEe>true</useJakartaEe>
                            <serializationLibrary>jackson</serializationLibrary>
                            <dateLibrary>java8</dateLibrary>
                            <useBeanValidation>true</useBeanValidation>
                            <openApiNullable>false</openApiNullable>
                            <sourceFolder>src/gen/java</sourceFolder>
                        </configOptions>

                        <additionalProperties>
                            <commonPackage>commonPackage=your.base.openapi.client.common</commonPackage>
                        </additionalProperties>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- 4️⃣ Add generated sources to compilation -->
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>build-helper-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>add-generated-sources</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>add-source</goal>
                    </goals>
                    <configuration>
                        <sources>
                            <source>${project.build.directory}/generated-sources/openapi/src/gen/java</source>
                        </sources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 🧠 4. Why These Plugins Matter

| Plugin                             | Purpose                                                          |
| ---------------------------------- | ---------------------------------------------------------------- |
| **maven-dependency-plugin**        | Unpacks built-in OpenAPI templates from the generator JAR.       |
| **maven-resources-plugin**         | Overlays your local Mustache templates on top of upstream ones.  |
| **openapi-generator-maven-plugin** | Generates type-safe client code using the effective templates.   |
| **build-helper-maven-plugin**      | Ensures generated sources are included in the compilation phase. |

Together, these guarantee your **generics‑aware response wrappers** (e.g., `ServiceClientResponse<T>`) are generated cleanly and consistently across builds.

---

✅ With this setup, your client build will always:

* Resolve templates dynamically from the current OpenAPI Generator version.
* Apply your overlay Mustache templates automatically.
* Generate **RFC 7807‑aware**, `data + meta` aligned clients ready for production use.
