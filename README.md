EasySocket
==========

Build socket based applications easily.


Hello Fxxk World!!


``` java

		SocketIO socket = new SocketIO("http://127.0.0.1:3001/");
		socket.connect(new IOCallback() {
			@Override
			public void onMessage(JSONObject json, IOAcknowledge ack) {
				try {
					System.out.println("Server said:" + json.toString(2));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onMessage(String data, IOAcknowledge ack) {
				System.out.println("Server said: " + data);
			}

			@Override
			public void onError(SocketIOException socketIOException) {
				System.out.println("an Error occured");
				socketIOException.printStackTrace();
			}

			@Override
			public void onDisconnect() {
				System.out.println("Connection terminated.");
			}

			@Override
			public void onConnect() {
				System.out.println("Connection established");
			}

			@Override
			public void on(String event, IOAcknowledge ack, Object... args) {
				System.out.println("Server triggered event '" + event + "'");
			}
		});
		
		// This line is cached until the connection is establisched.
		socket.send("Hello Server!");

```
