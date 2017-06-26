package com.manev.quislisting.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manev.quislisting.domain.QlConfig;
import com.manev.quislisting.domain.QlMenuConfig;
import com.manev.quislisting.domain.QlMenuPosConfig;
import com.manev.quislisting.domain.post.AbstractPost;
import com.manev.quislisting.domain.qlml.Language;
import com.manev.quislisting.domain.qlml.LanguageTranslation;
import com.manev.quislisting.domain.taxonomy.discriminator.NavMenu;
import com.manev.quislisting.repository.qlml.LanguageRepository;
import com.manev.quislisting.repository.qlml.LanguageTranslationRepository;
import com.manev.quislisting.repository.taxonomy.NavMenuRepository;
import com.manev.quislisting.service.QlConfigService;
import com.manev.quislisting.service.post.AbstractPostService;
import com.manev.quislisting.web.model.ActiveLanguageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaseController {

    private static Logger log = LoggerFactory.getLogger(BaseController.class);

    static final String REDIRECT = "redirect:/";

    protected NavMenuRepository navMenuRepository;

    protected QlConfigService qlConfigService;

    protected LanguageRepository languageRepository;
    protected LocaleResolver localeResolver;
    protected AbstractPostService abstractPostService;
    private LanguageTranslationRepository languageTranslationRepository;

    public BaseController(NavMenuRepository navMenuRepository, QlConfigService qlConfigService,
                          LanguageRepository languageRepository, LanguageTranslationRepository languageTranslationRepository,
                          LocaleResolver localeResolver, AbstractPostService abstractPostService) {
        this.navMenuRepository = navMenuRepository;
        this.qlConfigService = qlConfigService;
        this.languageRepository = languageRepository;
        this.languageTranslationRepository = languageTranslationRepository;
        this.localeResolver = localeResolver;
        this.abstractPostService = abstractPostService;
    }

    @ModelAttribute("baseModel")
    public BaseModel baseModel(HttpServletRequest request) throws IOException {
        BaseModel baseModel = new BaseModel();

        Locale locale = localeResolver.resolveLocale(request);
        String language = locale.getLanguage();
        log.debug("Language from cookie: {}", language);

        QlConfig qlMenuConfigs = qlConfigService.findOneByKey("ql-menu-configurations");

        QlMenuConfig qlMenuPosConfig = new ObjectMapper().readValue(qlMenuConfigs.getValue(),
                QlMenuConfig.class);

        QlMenuPosConfig qlMenuPosByLanguageCode = findQlMenuPosByLanguageCode(language, qlMenuPosConfig.getQlMenuPosConfigs());
        if (qlMenuPosByLanguageCode != null) {
            Long topHeaderMenuRefId = qlMenuPosByLanguageCode.getTopHeaderMenuRefId();
            if (topHeaderMenuRefId != null) {
                NavMenu topHeaderMenu = navMenuRepository.findOne(topHeaderMenuRefId);
                baseModel.setTopHeaderMenus(topHeaderMenu.getNavMenuItems());
            }

            Long footerMenuRefId = qlMenuPosByLanguageCode.getFooterMenuRefId();
            if (footerMenuRefId != null) {
                NavMenu footerMenu = navMenuRepository.findOne(footerMenuRefId);
                baseModel.setFooterMenus(footerMenu.getNavMenuItems());
            }
        }

        List<Language> activeLanguages = languageRepository.findAllByActive(true);

        if (!language.equals("en")) {
            // needs translation
            // find translations for active languages
            List<LanguageTranslation> languageTranslations = languageTranslationRepository.
                    findAllByLanguageCodeInAndDisplayLanguageCode(getLanguageCodes(activeLanguages), language);
            List<ActiveLanguageBean> activeLanguageBeans = makeActiveLanguagesForTranslations(activeLanguages, languageTranslations);
            baseModel.activeLanugages(activeLanguageBeans);
        } else {
            baseModel.setActiveLanguages(makeActiveLanguageBeansNoTranslation(activeLanguages));
        }

        QlConfig accountProfilePageConfig = qlConfigService.findOneByKey("account-profile-page-id");

        baseModel.setProfilePage(abstractPostService.retrievePost(language, accountProfilePageConfig.getValue()));

        return baseModel;
    }

    private List<ActiveLanguageBean> makeActiveLanguageBeansNoTranslation(List<Language> activeLanguages) {
        List<ActiveLanguageBean> activeLanguageBeans = new ArrayList<>();

        for (Language activeLanguage : activeLanguages) {
            ActiveLanguageBean activeLanguageBean = new ActiveLanguageBean();
            activeLanguageBean.setLanguage(activeLanguage);
            activeLanguageBeans.add(activeLanguageBean);
        }
        return activeLanguageBeans;
    }

    private List<ActiveLanguageBean> makeActiveLanguagesForTranslations(List<Language> activeLanguages, List<LanguageTranslation> languageTranslations) {
        List<ActiveLanguageBean> activeLanguageBeans = new ArrayList<>();

        for (Language activeLanguage : activeLanguages) {
            for (LanguageTranslation languageTranslation : languageTranslations) {
                if (activeLanguage.getCode().equals(languageTranslation.getLanguageCode())) {
                    ActiveLanguageBean activeLanguageBean = new ActiveLanguageBean();
                    activeLanguageBean.setLanguage(activeLanguage);
                    activeLanguageBean.setLanguageTranslation(languageTranslation);
                    activeLanguageBeans.add(activeLanguageBean);
                }
            }
        }

        return activeLanguageBeans;
    }

    private List<String> getLanguageCodes(List<Language> languages) {
        List<String> result = new ArrayList<>();

        for (Language language : languages) {
            result.add(language.getCode());
        }

        return result;
    }

    private QlMenuPosConfig findQlMenuPosByLanguageCode(String languageCode, List<QlMenuPosConfig> qlMenuPosConfigs) {
        for (QlMenuPosConfig qlMenuPosConfig : qlMenuPosConfigs) {
            if (qlMenuPosConfig.getLanguageCode().equals(languageCode)) {
                return qlMenuPosConfig;
            }
        }
        return null;
    }

    protected String redirectToPageNotFound() throws UnsupportedEncodingException {
        QlConfig notFoundPageConfig = qlConfigService.findOneByKey("not-found-page-id");
        AbstractPost notFoundPage = abstractPostService.findOne(Long.valueOf(notFoundPageConfig.getValue()));
        return REDIRECT + URLEncoder.encode(notFoundPage.getName(), "UTF-8");
    }

}
