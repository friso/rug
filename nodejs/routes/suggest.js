var redis = require('redis')
var client = redis.createClient()

client.on('error', function(err) {
	//never mind the error, just create a new client
	console.warn('Could not connect to Redis')
})

module.exports = function(req, res) {
	if (!client.connected) {
		res.status(500)
		res.end('Not connected to Redis')
	} else {
		res.set('Content-Type', 'application/json')
		client.get(req.query['prefix'], function(err, data) {
			if (data) {
				res.end(JSON.stringify(data.split('\n')))
			} else {
				res.end(JSON.stringify([]))
			}
		})
	}
}
