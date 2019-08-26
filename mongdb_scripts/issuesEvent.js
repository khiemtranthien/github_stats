db.getCollection('issuesEvent').find({});

db.getCollection('issuesEvent').createIndex( { "repo.id": 1 } , {background: true});
db.getCollection('issuesEvent').createIndex( { "created_at": -1 } , {background: true});
db.getCollection('issuesEvent').createIndex( { "payload.action": 1 } , {background: true});

db.getCollection('issuesEvent').find({"repo.id": 153640774, "payload.action": {"$ne": "opened"}});
db.getCollection('issuesEvent').find({"repo.id": 153640774, "payload.issue.id": 482578130});

db.getCollection('issuesEvent').aggregate([
{"$match" : {"repo.id" : { $in: [20313056, 153640774] }, "payload.action": {"$eq": "opened"} }},
{
    $group : {
                _id:{'repo': '$repo.id', 'issue': '$payload.issue.id'}, 
                maxDate:{$max: '$created_at'}
         }
},
]);

db.getCollection('issuesEvent').aggregate([
{"$match" : {"repo.id" : { $in: [20313056, 153640774] }, "payload.action": {"$ne": "opened"} }},
{
    $group : {
                _id:{'repo': '$repo.id', 'issue': '$payload.issue.id'}, 
                minDate:{$min: '$created_at'}
         }
},
]);
