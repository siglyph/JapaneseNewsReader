package com.jkobura.JapaneseNewsReader;

public class NewsArticle {
    private String articleName;
    private String content;
    private String furiganaContent;

    public void setContent(String content){
        this.content = content;
    }
    public void setArticleName(String articleName){
        this.articleName = articleName;
    }
    public String getContent(){
        return this.content;
    }
    public String getArticleName(){
        return this.articleName;
    }
    public void setContentWithFurigana(String furiganaContent) {
        this.furiganaContent = furiganaContent;
    }
}
