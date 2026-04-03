package io.github.bsayli.customerservice.common.i18n;

import java.util.Locale;

public interface LocalizedMessageResolver {

  String getMessage(String messageKey);

  String getMessage(String messageKey, Object... args);

  String getMessage(String messageKey, Locale locale);

  String getMessage(String messageKey, Locale locale, Object... args);
}
