import org.gradle.api.GradleException
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import java.io.File

plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.blueprint-platform"
version = "1.3.0-SNAPSHOT"

val openApiGeneratorVersion = "7.25.0"

val repositoryRoot = rootProject.projectDir.parentFile
val repositoryPom = repositoryRoot.resolve("pom.xml")
val codegenModule = repositoryRoot.resolve("openapi-generics-java-codegen")

val functionalTestPluginRepository =
    layout.buildDirectory.dir("functional-test-plugin-repository")

val functionalTestCodegenRepository =
    layout.buildDirectory.dir("functional-test-codegen-repository")

repositories {
    gradlePluginPortal()
    mavenCentral()
}

/*
 * Verification layers:
 *
 * test           -> plugin-owned unit contracts
 *                  (template patch, template IO, selection, source layout,
 *                  official extra-configuration compatibility)
 * functionalTest -> TestKit consumer contracts against a fake codegen jar
 *                  (apply, classpath, selection, source registration,
 *                  template preparation, configuration cache)
 * e2eTest        -> real java-generics-contract generation and compilation
 *
 * check runs test + functionalTest.
 * e2eTest stays explicit: it needs the Maven reactor codegen artifact.
 */
val functionalTest: SourceSet = sourceSets.create("functionalTest")
val e2eTest: SourceSet = sourceSets.create("e2eTest")

/*
 * Production compiles against the public OpenAPI Generator Gradle plugin API,
 * but does not publish or embed the official plugin implementation.
 */
val openApiGeneratorGradlePlugin =
    "org.openapi.generator:org.openapi.generator.gradle.plugin:$openApiGeneratorVersion"

configurations.named(sourceSets.test.get().implementationConfigurationName) {
    extendsFrom(configurations.compileOnly.get())
}

configurations.named(functionalTest.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(functionalTest.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

configurations.named(e2eTest.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(e2eTest.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    /*
     * Declared once. testImplementation inherits compileOnly so component tests
     * can instantiate GenerateTask without repeating the same coordinate.
     */
    compileOnly(openApiGeneratorGradlePlugin)

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    /*
     * java-gradle-plugin already contributes Gradle TestKit to testImplementation.
     * The custom source sets inherit testImplementation above.
     */
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Implementation-Title" to "OpenAPI Generics Gradle Plugin",
            "Implementation-Version" to project.version.toString()
        )
    }
}

gradlePlugin {
    plugins {
        create("openApiGenerics") {
            id = "io.github.blueprint-platform.openapi-generics"
            implementationClass =
                "io.github.blueprintplatform.openapi.generics.gradle.OpenApiGenericsGradlePlugin"
            displayName = "OpenAPI Generics Gradle Plugin"
            description =
                "Gradle integration for contract-aware Java client generation with OpenAPI Generics."
        }
    }
}

/*
 * TestKit consumer projects resolve the plugin marker and implementation
 * artifact from this isolated Maven repository.
 */
publishing {
    repositories {
        maven {
            name = "functionalTest"
            url = functionalTestPluginRepository.get().asFile.toURI()
        }
    }
}

val publishFunctionalTestPlugin =
    tasks.named("publishAllPublicationsToFunctionalTestRepository")

fun resolveMavenExecutable(): String {
    val wrapperName =
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            "mvnw.cmd"
        } else {
            "mvnw"
        }

    val repositoryWrapper =
        repositoryRoot.resolve(wrapperName)

    if (repositoryWrapper.isFile) {
        return repositoryWrapper.absolutePath
    }

    val configuredExecutable =
        providers.gradleProperty("e2eMavenExecutable").orNull
            ?.takeIf(String::isNotBlank)
            ?: System.getenv("MAVEN_EXECUTABLE")
                ?.takeIf(String::isNotBlank)

    if (configuredExecutable != null) {
        val executable = file(configuredExecutable)

        if (!executable.isFile) {
            throw GradleException(
                "Configured Maven executable does not exist: " +
                    "'${executable.absolutePath}'."
            )
        }

        return executable.absolutePath
    }

    val executableName =
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            "mvn.cmd"
        } else {
            "mvn"
        }

    val pathEntries =
        System.getenv("PATH")
            .orEmpty()
            .split(File.pathSeparator)
            .filter(String::isNotBlank)

    val pathExecutable =
        pathEntries
            .asSequence()
            .map { File(it, executableName) }
            .firstOrNull(File::isFile)

    if (pathExecutable != null) {
        return pathExecutable.absolutePath
    }

    throw GradleException(
        "Maven executable could not be resolved for the real-codegen E2E setup. " +
            "Add a Maven wrapper to '${repositoryRoot.absolutePath}', expose Maven " +
            "through PATH, set MAVEN_EXECUTABLE, or pass " +
            "-Pe2eMavenExecutable=<absolute Maven executable path>."
    )
}

/*
 * Repository-level prerequisite for the real-codegen E2E.
 *
 * This task is deliberately separate from test and functionalTest. It runs only
 * when e2eTest is requested, builds the actual codegen module through the Maven
 * reactor, and installs the result into a Gradle-owned isolated repository.
 */
val prepareRealCodegenRepository =
    tasks.register<Exec>("prepareRealCodegenRepository") {
        group = "verification"
        description =
            "Builds the real OpenAPI Generics codegen artifact for e2eTest."

        inputs.file(repositoryPom)
        inputs.file(codegenModule.resolve("pom.xml"))
        inputs.dir(codegenModule.resolve("src"))

        outputs.dir(functionalTestCodegenRepository)

        workingDir(repositoryRoot)

        doFirst {
            if (!repositoryPom.isFile) {
                throw GradleException(
                    "OpenAPI Generics repository root could not be located at " +
                        "'${repositoryRoot.absolutePath}'."
                )
            }

            val codegenPom =
                codegenModule.resolve("pom.xml")

            if (!codegenPom.isFile) {
                throw GradleException(
                    "OpenAPI Generics codegen module could not be located at " +
                        "'${codegenModule.absolutePath}'."
                )
            }

            val isolatedRepository =
                functionalTestCodegenRepository.get().asFile

            isolatedRepository.mkdirs()

            commandLine(
                resolveMavenExecutable(),
                "--batch-mode",
                "--no-transfer-progress",
                "-pl",
                "openapi-generics-java-codegen",
                "-am",
                "install",
                "-DskipTests",
                "-Dmaven.repo.local=${isolatedRepository.absolutePath}"
            )
        }
    }

val functionalTestTask =
    tasks.register<Test>("functionalTest") {
        group = "verification"
        description = "Runs isolated Gradle TestKit functional tests."

        testClassesDirs = functionalTest.output.classesDirs
        classpath = functionalTest.runtimeClasspath

        useJUnitPlatform()
        shouldRunAfter(tasks.named("test"))
        dependsOn(publishFunctionalTestPlugin)

        systemProperty(
            "functionalTestPluginRepository",
            functionalTestPluginRepository.get().asFile.absolutePath
        )
        systemProperty(
            "pluginUnderTestVersion",
            project.version.toString()
        )

        testLogging {
            showStandardStreams = true
        }
    }

val e2eTestTask =
    tasks.register<Test>("e2eTest") {
        group = "verification"
        description =
            "Runs real OpenAPI Generics code generation and compilation."

        testClassesDirs = e2eTest.output.classesDirs
        classpath = e2eTest.runtimeClasspath

        useJUnitPlatform()
        shouldRunAfter(functionalTestTask)

        dependsOn(publishFunctionalTestPlugin)
        dependsOn(prepareRealCodegenRepository)

        systemProperty(
            "functionalTestPluginRepository",
            functionalTestPluginRepository.get().asFile.absolutePath
        )
        systemProperty(
            "functionalTestCodegenRepository",
            functionalTestCodegenRepository.get().asFile.absolutePath
        )
        systemProperty(
            "pluginUnderTestVersion",
            project.version.toString()
        )

        testLogging {
            showStandardStreams = true
        }
    }

tasks.named("check") {
    dependsOn(functionalTestTask)
}