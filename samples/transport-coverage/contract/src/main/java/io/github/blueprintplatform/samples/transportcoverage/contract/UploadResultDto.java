package io.github.blueprintplatform.samples.transportcoverage.contract;

public record UploadResultDto(
        String fileName, String contentType, long size, String description, String category) {
}
