function TotalForkByRepoId() {
    var query = [
        {"$match" : {"repo.id" : { $in: ["%(repoIds)"] } }},
            {
                $group : {
                        _id:"$repo.id",
                        count:{$sum:1}
                 }
            }
     ];

    return JSON.stringify(query);
}