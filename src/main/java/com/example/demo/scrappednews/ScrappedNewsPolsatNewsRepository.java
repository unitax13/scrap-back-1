package com.example.demo.scrappednews;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrappedNewsPolsatNewsRepository extends  MongoRepository<ScrappedNewsPolsatNews, String>{
}
