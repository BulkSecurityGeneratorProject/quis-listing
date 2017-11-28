package com.manev.quislisting.web.mvc;

import com.manev.quislisting.repository.qlml.LanguageRepository;
import com.manev.quislisting.repository.qlml.LanguageTranslationRepository;
import com.manev.quislisting.repository.taxonomy.NavMenuRepository;
import com.manev.quislisting.service.QlConfigService;
import com.manev.quislisting.service.post.StaticPageService;
import org.springframework.context.MessageSource;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.manev.quislisting.security.jwt.JWTFilter.QL_AUTH;

@Controller
@RequestMapping("/sign-out")
public class SignOutController extends BaseController {

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public SignOutController(NavMenuRepository navMenuRepository, QlConfigService qlConfigService,
                             LanguageRepository languageRepository, LocaleResolver localeResolver,
                             LanguageTranslationRepository languageTranslationRepository,
                             StaticPageService staticPageService, MessageSource messageSource) {
        super(navMenuRepository, qlConfigService, languageRepository, languageTranslationRepository, localeResolver,
                staticPageService, messageSource);
    }

    @RequestMapping(method = RequestMethod.GET)
    public void authorize(HttpServletRequest request,
                          HttpServletResponse response, ModelMap model) throws IOException {
        response.addCookie(new Cookie(QL_AUTH, null));
        redirectStrategy.sendRedirect(request, response, "/");
    }

}
