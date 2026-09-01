package io.github.blueprintplatform.samples.gradle.customer;

import io.github.blueprintplatform.samples.gradle.customer.generated.api.CustomerControllerApi;
import io.github.blueprintplatform.samples.gradle.customer.generated.dto.ServiceResponseCustomerDto;
import io.github.blueprintplatform.samples.gradle.customer.generated.dto.ServiceResponsePageCustomerDto;
import java.util.List;

/**
 * Compile-time proof that the Gradle plugin generated and registered both API
 * and generic wrapper source trees for normal Java compilation.
 */
public final class GeneratedCustomerClientProbe {

  private GeneratedCustomerClientProbe() {}

  public static List<Class<?>> representativeGeneratedTypes() {
    return List.of(
        CustomerControllerApi.class,
        ServiceResponseCustomerDto.class,
        ServiceResponsePageCustomerDto.class);
  }
}
