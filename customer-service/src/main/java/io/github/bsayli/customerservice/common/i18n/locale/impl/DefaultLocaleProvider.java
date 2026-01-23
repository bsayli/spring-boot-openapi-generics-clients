package io.github.bsayli.customerservice.common.i18n.locale.impl;

import io.github.bsayli.customerservice.common.i18n.locale.CurrentLocaleProvider;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class DefaultLocaleProvider implements CurrentLocaleProvider {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    @Override
    public Locale getCurrentLocale() {
        return DEFAULT_LOCALE;
    }
}
