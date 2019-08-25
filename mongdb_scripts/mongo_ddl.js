db.stats(1024*1024*1024);

db.getCollection('githubEvents').createIndex( { "type": 1, "created_at": -1 } , {background: true});


db.githubEvents.aggregate( [
                      {$match: {'type': 'PushEvent'}},
                      { $out : "pushEvent" }
                  ] );

