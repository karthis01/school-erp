package com.schoolerp.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;

/**
 * A DataSource whose every connection request is routed, per-request, to whichever
 * school's pool TenantContext currently points at.
 *
 * We deliberately do NOT rely on AbstractRoutingDataSource's built-in
 * "resolvedDataSources" map (that map is fixed at startup, so newly-registered schools
 * would never be picked up without a restart). Instead determineTargetDataSource() is
 * overridden to ask TenantDataSourceProvider directly, which lazily creates/caches pools.
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final TenantDataSourceProvider provider;

    public TenantRoutingDataSource(TenantDataSourceProvider provider) {
        this.provider = provider;
        // Required to be non-null by AbstractRoutingDataSource, but unused since we
        // override determineTargetDataSource() below.
        setTargetDataSources(new HashMap<>());
        afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentSchool();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String schoolCode = TenantContext.getCurrentSchool();
        if (schoolCode == null) {
            throw new IllegalStateException(
                    "No school/tenant context is set on this thread - every tenant-scoped " +
                            "request must set TenantContext before touching the database.");
        }
        return provider.getDataSource(schoolCode);
    }
}
