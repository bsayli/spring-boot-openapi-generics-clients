package io.github.bsayli.customerservice.api.error;

public record ProblemDescriptor(String typeSlug, String title, String detail) {}
