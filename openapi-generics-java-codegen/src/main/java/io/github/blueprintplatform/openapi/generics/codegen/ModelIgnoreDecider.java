package io.github.blueprintplatform.openapi.generics.codegen;

import io.swagger.v3.oas.models.media.Schema;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decides whether a model should be excluded from generation.
 *
 * <p>A model is ignored if:
 *
 * <ul>
 *   <li>{@code x-ignore-model=true} is present in schema extensions
 *   <li>it is registered as an external model
 * </ul>
 *
 * <p>Also keeps track of ignored models for later filtering steps.
 */
public class ModelIgnoreDecider {

  private static final Logger log = LoggerFactory.getLogger(ModelIgnoreDecider.class);

  private static final String EXT_IGNORE_MODEL = "x-ignore-model";

  private final Set<String> ignored = new HashSet<>();
  private final ExternalModelRegistry registry;

  public ModelIgnoreDecider(ExternalModelRegistry registry) {
    this.registry = registry;
  }

  /** Evaluates ignore rules for a model. */
  public boolean shouldIgnore(String name, Schema model) {
    boolean byExtension = isIgnoredByExtension(model);
    boolean byExternal = registry.isExternal(name);

    if (byExtension) {
      log.debug("Model ignored by extension (x-ignore-model): {}", name);
    } else if (byExternal) {
      log.debug("Model ignored as external model: {}", name);
    }

    return byExtension || byExternal;
  }

  /** Marks model as ignored. */
  public void markIgnored(String name) {
    ignored.add(name);
    log.debug("Marked model as ignored: {}", name);
  }

  /**
   * @return true if model is already marked as ignored
   */
  public boolean isIgnored(String name) {
    return ignored.contains(name);
  }

  private boolean isIgnoredByExtension(Schema model) {
    if (model == null) return false;

    Map<String, Object> ext = model.getExtensions();
    return ext != null && Boolean.TRUE.equals(ext.get(EXT_IGNORE_MODEL));
  }
}