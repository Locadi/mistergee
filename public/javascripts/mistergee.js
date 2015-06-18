function createOverpassQuery(id, kind) {
    var query;
    if ('ags' == kind) {
        query = "[timeout:6000];rel[\"de:amtlicher_gemeindeschluessel\"=\"" + id + "\"];(._;>;);out;";
    }
    else if ('osmId' == kind) {
        var areaId = id + 3600000000;
        query = "[timeout:6000]; area(" + areaId + "); (._;.boundaryarea; )->.boundaryarea; rel(pivot.boundaryarea); out geom;";
    }
    return query;
}

function callOverpass(query, id, overpassSuccess) {
    $.ajax({
        type: "POST",
        url: "http://overpass-api.de/api/interpreter",
        data: query,
        success: function (data) {
            overpassSuccess(data, id)
        },
        dataType: "xml",
        error: function (xhr, ajaxOptions, thrownError) {
            alert('Error with status: ' + xhr.status + ', ' + thrownError);
        }
    });
}

function updateUI(data, id) {
    try {
        var features = parseData(data);
        drawFeatures(features);
        createGeoJSONLink(features, id);
    } catch (e) {
        alert('Failed to parse data: ' + data + ", with error: " + e)
    }
}

function parseData(data, id) {
    var geojson = osmtogeojson(data);
    var features = (new ol.format.GeoJSON()).readFeatures(geojson, {projection: 'EPSG:4326'});
    features.splice(1, features.length - 1)
    var geom = features[0].getGeometry();
    geom.transform('EPSG:4326', 'EPSG:3857');
    return features;
}


function createGeoJSONLink(features, id) {
    var geom = features[0].getGeometry();
    var transformedGeojson = (new ol.format.GeoJSON()).writeGeometry(geom);
    $('#geojson_link').empty()
        .append('<span>GeoJSON file for identifier <strong>' + id + '</strong> is ready for download: </span>')
        .append($('<a/>').attr('href', 'data:application/json,' + transformedGeojson).text('Download').attr('download', id + '.json').attr('class', 'button'));
}

$(document).ajaxStart(function () {
    $('#ajax_busy').show();
}).ajaxStop(function () {
    $('#ajax_busy').hide();
});