package com.example.demo.scrappednews;

        import org.springframework.data.mongodb.repository.MongoRepository;
        import org.springframework.stereotype.Repository;

@Repository
public interface ScrappedNewsGazetaPlRepository extends MongoRepository<ScrappedNewsGazetaPl, String> {
}
