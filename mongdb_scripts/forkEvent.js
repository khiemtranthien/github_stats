db.getCollection('forkEvent').find({});

db.getCollection('forkEvent').createIndex( { "repo.id": 1 } , {background: true});

db.getCollection('forkEvent').aggregate([
{"$match" : {"repo.id" : { $in: [119611077, 78753723, 22956623] } }},
    {
        $group : {
                _id:"$repo.id", 
                count:{$sum:1}
         }
    },
]);