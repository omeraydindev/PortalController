# PortalController
A TeamViewer-like app for Android-to-Android remote control, using node.js and websockets (ws).

### Some insight
The reason I call it rudimentary is because it's very naively implemented -- in the sense that it doesn't use any fancy compressing algorithm to boost the streaming speed. It literally compares the current frame to the last frame, if the *change in pixels* is lower than the threshold (e.g 30_000) it sends those pixels in a String compressed with GZIP. Otherwise, it compresses the whole frame using JPEG -> Base64 then compresses that too with GZIP and sends that. Still though, streaming speed kind of sucks.

### Some background
This project was put together in a very short time for a contest and therefore has a lot of room to improve. Such as using **WebRTC** instead of WebSockets, implementing a proper way to make the streaming smooth, et cetera. Needless to say, contributions are very welcome.
