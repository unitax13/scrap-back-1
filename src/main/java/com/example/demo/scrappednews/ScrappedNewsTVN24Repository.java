package com.example.demo.scrappednews;

        import org.springframework.data.mongodb.repository.MongoRepository;
        import org.springframework.stereotype.Repository;

@Repository
public interface ScrappedNewsTVN24Repository extends MongoRepository<ScrappedNewsTVN24, String> {
}
