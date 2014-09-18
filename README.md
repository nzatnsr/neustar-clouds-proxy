<img src="http://neustarpc.github.com/neustar-clouds/images/logo.png"><br>

This is a prototype of a HTTP proxy server that allows guardians to manage HTTP accesses via browsers
of dependents via XDI graphs.

### How to build

First, [XDI2](http://github.com/projectdanube/xdi2) and [LittleProxy](http://github.com/adamfisk/LittleProxy) need to be build.

After that, just run

    mvn clean install

To build all components.

### How to run

    mvn jetty:run

Then the proxy status page is available at

    http://localhost:10288/proxies/status
