package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasOAuth20AuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuth20Configuration;
import org.apereo.cas.config.CasOAuth20ThrottleConfiguration;
import org.apereo.cas.config.CasOAuth20WebflowConfiguration;
import org.apereo.cas.oidc.config.OidcComponentSerializationConfiguration;
import org.apereo.cas.oidc.config.OidcConfiguration;
import org.apereo.cas.oidc.config.OidcThrottleConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    ThymeleafAutoConfiguration.class,
    OidcConfiguration.class,
    OidcComponentSerializationConfiguration.class,
    OidcThrottleConfiguration.class,
    CasOAuth20Configuration.class,
    CasOAuth20AuthenticationServiceSelectionStrategyConfiguration.class,
    CasOAuth20ThrottleConfiguration.class,
    CasOAuth20WebflowConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
public class OidcWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
    }
}
