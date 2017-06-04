package com.manev.quislisting.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manev.quislisting.domain.QlConfig;
import com.manev.quislisting.domain.QlMenuConfig;
import com.manev.quislisting.domain.QlMenuPosConfig;
import com.manev.quislisting.domain.qlml.Language;
import com.manev.quislisting.domain.taxonomy.discriminator.NavMenu;
import com.manev.quislisting.repository.QlConfigRepository;
import com.manev.quislisting.repository.qlml.LanguageRepository;
import com.manev.quislisting.repository.taxonomy.NavMenuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BaseController {

    private static Logger log = LoggerFactory.getLogger(BaseController.class);

    protected NavMenuRepository navMenuRepository;

    protected QlConfigRepository qlConfigRepository;

    protected LanguageRepository languageRepository;

    protected LocaleResolver localeResolver;

    public BaseController(NavMenuRepository navMenuRepository, QlConfigRepository qlConfigRepository,
                          LanguageRepository languageRepository, LocaleResolver localeResolver) {
        this.navMenuRepository = navMenuRepository;
        this.qlConfigRepository = qlConfigRepository;
        this.languageRepository = languageRepository;
        this.localeResolver = localeResolver;
    }

    @ModelAttribute("baseModel")
    public BaseModel baseModel(HttpServletRequest request) throws IOException {
        BaseModel baseModel = new BaseModel();

        Locale locale = localeResolver.resolveLocale(request);
        String language = locale.getLanguage();
        log.debug("Language from cookie: {}", language);

        QlConfig qlMenuConfigs = qlConfigRepository.findOneByKey("ql-menu-configurations");

        QlMenuConfig qlMenuPosConfig = new ObjectMapper().readValue(qlMenuConfigs.getValue(),
                QlMenuConfig.class);

        QlMenuPosConfig qlMenuPosByLanguageCode = findQlMenuPosByLanguageCode(language, qlMenuPosConfig.getQlMenuPosConfigs());
        if (qlMenuPosByLanguageCode != null) {
            Long topHeaderMenuRefId = qlMenuPosByLanguageCode.getTopHeaderMenuRefId();
            if (topHeaderMenuRefId!=null) {
                NavMenu topHeaderMenu = navMenuRepository.findOne(topHeaderMenuRefId);
                baseModel.setTopHeaderMenus(topHeaderMenu.getNavMenuItems());
            }

            Long footerMenuRefId = qlMenuPosByLanguageCode.getFooterMenuRefId();
            if (footerMenuRefId!=null) {
                NavMenu footerMenu = navMenuRepository.findOne(footerMenuRefId);
                baseModel.setFooterMenus(footerMenu.getNavMenuItems());
            }
        }

        List<Language> allByActive = languageRepository.findAllByActive(true);

        return baseModel
                .activeLanugages(allByActive);
    }

    private QlMenuPosConfig findQlMenuPosByLanguageCode(String languageCode, List<QlMenuPosConfig> qlMenuPosConfigs) {
        for (QlMenuPosConfig qlMenuPosConfig : qlMenuPosConfigs) {
            if (qlMenuPosConfig.getLanguageCode().equals(languageCode)) {
                return qlMenuPosConfig;
            }
        }
        return null;
    }

}
