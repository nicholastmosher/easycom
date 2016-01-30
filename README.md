# Easycom

Universal hobby project remote control app.

Easycom, originally TEC-COMM, aims to simplify the way Android users interact
with remote projects. It's target audience is hobbyists and hackers who want an
easy interface for remotely controlling their creations.

# Easycom Core

The simplicity of Easycom comes from the core package, which is separately
located at the [easycom-core](https://github.com/nicholastmosher/easycom-core)
repository. This package provides a unifying interface for transmitting data
over bluetooth and tcp/ip, with an extensible, inheritance-based framework
which makes it easy to add support for other interfaces and protocols.

# The Easycom Vision

When I began developing Easycom, I wanted to build a tool to fill a niche I'd
identified in the robotics and DIY electronics world. I wanted to build a
hacker's go-to toolbox app for controlling projects with support for enough
protocols to make it a viable solution for most small projects.

## Possibilities

### Terminal

With the standardized input and output streams provided by easycom-core,
viewing and manipulating data becomes a matter of harnessing the UI. Simple
interactions with data can be provided by a terminal-type user interface
which, if implemented gracefully, could be made to display the data in
multiple representations such as ascii, hex, and binary.

### Configurable Controls

Implementing a dynamically reconfigurable UI could lead to very interesting
possibilities involving how users interact with their data and devices. By
providing a pallette of UI elements and a canvas to arrange them on,
we can give users full control over their interface. Joysticks could be
configured to send streams of data referring to their status, which could be
interpreted by the user's project (such as an Arduino), or indicators could
display information regarding internal statuses for debugging.
