var http = require('http')

module.exports = function(req, res) {
	if (!req.query['q']) {
		res.status(400).end('Missing request parameter: q')
		return
	}
	
	var terms = req.query['q']
	var responseData = ''
	var ecRequest = http.request( {
		host: 'localhost',
		port: 9200,
		path: '/test/movie/_search',
		method: 'POST'
	},
	function(ecResponse) {
		ecResponse.on('data', function(d) {
			responseData += d
		})
		ecResponse.on('end', function() {
			res.set('Content-Type', 'application/json')
			var searchResult = JSON.parse(responseData)['hits']['hits']
			searchResult = searchResult.map(function(x) {
				return {
					id: x["_id"],
					title: x["_source"]["title"]
				}
			})
			res.end(JSON.stringify(searchResult))
		})
	})
	
	ecRequestObject = {
		"query": {
			"query_string": {
				"query": req.query['q']
			}
		}
	}
	
	ecRequest.write(JSON.stringify(ecRequestObject))
	ecRequest.end()
}
