package com.example.demo.scrappednews;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("POLSATNEWS_COLLECTION")
public class ScrappedNewsPolsatNews extends ScrappedNews{

   public final static String stringRepresentation = "polsatnews";

   public ScrappedNewsPolsatNews(String _id, String title, String news_preview, LocalDateTime scrap_time, LocalDateTime date_published) {
      super(_id, title, news_preview, scrap_time, date_published);
   }

   public  static String getStringRepresentation() {
      return stringRepresentation;
   }
}
