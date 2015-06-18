var map;

var vectorLayer;

function initMap() {
    map = new ol.Map({
        layers: [
            new ol.layer.Tile({
                source: new ol.source.OSM()
            })
        ],
        controls: ol.control.defaults({
            attributionOptions:  ({
                collapsible: false
            })
        }),
        target: 'map',
        view: new ol.View({
            center: [0, 0],
            zoom: 2
        })
    });
}

function drawFeatures(features) {
    if (vectorLayer) {
        map.removeLayer(vectorLayer);
    }

    var vectorSource = new ol.source.Vector({
        features: features
    });

    vectorLayer = new ol.layer.Vector({
        source: vectorSource
    });

    map.addLayer(vectorLayer);

    map.getView().fitExtent(vectorSource.getExtent(), map.getSize());
}
