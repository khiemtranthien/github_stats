db.getCollection('pushEvent').find({});
db.getCollection('pushEvent').count({});

db.createCollection('pushEvent');

db.getCollection('pushEvent').createIndex( { "created_at": -1 } , {background: true});
db.getCollection('pushEvent').createIndex( { "repo.id": 1 } , {background: true});

db.getCollection('pushEvent').aggregate([
    {
        $group : {
                _id:{'repo': '$repo.id', day: { $dateToString: { format: '%Y-%m-%d', date: '$created_at' } }}, 
                count:{$sum:1}
         }
    },
    { 
        $group: {
            _id: {repo: '$_id.repo'},
            totalCount: { $sum: '$count' },
            distinctCount: { $sum: 1 }
        }
    },
    { $sort : { totalCount : -1 } }
], {
"allowDiskUse": true
});

db.getCollection('pushEvent').find({
    "created_at": {"$gte": ISODate("2019-08-19T00:00:00Z")}, "created_at": {"$lt": ISODate("2019-08-20T00:00:00Z")}
    }, {"repo": 1});
    
db.pushEvent.distinct("repo.id", {
    "created_at": {"$gte": ISODate("2019-08-19T00:00:00Z")}, "created_at": {"$lt": ISODate("2019-08-20T00:00:00Z")}
    });
    
db.pushEvent.count({});

db.getCollection('pushEvent').aggregate([
{"$match" : {"repo.id" : { $in: [146959896, 121027550] } }},
    {
        
        $group : {
                _id:"$repo.id", 
                count:{$sum:1}
         }
    },
]);
