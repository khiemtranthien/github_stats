function TotalReleaseByRepoId() {
    var TotalReleaseByRepoIdQuery = [
        {"$match" : {"repo.id" : { $in: ["%(repoIds)"] } }},
         {
             $group : {
                     _id:"$repo.id",
                     count:{$sum:1}
              }
         }
     ];

    return JSON.stringify(TotalReleaseByRepoIdQuery);
}