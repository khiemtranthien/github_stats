db.getCollection('issuesEvent').find({});

db.getCollection('issuesEvent').createIndex( { "repo.id": 1 } , {background: true});