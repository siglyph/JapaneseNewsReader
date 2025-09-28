package com.jkobura.JapaneseNewsReader;

import ch.qos.logback.core.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class NewsController {

    @Autowired
    private NewsFetcherService newsService;
    @Autowired
    private DictionaryService dictionaryService;

    // Home page - list news with furigana
    @GetMapping("/")
    public String showNews(Model model) {
        List<NewsArticle> articles = newsService.fetchLatestArticles();
        // Add furigana to each article content
        for (NewsArticle article : articles) {
            String text = article.getContent();
            article.setContentWithFurigana(newsService.addFurigana(text));
        }
        model.addAttribute("articles", articles);
        return "news"; // Thymeleaf template name (news.html)
    }

    // API endpoint for word lookup
    @ResponseBody
    @GetMapping("/api/define")
    public String defineWord(@RequestParam("word") String word) {
        String meaning = dictionaryService.lookup(word);
        if (meaning == null) {
            return "No definition found";
        }
        return meaning;
    }

    // Export endpoint (for demonstration, exports entire dictionary or could export selected words)
    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<String> exportDictionary() {
        // Build TSV content from dictionary
        StringBuilder sb = new StringBuilder();
        dictionaryService.getDictionary().forEach((jp, en) -> {
            sb.append(jp).append("\t").append(en).append("\n");
        });
        // Set headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/tab-separated-values"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dictionary.tsv\"");
        return new ResponseEntity<>(sb.toString(), headers, HttpStatus.OK);
    }
}
