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
@RequestMapping(value = "/v1/public/characters")
@AllArgsConstructor
public class CharacterController {

    private final CharacterRepository characterRepository;
    private final ComicRepository comicRepository;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Page<CharacterEntity>> findCharacters(Pageable pageable,
                                                                @RequestParam("name") Optional<String> name,
                                                                @RequestParam("nameStartsWith") Optional<String> start) {

        return name.map(n -> new ResponseEntity<>(characterRepository
                        .findByNameStartsWithAndName(start.orElse(""), n, pageable), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(characterRepository
                        .findByNameStartsWith(start.orElse(""), pageable), HttpStatus.OK));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Page<CharacterEntity>> findCharacterById(Pageable pageable,
                                                                   @PathVariable(value = "id") String id) {
        return new ResponseEntity<>(characterRepository.findById(id, pageable), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/comics", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Page<ComicEntity>> findCharacterComics(Pageable pageable,
                                                                 @PathVariable(value = "id") String id) {
        return new ResponseEntity<>(comicRepository.findByCharacters(id, pageable), HttpStatus.OK);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> addCharacter(@RequestBody @Valid CharacterEntity character) {
        if (!characterRepository.existsById(character.getId())) {
            if (!Objects.isNull(character.getComics())) {
                character.getComics().removeIf(id -> !comicRepository.existsById(id));
            } else {
                character.setComics(new ArrayList<>());
            }
            for (String comicId :
                    character.getComics()) {
                ComicEntity comic = comicRepository.findById(comicId).get();
                if (!comic.getCharacters().contains(character.getId())) {
                    comic.getCharacters().add(character.getId());
                    comicRepository.save(comic);
                }
            }
            character.setResourceURI("http://localhost:8080/v1/public/characters/" + character.getId());
            characterRepository.save(character);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> updateCharacter(@RequestBody @Valid CharacterEntity character) {
        if (characterRepository.existsById(character.getId())) {
            if (!Objects.isNull(character.getComics())) {
                character.getComics().removeIf(id -> !comicRepository.existsById(id));
            } else {
                character.setComics(new ArrayList<>());
            }
            ArrayList<String> bufComicsIds = new ArrayList<>(characterRepository.findById(character.getId()).get().getComics());
            bufComicsIds.removeAll(character.getComics());
            for (String comicId :
                    bufComicsIds) {
                ComicEntity comic = comicRepository.findById(comicId).get();
                comic.getCharacters().remove(character.getId());
                comicRepository.save(comic);
            }
            bufComicsIds = new ArrayList<>(character.getComics());
            bufComicsIds.removeAll(characterRepository.findById(character.getId()).get().getComics());
            for (String comicId :
                    bufComicsIds) {
                ComicEntity comic = comicRepository.findById(comicId).get();
                if (!comic.getCharacters().contains(character.getId())) {
                    comic.getCharacters().add(character.getId());
                    comicRepository.save(comic);
                }
            }
            character.setResourceURI(characterRepository.findById(character.getId()).get().getResourceURI());
            characterRepository.save(character);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
