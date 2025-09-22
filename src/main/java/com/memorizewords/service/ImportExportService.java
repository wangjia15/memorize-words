package com.memorizewords.service;

import com.memorizewords.dto.request.BulkImportOptions;
import com.memorizewords.dto.request.CreateWordRequest;
import com.memorizewords.dto.response.BulkImportResult;
import com.memorizewords.dto.response.WordDto;
import com.memorizewords.dto.response.WordImportDto;
import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.exception.DuplicateWordException;
import com.memorizewords.exception.ImportException;
import com.memorizewords.repository.WordRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing and exporting words in various formats.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ImportExportService {

    private final WordRepository wordRepository;
    private final WordService wordService;

    public BulkImportResult bulkImportWords(MultipartFile file, BulkImportOptions options, User user) {
        log.info("Starting bulk import for user: {} with format: {}", user.getUsername(), options.getFormat());

        try {
            List<WordImportDto> importWords = parseImportFile(file, options.getFormat());
            BulkImportResult result = new BulkImportResult();
            result.setTotalWords(importWords.size());

            for (WordImportDto importWord : importWords) {
                try {
                    CreateWordRequest request = mapImportToRequest(importWord);
                    WordDto created = wordService.createWord(request, user);
                    result.addSuccess(created);
                } catch (DuplicateWordException e) {
                    if (options.getSkipDuplicates()) {
                        result.addSkipped(importWord.getWord(), "Duplicate word");
                    } else {
                        result.addError(importWord.getWord(), e.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Error importing word: {}", importWord.getWord(), e);
                    result.addError(importWord.getWord(), e.getMessage());
                }
            }

            log.info("Bulk import completed. Success: {}, Skipped: {}, Errors: {}",
                result.getSuccessCount(), result.getSkippedCount(), result.getErrorCount());

            return result;
        } catch (Exception e) {
            log.error("Bulk import failed", e);
            throw new ImportException("Failed to import words: " + e.getMessage());
        }
    }

    public Resource exportWords(String format, java.util.Set<Long> wordIds, User user) {
        log.info("Exporting words in {} format for user: {}", format, user.getUsername());

        try {
            List<Word> words;
            if (wordIds != null && !wordIds.isEmpty()) {
                words = wordRepository.findAllById(wordIds);
            } else {
                // Export all accessible words for the user
                words = wordRepository.findAccessibleWords(user, org.springframework.data.domain.Pageable.unpaged()).getContent();
            }

            if ("csv".equalsIgnoreCase(format)) {
                return exportToCsv(words);
            } else if ("json".equalsIgnoreCase(format)) {
                return exportToJson(words);
            } else {
                throw new ImportException("Unsupported export format: " + format);
            }
        } catch (Exception e) {
            log.error("Export failed", e);
            throw new ImportException("Failed to export words: " + e.getMessage());
        }
    }

    private List<WordImportDto> parseImportFile(MultipartFile file, String format) throws IOException, CsvValidationException {
        List<WordImportDto> words = new ArrayList<>();

        if ("csv".equalsIgnoreCase(format)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                 CSVReader csvReader = new CSVReader(reader)) {

                String[] headers = csvReader.readNext();
                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    WordImportDto word = new WordImportDto();
                    for (int i = 0; i < Math.min(headers.length, line.length); i++) {
                        String header = headers[i].toLowerCase().trim();
                        String value = line[i].trim();

                        switch (header) {
                            case "word":
                                word.setWord(value);
                                break;
                            case "language":
                                word.setLanguage(value);
                                break;
                            case "definition":
                                word.setDefinition(value);
                                break;
                            case "pronunciation":
                                word.setPronunciation(value);
                                break;
                            case "example":
                                word.setExample(value);
                                break;
                            case "difficulty":
                                word.setDifficulty(value);
                                break;
                            case "categories":
                                word.setCategories(value);
                                break;
                            case "tags":
                                word.setTags(value);
                                break;
                            case "ispublic":
                                word.setIsPublic(value);
                                break;
                        }
                    }
                    words.add(word);
                }
            }
        } else if ("json".equalsIgnoreCase(format)) {
            // JSON parsing would be implemented here
            throw new ImportException("JSON import not yet implemented");
        } else {
            throw new ImportException("Unsupported import format: " + format);
        }

        return words;
    }

    private CreateWordRequest mapImportToRequest(WordImportDto importWord) {
        CreateWordRequest request = new CreateWordRequest();

        if (!java.util.Objects.toString(importWord.getWord(), "").trim().isEmpty()) {
            request.setWord(importWord.getWord());
        } else {
            throw new IllegalArgumentException("Word is required");
        }

        if (!java.util.Objects.toString(importWord.getLanguage(), "").trim().isEmpty()) {
            request.setLanguage(importWord.getLanguage());
        } else {
            request.setLanguage("english"); // Default language
        }

        request.setDefinition(importWord.getDefinition());
        request.setPronunciation(importWord.getPronunciation());
        request.setExample(importWord.getExample());

        if (importWord.getDifficulty() != null && !importWord.getDifficulty().trim().isEmpty()) {
            try {
                request.setDifficulty(com.memorizewords.enum.DifficultyLevel.valueOf(importWord.getDifficulty().toUpperCase()));
            } catch (IllegalArgumentException e) {
                request.setDifficulty(com.memorizewords.enum.DifficultyLevel.BEGINNER);
            }
        } else {
            request.setDifficulty(com.memorizewords.enum.DifficultyLevel.BEGINNER);
        }

        if (importWord.getIsPublic() != null && !importWord.getIsPublic().trim().isEmpty()) {
            request.setIsPublic(Boolean.parseBoolean(importWord.getIsPublic()));
        }

        return request;
    }

    private Resource exportToCsv(List<Word> words) throws IOException {
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

        // Write header
        String[] headers = {"ID", "Word", "Language", "Definition", "Pronunciation", "Example", "Difficulty", "Categories", "Tags", "IsPublic", "CreatedBy", "CreatedAt"};
        csvWriter.writeNext(headers);

        // Write data
        for (Word word : words) {
            String[] row = {
                word.getId().toString(),
                word.getWord(),
                word.getLanguage(),
                word.getDefinition() != null ? word.getDefinition() : "",
                word.getPronunciation() != null ? word.getPronunciation() : "",
                word.getExample() != null ? word.getExample() : "",
                word.getDifficulty() != null ? word.getDifficulty().toString() : "",
                word.getCategories() != null ? String.join(",", word.getCategories().stream().map(Enum::toString).toList()) : "",
                word.getTags() != null ? String.join(",", word.getTags()) : "",
                word.getIsPublic() != null ? word.getIsPublic().toString() : "false",
                word.getCreatedBy() != null ? word.getCreatedBy().getUsername() : "",
                word.getCreatedAt() != null ? word.getCreatedAt().toString() : ""
            };
            csvWriter.writeNext(row);
        }

        csvWriter.close();
        return new ByteArrayResource(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Resource exportToJson(List<Word> words) throws IOException {
        // JSON export would be implemented here
        throw new ImportException("JSON export not yet implemented");
    }
}