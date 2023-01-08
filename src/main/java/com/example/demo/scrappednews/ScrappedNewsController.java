package com.example.demo.scrappednews;

import org.springframework.beans.factory.annotation.Autowired;
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

   final ArrayList<String> services = new ArrayList<>(Arrays.asList("dgp", "polsatnews", "rmf24", "tvn24", "gazetapl", "wydarzeniainteria"));
   private final ScrappedNewsService newsService;
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

   HashMap<String, TreeMap<LocalDate, Integer>> timetables = new HashMap<>();
   //HashMap<String, ArrayList<String>> textWarnings = new HashMap<>();


   LocalTime dbFetchTime;
   LocalTime nextFetchTime;

   @Autowired
   public ScrappedNewsController(ScrappedNewsService newsService) {
      this.newsService = newsService;
      RefetchAspect.snc = this;
      fetchDataFromRepositories();
      timetables = generateTimeTables(null);
//      generateTextWarnings();
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
//      if (LocalTime.now().isAfter(nextFetchTime)) {
//         System.out.println("It's time for a new fetch.");
//         fetchDataFromRepositories();
//      }
   }

   private HashMap<String, TreeMap<LocalDate, Integer>> generateTimeTables(ArrayList<ScrappedNews> newsPool) {
      dgpTimeTable = generateTimeTable("dgp", newsPool);
      polsatnewsTimeTable = generateTimeTable("polsatnews", newsPool);
      rmf24TimeTable = generateTimeTable("rmf24", newsPool);
      tvn24TimeTable = generateTimeTable("tvn24", newsPool);
      gazetaplTimeTable = generateTimeTable("gazetapl", newsPool);
      wydarzeniainteriaTimeTable = generateTimeTable("wydarzeniainteria", newsPool);

      HashMap<String, TreeMap<LocalDate, Integer>> timetablesToReturn = new HashMap<>();

      timetablesToReturn.put(ScrappedNewsDGP.getStringRepresentation(), dgpTimeTable);
      timetablesToReturn.put(ScrappedNewsPolsatNews.getStringRepresentation(), polsatnewsTimeTable);
      timetablesToReturn.put(ScrappedNewsRMF24.getStringRepresentation(), rmf24TimeTable);
      timetablesToReturn.put(ScrappedNewsTVN24.getStringRepresentation(), tvn24TimeTable);
      timetablesToReturn.put(ScrappedNewsGazetaPl.getStringRepresentation(), gazetaplTimeTable);
      timetablesToReturn.put(ScrappedNewsWydarzeniaInteria.getStringRepresentation(), wydarzeniainteriaTimeTable);

      return timetablesToReturn;
   }

   private TreeMap<LocalDate, Integer> generateTimeTable(String whichOne, ArrayList<ScrappedNews> newsPool) {

      ArrayList<ScrappedNews> chosenNewsPool;
      TreeMap<LocalDate, Integer> currentTimeTable = new TreeMap<LocalDate, Integer>();
      if (newsPool == null) {
         chosenNewsPool = new ArrayList<>();

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
      } else {
         chosenNewsPool = newsPool;
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
      System.out.println(currentTimeTable);
      return currentTimeTable;


   }

   private HashMap<String, ArrayList<String>> generateTextWarningsBasedOnPreviousTimetable (String [] servicesGotten, LocalDate dateFrom, LocalDate dateTo) {
      HashMap<String, ArrayList<String>> textWarnings = new HashMap<>();

      for (String service: servicesGotten) {
         ArrayList<String> warnings = new ArrayList<>();

         TreeMap<LocalDate, Integer> currentTimeTable = timetables.get(service);

         Set<Map.Entry<LocalDate, Integer>> entries = currentTimeTable.entrySet();

         if (dateFrom.isBefore(currentTimeTable.firstKey())) {
            warnings.add("Brak " + service.toUpperCase() + " przed dniem " + currentTimeTable.firstKey() );
         }

         LocalDate currentStartDate = null;
         LocalDate currentEndDate = null;

         int toJump = 0;

         for (Map.Entry<LocalDate, Integer> entry : entries) {
            if ( (entry.getKey().isAfter(dateFrom) || entry.getKey().isEqual(dateFrom) ) && entry.getKey().isBefore(dateTo)) {

               if (toJump > 0) {
                  toJump--;
                  continue;
               }

               if (entry.getValue() <= 2) {
                  currentStartDate = entry.getKey();
                  currentEndDate = currentStartDate.plus(Period.ofDays(1));

                  while (currentTimeTable.containsKey(currentEndDate) && currentTimeTable.get(currentEndDate) <= 2) {
                     currentEndDate = currentEndDate.plus(Period.ofDays(1));
                     toJump++;
                  }
                  if (toJump < 1) {
                     warnings.add("Brak " + service.toUpperCase() + " w dniu " + currentStartDate);
                  } else {
                     warnings.add("Brak " + service.toUpperCase() + " w dniach " + currentStartDate + " -- " + currentEndDate.minus(Period.ofDays(1)));
                  }
               }
            }
         }
         for (String s1 : warnings) {
            System.out.println(s1);
         }
         textWarnings.put(service, warnings);
      }
      return textWarnings;

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
      String[] servicesGotten;


//      if (LocalTime.now().isAfter(nextFetchTime)) {
//         System.out.println("It's time for a new fetch.");
//         fetchDataFromRepositories();
//      }

      if (service == null) {
         return new ResponseEntity<Object>("Service not specified. Available services are " + services.toString(), HttpStatus.NOT_ACCEPTABLE);
      } else {
         servicesGotten = service.split(",");
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

      LocalDateTime date_to = null;
      LocalDateTime date_from = null;

      try {
         if (to != null) {
            to = to + " 00:00:00";
            date_to = LocalDateTime.parse(to, dtf);
            LocalDateTime finalDate_to = date_to;
            newsPool.removeIf((sn -> sn.getDate_published().isAfter(ChronoLocalDateTime.from(finalDate_to))));
         }
         if (from != null) {
            from = from + " 00:00:00";
            date_from = LocalDateTime.parse(from, dtf);
            LocalDateTime finalDate_from = date_from;
            newsPool.removeIf((sn -> sn.getDate_published().isBefore(ChronoLocalDateTime.from(finalDate_from))));
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

      HashMap<String, ArrayList<String>> currentWarnings = new HashMap<>();

      if (date_from == null) {
         date_from =  LocalDateTime.of(2022,8,1,1,0);
      }
      if (date_to == null) {
         date_to = LocalDateTime.now();
      }
      currentWarnings = generateTextWarningsBasedOnPreviousTimetable(servicesGotten, date_from.toLocalDate(), date_to.toLocalDate());


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

      HashMap<String, Integer> counts = new HashMap<>();
      counts.put(ScrappedNewsDGP.getStringRepresentation(), 0);
      counts.put(ScrappedNewsPolsatNews.getStringRepresentation(), 0);
      counts.put(ScrappedNewsGazetaPl.getStringRepresentation(), 0);
      counts.put(ScrappedNewsTVN24.getStringRepresentation(), 0);
      counts.put(ScrappedNewsRMF24.getStringRepresentation(), 0);
      counts.put(ScrappedNewsWydarzeniaInteria.getStringRepresentation(), 0);
      for (Object o : newsPool) {
         if (o.getClass().toString().equals(ScrappedNewsDGP.class.toString())) {
            counts.put(ScrappedNewsDGP.getStringRepresentation(), counts.get(ScrappedNewsDGP.getStringRepresentation()) + 1);
         } else if (o.getClass().toString().equals(ScrappedNewsRMF24.class.toString())) {
            counts.put(ScrappedNewsRMF24.getStringRepresentation(), counts.get(ScrappedNewsRMF24.getStringRepresentation()) + 1);
         } else if (o.getClass().toString().equals(ScrappedNewsTVN24.class.toString())) {
            counts.put(ScrappedNewsTVN24.getStringRepresentation(), counts.get(ScrappedNewsTVN24.getStringRepresentation()) + 1);
         } else if (o.getClass().toString().equals(ScrappedNewsWydarzeniaInteria.class.toString())) {
            counts.put(ScrappedNewsWydarzeniaInteria.getStringRepresentation(), counts.get(ScrappedNewsWydarzeniaInteria.getStringRepresentation()) + 1);
         } else if (o.getClass().toString().equals(ScrappedNewsPolsatNews.class.toString())) {
            counts.put(ScrappedNewsPolsatNews.getStringRepresentation(), counts.get(ScrappedNewsPolsatNews.getStringRepresentation()) + 1);
         } else if (o.getClass().toString().equals(ScrappedNewsGazetaPl.class.toString())) {
            counts.put(ScrappedNewsGazetaPl.getStringRepresentation(), counts.get(ScrappedNewsGazetaPl.getStringRepresentation()) + 1);
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
      return new ResponseEntity<Object>(List.of(listToReturn, counts, currentWarnings), HttpStatus.OK);


//      return new ResponseEntity<Object>(Collections.<ScrappedNews>emptyList(), HttpStatus.NOT_ACCEPTABLE);
      //return newsService.getNewsRepository().findAll().subList(500, 550);
   }


}
