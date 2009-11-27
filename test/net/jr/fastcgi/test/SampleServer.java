package net.jr.fastcgi.test;

import com.fastcgi.FCGIInterface;

public class SampleServer implements Runnable{

	public static final int DEFAULT_PORT = 59812;
	
	private int port = DEFAULT_PORT;
		
	public void run(){
		int count = 0;
		System.setProperty("FCGI_PORT", ""+port);
		FCGIInterface itf = new FCGIInterface();
		while (itf.FCGIaccept() >= 0) {
			count++;
			System.out.println("Content-type: text/html\n\n");
			System.out.println("<html>");
			System.out
					.println("<head><TITLE>FastCGI-Hello Java stdio</TITLE></head>");
			System.out.println("<body>");
			System.out.println("<H3>FastCGI-HelloJava stdio</H3>");
			System.out.println("request number " + count + " running on host "
					+ System.getProperty("SERVER_NAME"));
			System.out.println("</body>");
			System.out.println("</html>");
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
