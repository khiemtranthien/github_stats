var query = [

    {
        $match: {'type': 'PushEvent', 'created_at' : { $gte: '2019-08-19'}}},
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
    { $sort : { distinctCount : -1 } }
];

function getQuery() {
    return JSON.stringify(query);
}