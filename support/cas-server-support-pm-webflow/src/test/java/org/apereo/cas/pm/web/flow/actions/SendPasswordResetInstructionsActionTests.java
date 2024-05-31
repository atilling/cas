package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SendPasswordResetInstructionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnabledIfListeningOnPort(port = 25000)
@Tag("Mail")
class SendPasswordResetInstructionsActionTests {

    @TestConfiguration(value = "PasswordManagementTestConfiguration", proxyBeanMethods = false)
    static class PasswordManagementTestConfiguration {
        @Bean
        @Autowired
        public PasswordManagementService passwordChangeService() throws Throwable {
            val service = mock(PasswordManagementService.class);
            when(service.createToken(any())).thenReturn(null);
            when(service.findUsername(any())).thenReturn("casuser");
            when(service.findEmail(any())).thenReturn("casuser@example.org");
            return service;
        }
    }

    @Nested
    class DefaultTests extends BasePasswordManagementActionTests {

        @BeforeEach
        public void setup() {
            val request = new MockHttpServletRequest();
            request.setRemoteAddr("223.456.789.000");
            request.setLocalAddr("123.456.789.000");
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));
            ticketRegistry.deleteAll();
        }

        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.setParameter("username", "casuser");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, sendPasswordResetInstructionsAction.execute(context).getId());
            val tickets = ticketRegistry.getTickets();
            assertEquals(1, tickets.size());
            assertInstanceOf(HardTimeoutExpirationPolicy.class, tickets.iterator().next().getExpirationPolicy());
        }

        @Test
        void verifyNoPhoneOrEmail() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter("username", "none");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, sendPasswordResetInstructionsAction.execute(context).getId());
        }

        @Test
        void verifyNoUsername() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, sendPasswordResetInstructionsAction.execute(context).getId());
        }
    }
    
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pm.reset.mail.html=true",
        "cas.authn.pm.reset.mail.text=classpath:/password-reset.html"
    })
    class HtmlEmailTests extends BasePasswordManagementActionTests {
        @Test
        void verifyHtmlEmail() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.setParameter("username", "casuser");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, sendPasswordResetInstructionsAction.execute(context).getId());
            val tickets = ticketRegistry.getTickets();
            assertEquals(1, tickets.size());
            assertInstanceOf(HardTimeoutExpirationPolicy.class, tickets.iterator().next().getExpirationPolicy());
        }
    }

    @Nested
    @SpringBootTest(classes = {
        BasePasswordManagementActionTests.SharedTestConfiguration.class,
        CasPersonDirectoryTestConfiguration.class
    }, properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.authn.pm.core.enabled=true",
        "cas.authn.pm.groovy.location=classpath:PasswordManagementService.groovy",
        "cas.authn.pm.forgot-username.mail.from=cas@example.org",
        "cas.authn.pm.reset.mail.from=cas@example.org",
        "cas.authn.pm.reset.security-questions-enabled=true",
        "cas.authn.pm.reset.number-of-uses=1"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class NoMultifactorRegisteredDevicesTests extends BasePasswordManagementActionTests {
        
        @Test
        @Order(1)
        void verifyActionRequiresMfa() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            context.setParameter("username", "user-without-devices");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_DENY, sendPasswordResetInstructionsAction.execute(context).getId());
        }

        @Test
        @Order(0)
        void verifyActionMultiUse() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter("username", "casuser");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, sendPasswordResetInstructionsAction.execute(context).getId());
            val tickets = ticketRegistry.getTickets();
            assertEquals(1, tickets.size());
            assertInstanceOf(MultiTimeUseOrTimeoutExpirationPolicy.class, tickets.iterator().next().getExpirationPolicy());
        }
    }
    
    @Nested
    @Import(PasswordManagementTestConfiguration.class)
    class WithoutTokens extends BasePasswordManagementActionTests {

        @Test
        void verifyNoLinkAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter("username", "unknown");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, sendPasswordResetInstructionsAction.execute(context).getId());
        }
    }
}
