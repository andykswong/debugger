import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestJUnitCoverage {
	TestObject test;
	
	@Before
	public void setUp() throws Exception {
		test = new TestObject();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRandomSleep() throws Exception {
		long result = test.randomSleep();
		assertTrue(result >= 500 && result < 1000);
	}
	
	@Test
	public void testHello() {
		test.hello(0);
		test.hello(1);
		
		// Oops, didn't test the else part
		//test.hello(-1);
	}
	
	@Test
	public void testMain() {		
		TestObject.main(new String[] {});		
		TestObject.main(new String[] {"zzz"});
	}

}
