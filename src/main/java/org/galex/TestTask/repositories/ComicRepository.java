package org.galex.TestTask.repositories;

import org.galex.TestTask.models.ComicEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComicRepository extends MongoRepository<ComicEntity, String> {
    Page<ComicEntity> findById(String id, Pageable pageable);
    Optional<ComicEntity> findById(String id);
    Page<ComicEntity> findByTitleStartsWith(String title, Pageable pageable);
    Page<ComicEntity> findByTitleStartsWithAndTitle(String start, String title, Pageable pageable);
    Page<ComicEntity> findByCharacters(String charId, Pageable pageable);
}
