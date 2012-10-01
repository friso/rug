exports.index = function(req, res) {
	res.send('Movie search!')
}

exports.search = require('./search.js')
exports.suggest = require('./suggest.js')
