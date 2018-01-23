package com.manev.quislisting.web.rest.admin;

import com.manev.quislisting.service.taxonomy.DlLocationService;
import com.manev.quislisting.service.taxonomy.dto.ActiveLanguageDTO;
import com.manev.quislisting.service.taxonomy.dto.DlLocationDTO;
import com.manev.quislisting.web.rest.util.HeaderUtil;
import com.manev.quislisting.web.rest.util.PaginationUtil;
import com.manev.quislisting.web.rest.util.ResponseUtil;
import com.manev.quislisting.web.rest.vm.BindDlTermTaxonomyVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

@RestController
public class DlLocationAdminRest {

    private static final String ENTITY_NAME = "DlLocation";

    private final Logger log = LoggerFactory.getLogger(DlLocationAdminRest.class);
    private final DlLocationService dlLocationService;

    public DlLocationAdminRest(DlLocationService dlLocationService) {
        this.dlLocationService = dlLocationService;
    }

    @PostMapping(AdminRestRouter.DlLocation.LIST)
    public ResponseEntity<DlLocationDTO> createDlLocation(@RequestBody DlLocationDTO dlLocationDTO) throws URISyntaxException {
        log.debug("REST request to save DlLocationDTO : {}", dlLocationDTO);
        if (dlLocationDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new entity cannot already have an ID")).body(null);
        }

        DlLocationDTO result = dlLocationService.save(dlLocationDTO);
        return ResponseEntity.created(new URI(AdminRestRouter.DlLocation.LIST + String.format("/%s", result.getId())))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping(AdminRestRouter.DlLocation.LIST)
    public ResponseEntity<DlLocationDTO> updateDlLocation(@RequestBody DlLocationDTO dlLocationDTO) throws URISyntaxException {
        log.debug("REST request to update DlLocationDTO : {}", dlLocationDTO);
        if (dlLocationDTO.getId() == null) {
            return createDlLocation(dlLocationDTO);
        }
        DlLocationDTO result = dlLocationService.save(dlLocationDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, dlLocationDTO.getId().toString()))
                .body(result);
    }

    @GetMapping(AdminRestRouter.DlLocation.LIST)
    public ResponseEntity<List<DlLocationDTO>> getAllDlLocations(Pageable pageable, @RequestParam Map<String, String> allRequestParams) {
        log.debug("REST request to get a page of DlLocationDTO");
        Page<DlLocationDTO> page = dlLocationService.findAll(pageable, allRequestParams);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, AdminRestRouter.DlLocation.LIST);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping(AdminRestRouter.DlLocation.DETAIL)
    public ResponseEntity<DlLocationDTO> getDlLocation(@PathVariable Long id) {
        log.debug("REST request to get DlLocationDTO : {}", id);
        DlLocationDTO dlLocationDTO = dlLocationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dlLocationDTO));
    }

    @GetMapping(AdminRestRouter.DlLocation.DETAIL_BY_TRANSLATION)
    public ResponseEntity<DlLocationDTO> getDlCategoryByTranslationId(@PathVariable Long id) {
        log.debug("REST request to get DlCategoryDTO by translation id : {}", id);
        DlLocationDTO dlLocationDTO = dlLocationService.findOneByTranslationId(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dlLocationDTO));
    }

    @PostMapping(AdminRestRouter.DlLocation.BIND_LOCATIONS)
    public ResponseEntity<Void> bindDlCategories(@RequestBody BindDlTermTaxonomyVM bindDlTermTaxonomyVM) {
        dlLocationService.bindDlLocations(bindDlTermTaxonomyVM.getSourceId(), bindDlTermTaxonomyVM.getTargetId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(AdminRestRouter.DlLocation.DETAIL)
    public ResponseEntity<Void> deleteDlLocation(@PathVariable Long id) {
        log.debug("REST request to delete DlLocationDTO : {}", id);
        dlLocationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping(AdminRestRouter.DlLocation.ACTIVE_LANGUAGES)
    public List<ActiveLanguageDTO> getActiveLanguages() {
        log.debug("REST request to retrieve active languages for dlCategories : {}");
        return dlLocationService.findAllActiveLanguages();
    }

}
