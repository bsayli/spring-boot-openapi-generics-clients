package io.github.blueprintplatform.samples.generationcoverage;

import io.github.blueprintplatform.samples.generationcoverage.customer.api.CustomerControllerApi;
import io.github.blueprintplatform.samples.generationcoverage.customer.dto.ServiceResponseCustomerDto;
import io.github.blueprintplatform.samples.generationcoverage.customer.dto.ServiceResponsePageCustomerDto;
import io.github.blueprintplatform.samples.generationcoverage.serviceresponse.api.AsyncPayloadControllerApi;
import io.github.blueprintplatform.samples.generationcoverage.serviceresponse.api.ScalarPayloadControllerApi;
import io.github.blueprintplatform.samples.generationcoverage.serviceresponse.dto.ServiceResponseListTypeSummaryDto;
import io.github.blueprintplatform.samples.generationcoverage.serviceresponse.dto.ServiceResponsePageTypeSummaryDto;
import io.github.blueprintplatform.samples.generationcoverage.serviceresponse.dto.ServiceResponseString;
import io.github.blueprintplatform.samples.generationcoverage.serviceresponse.dto.ServiceResponseWindowTypeSummaryDto;
import java.util.List;

/**
 * Compile-time proof that both OpenAPI Generator executions contributed isolated,
 * usable API and model source trees to the same Maven module.
 *
 * <p>The selected types intentionally cover representative contract shapes rather than every
 * generated class:
 *
 * <ul>
 *   <li>customer API and BYOC-backed direct wrapper
 *   <li>customer paged wrapper
 *   <li>service-response API
 *   <li>scalar, list, page, and application-owned container wrappers
 *   <li>async-controller-generated API surface
 * </ul>
 */
public final class MultiSpecGenerationProbe {

  private MultiSpecGenerationProbe() {}

  public static List<Class<?>> generatedTypes() {
    return List.of(
            CustomerControllerApi.class,
            ServiceResponseCustomerDto.class,
            ServiceResponsePageCustomerDto.class,
            ScalarPayloadControllerApi.class,
            AsyncPayloadControllerApi.class,
            ServiceResponseString.class,
            ServiceResponseListTypeSummaryDto.class,
            ServiceResponsePageTypeSummaryDto.class,
            ServiceResponseWindowTypeSummaryDto.class);
  }
}