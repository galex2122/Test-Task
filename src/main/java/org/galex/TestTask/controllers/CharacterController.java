package org.galex.TestTask.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "Characters", description = "Interacting with characters")
@RestController
@RequestMapping(value = "/v1/public/characters")
@AllArgsConstructor
public class CharacterController {

    private final CharacterRepository characterRepository;
    private final ComicRepository comicRepository;

    @Operation(
            summary = "Search characters",
            description = "Returns page of characters which can be sorted and filtered"
    )
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiResponse(
            responseCode = "200",
            description = "Returned page of characters, may be empty"
    )
    public ResponseEntity<Page<CharacterEntity>> findCharacters(Pageable pageable,
                                                                @RequestParam("name") Optional<String> name,
                                                                @RequestParam("nameStartsWith") Optional<String> start) {

        return name.map(n -> new ResponseEntity<>(characterRepository
                .findByNameStartsWithAndName(start.orElse(""), n, pageable), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(characterRepository
                        .findByNameStartsWith(start.orElse(""), pageable), HttpStatus.OK));
    }

    @Operation(
            summary = "Search character by id",
            description = "Returns page with character that contains provided id if it exists, otherwise returns a blank page"
    )
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiResponse(
            responseCode = "200",
            description = "Returned page with character, may be empty"
    )
    public ResponseEntity<Page<CharacterEntity>> findCharacterById(Pageable pageable,
                                                                   @PathVariable(value = "id") String id) {
        return new ResponseEntity<>(characterRepository.findById(id, pageable), HttpStatus.OK);
    }

    @Operation(
            summary = "Search comics with this character",
            description = "Returns page of comics, whose ids are present in character. Page can be sorted and filtered"
    )
    @RequestMapping(value = "/{id}/comics", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiResponse(
            responseCode = "200",
            description = "Returned page with comics, may be empty"
    )
    public ResponseEntity<Page<ComicEntity>> findCharacterComics(Pageable pageable,
                                                                 @PathVariable(value = "id") String id) {
        return new ResponseEntity<>(comicRepository.findByCharacters(id, pageable), HttpStatus.OK);
    }

    @Operation(
            summary = "Add new character",
            description = "Allows adding new character entity to database"
    )
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Character created"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Character with same id already exists"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Provided entity not valid"
            )
    }
    )
    public ResponseEntity<CharacterEntity> addCharacter(@RequestBody @Valid CharacterEntity character,
                                                        HttpServletRequest httpServletRequest) {
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
            character.setResourceURI(httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName()
                    + ":" + httpServletRequest.getServerPort() + "/v1/public/characters/" + character.getId());
            characterRepository.save(character);
            return new ResponseEntity<>(character, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @Operation(
            summary = "Update existing character",
            description = "Allows to update already existing in database character entity"
    )
    @RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Character updated"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Character with provided id not found"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Provided entity not valid"
            )
    }
    )
    public ResponseEntity<CharacterEntity> updateCharacter(@RequestBody @Valid CharacterEntity character) {
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
            return new ResponseEntity<>(character, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
