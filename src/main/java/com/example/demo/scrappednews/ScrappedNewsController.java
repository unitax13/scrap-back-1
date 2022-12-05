package com.example.demo.scrappednews;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:5173/", methods = {RequestMethod.OPTIONS, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}, allowedHeaders = "*", allowCredentials = "true")
@RequestMapping(path = "news/get")
public class ScrappedNewsController {

   private final ScrappedNewsService newsService;
   final ArrayList<String> services = new ArrayList<>(Arrays.asList("dgp", "polsatnews", "rmf24", "tvn24", "gazetapl", "wydarzeniainteria"));

   ArrayList<ScrappedNewsDGP> newsDGP = new ArrayList<>();
   ArrayList<ScrappedNewsGazetaPl> newsGazetaPl = new ArrayList<>();
   ArrayList<ScrappedNewsPolsatNews> newsPolsatNews = new ArrayList<>();
   ArrayList<ScrappedNewsRMF24> newsRMF24 = new ArrayList<>();
   ArrayList<ScrappedNewsTVN24> newsTVN24 = new ArrayList<>();
   ArrayList<ScrappedNewsWydarzeniaInteria> newsWydarzeniaInteria = new ArrayList<>();
   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

   TreeMap<LocalDate, Integer> dgpTimeTable = new TreeMap<>();
   TreeMap<LocalDate, Integer> polsatnewsTimeTable = new TreeMap<>();
   TreeMap<LocalDate, Integer> rmf24TimeTable = new TreeMap<>();
   TreeMap<LocalDate, Integer> tvn24TimeTable = new TreeMap<>();
   TreeMap<LocalDate, Integer> gazetaplTimeTable = new TreeMap<>();
   TreeMap<LocalDate, Integer> wydarzeniainteriaTimeTable = new TreeMap<>();

   HashMap<String,TreeMap<LocalDate,Integer>> timetables = new HashMap<>();

   LocalTime dbFetchTime;
   LocalTime nextFetchTime;

   @Autowired
   public ScrappedNewsController(ScrappedNewsService newsService) {
      this.newsService = newsService;
      RefetchAspect.snc = this;
      fetchDataFromRepositories();
      generateTimeTables();
   }

   private void fetchDataFromRepositories() {

      newsDGP = new ArrayList<>();
      newsGazetaPl = new ArrayList<>();
      newsPolsatNews = new ArrayList<>();
      newsRMF24 = new ArrayList<>();
      newsTVN24 = new ArrayList<>();
      newsWydarzeniaInteria = new ArrayList<>();

      newsDGP.addAll(newsService.getDGPNewsRepository().findAll());
      newsPolsatNews.addAll(newsService.getPolsatNewsRepository().findAll());
      newsRMF24.addAll(newsService.getRmf24Repository().findAll());
      newsTVN24.addAll(newsService.getTVN24Repository().findAll());
      newsGazetaPl.addAll(newsService.getGazetaPlRepository().findAll());
      newsWydarzeniaInteria.addAll(newsService.getWydarzeniaInteriaRepository().findAll());

      dbFetchTime = LocalTime.now();
      nextFetchTime = dbFetchTime.plus(Duration.ofMinutes(30));
   }

   public void checkIfItsTimeToRefetchAndIfSoDoSo() {
      if (LocalTime.now().isAfter(nextFetchTime)) {
         System.out.println("It's time for a new fetch.");
         fetchDataFromRepositories();
      }
   }

   private void generateTimeTables() {
      dgpTimeTable = generateTimeTable("dgp");
      polsatnewsTimeTable = generateTimeTable("polsatnews");
      rmf24TimeTable = generateTimeTable("rmf24");
      tvn24TimeTable = generateTimeTable("tvn24");
      gazetaplTimeTable = generateTimeTable("gazetapl");
      wydarzeniainteriaTimeTable = generateTimeTable("wydarzeniainteria");

      timetables.put(ScrappedNewsDGP.getStringRepresentation(),dgpTimeTable);
      timetables.put(ScrappedNewsPolsatNews.getStringRepresentation(),polsatnewsTimeTable);
      timetables.put(ScrappedNewsRMF24.getStringRepresentation(),rmf24TimeTable);
      timetables.put(ScrappedNewsTVN24.getStringRepresentation(),tvn24TimeTable);
      timetables.put(ScrappedNewsGazetaPl.getStringRepresentation(),gazetaplTimeTable);
      timetables.put(ScrappedNewsWydarzeniaInteria.getStringRepresentation(),wydarzeniainteriaTimeTable);
   }

   private TreeMap<LocalDate, Integer> generateTimeTable(String whichOne) {

      ArrayList<ScrappedNews> chosenNewsPool = new ArrayList<>();
      TreeMap<LocalDate, Integer> currentTimeTable = new TreeMap<LocalDate, Integer>();
      switch (whichOne) {
         case "dgp":
            chosenNewsPool.addAll(newsDGP);
            break;
         case "polsatnews":
            chosenNewsPool.addAll(newsPolsatNews);
            break;
         case "rmf24":
            chosenNewsPool.addAll(newsRMF24);
            break;
         case "tvn24":
            chosenNewsPool.addAll(newsTVN24);
            break;
         case "gazetapl":
            chosenNewsPool.addAll(newsGazetaPl);
            break;
         case "wydarzeniainteria":
            chosenNewsPool.addAll(newsWydarzeniaInteria);
            break;
         default:
            return currentTimeTable;
      }


      LocalDate currentEarliestDate = LocalDate.now();
      LocalDate currentLatestDate = LocalDate.MIN;

      for (ScrappedNews sn : chosenNewsPool) {
         LocalDate currentDate = sn.getDate_published().toLocalDate();
         if (currentDate.isBefore(currentEarliestDate)) {
            currentEarliestDate = currentDate;
         }
         if (currentDate.isAfter(currentLatestDate)) {
            currentLatestDate = currentDate;
         }
         if (currentTimeTable.containsKey(currentDate)) {
            currentTimeTable.replace((currentDate), currentTimeTable.get(currentDate) + 1);
         } else {
            currentTimeTable.put(currentDate, 1);
         }

         while (currentEarliestDate.isBefore(currentLatestDate)) {
            currentEarliestDate = currentEarliestDate.plus(Period.ofDays(1));
            if (!currentTimeTable.containsKey(currentEarliestDate)) {
               currentTimeTable.put(currentEarliestDate, 0);
            }
         }
      }

      return currentTimeTable;


   }


   @RequestMapping(path = "/timetable")
   @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Object> getTimeTable(@RequestParam(required = true) String service) {

      if (service == null) {
         return new ResponseEntity<Object>("Service not specified. Available services are " + services.toString(), HttpStatus.NOT_ACCEPTABLE);
      } else {
         String[] servicesGotten = service.split(",");
         System.out.println("Services gotten: " + Arrays.toString(servicesGotten));
         for (String singleService : servicesGotten) {
            return switch (singleService) {
               case "dgp" -> new ResponseEntity<Object>(dgpTimeTable, HttpStatus.OK);
               case "polsatnews" -> new ResponseEntity<Object>(polsatnewsTimeTable, HttpStatus.OK);
               case "rmf24" -> new ResponseEntity<Object>(rmf24TimeTable, HttpStatus.OK);
               case "tvn24" -> new ResponseEntity<Object>(tvn24TimeTable, HttpStatus.OK);
               case "gazetapl" -> new ResponseEntity<Object>(gazetaplTimeTable, HttpStatus.OK);
               case "wydarzeniainteria" -> new ResponseEntity<Object>(wydarzeniainteriaTimeTable, HttpStatus.OK);
               default -> new ResponseEntity<Object>("Service parameter error. Available services are " + services.toString(), HttpStatus.NOT_ACCEPTABLE);
            };
         }
      }
      return new ResponseEntity<Object>("Service parameter error. Available services are " + services.toString(), HttpStatus.NOT_ACCEPTABLE);
   }

   public ScrappedNewsService getNewsService() {
      return newsService;
   }

   @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Object> getScrappedNews(@RequestParam(required = false) String service,
                                                 @RequestParam(required = false) String start,
                                                 @RequestParam(required = false) String end,
                                                 @RequestParam(required = false) String onlyTitles,
                                                 @RequestParam(required = false) String from,
                                                 @RequestParam(required = false) String to,
                                                 @RequestParam(required = false) String searchFor) throws Exception {

      List<?> listToReturn;
      ArrayList<ScrappedNews> newsPool = new ArrayList<>();


//      if (LocalTime.now().isAfter(nextFetchTime)) {
//         System.out.println("It's time for a new fetch.");
//         fetchDataFromRepositories();
//      }

      if (service == null) {
         return new ResponseEntity<Object>("Service not specified. Available services are " + services.toString(), HttpStatus.NOT_ACCEPTABLE);
      } else {
         String[] servicesGotten = service.split(",");
         System.out.println("Services gotten: " + Arrays.toString(servicesGotten));
         for (String singleService : servicesGotten) {
            switch (singleService) {
               case "dgp":
                  newsPool.addAll(newsDGP);
                  break;
               case "polsatnews":
                  newsPool.addAll(newsPolsatNews);
                  break;
               case "rmf24":
                  newsPool.addAll(newsRMF24);
                  break;
               case "tvn24":
                  newsPool.addAll(newsTVN24);
                  break;
               case "gazetapl":
                  newsPool.addAll(newsGazetaPl);
                  break;
               case "wydarzeniainteria":
                  newsPool.addAll(newsWydarzeniaInteria);
                  break;
               default:
                  return new ResponseEntity<Object>("Service parameter error. Available services are " + services.toString(), HttpStatus.NOT_ACCEPTABLE);

            }
         }
      }

      try {
         if (to != null) {
            to = to + " 00:00:00";
            LocalDateTime date_to = LocalDateTime.parse(to, dtf);
            newsPool.removeIf((sn -> sn.getDate_published().isAfter(ChronoLocalDateTime.from(date_to))));
         }
         if (from != null) {
            from = from + " 00:00:00";
            LocalDateTime date_from = LocalDateTime.parse(from, dtf);
            newsPool.removeIf((sn -> sn.getDate_published().isBefore(ChronoLocalDateTime.from(date_from))));
         }
      } catch (DateTimeException e) {
         return new ResponseEntity<>("Invalid date", HttpStatus.BAD_REQUEST);
      }

      newsPool.sort(new Comparator<ScrappedNews>() {
         @Override
         public int compare(ScrappedNews o1, ScrappedNews o2) {
            if (o1.getDate_published().isAfter(o2.getDate_published())) {
               return -1;
            } else if (o1.getDate_published().isEqual(o2.getDate_published())) {
               return 0;
            } else return 1;
         }
      });





      if (onlyTitles != null && onlyTitles.equals("true")) {
         ArrayList<String> titlesList = new ArrayList<>();

         for (ScrappedNews sn : newsPool) {
            titlesList.add(sn.getTitle());
         }
         if (searchFor != null && !searchFor.equals("")) {
            titlesList.removeIf(title -> !title.toLowerCase(Locale.ROOT).contains(searchFor.toLowerCase(Locale.ROOT)));
         }
         listToReturn = titlesList;
         return new ResponseEntity<Object>(listToReturn, HttpStatus.OK);
      }

      if (searchFor != null) {
         newsPool.removeIf(scrappedNews -> !scrappedNews.getTitle().toLowerCase(Locale.ROOT).contains(searchFor.toLowerCase(Locale.ROOT)));
      }

      HashMap <String, Integer> counts = new HashMap<>();
      counts.put(ScrappedNewsDGP.getStringRepresentation(),0);
      counts.put(ScrappedNewsPolsatNews.getStringRepresentation(),0);
      counts.put(ScrappedNewsGazetaPl.getStringRepresentation(),0);
      counts.put(ScrappedNewsTVN24.getStringRepresentation(),0);
      counts.put(ScrappedNewsRMF24.getStringRepresentation(),0);
      counts.put(ScrappedNewsWydarzeniaInteria.getStringRepresentation(),0);
      for (Object o: newsPool) {
         if (o.getClass().toString().equals(ScrappedNewsDGP.class.toString())) {
            counts.put(ScrappedNewsDGP.getStringRepresentation(), counts.get(ScrappedNewsDGP.getStringRepresentation()) + 1 );
         } else if (o.getClass().toString().equals(ScrappedNewsRMF24.class.toString())) {
            counts.put(ScrappedNewsRMF24.getStringRepresentation(), counts.get(ScrappedNewsRMF24.getStringRepresentation()) + 1 );
         } else if (o.getClass().toString().equals(ScrappedNewsTVN24.class.toString())) {
            counts.put(ScrappedNewsTVN24.getStringRepresentation(), counts.get(ScrappedNewsTVN24.getStringRepresentation()) + 1 );
         } else if (o.getClass().toString().equals(ScrappedNewsWydarzeniaInteria.class.toString())) {
            counts.put(ScrappedNewsWydarzeniaInteria.getStringRepresentation(), counts.get(ScrappedNewsWydarzeniaInteria.getStringRepresentation()) + 1 );
         } else if (o.getClass().toString().equals(ScrappedNewsPolsatNews.class.toString())) {
            counts.put(ScrappedNewsPolsatNews.getStringRepresentation(), counts.get(ScrappedNewsPolsatNews.getStringRepresentation()) + 1 );
         } else if (o.getClass().toString().equals(ScrappedNewsGazetaPl.class.toString())) {
            counts.put(ScrappedNewsGazetaPl.getStringRepresentation(), counts.get(ScrappedNewsGazetaPl.getStringRepresentation()) + 1 );
         } else {
            throw new Exception("error matching classes to strings");
         }

//         System.out.println((o.getClass().toString() + " " + ScrappedNewsDGP.class.toString()));
//         System.out.println(o.get);
      }
      System.out.println(counts.toString());

      int i_start = 0;
      int i_end = 0;
      if (start != null && end != null) {
         try {
            i_start = Integer.parseInt(start);
            i_end = Integer.parseInt(end);

            if (i_start > i_end) {
               return new ResponseEntity<Object>(Collections.<ScrappedNews>emptyList(), HttpStatus.NOT_ACCEPTABLE);
            }
            if (i_end > newsPool.size()) {
               i_end = newsPool.size();
            }
         } catch (Exception e) {
            System.out.println(("Failed to convert strings to int"));
         }

         ArrayList<ScrappedNews> newsPool2 = new ArrayList<>();
         if (i_start < newsPool.size()) {
            for (int i = i_start; i < i_end && i < newsPool.size(); i++) {
               newsPool2.add(newsPool.get(i));
            }
         }

         newsPool = newsPool2;
      }
      listToReturn = newsPool;



      System.out.println("returning response entity");

//      listToReturn.add(counts);
//      HttpHeaders headers = new HttpHeaders();
//      headers.add("Access-Control-Allow-Origin", "*");
      return new ResponseEntity<Object>(List.of(listToReturn,counts,timetables), HttpStatus.OK);


//      return new ResponseEntity<Object>(Collections.<ScrappedNews>emptyList(), HttpStatus.NOT_ACCEPTABLE);
      //return newsService.getNewsRepository().findAll().subList(500, 550);
   }


}
