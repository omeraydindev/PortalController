const express = require('express');
const { createServer } = require('http');
const WebSocket = require('ws');
const url = require('url');

const PORT = 8080;
const app = express();

const server = createServer(app);
const wss = new WebSocket.Server({ server: server });


const verify = (ws, req) => {
    const query = url.parse(req.url, true).query;

    if (query.token) {
        console.log('server: accepted: %s', query.token);
        ws.token = query.token;
    } else {
        console.log('server: rejected client (no token)');
    }

    return query.token;
};

wss.on('connection', (ws, req) => {
    if (!verify(ws, req)) {
        ws.terminate();
        return;
    }
    console.log('server: connected: %s', ws.token);

    ws.on('message', data => {
        wss.clients.forEach(client => {
            if (client !== ws && client.readyState === WebSocket.OPEN) {
                client.send(data);
            }
        });

        console.log('server: %s received packet length: %s', ws.token, data.length);
        if (data.includes("pixels")) {
            console.log('server:    pixels');
        } else if (data.includes("screen")) {
            console.log('server:    screen img');
        }
    });

    ws.on('close', () => {
        console.log('server: disconnected: %s', ws.token);
    });
});

app.get('/hosts', (_req, res) => {
    var tokens = [];

    wss.clients.forEach(client => {
        tokens.push(client.token);
    });

    res.send(JSON.stringify(tokens));
});

server.listen(process.env.PORT || PORT, () => { });
