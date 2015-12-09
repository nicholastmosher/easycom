# TEC-COMM
Universal hobby project remote control app.

TEC-COMM is an Android open-source project whose goal is to provide a unifying
interface for communicating with electronic projects.  It offers a wide range of
connection options, including reconfigurable Bluetooth and TCP/IP.  Data can be
represented as String, Hex, or Binary, and arranged into pre-established
commands that can be easily repeated or tweaked.

TEC-COMM has several unique features:

## Communications options

TEC-COMM brings an object-oriented approach to the problem of expanding
communications options.  Any network interface available to android that can
stream bytes is compatible with the custom Connections framework built into
TEC-COMM.  By identifying operations common to all or most communications on a
conceptual level, all Connections in code may be abstracted to an
application-frienly programming interface.  These common operations are:

 * Connecting
 * Disconnecting
 * Sending Data
 * Receiving Data

In the code, network interfaces are made compatible by writing a subclass of
the "Connection" class.  Each subclass provides the implementations necessary
for establishing connections and transferring data, while complying with the
API defined by the Connection superclass.  Due to this, Connections of differing
types (e.g. Bluetooth and TCP/IP) need only be treated separately in code during
construction, after which they may be treated identically.

## Multithreading

As new Connections are established, they are assigned TransferManagers, which
exist in the ConnectionService and are in charge of asynchronously sending and
receiving data.  A TransferManager is destroyed when its corresponding
Connection is disconnected.

If this seems like a project that you would be interested in contributing to,
feel free to contact me at nicholastmosher@gmail.com
