package com.jkobura.JapaneseNewsReader;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.atilika.kuromoji.ipadic.Token;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsFetcherService {

    private static final String FEED_URL = System.getenv("FEED_URL");
    private final Tokenizer tokenizer = new Tokenizer();

    public List<NewsArticle> fetchLatestArticles(){
        List<NewsArticle> articles = new ArrayList<>();
        try{
            URL url = new URL(FEED_URL != null ? FEED_URL : "https://www3.nhk.or.jp/rss/news/cat0.xml");
            XmlReader xmlReader = new XmlReader(url);
            SyndFeed feed = new SyndFeedInput().build(xmlReader);

            for (SyndEntry entry : feed.getEntries()){
                String title = entry.getTitle();
                String content = "";

                if (entry.getContents() != null && !entry.getContents().isEmpty()){
                    SyndContent syndContent = (SyndContent) entry.getContents().get(0);
                    content = syndContent.getValue();
                }
                else if (entry.getDescription() != null){
                    content = entry.getDescription().getValue();
                }
                // Remove HTML Tags
                content = content.replaceAll("<[^>]+>", "");
                NewsArticle article = new NewsArticle();
                article.setArticleName(title);
                article.setContent(content);
                articles.add(article);

            }

        } catch (Exception e){
            e.printStackTrace();
        }


        return articles;
    }


    private static boolean containsKanji(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F) {
                return true;
            }
        }
        return false;
    }

    // Convert Kuromoji's reading (katakana) to hiragana for furigana display.
    // IMPORTANT: We DO NOT convert surface text. Katakana words remain katakana.
    private static String katakanaToHiragana(String kata) {
        if (kata == null) return "";
        StringBuilder sb = new StringBuilder(kata.length());
        for (int i = 0; i < kata.length(); i++) {
            char c = kata.charAt(i);
            if (c >= 'ァ' && c <= 'ン') {
                sb.append((char)(c - 0x60));
            } else if (c == 'ヴ') {
                sb.append("ゔ");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String escapeHtml(String s) {
        return s == null ? "" : s.replace("&","&amp;")
                                 .replace("<","&lt;")
                                 .replace(">","&gt;");
    }

    public String addFurigana(String text){
        if (text == null || text.isEmpty()) return "";

        StringBuilder out = new StringBuilder();
        for (Token tk : tokenizer.tokenize(text)) {
            String surface = tk.getSurface();
            String readingKatakana = tk.getReading(); // may be null for punctuation/symbols

            // Only annotate tokens that CONTAIN KANJI.
            if (containsKanji(surface) && readingKatakana != null && !readingKatakana.isEmpty()) {
                // Convert the READING to hiragana for furigana. We do NOT alter katakana surface words.
                String readingHiragana = katakanaToHiragana(readingKatakana);
                out.append("<ruby><rb>")
                    .append(escapeHtml(surface))
                    .append("</rb><rt>")
                    .append(escapeHtml(readingHiragana))
                    .append("</rt></ruby>");
            } else {
                // Keep non-kanji tokens (including katakana words) exactly as-is (no conversion).
                out.append(escapeHtml(surface));
            }
        }
        return out.toString();
    }

}
