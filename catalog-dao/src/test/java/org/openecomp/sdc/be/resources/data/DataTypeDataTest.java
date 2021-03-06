package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class DataTypeDataTest {

	private DataTypeData createTestSubject() {
		return new DataTypeData();
	}

	@Test
	public void testCtor() throws Exception {
		new DataTypeData(new DataTypeDataDefinition());
		new DataTypeData(new HashMap<>());
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		DataTypeData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetDataTypeDataDefinition() throws Exception {
		DataTypeData testSubject;
		DataTypeDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataTypeDataDefinition();
	}

	
	@Test
	public void testSetDataTypeDataDefinition() throws Exception {
		DataTypeData testSubject;
		DataTypeDataDefinition dataTypeDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDataTypeDataDefinition(dataTypeDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		DataTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		DataTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}
}