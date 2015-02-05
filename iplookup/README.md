# com.svds.example
# iplookup
=====================

Purpose
-------
This program processes a server access log and reformats it while also correlating IP location information, the ISP name, and organization of the client IP address who made the request to the server.

The layout of the new file is:
* date & time request was processed by the web server as epoch (from the file)
* uri user click on (from the file)
* referer (from the file)
* ip address (from the file)
* organization (from the ip address)
* latitude (from the ip address)
* longitude (from the ip address)
* isp name (from the ip address)

Local Database
--------------
My first attempt was to try using a local database file. You can download the file from http://dev.maxmind.com/geoip/geoip2/geolite2/.
Please download the MaxMind GeoLite2 city database file in order to execute this process.

Unfortunately the city database file only provides the location data, and does not provide any information about the ISP or organization. MaxMind only provides the ISP data after purchasing a license, so I was not able to collect this for my output file. However the logic to read the ISP database file was still implemented in case it was available to someone using this program.

NOTE: because the ISP file is likely not available, iplookup will graciously just ignore trying to read that file without throwing an error. However iplookup will abort with an error if the City database file cannot be found.

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

The [IpInfo API documentation](https://ipinfo.io/developers) states that a caller is restricted to just 1,000 requests per day. To avoid exceeding this limit a Sqlite database is used to store the results from every service call, and then checked before the next service call.  

Building
--------
This project uses Maven for project management. You should just need to run "mvn package" at the iplookup folder to build an executable jar.
For testing and simple validation, a sample log file is saved at "test/resources/sample.log".

Running
-------
Before running the complete log file can be downloaded from: https://github.com/silicon-valley-data-science/datasets/blob/master/access.log 

By default iplookup is setup to:
* read a file named "access.log" from the current directory
* write out to a file named "access_log.out" to the current directory
* use the IpInfo REST service
 
However it is possible to override these defaults. The following options are supported:
* --database to identify which database file. If using MaxMind this is the directory where the GeoLite2-City.mmdb and GeoLite2-Files.mmdb are. If using the IpInfo service this is the file location where the Sqlite database is. If nothing specified the convention is to use the current directory.

* --input the path to the input file to read. If nothing is specified the convention is to read a file named "access.log" in the current directory.
* --output the path to write the output file to. If nothing is specified the convention is to write to a file named "access_log.out" in the current directory.
* --json tells the program to write out the records in JSON format. This option is mutually exclusive with the --pipe option.
* --pipe tells the program to write out the records in a pipe-delimited format. This format has performance gains over the JSON format.
* --maxmind tells the program to use the MaxMind local database
* --ipinfo tells the program to call the IpInfo REST service. This option is mutually exclusive with the --maxmind option. It also is the default if nothing is specified.

Usage
-----
	iptool -cp...  com.svds.example.accesslog.Main --input /tmp/catalina.out --output ~/mycopy.log --pipe --maxmind
In this example:
* The class that is executed is com.svds.example.accesslog.Main. 
* The input file is found at "/tmp/catalina.out". 
* The output file will be written to "~/mycopy.log".
* The output file will be written as a pipe-delimited file.
* The program will use the MaxMind local database file. 


