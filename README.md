# jFastCGI

jFastCGI is the implementation of the FastCGI Protocol with the Java language. 

The project gets build using Apache Maven 3 and is split into two main modules:

## Server

This module contains code originally published on http://www.fastcgi.com/ to 
allow simple Java Programs to expose their functionality to a FastCGI client. 

In our code its main purpose is for Unit Testing, but you can of course use it
to build your own applications that want to expose a FastCGI Interface. 


## Client

This module contains code to _access_ a FastCGI Service. Usually those services
are written in other languages. A common example for a FastCGI Service is a PHP binary
exposed through FastCGI. 

The client module contains some submodules to ease your every day work:

### Client "CORE"


This module contains the base code used to access a FastCGI Service. It abstracts
the Request (including all headers), allows the request to be sent to the service
and a Response to be received. 


### Client Servlet


This module contains code to directly bridge a Java Servlet to a FastCGI Service,
so that the FastCGI Service can be accessed through the Servlet. 


### Client Portlet


This module contains code to directly bridge a FastCGI Service into a Portlet, 
so the FastCGI Service can be integrated into a Portlet Container. 


### Client Spring


This module contains code to allow integration with the Spring Framework.




## Credits

Credits go out to Julien Rialland who created the base version of the jFastCGI Project on 
Sourceforge: http://sourceforge.net/projects/jfastcgi/ 
I imported the source code from sourceforge using the git svn command. The last commit on the
sourceforge project was in April 2012, I tagged it as "v2.2_ref69_sourceforge". 


## License

Most of the code from Julien is BSD Licensed. The code from fastcgi.com is custom licensed.
I'm trying to get into contact with the fastcgi.com guys and Julian to get the whole codebase Apache 2
licensed.

