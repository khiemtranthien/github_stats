db.getCollection('gitRepoStats').find({});
db.getCollection('gitRepoStats').find({'_id': 140810600});

db.getCollection('gitRepoStats').count({});

db.getCollection('gitRepoStats').aggregate([
    { $match: {"issue_opened_avg": {"$gt": 0.0}}},
    { $group : { _id: null, min: { $min : "$issue_opened_avg" }}}
]);

db.getCollection('gitRepoStats').find().sort({"health_score": -1}).limit(100);
db.getCollection('gitRepoStats').find().sort({"issue_opened_avg": -1}).limit(100);

 
