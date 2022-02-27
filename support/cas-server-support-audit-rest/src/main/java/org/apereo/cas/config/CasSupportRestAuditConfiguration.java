package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.RestAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSupportRestAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "CasSupportRestAuditConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Audit, module = "rest")
public class CasSupportRestAuditConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "restAuditTrailManager")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditTrailManager restAuditTrailManager(final CasConfigurationProperties casProperties) {
        val rest = casProperties.getAudit().getRest();
        return new RestAuditTrailManager(rest);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "restAuditTrailExecutionPlanConfigurer")
    public AuditTrailExecutionPlanConfigurer restAuditTrailExecutionPlanConfigurer(
        @Qualifier("restAuditTrailManager")
        final AuditTrailManager restAuditTrailManager) {
        return plan -> plan.registerAuditTrailManager(restAuditTrailManager);
    }
}
