package com.samjava.damon.frame;

import java.net.InetAddress;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServerSocketFrameTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@Test
	void test() throws Exception {
		InetAddress inet = InetAddress.getLocalHost();
		
		
		ServerSocketFrame socketFrame = new ServerSocketFrame(inet, 13232, "com.samjava.damon.frame.PushWorker");
		socketFrame.setIgnoreIplist(Arrays.asList("182.182.182.141", "182.1822.23.142"));
		socketFrame.loadServerSocketFrame();
	}

}
