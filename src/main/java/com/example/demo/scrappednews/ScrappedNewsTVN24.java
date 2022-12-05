package com.example.demo.scrappednews;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("TVN24_COLLECTION")
public class ScrappedNewsTVN24 extends ScrappedNews {


   public  static final String stringRepresentation = "tvn24";

   public ScrappedNewsTVN24(String _id, String title, String news_preview, LocalDateTime scrap_time, LocalDateTime date_published) {
      super(_id, title, news_preview, scrap_time, date_published);
   }

   public  static String getStringRepresentation() {
      return stringRepresentation;
   }

}
