Tested on MacOS Monterey version 12.6 (21G115)


To reproduce set up a loopback alias:

`sudo ifconfig lo0 alias 192.168.22.2`

Now the server correctly responds to calls on: 

`curl 127.0.0.1:{port-number}`

But calls to the loopback alias return an error:

```
curl 192.168.22.2:{port-number}

curl: (52) Empty reply from server
```

And the jetty server throws an exception:

`java.io.IOException: Socket is not connected`