package com.manev.quislisting.web.rest.post;

import com.manev.quislisting.service.post.DlListingService;
import com.manev.quislisting.service.post.dto.DlListingDTO;
import com.manev.quislisting.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.manev.quislisting.web.rest.Constants.RESOURCE_API_PUBLIC_DL_LISTINGS;

@RestController
@RequestMapping(RESOURCE_API_PUBLIC_DL_LISTINGS)
public class DlListingPublicResource {

    private final Logger log = LoggerFactory.getLogger(DlListingPublicResource.class);

    private final LocaleResolver localeResolver;
    private final DlListingService dlListingService;

    public DlListingPublicResource(LocaleResolver localeResolver, DlListingService dlListingService) {
        this.localeResolver = localeResolver;
        this.dlListingService = dlListingService;
    }

    @GetMapping
    public ResponseEntity<List<DlListingDTO>> getAllListings(Pageable pageable,
                                                             @RequestParam Map<String, String> allRequestParams,
                                                             HttpServletRequest request) {
        log.debug("REST request to get a page of DlListingDTO");

        Locale locale = localeResolver.resolveLocale(request);
        String language = locale.getLanguage();
        log.debug("Language from cookie: {}", language);
        Page<DlListingDTO> page = dlListingService.findAllForFrontPage(pageable, language);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, RESOURCE_API_PUBLIC_DL_LISTINGS);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
