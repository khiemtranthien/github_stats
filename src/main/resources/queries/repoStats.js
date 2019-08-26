function GetMaxPush() {
    var query = [
         { $group : { _id: null, max: { $max : "$push" }}}
     ];

     return JSON.stringify(query);
}

function GetMaxRelease() {
    var query = [
         { $group : { _id: null, max: { $max : "$release" }}}
     ];

     return JSON.stringify(query);
}

function GetMaxContributor() {
    var query = [
         { $group : { _id: null, max: { $max : "$contributor" }}}
     ];

     return JSON.stringify(query);
}

function GetMinIssueOpenTime() {
    var query = [
         { $match: {"issue_opened_avg": {"$gt": 0.0}}},
         { $group : { _id: null, min: { $min : "$issue_opened_avg" }}}
     ];

     return JSON.stringify(query);
}