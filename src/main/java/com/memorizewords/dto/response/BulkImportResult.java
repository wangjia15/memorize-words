package com.memorizewords.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for bulk import results.
 */
@Data
public class BulkImportResult {

    private int totalWords = 0;

    private int successCount = 0;

    private int skippedCount = 0;

    private int errorCount = 0;

    private List<ImportSuccess> successes = new ArrayList<>();

    private List<ImportSkipped> skipped = new ArrayList<>();

    private List<ImportError> errors = new ArrayList<>();

    public void addSuccess(WordDto word) {
        successes.add(new ImportSuccess(word));
        successCount++;
    }

    public void addSkipped(String word, String reason) {
        skipped.add(new ImportSkipped(word, reason));
        skippedCount++;
    }

    public void addError(String word, String error) {
        errors.add(new ImportError(word, error));
        errorCount++;
    }

    @Data
    public static class ImportSuccess {
        private WordDto word;
        public ImportSuccess(WordDto word) {
            this.word = word;
        }
    }

    @Data
    public static class ImportSkipped {
        private String word;
        private String reason;
        public ImportSkipped(String word, String reason) {
            this.word = word;
            this.reason = reason;
        }
    }

    @Data
    public static class ImportError {
        private String word;
        private String error;
        public ImportError(String word, String error) {
            this.word = word;
            this.error = error;
        }
    }
}