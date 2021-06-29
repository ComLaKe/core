wrk.method = 'POST'
wrk.path = '/find'
wrk.headers['Content-Type'] = 'application/json'
wrk.body = '["==", [".", ["$"], "cid"], "Qm"]'
