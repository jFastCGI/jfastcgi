package net.jr.fastcgi;

import java.net.Socket;

public interface ConnectionFactory {

	public Socket getConnection();
	
	public void releaseConnection(Socket socket);
}
