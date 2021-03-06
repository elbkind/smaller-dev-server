(function() {
  var __socket = new WebSocket(location.href.replace(/https?:\/\/([^/]+)\/.*/, 'ws://$1/live-reload'), 'live-reload');
  __socket.onmessage = function(e) {
    var message = JSON.parse(e.data);
    if (message.kind === 'reload') {
      console.log('reloading window', e);
      location.reload();
    } else if (message.kind === 'change') {
      if (message.data.messages.length > 0) {
        var node = document.createElement("div");
        var msgs = [];
        msgs.push('<ul style="background-color: lightyellow;border: 2px solid gold;padding: 5px;font-weight:bold;">');
        for (var idx in message.data.messages) {
          msgs.push('<li>' + message.data.messages[idx] + '</li>');
        }
        msgs.push('</ul>');
        node.innerHTML = msgs.join('');
        var body = document.querySelector('body');
        body.insertBefore(node, body.childNodes[0]);
      } else if (message.data.fullReload) {
        location.reload();
      } else {
        if (message.data.js) {
          var node = document.querySelector('script[src^="' + message.data.js + '"]');
          if (node) {
            node.parentNode.removeChild(node);
            var script = document.createElement("script");
            script.setAttribute('src', message.data.js);
            document.body.appendChild(script);
          }
        }
        if (message.data.css) {
          var node = document.querySelector('link[href^="' + message.data.css + '"]');
          node && node.setAttribute('href', node.getAttribute('href'));
        }
      }
    } else if (message.kind === 'ping') {
      __socket.send('pong');
    }
  }
})();
