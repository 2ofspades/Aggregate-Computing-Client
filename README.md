# **Aggregate Computing Client**

The goal of this project is the development of an Application that use [Aggregate Computing Backend](https://github.com/matteozattoni/Aggregate-Computing-Backend) for sending message using a Peer to Peer Network, when each Peer can send and forward message to each other. This application is designed for those scenarios where Internet is down, for example in case of natural disasters or wars.

We suggest reading the work done in [Aggregate Computing Backend](https://github.com/matteozattoni/Aggregate-Computing-Backend) if you want understanding how the Backend works, and reading this [paper](https://jakebeal.github.io/Publications/QUANTICOL16-AggregateProgramming.pdf) that explains the concept underneath the Aggregate Programming.

This client has the responsibility to manage the data storage, show them to the user, using a network framework and execute the aggregate program.
The network framework can be Wi-Fi Aware or BLE, as explained in the Backend, the framework has not dependencies on the backend part.
The aggregate program has those responsibilities:
* know which devices are online
* fetch the information about the device
* send messages to other devices
* know the distance (hop) to each device
