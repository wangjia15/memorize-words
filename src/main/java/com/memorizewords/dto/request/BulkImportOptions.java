package com.memorizewords.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for bulk import options.
 */
@Data
public class BulkImportOptions {

    @NotBlank(message = "Import format is required")
    private String format = "csv";

    @NotNull(message = "Skip duplicates option is required")
    private Boolean skipDuplicates = true;

    private Boolean overwriteExisting = false;

    private String delimiter = ",";
}