package io.github.blueprintplatform.functional.multitask;

import io.github.blueprintplatform.functional.multitask.customer.model.CustomerDto;
import io.github.blueprintplatform.functional.multitask.customer.model.ServiceResponseCustomerDto;
import io.github.blueprintplatform.functional.multitask.order.model.OrderDto;
import io.github.blueprintplatform.functional.multitask.order.model.ServiceResponsePageOrderDto;
import java.util.List;

/**
 * Compile-time proof that both selected GenerateTask outputs are registered
 * on the same main Java source set.
 */
public final class MultiTaskGenerationProbe {

    private MultiTaskGenerationProbe() {}

    public static List<Class<?>> generatedTypes() {
        return List.of(
                CustomerDto.class,
                ServiceResponseCustomerDto.class,
                OrderDto.class,
                ServiceResponsePageOrderDto.class);
    }
}
