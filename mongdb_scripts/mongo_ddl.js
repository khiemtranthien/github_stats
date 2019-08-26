db.getCollection('pushEvent').createIndex( { "created_at": -1 } , {background: true});
db.getCollection('pushEvent').createIndex( { "repo.id": 1 } , {background: true});

db.getCollection('releaseEvent').createIndex( { "created_at": -1 } , {background: true});
db.getCollection('releaseEvent').createIndex( { "repo.id": 1 } , {background: true});

db.getCollection('forkEvent').createIndex( { "repo.id": 1 } , {background: true});
db.getCollection('forkEvent').createIndex( { "created_at": -1 } , {background: true});

db.getCollection('issuesEvent').createIndex( { "repo.id": 1 } , {background: true});
db.getCollection('issuesEvent').createIndex( { "created_at": -1 } , {background: true});
db.getCollection('issuesEvent').createIndex( { "payload.action": 1 } , {background: true});
