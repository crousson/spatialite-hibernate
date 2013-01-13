package org.sqlite.spatialite.io;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sqlite.SQLiteConfig;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

public class SpatiaLiteTest {

	Connection conn;
	GeometryBlobReader reader = new GeometryBlobReader();
	GeometryBlobWriter writer = new GeometryBlobWriter();

	@Before
	public void setUp() throws Exception {

		Class.forName("org.sqlite.JDBC");

		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);

		conn = DriverManager.getConnection("jdbc:sqlite:target/spatialite.sample", config.toProperties());

		Statement stmt = conn.createStatement();
		stmt.setQueryTimeout(30); // set timeout to 30 sec.

		// loading SpatiaLite
		stmt.execute("SELECT load_extension('/usr/local/lib/libspatialite.so')");
		stmt.execute("SELECT InitSpatialMetadata()");

		stmt.close();

	}

	@Test
	public void testPoint() throws Exception {

		conn.setAutoCommit(false);

		Statement stmt = conn.createStatement();
		
		String sql = "DROP TABLE IF EXISTS test_pt";
		stmt.execute(sql);

		// creating a POINT table
		sql = "CREATE TABLE test_pt (";
		sql += "id INTEGER NOT NULL PRIMARY KEY,";
		sql += "name TEXT NOT NULL)";
		stmt.execute(sql);

		// creating a POINT Geometry column
		sql = "SELECT AddGeometryColumn('test_pt', ";
		sql += "'geom', 4326, 'POINT', 'XY')";
		stmt.execute(sql);

		for (int i = 0; i < 1000; i++)
		{
			// for POINTs we'll use full text sql statements
			sql = "INSERT INTO test_pt (id, name, geom) VALUES (";
			sql += i + 1;
			sql += ", 'test POINT #";
			sql += i + 1;
			sql += "', GeomFromText('POINT(";
			sql += i / 1000.0;
			sql += " ";
			sql += i / 1000.0;
			sql += ")', 4326))";
			stmt.executeUpdate(sql);
		}
		conn.commit();

		sql = "SELECT id, name, geom FROM test_pt";
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			byte[] blob = (byte[]) rs.getObject(3);
			Geometry geometry = reader.read(blob);
			
			assertTrue(geometry instanceof Point);
			
			byte[] bytes = writer.write(geometry);
			
			assertArrayEquals(blob, bytes);
		}

		rs.close();
		conn.commit();
		stmt.close();
	}

	@Test
	public void testPolygon() throws Exception {

		Statement stmt = conn.createStatement();

		String sql = "DROP TABLE IF EXISTS test_poly";
		stmt.execute(sql);

		// creating a POINT table
		sql = "CREATE TABLE test_poly (";
		sql += "id INTEGER NOT NULL PRIMARY KEY)";
		stmt.execute(sql);

		// creating a POINT Geometry column
		sql = "SELECT AddGeometryColumn('test_poly', ";
		sql += "'geometry', 4326, 'POLYGON', 'XY')";
		stmt.execute(sql);

		stmt.close();

		WKTReader wktReader = new WKTReader();
		PreparedStatement ins_stmt;

		sql = "INSERT INTO test_poly (id, geometry) ";
		sql += "VALUES (?, GeomFromText(?, 4326))";
		ins_stmt = conn.prepareStatement(sql);
		conn.setAutoCommit(false);

		for (int i = 0; i < 1000; i++) {
			String geom = "POLYGON((";
			geom += -10.0 - (i / 1000.0);
			geom += " ";
			geom += -10.0 - (i / 1000.0);
			geom += ", ";
			geom += 10.0 + (i / 1000.0);
			geom += " ";
			geom += -10.0 - (i / 1000.0);
			geom += ", ";
			geom += 10.0 + (i / 1000.0);
			geom += " ";
			geom += 10.0 + (i / 1000.0);
			geom += ", ";
			geom += -10.0 - (i / 1000.0);
			geom += " ";
			geom += 10.0 + (i / 1000.0);
			geom += ", ";
			geom += -10.0 - (i / 1000.0);
			geom += " ";
			geom += -10.0 - (i / 1000.0);
			geom += "))";

			Geometry geometry = wktReader.read(geom);

			ins_stmt.setInt(1, i);
			ins_stmt.setString(2, geometry.toText());
			ins_stmt.executeUpdate();
		}

		conn.commit();
		ins_stmt.close();
		
		sql = "SELECT id, geometry FROM test_poly";
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			byte[] blob = (byte[]) rs.getObject(2);
			Geometry geometry = reader.read(blob);
			assertTrue(geometry instanceof Polygon);
			
			byte[] bytes = writer.write(geometry);
			assertArrayEquals(blob, bytes);
		}

		rs.close();
		conn.commit();
		stmt.close();

	}

}
