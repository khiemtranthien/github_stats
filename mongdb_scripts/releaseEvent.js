db.getCollection('releaseEvent').find({});
db.getCollection('releaseEvent').count({});

db.getCollection('releaseEvent').createIndex( { "created_at": -1 } , {background: true});
db.getCollection('releaseEvent').createIndex( { "repo.id": 1 } , {background: true});

db.getCollection('releaseEvent').aggregate([
{"$match" : {"repo.id" : { $in: [11581991, 42778193] } }},
    {
        $group : {
                _id:"$repo.id", 
                count:{$sum:1}
         }
    },
]);
