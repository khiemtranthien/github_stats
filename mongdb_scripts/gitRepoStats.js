db.getCollection('gitRepoStats').find({});

db.getCollection('gitRepoStats').count({});

db.getCollection('gitRepoStats').aggregate([
    
]);

db.getCollection('gitRepoStats').find().sort({"health_score": -1}).limit(100);