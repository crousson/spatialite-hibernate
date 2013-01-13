package org.sqlite.spatialite.hibernate;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.sqlite.spatialite.io.GeometryBlobReader;
import org.sqlite.spatialite.io.GeometryBlobWriter;

import com.vividsolutions.jts.geom.Geometry;

public class SpatialiteGeometryUserType implements UserType {
	
	private GeometryBlobReader reader;
	private GeometryBlobWriter writer;
	
	public SpatialiteGeometryUserType() {
		this.reader = new GeometryBlobReader();
		this.writer = new GeometryBlobWriter();
	}

	@Override
	public int[] sqlTypes() {
		return new int[] { java.sql.Types.BINARY };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class returnedClass() {
		return Geometry.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == null) {
			return (y == null);
		} else {
			return x.equals(y);
		}
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		
		Geometry geometry;
		byte[] bytes = rs.getBytes(names[0]);
		
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		
		try {
			geometry = reader.read(bytes);
		} catch (IOException e) {
			throw new HibernateException(e);
		}
		
		return geometry;
		
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		
		if (value == null) {
			st.setNull(index, java.sql.Types.BINARY);
		}
		
		if (value instanceof Geometry) {
			
			try {
				
				byte[] bytes = writer.write((Geometry) value);
				st.setBytes(index, bytes);
				
			} catch (IOException e) {
				throw new HibernateException(e);
			}
			
		} else {
			
			throw new HibernateException(
				new IllegalArgumentException());
	
		}
		
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		if (value instanceof Geometry) {
			Geometry clone = ((Geometry) value).getFactory().createGeometry((Geometry) value);
			clone.setSRID(((Geometry) value).getSRID());
			// TODO handle cloneable user data
			clone.setUserData(((Geometry) value).getUserData());
			return clone;
		} else {
			throw new HibernateException(
				new IllegalArgumentException());
		}
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) deepCopy(value);
	}

	@Override
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return deepCopy(cached);
	}

	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return deepCopy(original);
	}

}
