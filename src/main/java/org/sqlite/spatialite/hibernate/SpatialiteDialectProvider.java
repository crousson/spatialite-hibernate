package org.sqlite.spatialite.hibernate;

import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.spi.SpatialDialectProvider;

public class SpatialiteDialectProvider implements SpatialDialectProvider {

	@Override
	public SpatialDialect createSpatialDialect(String dialect) {
		if (SpatialiteDialect.class.getName().equals(dialect)) {
			return new SpatialiteDialect();
		} else {
			return null;
		}
	}

	@Override
	public SpatialDialect getDefaultDialect() {
		return new SpatialiteDialect();
	}

	@Override
	public String[] getSupportedDialects() {
		String[] names = new String[1];
		names[0] = SpatialiteDialect.class.getName();
		return names;
	}

}
