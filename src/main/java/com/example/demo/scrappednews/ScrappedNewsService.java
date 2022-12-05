package com.example.demo.scrappednews;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableMongoRepositories
public class ScrappedNewsService {

   @Autowired
   ScrappedNewsDGPRepository DGPNewsRepository;
   @Autowired
   ScrappedNewsPolsatNewsRepository polsatNewsRepository;
   @Autowired
   ScrappedNewsRMF24Repository rmf24Repository;
   @Autowired
   ScrappedNewsTVN24Repository tvn24Repository;
   @Autowired
   ScrappedNewsGazetaPlRepository gazetaPlRepository;
   @Autowired
   ScrappedNewsWydarzeniaInteriaRepository wydarzeniaInteriaRepository;


   public ScrappedNewsDGPRepository getDGPNewsRepository() {
      return DGPNewsRepository;
   }

   public ScrappedNewsPolsatNewsRepository getPolsatNewsRepository() {
      return polsatNewsRepository;
   }

   public ScrappedNewsRMF24Repository getRmf24Repository() {
      return rmf24Repository;
   }

   public ScrappedNewsTVN24Repository getTVN24Repository () {
      return tvn24Repository;
   }

   public ScrappedNewsGazetaPlRepository getGazetaPlRepository () {
      return gazetaPlRepository;
   }
   public ScrappedNewsWydarzeniaInteriaRepository getWydarzeniaInteriaRepository() {
      return wydarzeniaInteriaRepository;
   }



}
