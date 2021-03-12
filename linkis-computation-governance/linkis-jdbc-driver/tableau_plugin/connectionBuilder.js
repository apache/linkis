(function dsbuilder(attr) {
    var urlBuilder = "jdbc:linkis://" + attr[connectionHelper.attributeServer] + ":" + attr[connectionHelper.attributePort] + "/" + attr[connectionHelper.attributeDatabase];

    return [urlBuilder];
})
