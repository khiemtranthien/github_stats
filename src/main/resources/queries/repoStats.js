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