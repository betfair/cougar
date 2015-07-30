---
layout: default
---
# Cougar Socket Client Session Recycling

The capacity on Cougar servers, may need to be ramped up in order to support new clients, sometimes on a short notice.
Since the cougar clients maintain a list of available server endpoints and load balance across them, we need a mechanism
by which the they can automatically discover the new nodes and start directing some of the load to them. This is mainly
to avoid any manual intervention to update the config and restart all the client nodes, which can be particularly
problematic when there are hundreds of clients across the estate.

The solution is to perform a periodic resolution of the configured endpoints into a set of server hosts and create new
sessions to any new nodes that have been added or close sessions to nodes that have been removed. The resolution can be
done either via DNS or using a local config file, by setting a cougar property.

For DNS based resolution, a single DNS entry is configured with a list of IP addresses of all the server nodes, for each
service and the cougar client is configured with this endpoint. When cougar client is initialised, it will resolve this
address into the list of server hosts and establish sessions to each of them.

When FILE based resolution is used, the mappings are loaded from a file on the local filesystem. The default location of
the file is /etc/bf-cougar/endpoints.dat. The format of the file is going to be similar to


        # APP1 binary endpoints
        app1.yourdomain.com=10.10.10.10,10.10.10.11,10.10.10.12
        # APP2 binary endpoints
        app2.yourdomain.com=20.20.20.20,20.20.20.21,20.20.20.22
        # APP3 binary endpoints
        app3.yourdomain.com=30.30.30.30,30.30.30.31,30.30.30.32
        # APP4 binary endpoints
        app4.yourdomain.com=40.40.40.40,40.40.40.41,40.40.40.42


So when ever there is a change in topology, you will need push the updated file to all the nodes and the cougar clients
would be able to pick up the changes within a few minutes. Ideally you would also be distributing this file on a periodic
basis, to all the nodes in the datacentre.

The address resolution can also be triggered on the cougar client via JMX manually on mbean
`CoUGAR.socket.transport.client:name=socketSessionRecycler,instance=XXX`.

Relevant cougar properties with their defaults are as below.

        # Duration after which the endpoints are re-resolved and socket sessions are recycled if needed
        cougar.client.socket.session.recycle.interval=1800000
        # Mode of resolving server endpoints into host addresses
        # Valid values are DNS or FILE
        cougar.client.socket.address.resolver=DNS
        # Location of the config file to be used for address resolution
        # Is only applicable if
        # cougar.client.socket.address.resolver=FILE
        # By default set to /etc/cougar.hosts
        cougar.client.socket.address.resolver.config.file=/etc/bf-cougar/cougar.hosts
