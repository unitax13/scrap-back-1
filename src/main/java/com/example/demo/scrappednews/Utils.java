package com.example.demo.scrappednews;

import java.time.LocalDate;

public class Utils {

   public static LocalDate convertStringDateToDate(String s) {

      int year = 0, month = 0, day = 0;
      String[] dates = s.split("-");

      try {
         year = Integer.parseInt(dates[0]);
         month = Integer.parseInt(dates[1]);
         day = Integer.parseInt(dates[2]);
      } catch (Exception e) {
         System.out.println("failed to convert to integers");
         e.printStackTrace();
         return LocalDate.MIN;
      }


      return LocalDate.parse(s);

   }

}
