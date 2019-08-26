db.getCollection('githubEvents').find({});
db.getCollection('githubEvents').count();

db.getCollection('githubEvents').createIndex( { "type": 1, "created_at": -1 } , {background: true});

db.githubEvents.aggregate( [
  {$match: {'type': 'PushEvent'}},
  { $out : "pushEvent" }
] );

db.getCollection('githubEvents').find({
    'type': 'PushEvent'
    });
    
db.getCollection('githubEvents').aggregate([
    {"$match": {'type': 'PushEvent'}},
    {"$group" : {
            _id:{repo: "$repo.id", day: { $dateToString: { format: "%Y-%m-%d", date: "$created_at" } }}, 
            count:{$sum:1}
        }
    }
]);
    
db.getCollection('githubEvents').aggregate([
    
    {"$match": {'type': 'PushEvent'}},
    {
        "$group" : {
                _id:{repo: "$repo.id", day: { $dateToString: { format: "%Y-%m-%d", date: "$created_at" } }}, 
                count:{$sum:1}
         }
    },
    { 
        "$group": {
            _id: {repo: "$_id.repo"},
            "totalCount": { "$sum": "$count" },
            "distinctCount": { "$sum": 1 }
        }
    },
    { $sort : { distinctCount : -1 } }
], {
"allowDiskUse": true
});
