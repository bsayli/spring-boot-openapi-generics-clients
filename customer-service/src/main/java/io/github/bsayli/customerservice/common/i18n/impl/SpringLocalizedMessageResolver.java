package io.github.bsayli.customerservice.common.i18n.impl;

import io.github.bsayli.customerservice.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.customerservice.common.i18n.locale.CurrentLocaleProvider;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class SpringLocalizedMessageResolver implements LocalizedMessageResolver {

    private final MessageSource messageSource;
    private final CurrentLocaleProvider localeProvider;

    public SpringLocalizedMessageResolver(
            MessageSource messageSource, CurrentLocaleProvider localeProvider) {
        this.messageSource = messageSource;
        this.localeProvider = localeProvider;
    }

    @Override
    public String getMessage(String messageKey) {
        return messageSource.getMessage(messageKey, null, localeProvider.getCurrentLocale());
    }

    @Override
    public String getMessage(String messageKey, Object... args) {
        return messageSource.getMessage(messageKey, args, localeProvider.getCurrentLocale());
    }

    @Override
    public String getMessage(String messageKey, Locale locale) {
        return messageSource.getMessage(messageKey, null, locale);
    }

    @Override
    public String getMessage(String messageKey, Locale locale, Object... args) {
        return messageSource.getMessage(messageKey, args, locale);
    }
}
