package com.manev.quislisting.service.qlml;

import com.manev.quislisting.domain.qlml.Language;
import com.manev.quislisting.repository.qlml.LanguageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service Implementation for managing Language.
 */
@Service
@Transactional
public class LanguageService {

    private final Logger log = LoggerFactory.getLogger(LanguageService.class);

    private final LanguageRepository languageRepository;

    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    /**
     * Save a language.
     *
     * @param language the entity to save
     * @return the persisted entity
     */
    public Language save(Language language) {
        log.debug("Request to save Language : {}", language);
        Language result = languageRepository.save(language);
        return result;
    }

    /**
     * Get all the languages.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Language> findAll(Pageable pageable, Map<String, String> allRequestParams) {
        log.debug("Request to get all Languages");

        String active = allRequestParams.get("active");

        if (active != null) {
            return languageRepository.findAllByActive(pageable, Boolean.valueOf(active));
        } else {
            return languageRepository.findAll(pageable);
        }
    }

    /**
     * Get one language by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Language findOne(Long id) {
        log.debug("Request to get Language : {}", id);
        Language language = languageRepository.findOne(id);
        return language;
    }

    /**
     * Delete the  language by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Language : {}", id);
        languageRepository.delete(id);
    }

}