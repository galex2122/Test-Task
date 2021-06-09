package org.galex.TestTask.controllers;

import lombok.AllArgsConstructor;
import org.galex.TestTask.models.CharacterEntity;
import org.galex.TestTask.models.ComicEntity;
import org.galex.TestTask.repositories.CharacterRepository;
import org.galex.TestTask.repositories.ComicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(value = "/v1/public/comics")
@AllArgsConstructor
public class ComicController {

    private final ComicRepository comicRepository;
    private final CharacterRepository characterRepository;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Page<ComicEntity>> findComics(Pageable pageable,
                                                        @RequestParam("title") Optional<String> title,
                                                        @RequestParam("titleStartsWith") Optional<String> start) {

        return title.map(t -> new ResponseEntity<>(comicRepository
                        .findByTitleStartsWithAndTitle(start.orElse(""), t, pageable), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(comicRepository
                        .findByTitleStartsWith(start.orElse(""), pageable), HttpStatus.OK));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Page<ComicEntity>> findComicById(Pageable pageable,
                                                           @PathVariable(value = "id") String id) {
        return new ResponseEntity<>(comicRepository.findById(id, pageable), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/characters", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Page<CharacterEntity>> findComicCharacters(Pageable pageable,
                                                                     @PathVariable(value = "id") String id) {
        return new ResponseEntity<>(characterRepository.findByComics(id, pageable), HttpStatus.OK);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<ComicEntity> addComic(@RequestBody @Valid ComicEntity comic) {
        if (!comicRepository.existsById(comic.getId())) {
            if (!Objects.isNull(comic.getCharacters())) {
                comic.getCharacters().removeIf(id -> !characterRepository.existsById(id));
            } else {
                comic.setCharacters(new ArrayList<>());
            }
            for (String charId :
                    comic.getCharacters()) {
                CharacterEntity character = characterRepository.findById(charId).get();
                if (!character.getComics().contains(comic.getId())) {
                    character.getComics().add(comic.getId());
                    characterRepository.save(character);
                }
            }
            comic.setResourceURI("http://localhost:8080/v1/public/comics/" + comic.getId());
            comicRepository.save(comic);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> updateComic(@RequestBody @Valid ComicEntity comic) {
        if (comicRepository.existsById(comic.getId())) {
            if (!Objects.isNull(comic.getCharacters())) {
                comic.getCharacters().removeIf(id -> !characterRepository.existsById(id));
            } else {
                comic.setCharacters(new ArrayList<>());
            }
            ArrayList<String> bufCharsIds = new ArrayList<>(comicRepository.findById(comic.getId()).get().getCharacters());
            bufCharsIds.removeAll(comic.getCharacters());
            for (String charId :
                    bufCharsIds) {
                CharacterEntity character = characterRepository.findById(charId).get();
                character.getComics().remove(comic.getId());
                characterRepository.save(character);
            }
            bufCharsIds = new ArrayList<>(comic.getCharacters());
            bufCharsIds.removeAll(comicRepository.findById(comic.getId()).get().getCharacters());
            for (String charId :
                    bufCharsIds) {
                CharacterEntity character = characterRepository.findById(charId).get();
                if (!character.getComics().contains(comic.getId())) {
                    character.getComics().add(comic.getId());
                    characterRepository.save(character);
                }
            }
            comic.setResourceURI(comicRepository.findById(comic.getId()).get().getResourceURI());
            comicRepository.save(comic);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
