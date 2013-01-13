package org.sqlite.spatialite.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.ConnectionProviderFactory;

public class SpatialiteConnectionProvider implements ConnectionProvider {
	
	private ConnectionProvider delegate;
	private String lib;

	@Override
	public void configure(Properties props) throws HibernateException {
		
		lib = props.getProperty("spatialite.lib");
		
		Properties delegateProps = new Properties();
		delegateProps.putAll(props);
		delegateProps.put("hibernate.connection.enable_load_extension", "true");
		delegateProps.remove(Environment.CONNECTION_PROVIDER);
		delegateProps.remove("spatialite.lib");
		
		delegate = ConnectionProviderFactory.newConnectionProvider(delegateProps);
		
	}

	@Override
	public Connection getConnection() throws SQLException {
		
		Connection conn = delegate.getConnection();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT load_extension('" + lib + "')");
		stmt.close();
		
		return conn;
		
	}

	@Override
	public void closeConnection(Connection conn) throws SQLException {
		delegate.closeConnection(conn);
	}

	@Override
	public void close() throws HibernateException {
		delegate.close();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return delegate.supportsAggressiveRelease();
	}

}
