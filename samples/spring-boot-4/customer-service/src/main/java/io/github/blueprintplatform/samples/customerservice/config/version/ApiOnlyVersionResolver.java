package io.github.blueprintplatform.samples.customerservice.config.version;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.accept.ApiVersionResolver;

public class ApiOnlyVersionResolver implements ApiVersionResolver {

    private final String headerName;
    private final String contextPath;
    private final String apiBasePath;

    public ApiOnlyVersionResolver(
            String headerName,
            String contextPath,
            String apiBasePath) {

        this.headerName = headerName;
        this.contextPath = normalize(contextPath);
        this.apiBasePath = normalize(apiBasePath);
    }

    @Override
    public String resolveVersion(HttpServletRequest request) {

        String uri = request.getRequestURI();
        String fullApiPath = contextPath + apiBasePath;

        if (uri.equals(fullApiPath) || uri.startsWith(fullApiPath + "/")) {
            return request.getHeader(headerName);
        }

        return null;
    }

    private String normalize(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}