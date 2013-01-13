package org.sqlite.spatialite.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import org.hibernatespatial.mgeom.MCoordinate;
import org.hibernatespatial.mgeom.MGeometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKBConstants;

public class GeometryBlobWriter {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(GeometryBlobWriter.class);
	
	public byte[] write(Geometry geometry) throws IOException {
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ByteOrderDataOutputStream dos = new ByteOrderDataOutputStream(os, ByteOrder.nativeOrder());
		
		// 0x00 : START MARKER
		dos.writeByte(GeometryBlobConstants.START);
		
		// 0x01 : BYTE ORDER
		if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) {
			dos.writeByte(GeometryBlobConstants.BIG_ENDIAN);
		} else {
			dos.writeByte(GeometryBlobConstants.LITTLE_ENDIAN);
		}
		
		// 0x02 : SRID, 4 bytes (32-bits integer)
		dos.write(geometry.getSRID());
		
		Envelope envelope = geometry.getEnvelopeInternal();
		
		// 0x06 : MBR minx, 8 bytes (64-bits double value)
		dos.writeDouble(envelope.getMinX());
		
		// 0x14 : MBR miny, 8 bytes (64-bits double value)
		dos.writeDouble(envelope.getMinY());

		// 0x22 : MBR maxx, 8 bytes (64-bits double value)
		dos.writeDouble(envelope.getMaxX());

		// 0x30 : MBR maxy, 8 bytes (64-bits double value)
		dos.writeDouble(envelope.getMaxY());
		
		// 0x38 : END OF MBR MARKER
		dos.writeByte(GeometryBlobConstants.MBR_END);
		
		// 0x39 : GEOMETRY TYPE, 4 bytes (32-bits integer)
		int geometryType = getGeometryType(geometry);
		dos.write(geometryType);
		
		// 0x43 : geometry data
		writeGeometry(dos, geometry);
		
		// END OF RECORD MARKER
		dos.writeByte(GeometryBlobConstants.LAST);
		
		return os.toByteArray();
		
	}

	private void writeGeometry(ByteOrderDataOutputStream dos, Geometry geometry) throws IOException {
		
		if (geometry instanceof Point) {
			writePoint(dos, (Point) geometry);
		} else if (geometry  instanceof LineString) {
			writeLineString(dos, (LineString) geometry);
		} else if (geometry instanceof Polygon) {
			writePolygon(dos, (Polygon) geometry);
		} else if (geometry instanceof MultiPoint) {
			writeMultiPoint(dos, (MultiPoint) geometry);
		} else if (geometry instanceof MultiLineString) {
			writeMultiLineString(dos, (MultiLineString) geometry);
		} else if (geometry instanceof MultiPolygon) {
			writeMultiPolygon(dos, (MultiPolygon) geometry);
		} else if (geometry instanceof GeometryCollection) {
			writeGeometryCollection(dos, (GeometryCollection) geometry);
		} else {
			throw new IllegalArgumentException("Unknown geometry type : " + geometry.getGeometryType());
		}
		
	}

	private void writeGeometryCollection(ByteOrderDataOutputStream dos,
			GeometryCollection geometry) throws IOException {
		
		writeComponents(dos, geometry);
		
	}

	private void writeMultiPolygon(ByteOrderDataOutputStream dos,
			MultiPolygon geometry) throws IOException {
		
		writeComponents(dos, geometry);
		
	}

	private void writeMultiLineString(ByteOrderDataOutputStream dos,
			MultiLineString geometry) throws IOException {
		
		writeComponents(dos, geometry);
		
	}

	private void writeMultiPoint(ByteOrderDataOutputStream dos,
			MultiPoint geometry) throws IOException {
		
		writeComponents(dos, geometry);
		
	}

	private void writePolygon(ByteOrderDataOutputStream dos, Polygon geometry) throws IOException {
		
		int numberOfHoles = geometry.getNumInteriorRing();
		dos.write(numberOfHoles + 1);
		
		writeLineString(dos, geometry.getExteriorRing());
		
		for (int i=0; i<numberOfHoles; i++) {
			writeLineString(dos, geometry.getInteriorRingN(i));
		}
		
	}

	private void writeLineString(ByteOrderDataOutputStream dos,
			LineString geometry) throws IOException {
		
		int numberOfPoints = geometry.getNumPoints();
		dos.write(numberOfPoints);
		
		int dimension = geometry.getDimension();
		
		for (int i=0; i<numberOfPoints; i++) {
			Coordinate coordinate = geometry.getCoordinateN(i);
			writeCoordinate(dos, coordinate, dimension);
		}
		
	}
	
	private void writeComponents(ByteOrderDataOutputStream dos,
			Geometry geometry) throws IOException {
		
		int numberOfComponents = geometry.getNumGeometries();
		dos.write(numberOfComponents);
		
		for (int i=0; i<numberOfComponents; i++) {
			Geometry component = geometry.getGeometryN(i);
			dos.writeByte(GeometryBlobConstants.GEOMETRY_ENTITY);
			int geometryType = getComponentType(component);
			dos.write(geometryType);
			writeGeometry(dos, component);
		}
		
	}
	
	private void writePoint(ByteOrderDataOutputStream dos, Point geometry) throws IOException {
		
		Coordinate coordinate = geometry.getCoordinate();
		writeCoordinate(dos, coordinate, geometry.getDimension());
		
	}

	private void writeCoordinate(ByteOrderDataOutputStream dos, Coordinate coordinate, int dimension) throws IOException {
		
		dos.writeDouble(coordinate.x);
		dos.writeDouble(coordinate.y);
		
		if (dimension == 3) {
			dos.writeDouble(coordinate.z);
		}
		
		if (coordinate instanceof MCoordinate) {
			dos.writeDouble(((MCoordinate) coordinate).m);
		}
		
	}
	
	private int getComponentType(Geometry geometry) {
		
		int geometryType;
		
		if (geometry instanceof Point) {
			geometryType = WKBConstants.wkbPoint;
		} else if (geometry  instanceof LineString) {
			geometryType = WKBConstants.wkbLineString;
		} else if (geometry instanceof Polygon) {
			geometryType = WKBConstants.wkbPolygon;
		} else {
			throw new IllegalArgumentException("Illegal component type : " + geometry.getGeometryType());
		}
		
		return geometryType;
		
	}

	private int getGeometryType(Geometry geometry) {
		
		int geometryType;
		
		if (geometry instanceof Point) {
			geometryType = WKBConstants.wkbPoint;
		} else if (geometry  instanceof LineString) {
			geometryType = WKBConstants.wkbLineString;
		} else if (geometry instanceof Polygon) {
			geometryType = WKBConstants.wkbPolygon;
		} else if (geometry instanceof MultiPoint) {
			geometryType = WKBConstants.wkbMultiPoint;
		} else if (geometry instanceof MultiLineString) {
			geometryType = WKBConstants.wkbMultiLineString;
		} else if (geometry instanceof MultiPolygon) {
			geometryType = WKBConstants.wkbMultiPolygon;
		} else if (geometry instanceof GeometryCollection) {
			geometryType = WKBConstants.wkbGeometryCollection;
		} else {
			throw new IllegalArgumentException("Unknown geometry type : " + geometry.getGeometryType());
		}
		
		if (geometry.getDimension() == 3) {
			if (geometry instanceof MGeometry) {
				geometryType += GeometryBlobConstants.ZM_OFFSET;
			} else {
				geometryType += GeometryBlobConstants.Z_OFFSET;
			}
		} else if (geometry instanceof MGeometry) {
			geometryType += GeometryBlobConstants.M_OFFSET;
		}
		
		return geometryType;
		
	}

}
