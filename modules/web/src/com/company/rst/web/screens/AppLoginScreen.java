package com.company.rst.web.screens;

import com.company.rst.security.UserSessionExistsException;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.Route;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.security.auth.Credentials;
import com.haulmont.cuba.security.auth.LoginPasswordCredentials;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.app.login.LoginScreen;
import com.haulmont.cuba.web.gui.screen.ScreenDependencyUtils;
import com.vaadin.ui.Dependency;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;


@Route(path = "login", root = true)
@UiController("login")
@UiDescriptor("app-login-screen.xml")
public class AppLoginScreen extends LoginScreen {

    @Inject
    protected HBoxLayout bottomPanel;

    @Inject
    protected Label<String> poweredByLink;
    @Inject
    private Dialogs dialogs;

    @Subscribe
    public void onAppLoginScreenInit(InitEvent event) {
        loadStyles();

        initBottomPanel();
    }

    @Subscribe("submit")
    public void onSubmit(Action.ActionPerformedEvent event) {
        login();
    }

    protected void loadStyles() {
        ScreenDependencyUtils.addScreenDependency(this,
                "vaadin://brand-login-screen/login.css", Dependency.Type.STYLESHEET);
    }

    protected void initBottomPanel() {
        if (!globalConfig.getLocaleSelectVisible()) {
            poweredByLink.setAlignment(Component.Alignment.MIDDLE_CENTER);

            if (!webConfig.getLoginDialogPoweredByLinkVisible()) {
                bottomPanel.setVisible(false);
            }
        }
    }

    @Override
    protected void initLogoImage() {
        logoImage.setSource(RelativePathResource.class)
                .setPath("VAADIN/brand-login-screen/cuba-icon-login.svg");
    }

    @Override
    protected void doLogin(Credentials credentials) throws LoginException {
        String password = null;
        try {
            if (credentials instanceof LoginPasswordCredentials) {
                password = ((LoginPasswordCredentials) credentials).getPassword();
            }
            super.doLogin(credentials);
        } catch (UserSessionExistsException e) {
            if (credentials instanceof LoginPasswordCredentials) {
                String originalPassword = password;
                dialogs.createOptionDialog().withCaption(messages.getMainMessage("dialogs.Confirmation")).withMessage(messages.getMainMessage("abortSession"))
                        .withType(Dialogs.MessageType.CONFIRMATION).withActions(new DialogAction(DialogAction.Type.YES).withHandler(event ->
                                retryLogin((LoginPasswordCredentials) credentials, originalPassword)
                        ),
                        new DialogAction(DialogAction.Type.NO, Action.Status.PRIMARY)).show();
            }
        }
    }

    protected void retryLogin(LoginPasswordCredentials loginPasswordCredentials, String originalPassword) {
        try {
            Map<String, Object> params = loginPasswordCredentials.getParams();
            Map<String, Object> newParams = new HashMap<>();
            newParams.put("abortSession", Boolean.TRUE);
            if (params != null) {
                newParams.putAll(params);
            }
            loginPasswordCredentials.setParams(newParams);
            loginPasswordCredentials.setPassword(originalPassword);

            super.doLogin(loginPasswordCredentials);

        } catch (LoginException e1) {
            showLoginException(e1.getMessage());
        }
    }
}