package io.github.blueprintplatform.openapi.generics.gradle.support;

/**
 * Consumer build scripts that look like real adopter builds.
 *
 * <p>Inspection happens in dedicated execution-time tasks. The scripts do not realize
 * {@code GenerateTask} providers during configuration.
 */
public final class ConsumerBuildFiles {

    public static final String OFFICIAL_GENERATE_TASK = "openApiGenerate";

    public static final String GENERICS_GENERATE_TASK = "generateGenericsClient";

    public static final String KOTLIN_GENERATE_TASK = "generateKotlinClient";

    public static final String CUSTOMER_GENERATE_TASK = "generateCustomerClient";

    public static final String SERVICE_RESPONSE_GENERATE_TASK =
            "generateServiceResponseClient";

    public static final String PRINT_EXTRA_DEPENDENCIES =
            "printOpenApiGeneratorExtraDependencies";

    public static final String PRINT_TEMPLATE_DIRS = "printTemplateDirs";

    public static final String PRINT_TEMPLATE_DEPENDENCIES = "printTemplateDependencies";

    public static final String PRINT_GENERATOR_CLASSPATHS = "printGeneratorClasspaths";

    public static final String PRINT_MAIN_JAVA_SRC_DIRS = "printMainJavaSrcDirs";

    public static final String PRINT_COMPILE_JAVA_DEPS = "printCompileJavaTaskDeps";

    public static final String PRINT_REGISTERED_TASKS = "printRegisteredTasks";

    private ConsumerBuildFiles() {}

    public static String officialJavaConsumer(
            String openApiGeneratorVersion,
            String pluginVersion,
            String fixtureRepositoryUri) {

        return plugins(openApiGeneratorVersion, pluginVersion, true)
                + repositories(fixtureRepositoryUri)
                + officialGenerateConfiguration()
                + inspectionTasks(true);
    }

    public static String mixedGeneratorConsumer(
            String openApiGeneratorVersion,
            String pluginVersion,
            String fixtureRepositoryUri) {

        return plugins(openApiGeneratorVersion, pluginVersion, true)
                + repositories(fixtureRepositoryUri)
                + mixedGenerateTasks()
                + inspectionTasks(true);
    }

    public static String multipleGenericsTasksConsumer(
            String openApiGeneratorVersion,
            String pluginVersion,
            String fixtureRepositoryUri) {

        return plugins(openApiGeneratorVersion, pluginVersion, true)
                + repositories(fixtureRepositoryUri)
                + multipleGenericsTasks()
                + inspectionTasks(true);
    }

    public static String genericsPluginWithoutOpenApiGenerator(String pluginVersion) {

        return """
                plugins {
                    java
                    id("io.github.blueprint-platform.openapi-generics") version "%s"
                }

                repositories {
                    mavenCentral()
                }

                tasks.register("%s") {
                    doLast {
                        println("PREPARE_TASK_PRESENT=" + tasks.findByName("prepareOpenApiGenericsTemplates"))
                    }
                }
                """
                .formatted(pluginVersion, PRINT_REGISTERED_TASKS);
    }

    private static String plugins(
            String openApiGeneratorVersion,
            String pluginVersion,
            boolean includeJava) {

        return """
                import org.gradle.api.plugins.JavaPluginExtension
                import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

                plugins {
                    %s
                    id("org.openapi.generator") version "%s"
                    id("io.github.blueprint-platform.openapi-generics") version "%s"
                }

                """
                .formatted(
                        includeJava ? "java" : "",
                        openApiGeneratorVersion,
                        pluginVersion);
    }

    private static String repositories(String fixtureRepositoryUri) {
        return """
                repositories {
                    maven {
                        url = uri("%s")
                    }
                    mavenCentral()
                }

                """
                .formatted(fixtureRepositoryUri);
    }

    private static String officialGenerateConfiguration() {
        return """
                tasks.named<GenerateTask>("openApiGenerate") {
                    generatorName.set("java-generics-contract")
                    outputDir.set(layout.buildDirectory.dir("generated/openapi"))
                    configOptions.set(mapOf("sourceFolder" to "src/gen/java"))
                }

                """;
    }

    private static String mixedGenerateTasks() {
        return """
                tasks.register<GenerateTask>("generateGenericsClient") {
                    generatorName.set("java-generics-contract")
                    outputDir.set(layout.buildDirectory.dir("generated/generics"))
                    configOptions.set(mapOf("sourceFolder" to "src/gen/java"))
                }

                tasks.register<GenerateTask>("generateKotlinClient") {
                    generatorName.set("kotlin")
                    outputDir.set(layout.buildDirectory.dir("generated/kotlin"))
                    configOptions.set(emptyMap())
                }

                """;
    }

    private static String multipleGenericsTasks() {
        return """
                tasks.register<GenerateTask>("generateCustomerClient") {
                    generatorName.set("java-generics-contract")
                    outputDir.set(layout.buildDirectory.dir("generated/customer"))
                    configOptions.set(mapOf("sourceFolder" to "src/gen/java"))
                }

                tasks.register<GenerateTask>("generateServiceResponseClient") {
                    generatorName.set("java-generics-contract")
                    outputDir.set(layout.buildDirectory.dir("generated/service-response"))
                    configOptions.set(mapOf("sourceFolder" to "src/gen/java"))
                }

                """;
    }

    private static String inspectionTasks(boolean includeJavaInspections) {
        String javaInspections =
                includeJavaInspections
                        ? """
                                tasks.register("%s") {
                                    doLast {
                                        val javaExtension =
                                            project.extensions.getByType(JavaPluginExtension::class.java)
                                        javaExtension.sourceSets
                                            .getByName("main")
                                            .java
                                            .srcDirs
                                            .forEach { directory ->
                                                println(
                                                    "MAIN_JAVA_SRC="
                                                        + directory.absolutePath.replace('\\\\', '/')
                                                )
                                            }
                                    }
                                }

                                tasks.register("%s") {
                                    doLast {
                                        val compileJava = tasks.getByName("compileJava")
                                        compileJava.taskDependencies
                                            .getDependencies(compileJava)
                                            .forEach { task ->
                                                println("COMPILE_JAVA_DEP=" + task.name)
                                            }
                                    }
                                }

                                """
                        .formatted(
                                PRINT_MAIN_JAVA_SRC_DIRS,
                                PRINT_COMPILE_JAVA_DEPS)
                        : "";

        return javaInspections
                + """
                        tasks.register("%s") {
                            doLast {
                                configurations
                                    .getByName("openApiGeneratorExtra")
                                    .dependencies
                                    .forEach { dependency ->
                                        println(
                                            "OPENAPI_GENERATOR_EXTRA_DEPENDENCY="
                                                + "${dependency.group}:"
                                                + "${dependency.name}:"
                                                + "${dependency.version}"
                                        )
                                    }
                            }
                        }

                        tasks.register("%s") {
                            doLast {
                                val generateTasks =
                                    tasks.withType<GenerateTask>().toList()

                                generateTasks.forEach { task ->
                                    val templateDirectory =
                                        if (task.templateDir.isPresent) {
                                            task.templateDir
                                                .get()
                                                .asFile
                                                .absolutePath
                                                .replace('\\\\', '/')
                                        } else {
                                            "<absent>"
                                        }

                                    println(
                                        "TEMPLATE_DIR_"
                                            + task.name
                                            + "="
                                            + templateDirectory
                                    )

                                    println(
                                        "GENERATOR_NAME_"
                                            + task.name
                                            + "="
                                            + task.generatorName.orNull
                                    )
                                }
                            }
                        }

                        tasks.register("%s") {
                            doLast {
                                val generateTasks =
                                    tasks.withType<GenerateTask>().toList()

                                generateTasks.forEach { task ->
                                    val dependsOnPrepare =
                                        task.taskDependencies
                                            .getDependencies(task)
                                            .any {
                                                it.name == "prepareOpenApiGenericsTemplates"
                                            }

                                    println(
                                        "TEMPLATE_DEPENDENCY_"
                                            + task.name
                                            + "="
                                            + dependsOnPrepare
                                    )
                                }
                            }
                        }

                        tasks.register("%s") {
                            doLast {
                                val generateTasks =
                                    tasks.withType<GenerateTask>().toList()

                                generateTasks.forEach { task ->
                                    task.generatorClasspath.files
                                        .sortedBy { file -> file.absolutePath }
                                        .forEach { file ->
                                            println(
                                                "GENERATOR_CLASSPATH_"
                                                    + task.name
                                                    + "="
                                                    + file.absolutePath.replace('\\\\', '/')
                                            )
                                        }
                                }
                            }
                        }
                        """
                .formatted(
                        PRINT_EXTRA_DEPENDENCIES,
                        PRINT_TEMPLATE_DIRS,
                        PRINT_TEMPLATE_DEPENDENCIES,
                        PRINT_GENERATOR_CLASSPATHS);
    }
}
