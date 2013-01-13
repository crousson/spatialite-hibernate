package org.sqlite.spatialite.io;

import static org.junit.Assert.*;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryBlobWriterTest {

	@Test
	public void testRead() throws Exception {
		
		WKTReader wktReader = new WKTReader();
		GeometryBlobReader reader = new GeometryBlobReader();
		GeometryBlobWriter writer = new GeometryBlobWriter();
		
		Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 134567.89 0, 178765.89 -1.5654338987, 0 18987654.987, 0 0))");
		polygon.setSRID(4326);
		
		byte[] bytes = writer.write(polygon);
		System.out.println(new String(Hex.encodeHex(bytes, true)));
		
		Polygon re = (Polygon) reader.read(bytes);
		System.out.println(re.toText());
		
	}

}
