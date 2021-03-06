package com.manev.quislisting.web.rest.admin;

import com.manev.quislisting.domain.qlml.Language;
import com.manev.quislisting.service.qlml.LanguageService;
import com.manev.quislisting.web.rest.util.HeaderUtil;
import com.manev.quislisting.web.rest.util.PaginationUtil;
import com.manev.quislisting.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing Language.
 */
@RestController
public class LanguageAdminRest {

    private static final String ENTITY_NAME = "language";
    private final Logger log = LoggerFactory.getLogger(LanguageAdminRest.class);
    private final LanguageService languageService;

    public LanguageAdminRest(LanguageService languageService) {
        this.languageService = languageService;
    }

    /**
     * POST  /languages : Create a new language.
     *
     * @param language the language to create
     * @return the ResponseEntity with status 201 (Created) and with body the new language, or with status 400 (Bad Request) if the language has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping(AdminRestRouter.Language.LIST)
    public ResponseEntity<Language> createLanguage(@RequestBody Language language) throws URISyntaxException {
        log.debug("REST request to save Language : {}", language);
        if (language.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new language cannot already have an ID")).body(null);
        }
        Language result = languageService.save(language);
        return ResponseEntity.created(new URI("/api/languages/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /languages : Updates an existing language.
     *
     * @param language the language to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated language,
     * or with status 400 (Bad Request) if the language is not valid,
     * or with status 500 (Internal Server Error) if the language couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping(AdminRestRouter.Language.LIST)
    public ResponseEntity<Language> updateLanguage(@RequestBody Language language) throws URISyntaxException {
        log.debug("REST request to update Language : {}", language);
        if (language.getId() == null) {
            return createLanguage(language);
        }
        Language result = languageService.save(language);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, language.getId().toString()))
                .body(result);
    }

    /**
     * GET  /languages : get all the languages.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of languages in body
     */
    @GetMapping(AdminRestRouter.Language.LIST)
    public ResponseEntity<List<Language>> getAllLanguages(@PageableDefault(value = Integer.MAX_VALUE) Pageable pageable, @RequestParam Map<String, String> allRequestParams) {
        log.debug("REST request to get a page of Languages");
        Page<Language> page = languageService.findAll(pageable, allRequestParams);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, AdminRestRouter.Language.LIST);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /languages/:id : get the "id" language.
     *
     * @param id the id of the language to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the language, or with status 404 (Not Found)
     */
    @GetMapping(AdminRestRouter.Language.DETAIL)
    public ResponseEntity<Language> getLanguage(@PathVariable Long id) {
        log.debug("REST request to get Language : {}", id);
        Language language = languageService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(language));
    }

    /**
     * DELETE  /languages/:id : delete the "id" language.
     *
     * @param id the id of the language to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(AdminRestRouter.Language.DETAIL)
    public ResponseEntity<Void> deleteLanguage(@PathVariable Long id) {
        log.debug("REST request to delete Language : {}", id);
        languageService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
