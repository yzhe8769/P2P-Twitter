import static org.junit.Assert.*;

import java.io.*;

import org.junit.Before;
import org.junit.Test;

public class P2PTwitterTest {


	@Test
	public void test() {
		P2PTwitter.main(new String[]{"abcd1234"});
		final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(myOut));
		String string = "hi";
		InputStream stringStream = new ByteArrayInputStream(string.getBytes());
		System.setIn(stringStream);
		// test stuff here...

		final String standardOutput = myOut.toString();
		assertEquals(standardOutput, "Status: ### P2P tweets ###\n#");
		System.out.println(standardOutput);
	}

}
