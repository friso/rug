var express = require('express')
var routes = require('./routes')
var fs = require('fs')

var app = express()

// Configuration
app.configure(function() {
})

// serve static content
app.use(express.static(__dirname + '/public'))

//logging
var logfile = fs.createWriteStream(__dirname + '/logs/access.log', { flags: 'a', encoding: 'utf-8'})
express.logger.token('query', function(req, res) { return req.query['q'] ? req.query['q'] : '-' })
express.logger.token('isotime', function(req, res) { return new Date().toISOString() })
app.use(express.logger({
	buffer: true,
	format: ':remote-addr\t:isotime\t:method :url HTTP/:http-version\t:status\t:query',
	stream: logfile
}))

//error handler
app.use(function(err, req, res, next) {
	console.error(err)
	res.send(500, 'Something went wrong!')
})


//Routing
app.get('/', routes.index) //main page
app.get('/search', routes.search) //search results
app.get('/suggest', routes.suggest) //autcompletion suggestions


//GO!
app.listen(3000)
console.log('Linstening @ 3000...')
