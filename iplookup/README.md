# com.svds.example
# iplookup
=====================

Purpose
-------
This program processes a server access log and reformats it while also correlating IP location information, the ISP name, and organization of the client IP address who made the request to the server.

Local Database
--------------
The first uses a local file that can be downloaded from http://dev.maxmind.com/geoip/geoip2/geolite2/.
Please download the MaxMind GeoLite2 city database file in order to execute this process.

Unfortunately the city database file only provides the location data, and does not provide any information about the ISP or organization. MaxMind only provides the ISP data after purchasing a license, so was not able to collect this for my output file. However the logic to read the ISP database file was still implemented in case it was available to someone using this program.

Calling A WebService
--------------------
After realizing I could not collect the ISP information, I then created an alternative class: IpInfoGeoLocationService that calls the IpInfo [web-service](http://ipinfo.io). 
From this service we can obtain the following fields: 
* ip-address, if no arguments are provided.
* hostname
* city
* region
* country
* longitude, latitude
* organization
* postal-code

Note: I was still not able to collect the ISP name from this service.

The IpInfo 



Building
--------
This project uses Maven for project management. You should just need to run "mvn package" to build an executable jar.

Running
-------
By default iplookup is setup to:
* read a file named "access.log" from the current directory
* write out to a file named "access_log.out" to the current directory
* use the IpInfo REST service
 
