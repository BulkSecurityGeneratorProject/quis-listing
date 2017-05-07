package com.manev.quislisting.service.qlml;

import com.manev.quislisting.domain.qlml.QlString;
import com.manev.quislisting.domain.qlml.StringTranslation;
import com.manev.quislisting.repository.qlml.QlStringRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Created by adri on 4/10/2017.
 */
@Service
public class QlStringService {

    private final Logger log = LoggerFactory.getLogger(QlStringService.class);

    private final QlStringRepository qlStringRepository;

    public QlStringService(QlStringRepository qlStringRepository) {
        this.qlStringRepository = qlStringRepository;
    }

    public QlString save(QlString qlString) {
        log.debug("Request to save QlString : {}", qlString);
        Set<StringTranslation> stringTranslations = qlString.getStringTranslation();
        for (StringTranslation stringTranslation : stringTranslations) {
            stringTranslation.setQlString(qlString);
            stringTranslation.setTranslationDate(ZonedDateTime.now());
        }
        QlString result = qlStringRepository.save(qlString);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<QlString> findAll(Pageable pageable, Map<String, String> allRequestParams) {
        log.debug("Request to get all strings");
        Page<QlString> result = qlStringRepository.findAll(pageable);
        return result;
    }

    @Transactional(readOnly = true)
    public QlString findOne(Long id) {
        log.debug("Request to get one string : {}", id);
        QlString qlString = qlStringRepository.findOne(id);
        return qlString;
    }

}
