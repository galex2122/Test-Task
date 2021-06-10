package org.galex.TestTask.repositories;

import org.galex.TestTask.models.CharacterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterRepository extends MongoRepository<CharacterEntity, String> {
    Page<CharacterEntity> findById(String id, Pageable pageable);

    Optional<CharacterEntity> findById(String id);

    Page<CharacterEntity> findByNameStartsWith(String nameStart, Pageable pageable);

    Page<CharacterEntity> findByNameStartsWithAndName(String nameStart, String name, Pageable pageable);

    Page<CharacterEntity> findByComics(String id, Pageable pageable);
}
