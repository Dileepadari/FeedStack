File 1:
```java
package com.sismics.util.jpa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.tool.hbm2ddl.ConnectionHelper;

/**
 * A {@link ConnectionHelper} implementation based on an internally
 * built and managed {@link ConnectionProvider}.
 *
 * @author Steve Ebersole
 */
class ManagedProviderConnectionHelper implements ConnectionHelper {
    private final Properties cfgProperties;
    private StandardServiceRegistryImpl serviceRegistry;
    private Connection connection;

    ManagedProviderConnectionHelper(Properties cfgProperties) {
        this.cfgProperties = cfgProperties;
    }

    @Override
    public void prepare(boolean needsAutoCommit) throws SQLException {
        serviceRegistry = createServiceRegistry(cfgProperties);
        connection = serviceRegistry.getService(ConnectionProvider.class).getConnection();
        if (needsAutoCommit && !connection.getAutoCommit()) {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }

    private static StandardServiceRegistryImpl createServiceRegistry(Properties properties) {
        Environment.verifyProperties(properties);
        ConfigurationHelper.resolvePlaceHolders(properties);
        return (StandardServiceRegistryImpl) new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public void release() throws SQLException {
        try {
            releaseConnection();
        } finally {
            releaseServiceRegistry();
        }
    }

    private void releaseConnection() throws SQLException {
        if (connection != null) {
            try {
                new SqlExceptionHelper().logAndClearWarnings(connection);
            } finally {
                try {
                    serviceRegistry.getService(ConnectionProvider.class).closeConnection(connection);
                } finally {
                    connection = null;
                }
            }
        }
    }

    private void releaseServiceRegistry() {
        if (serviceRegistry != null) {
            try {
                serviceRegistry.destroy();
            } finally {
                serviceRegistry = null;
            }
        }
    }
}
```