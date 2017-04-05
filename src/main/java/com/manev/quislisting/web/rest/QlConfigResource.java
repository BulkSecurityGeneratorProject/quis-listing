package com.manev.quislisting.web.rest;

import com.manev.quislisting.service.QlConfigService;
import com.manev.quislisting.service.dto.QlConfigDTO;
import com.manev.quislisting.web.rest.util.HeaderUtil;
import com.manev.quislisting.web.rest.util.PaginationUtil;
import com.manev.quislisting.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static com.manev.quislisting.web.rest.Constants.RESOURCE_API_ADMIN_QL_CONFIG;

/**
 * Created by Стефан on 05.04.2017.
 */
@RestController
@RequestMapping(RESOURCE_API_ADMIN_QL_CONFIG)
public class QlConfigResource {

    private static final String ENTITY_NAME = "QlConfig";

    private final Logger log = LoggerFactory.getLogger(QlConfigResource.class);
    private final QlConfigService qlConfigService;

    public QlConfigResource(QlConfigService qlConfigService) {
        this.qlConfigService = qlConfigService;
    }

    @RequestMapping(method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QlConfigDTO> createQlConfig(@RequestBody QlConfigDTO qlConfigDTO) throws URISyntaxException {
        log.debug("REST request to save QlConfigDTO : {}", qlConfigDTO);
        if (qlConfigDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new entity cannot already have an ID")).body(null);
        }

        QlConfigDTO result = qlConfigService.save(qlConfigDTO);
        return ResponseEntity.created(new URI(RESOURCE_API_ADMIN_QL_CONFIG + "/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping
    public ResponseEntity<QlConfigDTO> updateQlConfig(@RequestBody QlConfigDTO qlConfigDTO) throws URISyntaxException{
        log.debug("REST request to update QLConfigDTO : {}", qlConfigDTO);
        if(qlConfigDTO.getId() == null){
            return createQlConfig(qlConfigDTO);
        }
        QlConfigDTO result = qlConfigService.save(qlConfigDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, qlConfigDTO.getId().toString()))
                .body(result);

    }

    @GetMapping
    public ResponseEntity<List<QlConfigDTO>> getAllQlConfigs(Pageable pageable) throws URISyntaxException{
        log.debug("REST request to get a page of QlConfigDTO");
        Page<QlConfigDTO> page = qlConfigService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, RESOURCE_API_ADMIN_QL_CONFIG);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QlConfigDTO> getQlConfig(@PathVariable Long id) {
        log.debug("REST request to get QlConfigDTO : {}", id);
        QlConfigDTO qlConfigDTO = qlConfigService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(qlConfigDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQlConfig(@PathVariable Long id){
        log.debug("REST request to delete QlConfigDTO : {}", id);
        qlConfigService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME,id.toString())).build();
    }

}
