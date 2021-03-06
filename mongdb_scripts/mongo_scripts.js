
db.getCollection('githubEvents').find({});
db.getCollection('githubEvents').count();

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
    
    {$match: {'type': 'PushEvent', 'created_at' : { $gte: ISODate('2019-08-15T00:00:00Z')}}},
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
