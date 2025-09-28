package com.jkobura.JapaneseNewsReader;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class DictionaryService {
    private final Map<String, String> dictionary = new HashMap<>();

    public DictionaryService() {
        // Load dictionary from file (path can be configured via env or properties)
        String dictPath = System.getenv("DICT_FILE");
        if (dictPath == null) {
            dictPath = "dictionary.txt"; // default name (will try on classpath)
        }
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(dictPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", 2);
                if (parts.length == 2) {
                    dictionary.put(parts[0], parts[1]);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load dictionary: " + e.getMessage());
        }
    }

    public String lookup(String word) {
        return dictionary.get(word);
    }

    public Iterable<Object> getDictionary() {
    }
}
