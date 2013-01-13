package org.sqlite.spatialite.hibernate;

import org.hibernate.usertype.UserType;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.SpatialFunction;
import org.sqlite.hibernate.SQLiteDialect;

public class SpatialiteDialect extends SQLiteDialect implements SpatialDialect {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7754004939098030818L;

	@Override
	public String getSpatialRelateSQL(String columnName, int spatialRelation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSpatialFilterExpression(String columnName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserType getGeometryUserType() {
		return new SpatialiteGeometryUserType();
	}

	@Override
	public String getSpatialAggregateSQL(String columnName, int aggregation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDWithinSQL(String columnName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHavingSridSQL(String columnName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIsEmptySQL(String columnName, boolean isEmpty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDbGeometryTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTwoPhaseFiltering() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsFiltering() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supports(SpatialFunction function) {
		// TODO Auto-generated method stub
		return false;
	}

}
