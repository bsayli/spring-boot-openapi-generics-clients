package io.github.blueprintplatform.openapi.generics.gradle.generation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

class OpenApiGenericsTaskSelectionTest {

    @Test
    void resolvesSelectionAfterConsumerConfiguration() {
        GenerateTask task = createGenerateTask();

        OpenApiGenericsTaskSelection selection = OpenApiGenericsTaskSelection.from(task);

        task.getGeneratorName()
                .set(OpenApiGenericsTaskSelection.OPENAPI_GENERICS_GENERATOR_NAME);

        assertTrue(selection.isSelected().get());
        assertEquals(
                OpenApiGenericsTaskSelection.OPENAPI_GENERICS_GENERATOR_NAME,
                selection.getDisplayGeneratorName());
    }

    @Test
    void doesNotSelectAnotherGenerator() {
        GenerateTask task = createGenerateTask();
        task.getGeneratorName().set("kotlin");

        OpenApiGenericsTaskSelection selection = OpenApiGenericsTaskSelection.from(task);

        assertFalse(selection.isSelected().get());
        assertEquals("kotlin", selection.getDisplayGeneratorName());
    }

    @Test
    void doesNotSelectMissingGenerator() {
        OpenApiGenericsTaskSelection selection =
                OpenApiGenericsTaskSelection.from(createGenerateTask());

        assertFalse(selection.isSelected().get());
        assertEquals("<not configured>", selection.getDisplayGeneratorName());
    }

    @Test
    void doesNotSelectStockJavaGenerator() {
        GenerateTask task = createGenerateTask();
        task.getGeneratorName().set("java");

        assertFalse(OpenApiGenericsTaskSelection.from(task).isSelected().get());
    }

    private GenerateTask createGenerateTask() {
        Project project = ProjectBuilder.builder().build();

        return project.getTasks().register("generateClient", GenerateTask.class).get();
    }
}
