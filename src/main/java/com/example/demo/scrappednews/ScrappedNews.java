package com.example.demo.scrappednews;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

//@Document("DGP_WIADOMOSCI_COLLECTION")
public class ScrappedNews {

   @Id
   private String _id;
   private String title;
   private String news_preview;
   private LocalDateTime scrap_time;
   private LocalDateTime date_published;

   public ScrappedNews(String _id, String title, String news_preview, LocalDateTime scrap_time, LocalDateTime date_published) {
      this._id = _id;
      this.title = title;
      this.news_preview = news_preview;
      this.scrap_time = scrap_time;
      this.date_published = date_published;
   }

   @Override
   public String toString() {
      return "ScrappedNews{" +
              "_id='" + _id + '\'' +
              ", title='" + title + '\'' +
              ", news_preview='" + news_preview + '\'' +
              ", scrap_time=" + scrap_time +
              ", date_published=" + date_published +
              '}';
   }

   public String get_id() {
      return _id;
   }

   public void set_id(String _id) {
      this._id = _id;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getNews_preview() {
      return news_preview;
   }

   public void setNews_preview(String news_preview) {
      this.news_preview = news_preview;
   }

   public LocalDateTime getScrap_time() {
      return scrap_time;
   }

   public void setScrap_time(LocalDateTime scrap_time) {
      this.scrap_time = scrap_time;
   }

   public LocalDateTime getDate_published() {
      return date_published.minus(Duration.ofHours(2));
   }

   public void setDate_published(LocalDateTime date_published) {
      this.date_published = date_published;
   }


}
