var redis = require('redis')
var client = redis.createClient()

client.on('error', function(err) {
	//never mind the error, just create a new client
	client = redis.createClient()
})

module.exports = function(req, res) {
	res.set('Content-Type', 'application/json')
	client.get(req.query['prefix'], function(err, data) {
		if (data) {
			res.end(JSON.stringify(data.split('\n')))
		} else {
			res.end(JSON.stringify([]))
		}
	})
}
