///* global L */
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import { useEffect } from 'react';

function Route() {
    useEffect(() => {
        const map = L.map('map').setView([40.7128, -74.0060], 12); // New York

        var Stadia_AlidadeSatellite = L.tileLayer('https://tiles.stadiamaps.com/tiles/alidade_satellite/{z}/{x}/{y}{r}.{ext}', {
        	minZoom: 0,
        	maxZoom: 200,
        	attribution: '&copy; CNES, Distribution Airbus DS, © Airbus DS, © PlanetObserver (Contains Copernicus Data) | &copy; <a href="https://www.stadiamaps.com/" target="_blank">Stadia Maps</a> &copy; <a href="https://openmaptiles.org/" target="_blank">OpenMapTiles</a> &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        	ext: 'jpg'
        });

        Stadia_AlidadeSatellite.addTo(map);

        return () => { map.remove(); };

    }, []);

    return (
        <div id="map"/>
    );
}

export default Route;