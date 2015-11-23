# jFastCGI

jFastCGI is the implementation of the FastCGI Protocol with the Java language. 

The project gets build using Apache Maven 3 and is split into two main modules:

[![Build Status](https://travis-ci.org/jFastCGI/jfastcgi.svg)](https://travis-ci.org//jFastCGI/jfastcgi)
[![Coverage Status](https://img.shields.io/coveralls/jFastCGI/jfastcgi.svg)](https://coveralls.io/r/jFastCGI/jfastcgi)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sf.jfastcgi/jfastcgi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sf.jfastcgi/jfastcgi)



![Commits](https://www.openhub.net/p/jfastcgi/analyses/latest/commits_spark.png)
[![Code statistics](http://www.ohloh.net/p/jfastcgi/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/jfastcgi)

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


### Client bundled


This module creates a "bundled" or sometimes called "shaded" version of the project.
The resulting JAR contains all classes from client-core, client-servlet, client-portlet and
client-spring. Its supposed to be used by people that are not using Maven or another dependency
resolver. See [the download site](http://www.jfastcgi.org/download.html) for further details.

## Credits

Credits go out to Julien Rialland who created the [base version](http://sourceforge.net/projects/jfastcgi/)
of the jFastCGI Project on Sourceforge: http://sourceforge.net/projects/jfastcgi/

Dominik Dorn imported the source code from sourceforge using the git svn command and converted it
into a full-blown maven project including submodules, sites, etc.
The last commit (rev69) on the sourceforge project was in April 2012, its tagged as "v2.2_ref69_sourceforge"
in our Git Repository.

## License

The codebase currently has 3 (three) licenses.

* The code originally provided by Julien is BSD licensed (see LICENSE\_JRIALLAND.txt) 
* The code originally provided by the fastcgi.com guys / Open Market, Inc. is also some kind of BSD / public domain license
* New code is licensed with the Apache Software License 2.


